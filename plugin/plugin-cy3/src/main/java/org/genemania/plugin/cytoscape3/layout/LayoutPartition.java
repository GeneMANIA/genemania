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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.view.layout.EdgeWeighter;
import org.cytoscape.view.layout.LayoutEdge;
import org.cytoscape.view.layout.LayoutNode;
import org.cytoscape.view.layout.LayoutPoint;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;


/**
 * The LayoutPartition class contains all of the information about a
 * single graph partition, where a partition is defined as all nodes
 * in a graph that connect only to each other.  This class also provides
 * static methods that are used to partition an existing graph.
 *
 * @author <a href="mailto:scooter@cgl.ucsf.edu">Scooter Morris</a>
 * 
 * @CyAPI.Final.Class
 * @CyAPI.InModule layout-api
 */
public final class LayoutPartition {
	
	private ArrayList<LayoutNode> nodeList;
	private ArrayList<LayoutEdge> edgeList;
	private Map<CyNode, LayoutNode> nodeToLayoutNode; 
	private int nodeIndex = 0;
	private int partitionNumber = 0;
	private EdgeWeighter edgeWeighter;
	private boolean ignoreHiddenElements;

	// Keep track of the node min and max values
	private double maxX = -100000;
	private double maxY = -100000;
	private double minX = 100000;
	private double minY = 100000;
	private double width = 0;
	private double height = 0;
	private double depth = 0;

	// Keep track of average location
	private double averageX = 0;
	private double averageY = 0;

	// Keep track of the number of locked nodes we have in this partition
	private int lockedNodes = 0;


	/**
	 * LayoutPartition: use this constructor to create an empty LayoutPartition.
	 *
	 * @param nodeCount    The number of nodes in the new partition.
	 * @param edgeCount    The number of edges in the new partition.
	 */
	public LayoutPartition(final int nodeCount, final int edgeCount) {
		nodeList = new ArrayList<LayoutNode>(nodeCount);
		edgeList = new ArrayList<LayoutEdge>(edgeCount);
		nodeToLayoutNode = new WeakHashMap<CyNode,LayoutNode>(nodeCount);
		partitionNumber = 1;
	}

	/**
	 * LayoutPartition: use this constructor to create a LayoutPartition that
	 * includes the entire network.
	 *
	 * @param networkView the CyNetworkView to use
	 * @param nodeSet the nodes to be considered
	 * @param edgeWeighter the weighter to use for edge weighting
	 */
	public LayoutPartition(CyNetworkView networkView, Collection<View<CyNode>> nodeSet, EdgeWeighter edgeWeighter,
			boolean ignoreHiddenElements) {
		initialize(networkView, nodeSet, edgeWeighter, ignoreHiddenElements);
	}

	private void initialize(CyNetworkView networkView, Collection<View<CyNode>> nodeSet, EdgeWeighter edgeWeighter,
			boolean ignoreHiddenElements) {
		this.edgeWeighter = edgeWeighter;
		this.ignoreHiddenElements = ignoreHiddenElements;

		// Initialize
		nodeList = new ArrayList<LayoutNode>(networkView.getModel().getNodeCount());
		edgeList = new ArrayList<LayoutEdge>(networkView.getModel().getEdgeCount());
		nodeToLayoutNode = new WeakHashMap<CyNode,LayoutNode>(networkView.getModel().getNodeCount());

		// Now, walk the iterators and fill in the values
		nodeListInitialize(networkView, nodeSet);
		edgeListInitialize(networkView);
		trimToSize();
		partitionNumber = 1;
	}

	/**
	 * Set the EdgeWeighter to use for this partition.  The EdgeWeighter should be
	 * shared by all partitions in the same graph to avoid contrary scaling problems.
	 *
	 * @param edgeWeighter the weighter to use for edge weighting
	 */
	public void setEdgeWeighter(EdgeWeighter edgeWeighter) {
		this.edgeWeighter = edgeWeighter;
	}

