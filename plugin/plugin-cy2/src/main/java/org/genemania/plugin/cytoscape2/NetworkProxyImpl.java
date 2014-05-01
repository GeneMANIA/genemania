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
/**
 * 
 */
package org.genemania.plugin.cytoscape2;

import giny.model.Edge;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.genemania.plugin.OneUseIterable;
import org.genemania.plugin.proxies.NetworkProxy;

import cytoscape.CyEdge;
import cytoscape.CyNetwork;
import cytoscape.CyNode;
import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;
import cytoscape.data.SelectFilter;

class NetworkProxyImpl extends ProxyImpl<CyNetwork> implements NetworkProxy<CyNetwork, CyNode, CyEdge> {
	NetworkProxyImpl(CyNetwork network) {
		super(network);
	}

	@Override
	protected CyAttributes getAttributes() {
		return Cytoscape.getNetworkAttributes();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<CyEdge> getEdges() {
		Set<CyEdge> edges = new HashSet<CyEdge>();
		for (CyEdge edge : new OneUseIterable<CyEdge>(getProxied().edgesIterator())) {
			edges.add(edge);
		}
		return edges;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<CyNode> getNodes() {
		Set<CyNode> nodes = new HashSet<CyNode>();
		for (CyNode node : new OneUseIterable<CyNode>(getProxied().nodesIterator())) {
			nodes.add(node);
		}
		return nodes;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Set<CyEdge> getSelectedEdges() {
		Set<CyEdge> edges = new HashSet<CyEdge>();
		for (CyEdge edge : (Set<CyEdge>) getProxied().getSelectedEdges()) {
			edges.add(edge);
		}
		return edges;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Set<CyNode> getSelectedNodes() {
		Set<CyNode> nodes = new HashSet<CyNode>();
		for (CyNode node : (Set<CyNode>) getProxied().getSelectedNodes()) {
			nodes.add(node);
		}
		return nodes;
	}

	@Override
	public String getTitle() {
		return getProxied().getTitle();
	}

	@Override
	public void setSelectedNode(CyNode node, boolean selected) {
		SelectFilter filter = getProxied().getSelectFilter();
		filter.setSelected(node, selected);
	}

	@Override
	public void setSelectedEdge(CyEdge edge, boolean selected) {
		SelectFilter filter = getProxied().getSelectFilter();
		filter.setSelected(edge, selected);
	}

	@Override
	public void setSelectedEdges(
			Collection<CyEdge> proxies, boolean selected) {
		SelectFilter filter = getProxied().getSelectFilter();
		Set<Edge> edges = new HashSet<Edge>();
		for (CyEdge proxy : proxies) {
			edges.add(proxy);
		}
		filter.setSelectedEdges(edges, selected);
	}

	@Override
	public void setSelectedNodes(Collection<CyNode> proxies,
			boolean selected) {
		SelectFilter filter = getProxied().getSelectFilter();
		Set<giny.model.Node> nodes = new HashSet<giny.model.Node>();
		for (CyNode proxy : proxies) {
			nodes.add(proxy);
		}
		filter.setSelectedNodes(nodes, selected);
	}

	@Override
	public void unselectAllEdges() {
		getProxied().getSelectFilter().unselectAllEdges();
	}

	@Override
	public void unselectAllNodes() {
		getProxied().getSelectFilter().unselectAllNodes();
	}

	@Override
	public String getIdentifier() {
		return getProxied().getIdentifier();
	}
	
	@Override
	public Collection<String> getNodeAttributeNames() {
		return Arrays.asList(Cytoscape.getNodeAttributes().getAttributeNames());
	}
	
	@Override
	public Collection<String> getEdgeAttributeNames() {
		return Arrays.asList(Cytoscape.getEdgeAttributes().getAttributeNames());
	}
	
	@Override
	public Collection<CyNode> getNeighbours(CyNode node) {
		Collection<CyNode> neighbours = new HashSet<CyNode>();
		CyNetwork network = getProxied();
		int nodeIndex = node.getRootGraphIndex();
		for (int index : network.getAdjacentEdgeIndicesArray(nodeIndex, true, true, true)) {
			CyEdge edge = (CyEdge) network.getEdge(index);
			CyNode other = (CyNode) edge.getSource();
			if (other.getRootGraphIndex() == nodeIndex) {
				other = (CyNode) edge.getTarget();
				if (other.getRootGraphIndex() == nodeIndex) {
					continue;
				}
			}
			neighbours.add((CyNode) other);
		}
		return neighbours;
	}
}