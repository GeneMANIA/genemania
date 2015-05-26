/**
 * This file is part of GeneMANIA.
 * Copyright (C) 2008-2011 University of Toronto.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.genemania.plugin.cytoscape3.layout;

import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_VISIBLE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_VISIBLE;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.layout.LayoutEdge;
import org.cytoscape.view.layout.LayoutNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.undo.UndoSupport;
import org.genemania.plugin.cytoscape3.layout.prefuse.util.force.DragForce;
import org.genemania.plugin.cytoscape3.layout.prefuse.util.force.ForceItem;
import org.genemania.plugin.cytoscape3.layout.prefuse.util.force.ForceSimulator;
import org.genemania.plugin.cytoscape3.layout.prefuse.util.force.NBodyForce;
import org.genemania.plugin.cytoscape3.layout.prefuse.util.force.SpringForce;


/**
 * This class wraps the Prefuse force-directed layout algorithm.
 * 
 * @see <a href="http://prefuse.org">Prefuse web site</a>
 */
public class GeneManiaFDLayoutTask extends AbstractPartitionLayoutTask {

	private String displayName;
	private ForceSimulator forceSim;
	private Map<LayoutNode, ForceItem> forceItems;
	private GeneManiaFDLayoutContext context;
	private double mass;
	
	/**
	 * Creates a new GeneManiaFDLayout object.
	 */
	public GeneManiaFDLayoutTask(
			final String displayName,
			final CyNetworkView networkView,
			final Set<View<CyNode>> nodesToLayOut,
			final GeneManiaFDLayoutContext context,
			final String attrName,
			final UndoSupport undo
	) {
		super(
				displayName,
				context.singlePartition,
				context.ignoreHiddenElements,
				networkView,
				maybeRemoveHidden(nodesToLayOut, networkView, context), // Remove invisible nodes, if necessary
				attrName,
				undo
		);
		this.displayName = displayName;
		this.context = context;
		
        final double L = context.maxNodeMass; // the curve's maximum value, or the max node mass
		final double k = context.curveSteepness; // the steepness of the curve
		final double x0 = context.midpointEdges; // the x-value of the sigmoid's midpoint
		final double x = context.ignoreHiddenElements ? getVisibleEdgeCount(networkView) : networkView.getModel().getEdgeCount();
		
		// Apply logistic function (http://en.wikipedia.org/wiki/Logistic_function) to the node mass
		// (also see: https://www.wolframalpha.com/input/?i=plot+%281000%2F%281%2Be^%28-0.007%28x-250%29%29%29+from+x%3D0+to+x%3D1000)
		mass = L / ( 1 + Math.pow( Math.E, (-k * (x - x0)) ) );
		mass = Math.max(context.minNodeMass, mass);
        
		edgeWeighter = context.edgeWeighter;
		edgeWeighter.setWeightAttribute(layoutAttribute);

		forceSim = new ForceSimulator();
		forceSim.addForce(new NBodyForce());
		forceSim.addForce(new SpringForce());
		forceSim.addForce(new DragForce());

		forceItems = new HashMap<LayoutNode, ForceItem>();
	}
	
	@Override
	public String toString() {
		return displayName;
	}
	