	/**
	 * Add a node to this partition.
	 *
	 * @param nv the View<CyNode> of the node to add
	 * @param locked a boolean value to determine if this node is locked or not
	 */
	protected void addNode(CyNetwork network, View<CyNode> nv, boolean locked) {
		CyNode node = nv.getModel();
		LayoutNode v = new LayoutNode(nv, nodeIndex++, network.getRow(node));
		nodeList.add(v);
		nodeToLayoutNode.put(node, v);

		if (locked) {
			v.lock();
			lockedNodes++;
		} else {
			updateMinMax(nv.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION),
						 nv.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION));
			this.width  += nv.getVisualProperty(BasicVisualLexicon.NODE_WIDTH).doubleValue(); 
			this.height += nv.getVisualProperty(BasicVisualLexicon.NODE_HEIGHT).doubleValue();
			this.depth  += nv.getVisualProperty(BasicVisualLexicon.NODE_DEPTH).doubleValue();
		}
	}

	/**
	 * Add an edge to this partition assuming that the source and target
	 * nodes are not yet known.
	 *
	 * @param edge    the Edge to add to the partition
	 */
	protected void addEdge(CyEdge edge, CyRow row) {
		LayoutEdge newEdge = new LayoutEdge(edge, row);
		updateWeights(newEdge);
		edgeList.add(newEdge);
	}

	/**
	 * Add an edge to this partition assuming that the source and target
	 * nodes <em>are</em> known.
	 *
	 * @param edge    the Edge to add to the partition
	 * @param v1    the LayoutNode of the edge source
	 * @param v2    the LayoutNode of the edge target
	 */
	protected void addEdge(CyEdge edge, LayoutNode v1, LayoutNode v2, CyRow row) {
		LayoutEdge newEdge = new LayoutEdge(edge, v1, v2, row);
		updateWeights(newEdge);
		edgeList.add(newEdge);
	}

	/**
	 * Randomize the graph locations.
	 * @param is3D ignores Z values if false
	 */
	public void randomizeLocations(boolean is3D) {
		// Get a seeded pseudo random-number generator
		Date today = new Date();
		Random random = new Random(today.getTime());
		// Reset our min and max values
		resetNodes();

		for (LayoutNode node: nodeList) {

			if (!node.isLocked()) {
				double x = random.nextDouble() * width;
				double y = random.nextDouble() * height;
				node.setLocation(x, y);
				updateMinMax(x, y);
			} else {
				updateMinMax(node.getX(), node.getY());
			}
		}
	}
	
	/**
	 * Randomize the graph locations (ignoring Z values).
	 */
	public void randomizeLocations() {
		randomizeLocations(false);
	}

	/**
	 * Move the node to its current X and Y values.  This is a wrapper
	 * to LayoutNode's moveToLocation, but has the property of updating
	 * the current min and max values for this partition.
	 *
	 * Note, updates the Z min and max to 0.
	 * 
	 * @param node the LayoutNode to move
	 */
	public void moveNodeToLocation(LayoutNode node) {
		
		// We provide this routine so that we can keep our min/max values updated
		if (node.isLocked())
			return;

		node.moveToLocation();
		updateMinMax(node.getX(), node.getY());
	}
	
	/**
	 * Move the node to its current X, Y and Z values.  This is a wrapper
	 * to LayoutNode's moveToLocation3D, but has the property of updating
	 * the current min and max values for this partition.
	 *
	 * @param node the LayoutNode to move
	 */
	public void moveNodeToLocation3D(LayoutNode node) {
		
		// We provide this routine so that we can keep our min/max values updated
		if (node.isLocked())
			return;

		updateMinMax(node.getX(), node.getY());
	}

	/**
	 * Convenience routine to update the source and target for all of the
	 * edges in a partition.  This is useful when the algorithm used makes it
	 * difficult to record source and target until it has completed.
	 */
	public void fixEdges() {
		for (final LayoutEdge lEdge: edgeList) {
			// Get the underlying edge
			final CyEdge edge = lEdge.getEdge();
			final CyNode target = edge.getTarget();
			final CyNode source = edge.getSource();

			final LayoutNode sourceLayoutNode = nodeToLayoutNode.get(source);
			final LayoutNode targetLayoutNode = nodeToLayoutNode.get(target);
			if (sourceLayoutNode != null && targetLayoutNode != null) {
				// Add the connecting nodes
				lEdge.addNodes(sourceLayoutNode, targetLayoutNode);
			}
		}
	}

	/**
	 * Calculate and set the edge weights.  Note that this will delete
	 * edges from the calculation (not the graph) when certain conditions
	 * are met.
	 */
	// TODO: is this necessary?
	public void calculateEdgeWeights() {

		// Use a ListIterator so that we can modify the list
		// as we go
		ListIterator<LayoutEdge>iter = edgeList.listIterator();
		while (iter.hasNext()) {
			LayoutEdge edge = iter.next();

			// If we're only dealing with selected nodes, drop any edges
			// that don't have any selected nodes
			if (edge.getSource().isLocked() && edge.getTarget().isLocked()) {
				iter.remove();
			} else if (edgeWeighter != null && edgeWeighter.normalizeWeight(edge) == false)
				iter.remove();
		}
	}

	/**
	 * Return the size of this partition, which is defined as the number of
	 * nodes that it contains.
	 *
	 * @return    partition size
	 */
	public int size() {
		return nodeList.size();
	}

	/**
	 * Return the list of LayoutNodes within this partition.
	 *
	 * @return    List of LayoutNodes
	 * @see LayoutNode
	 */
	public List<LayoutNode> getNodeList() {
		return nodeList;
	}

	/**
	 * Return the list of LayoutEdges within this partition.
	 *
	 * @return    List of LayoutEdges
	 * @see LayoutEdge
	 */
	public List<LayoutEdge> getEdgeList() {
		return edgeList;
	}

	/**
	 * Return an iterator over all of the LayoutNodes in this partition
	 *
	 * @return Iterator over the list of LayoutNodes
	 * @see LayoutNode
	 */
	public Iterator<LayoutNode> nodeIterator() {
		return nodeList.iterator();
	}

	/**
	 * Return an iterator over all of the LayoutEdges in this partition
	 *
	 * @return Iterator over the list of LayoutEdges
	 * @see LayoutEdge
	 */
	public Iterator<LayoutEdge> edgeIterator() {
		return edgeList.iterator();
	}

	/**
	 * Return the number of nodes in this partition
	 *
	 * @return number of nodes in the partition
	 */
	public int nodeCount() {
		return nodeList.size();
	}

	/**
	 * Return the number of edges in this partition
	 *
	 * @return number of edges in the partition
	 */
	public int edgeCount() {
		return edgeList.size();
	}

	/**
	 * Return the maximum X location of all of the LayoutNodes
	 *
	 * @return maximum X location
	 */
	public double getMaxX() {
		return maxX;
	}

	/**
	 * Return the maximum Y location of all of the LayoutNodes
	 *
	 * @return maximum Y location
	 */
	public double getMaxY() {
		return maxY;
	}
	
	/**
	 * Return the minimum X location of all of the LayoutNodes
	 *
	 * @return minimum X location
	 */
	public double getMinX() {
		return minX;
	}

	/**
	 * Return the minimum Y location of all of the LayoutNodes
	 *
	 * @return minimum Y location
	 */
	public double getMinY() {
		return minY;
	}

	/**
	 * Return the total width of all of the LayoutNodes
	 *
	 * @return total width of all of the LayoutNodes
	 */
	public double getWidth() {
		return width;
	}

	/**
	 * Return the total height of all of the LayoutNodes
	 *
	 * @return total height of all of the LayoutNodes
	 */
	public double getHeight() {
		return height;
	}
	
	/**
	 * Return the total depth of all of the LayoutNodes
	 *
	 * @return total depth of all of the LayoutNodes
	 */
	public double getDepth() {
		return depth;
	}

	/**
	 * Return the partition number of this partition
	 *
	 * @return partition number
	 */
	public int getPartitionNumber() {
		return partitionNumber;
	}

	/**
	 * Set the partition number of this partition
	 *
	 * @param part partition number
	 */
	public void setPartitionNumber(int part) {
		partitionNumber = part;
	}

	/**
	 * Return the number of locked nodes within this partition
	 *
	 * @return number of locked nodes in partition
	 */
	public int lockedNodeCount() {
		return lockedNodes;
	}


	/**
	 * Return the average location of the nodes in this partition
	 *
	 * @return average location of the nodes as a Dimension
	 */
	public LayoutPoint getAverageLocation() {
		int nodes = nodeCount() - lockedNodes;
		return new LayoutPoint(averageX / nodes, averageY / nodes);
	}

	/**
	 * Offset all of the nodes in the partition by a fixed
	 * amount.  This is used by algorithms of offset each
	 * partition after laying it out.
	 *
	 * @param xoffset the amount to offset in the X direction
	 * @param yoffset the amount to offset in the Y direction
	 */
	public void offset(double xoffset, double yoffset) {
		double myMinX = this.minX;
		double myMinY = this.minY;
		resetNodes();

		for (LayoutNode node: nodeList) {
			node.increment(xoffset - myMinX, yoffset - myMinY);
			moveNodeToLocation(node);
		}
	}


	/**
	 * Reset all of the data maintained for the LayoutNodes
	 * contained within this partition, including the min, max
	 * and average x and y values.
	 */
	public void resetNodes() {
		maxX = -100000;
		maxY = -100000;
		minX = 100000;
		minY = 100000;
		averageX = 0;
		averageY = 0;
	}

	private void nodeListInitialize(CyNetworkView networkView, Collection<View<CyNode>> nodeSet) {
		this.nodeList = new ArrayList<LayoutNode>(networkView.getModel().getNodeCount());

		for (View<CyNode> nv: networkView.getNodeViews()) {
			if (ignoreHiddenElements && nv.getVisualProperty(BasicVisualLexicon.NODE_VISIBLE) == Boolean.FALSE)
				continue;
			
			if (!nodeSet.contains(nv))
				addNode(networkView.getModel(), nv, true);
			else
				addNode(networkView.getModel(), nv, false);
		}
	}

	private void edgeListInitialize(final CyNetworkView networkView) {
		for (View<CyEdge> ev: networkView.getEdgeViews()) {
			if (ignoreHiddenElements && ev.getVisualProperty(BasicVisualLexicon.EDGE_VISIBLE) == Boolean.FALSE)
				continue;
			
			final CyEdge edge = ev.getModel();
			final CyNode source = edge.getSource();
			final CyNode target = edge.getTarget();

			// Ignore self edge
			if (source == target)
				continue;

			final LayoutNode v1 = nodeToLayoutNode.get(source);
			final LayoutNode v2 = nodeToLayoutNode.get(target);

			// Do we care about this edge?
			if (v1.isLocked() && v2.isLocked())
				continue; // no, ignore it

			addEdge(edge, v1, v2, networkView.getModel().getRow(edge));
		}
	}

	/**
	 * Space saving convenience function to trim the internal arrays to fit the
	 * contained data.  Useful to call this after a partition has been created
	 * and filled.  This is used by the static method PartitionUtil.partition()
	 */
	void trimToSize() {
		nodeList.trimToSize();
		edgeList.trimToSize();
	}

	private void updateMinMax(final double x, final double y) {		
		minX = Math.min(minX, x);
		minY = Math.min(minY, y);
		maxX = Math.max(maxX, x);
		maxY = Math.max(maxY, y);
		averageX += x;
		averageY += y;
	}

	private void updateWeights(final LayoutEdge newEdge) {
		if (edgeWeighter != null)
			edgeWeighter.setWeight(newEdge);
	}
}
