package org.genemania.plugin.cytoscape3.layout;

/*
 * #%L
 * Cytoscape Prefuse Layout Impl (layout-prefuse-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_VISIBLE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_VISIBLE;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.layout.EdgeWeighter;
import org.cytoscape.view.layout.WeightTypes;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableValidator;
import org.genemania.plugin.cytoscape.CytoscapeUtils;

public class GeneManiaFDLayoutContext implements TunableValidator {
	
	@ContainsTunables
	public EdgeWeighter edgeWeighter = new EdgeWeighter();
	
	@Tunable(description="Number of Iterations:")
	public int numIterations = 100;
	@Tunable(description="Default Spring Coefficient:")
	public double defaultSpringCoefficient = 0.1;
	@Tunable(description="Default Spring Length:")
	public double defaultSpringLength = 10;
	@Tunable(description="Min Node Mass:")
	public double minNodeMass = 10.0;
	@Tunable(description="Max Node Mass:")
	public double maxNodeMass = 5000.0;
	@Tunable(description="Number of Edges when Node Mass is Half of Max Mass:")
	public int midpointEdges = 700;
	@Tunable(description="Steepness of the Curve (Logistic Function):")
	public double curveSteepness = 0.007;
	@Tunable(description="Force deterministic layouts (slower):")
	public boolean isDeterministic;
	@Tunable(description="Don't partition graph before layout:", groups="Standard Settings")
	public boolean singlePartition;
	@Tunable(description="Ignore hidden nodes and edges:", groups="Standard Settings")
	public boolean ignoreHiddenElements = true;
	
	public GeneManiaFDLayoutContext() {
		edgeWeighter.setWeightType(WeightTypes.GUESS);
		edgeWeighter.setMinWeightCutoff(0.0);
		edgeWeighter.setMinWeightCutoff(10.0);
		edgeWeighter.defaultEdgeWeight = 0.5;
		edgeWeighter.setWeightAttribute(CytoscapeUtils.MAX_WEIGHT_ATTRIBUTE);
	}
	
	@Override
	public ValidationState getValidationState(final Appendable errMsg) {
		boolean invalid = false;
		
		try {
			if (!isPositive(numIterations))
				invalid = append(errMsg, "Number of iterations must be > 0; current value = " + numIterations);
			if (!isPositive(defaultSpringCoefficient))
				invalid = append(errMsg, "Default spring coefficient must be > 0; current value = " + defaultSpringCoefficient);
			if (!isPositive(defaultSpringLength))
				invalid = append(errMsg, "Default spring length must be > 0; current value = " + defaultSpringLength);
			if (!isPositive(minNodeMass))
				invalid = append(errMsg, "Min node mass must be > 0; current value = " + minNodeMass);
			if (!isPositive(maxNodeMass))
				invalid = append(errMsg, "Max node mass must be > 0; current value = " + maxNodeMass);
		} catch (IOException e) {
			return ValidationState.INVALID;
		}
		
		return invalid ? ValidationState.INVALID : ValidationState.OK;
	}
	
	public <T extends CyIdentifiable> Set<View<T>> getElementsToLayOut(final Collection<View<T>> views,
			final CyNetworkView netView) {
		if (!ignoreHiddenElements)
			return new LinkedHashSet<View<T>>(views);
		
		if (views != null)
			return getVisibleElements(views, netView);
		
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends CyIdentifiable> Set<View<T>> getVisibleElements(final Collection<View<T>> views,
			final CyNetworkView netView) {
		final Set<View<T>> visibleElements = new LinkedHashSet<View<T>>();
		
		if (views != null) {
			for (final View<T> v : views) {
				if (v.getModel() instanceof CyNode && v.getVisualProperty(NODE_VISIBLE) == Boolean.TRUE) {
					visibleElements.add(v);
				} else if (v.getModel() instanceof CyEdge && v.getVisualProperty(EDGE_VISIBLE) == Boolean.TRUE) {
					final View<CyEdge> ev = (View<CyEdge>) v;
					final View<CyNode> nv1 = netView.getNodeView(ev.getModel().getSource());
					final View<CyNode> nv2 = netView.getNodeView(ev.getModel().getTarget());
					
					if (nv1 != null && nv2 != null && 
							nv1.getVisualProperty(NODE_VISIBLE) == Boolean.TRUE &&
							nv2.getVisualProperty(NODE_VISIBLE) == Boolean.TRUE)
						visibleElements.add(v);
				}
			}
		}
		
		return visibleElements;
	}
	
	private static boolean isPositive(final int n) {
		return n > 0;
	}

	private static boolean isPositive(final double n) {
		return n > 0.0;
	}
	
	private boolean append(final Appendable errMsg, final String msg) throws IOException {
		final String NEW_LINE = errMsg.toString().isEmpty() ?  "" : "\n";
		errMsg.append(NEW_LINE + msg);
		
		return true;
	}
}
