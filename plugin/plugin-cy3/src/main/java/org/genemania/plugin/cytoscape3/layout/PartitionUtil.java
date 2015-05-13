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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.view.layout.EdgeWeighter;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;


/**
 * This class also provides static methods that are used to partition a network. 
 * @CyAPI.Static.Class
 * @CyAPI.InModule layout-api
 */
public final class PartitionUtil {

	private PartitionUtil() {};

	/**
	 * Partition the graph -- this builds the LayoutEdge and LayoutNode
	 * arrays as a byproduct.  The algorithm for this was taken from
	 * algorithms/graphPartition/SGraphPartition.java.
	 *
	 * @param networkView the CyNetworkView representing the graph
	 * @param selectedOnly only consider selected nodes
	 * @param edgeWeighter the weighter to use for edge weighting
	 * @return a List of LayoutPartitions
	 */
	public static List<LayoutPartition> partition(final CyNetworkView networkView, final boolean selectedOnly,
			final EdgeWeighter edgeWeighter, final boolean ignoreHiddenElements) {
		if (selectedOnly)
			return partition(networkView, CyTableUtil.getNodesInState(networkView.getModel(), CyNetwork.SELECTED ,true),
					edgeWeighter, ignoreHiddenElements);
		else
			return partition(networkView, networkView.getModel().getNodeList(), edgeWeighter, ignoreHiddenElements);
	}

	/**
	 * Partition the graph -- this builds the LayoutEdge and LayoutNode
	 * arrays as a byproduct.  The algorithm for this was taken from
	 * algorithms/graphPartition/SGraphPartition.java.
	 *
	 * @param networkView the CyNetworkView representing the graph
	 * @param nodeSet the set of nodes to consider
	 * @param edgeWeighter the weighter to use for edge weighting
	 * @return a List of LayoutPartitions
	 */
	public static List<LayoutPartition> partition(final CyNetworkView networkView, final Collection<CyNode> nodeSet,
			final EdgeWeighter edgeWeighter, final boolean ignoreHiddenElements) {
		final List<LayoutPartition> partitions = new ArrayList<LayoutPartition>();
		final CyNetwork network = networkView.getModel();

		final Map<CyNode, Boolean> nodesSeenMap = new HashMap<CyNode, Boolean>(); 
		final Map<CyEdge, Boolean> edgesSeenMap = new HashMap<CyEdge, Boolean>(); 
		final Map<CyNode, View<CyNode>> nodesToViews = new HashMap<CyNode,View<CyNode>>(); 
		int partitionNumber = 1;

		// Initialize the maps
		for (final View<CyNode> nv : networkView.getNodeViews()) {
			// Ignore hidden nodes?
			if (ignoreHiddenElements && nv.getVisualProperty(BasicVisualLexicon.NODE_VISIBLE) == Boolean.FALSE)
				continue;
			
			nodesSeenMap.put(nv.getModel(), Boolean.FALSE);
			nodesToViews.put(nv.getModel(), nv);
		}

		for (View<CyEdge> ev : networkView.getEdgeViews()) {
			// Ignore hidden edges?
			if (ignoreHiddenElements && ev.getVisualProperty(BasicVisualLexicon.EDGE_VISIBLE) == Boolean.FALSE)
				continue;
			
			edgesSeenMap.put(ev.getModel(), Boolean.FALSE);
		}

		// OK, now traverse the graph
		for (final CyNode node: nodeSet) {
			// Have we seen this already?
			if (nodesSeenMap.get(node) == Boolean.TRUE)
				continue;
			
			// Nope, first time
			final LayoutPartition part = new LayoutPartition(network.getNodeCount(),
			                                           network.getEdgeCount());
			part.setPartitionNumber(partitionNumber++);

			// Set the edge weighter
			part.setEdgeWeighter(edgeWeighter);

			nodesSeenMap.put(node, Boolean.TRUE);

			// Traverse through all connected nodes
			traverse(network, networkView, nodesToViews, node, part, nodesSeenMap, edgesSeenMap);

			// Done -- finalize the parition
			part.trimToSize();

			// Finally, now that we're sure we've touched all of our
			// nodes.  Fix up our edgeLayout list to have all of our
			// layoutNodes
			part.fixEdges();

			partitions.add(part);
		}

		// Now sort the partitions based on the partition's node count
		Collections.sort(partitions, new Comparator<LayoutPartition>() {
			@Override
			public int compare(LayoutPartition p1, LayoutPartition p2) {
				return (p2.size() - p1.size());
			}
		});

		return partitions; 
	}

	/**
	  * This method traverses nodes connected to the specified node.
	  * @param network            The GraphPerspective we are laying out
	  * @param networkView        The CyNetworkView we are laying out
	  * @param nodesToViews       A map that maps between nodes and views
	  * @param node               The node to search for connected nodes.
	  * @param partition          The partition that we're laying out
	  * @param nodesSeenMap       A map of nodes already visited.
	  * @param edgesSeenMap       A map of edges already visited.
	  */
	private static void traverse(
			CyNetwork network,
			CyNetworkView networkView,
			Map<CyNode, View<CyNode>> nodesToViews,
			CyNode node,
			LayoutPartition partition,
			Map<CyNode, Boolean> nodesSeenMap,
			Map<CyEdge, Boolean> edgesSeenMap
	) {
		// Get the View<CyNode>
		final View<CyNode> nv = nodesToViews.get(node);

		// Add this node to the partition
		partition.addNode(network, nv, false);

		// Iterate through each connected edge
		for (final CyEdge incidentEdge: network.getAdjacentEdgeList(node, CyEdge.Type.ANY)) {
			if (!edgesSeenMap.containsKey(incidentEdge))
				continue; // The map must contain all edges to be considered
			
			// Have we already seen this edge?
			if (edgesSeenMap.get(incidentEdge) == Boolean.TRUE)
				continue; // Yes, continue since it means we *must* have seen both sides

			edgesSeenMap.put(incidentEdge, Boolean.TRUE);

			// Add the edge to the partition
			partition.addEdge(incidentEdge, network.getRow(incidentEdge));

			// Determine the node's index that is on the other side of the edge
			final CyNode otherNode;

			if (incidentEdge.getSource() == node) {
				otherNode = incidentEdge.getTarget();
			} else {
				otherNode = incidentEdge.getSource();
			}

			// Have we seen the connecting node yet?
			if (nodesSeenMap.get(otherNode) == Boolean.FALSE) {
				// Mark it as having been seen
				nodesSeenMap.put(otherNode, Boolean.TRUE);

				// Traverse through this one
				traverse(network, networkView, nodesToViews, otherNode, partition, nodesSeenMap, edgesSeenMap);
			}
		}
	}
}