	@Override
	public void layoutPartition(final LayoutPartition part) {
		if (context.ignoreHiddenElements) {
			// Remove invisible edges
			final Iterator<LayoutEdge> edgeIterator = part.edgeIterator();
			
			while (edgeIterator.hasNext()) {
				if (isHidden(networkView.getEdgeView(edgeIterator.next().getEdge()), networkView))
					edgeIterator.remove();
			}
		}
		
		// Calculate our edge weights
		part.calculateEdgeWeights();

		forceSim = new ForceSimulator();
		forceSim.addForce(new NBodyForce());
		forceSim.addForce(new SpringForce());
		forceSim.addForce(new DragForce());

		forceItems.clear();
		
		List<LayoutNode> nodeList = part.getNodeList();
		List<LayoutEdge> edgeList = part.getEdgeList();
		
		if (context.isDeterministic){
			Collections.sort(nodeList);
			Collections.sort(edgeList);
		}
		
		// initialize nodes
		for (LayoutNode ln: nodeList) {
			ForceItem fitem = forceItems.get(ln); 
			
			if (fitem == null) {
				fitem = new ForceItem();
				forceItems.put(ln, fitem);
			}
			
			fitem.mass = getMassValue(ln);
			fitem.location[0] = 0f; 
			fitem.location[1] = 0f; 
			forceSim.addItem(fitem);
		}
		
		// initialize edges
		for (LayoutEdge e: edgeList) {
			LayoutNode n1 = e.getSource();
			ForceItem f1 = forceItems.get(n1); 
			LayoutNode n2 = e.getTarget();
			ForceItem f2 = forceItems.get(n2); 
			
			if (f1 == null || f2 == null)
				continue;

			forceSim.addSpring(f1, f2, getSpringCoefficient(e), getSpringLength(e)); 
		}

		if (taskMonitor != null)
			taskMonitor.setStatusMessage("Initializing partition " + part.getPartitionNumber());

		// perform layout
		long timestep = 1000L;
		
		for (int i = 0; i < context.numIterations && !cancelled; i++) {
			timestep *= (1.0 - i/(double)context.numIterations);
			long step = timestep+50;
			forceSim.runSimulator(step);
			setTaskStatus((int)(((double)i/(double)context.numIterations)*90.+5));
		}
		
		// update positions
		part.resetNodes(); // reset the nodes so we get the new average location
		
		for (LayoutNode ln: part.getNodeList()) {
			if (!ln.isLocked()) {
				ForceItem fitem = forceItems.get(ln); 
				ln.setX(fitem.location[0]);
				ln.setY(fitem.location[1]);
				part.moveNodeToLocation(ln);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static <T extends CyIdentifiable> boolean isHidden(final View<T>view, final CyNetworkView netView) {
		if (view != null) {
			if (view.getModel() instanceof CyNode && view.getVisualProperty(NODE_VISIBLE) == Boolean.TRUE) {
				return false;
			} else if (view.getModel() instanceof CyEdge && view.getVisualProperty(EDGE_VISIBLE) == Boolean.TRUE) {
				final View<CyEdge> ev = (View<CyEdge>) view;
				final View<CyNode> nv1 = netView.getNodeView(ev.getModel().getSource());
				final View<CyNode> nv2 = netView.getNodeView(ev.getModel().getTarget());
				
				if (nv1 != null && nv2 != null && 
						nv1.getVisualProperty(NODE_VISIBLE) == Boolean.TRUE &&
						nv2.getVisualProperty(NODE_VISIBLE) == Boolean.TRUE)
					return false;
			}
		}
		
		return true;
	}
	
	private static int getVisibleEdgeCount(final CyNetworkView netView) {
		int count = 0;
		
		for (final View<CyEdge> ev : netView.getEdgeViews()) {
			if (!isHidden(ev, netView))
				count++;
		}
			
		return count;
	}
	
	private static Set<View<CyNode>> maybeRemoveHidden(final Set<View<CyNode>> views, final CyNetworkView networkView,
			GeneManiaFDLayoutContext context) {
		if (context.ignoreHiddenElements) {
			final Iterator<View<CyNode>> iter = views.iterator();
			
			while (iter.hasNext()) {
				if (isHidden(iter.next(), networkView))
					iter.remove();
			}
		}
		
		return views;
	}
	
	/**
	 * Get the mass value associated with the given node. Subclasses should
	 * override this method to perform custom mass assignment.
	 * @param n the node for which to compute the mass value
	 * @return the mass value for the node. By default, all items are given
	 * a mass value of 1.0.
	 */
	protected float getMassValue(LayoutNode n) {
		return (float)mass;
	}

	/**
	 * Get the spring length for the given edge. Subclasses should
	 * override this method to perform custom spring length assignment.
	 * @param e the edge for which to compute the spring length
	 * @return the spring length for the edge. A return value of
	 * -1 means to ignore this method and use the global default.
	*/
	protected float getSpringLength(LayoutEdge e) {
		double weight = e.getWeight();
		return (float)(context.defaultSpringLength/weight);
	}

	/**
	 * Get the spring coefficient for the given edge, which controls the
	 * tension or strength of the spring. Subclasses should
	 * override this method to perform custom spring tension assignment.
	 * @param e the edge for which to compute the spring coefficient.
	 * @return the spring coefficient for the edge. A return value of
	 * -1 means to ignore this method and use the global default.
	 */
	protected float getSpringCoefficient(LayoutEdge e) {
		return (float)context.defaultSpringCoefficient;
	}
}
