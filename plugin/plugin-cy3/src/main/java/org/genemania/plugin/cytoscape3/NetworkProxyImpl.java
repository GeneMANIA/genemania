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
package org.genemania.plugin.cytoscape3;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyEdge.Type;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.genemania.plugin.proxies.NetworkProxy;

public class NetworkProxyImpl extends ProxyImpl<CyNetwork> implements NetworkProxy {

	private CyEventHelper eventHelper;
	
	public NetworkProxyImpl(CyNetwork network, CyEventHelper eventHelper) {
		super(network, network);
		this.eventHelper = eventHelper;
	}
	
	@Override
	public Collection<CyEdge> getEdges() {
		return getProxied().getEdgeList();
	}

	@Override
	public Collection<CyNode> getNodes() {
		return getProxied().getNodeList();
	}

	@Override
	public Set<CyEdge> getSelectedEdges() {
		Set<CyEdge> results = new HashSet<CyEdge>();
		CyNetwork network = getNetwork();
		for (CyEdge edge : getProxied().getEdgeList()) {
			if (network.getRow(edge).get(CyNetwork.SELECTED, Boolean.class)) {
				results.add(edge);
			}
		}
		return results;
	}
	
	@Override
	public Set<CyNode> getSelectedNodes() {
		Set<CyNode> results = new HashSet<CyNode>();
		CyNetwork network = getNetwork();
		for (CyNode node : getProxied().getNodeList()) {
			if (network.getRow(node).get(CyNetwork.SELECTED, Boolean.class)) {
				results.add(node);
			}
		}
		return results;
	}

	@Override
	public String getTitle() {
		CyNetwork network = getNetwork();
		return network.getRow(network).get(CyNetwork.NAME, String.class);
	}

	@Override
	public void setSelectedEdge(CyEdge edge, boolean selected) {
		getNetwork().getRow(edge).set(CyNetwork.SELECTED, selected);
		eventHelper.flushPayloadEvents();
	}

	@Override
	public void setSelectedEdges(Collection<CyEdge> edges, boolean selected) {
		CyNetwork network = getNetwork();
		for (CyEdge edge : edges) {
			network.getRow(edge).set(CyNetwork.SELECTED, selected);
		}
		eventHelper.flushPayloadEvents();
	}

	@Override
	public void setSelectedNode(CyNode node, boolean selected) {
		getNetwork().getRow(node).set(CyNetwork.SELECTED, selected);
		eventHelper.flushPayloadEvents();
	}

	@Override
	public void setSelectedNodes(Collection<CyNode> nodes, boolean selected) {
		CyNetwork network = getNetwork();
		for (CyNode node : nodes) {
			network.getRow(node).set(CyNetwork.SELECTED, selected);
		}
		eventHelper.flushPayloadEvents();
	}

	@Override
	public void unselectAllEdges() {
		setSelectedEdges(getProxied().getEdgeList(), false);
	}

	@Override
	public void unselectAllNodes() {
		setSelectedNodes(getProxied().getNodeList(), false);
	}
	
	@Override
	public Collection<String> getNodeAttributeNames() {
		CyTable table = getProxied().getDefaultNodeTable();
		return getNames(table.getColumns());
	}
	
	@Override
	public Collection<String> getEdgeAttributeNames() {
		CyTable table = getProxied().getDefaultEdgeTable();
		return getNames(table.getColumns());
	}

	private Collection<String> getNames(Collection<CyColumn> columns) {
		Set<String> names = new HashSet<String>();
		for (CyColumn column : columns) {
			names.add(column.getName());
		}
		return names;
	}
	
	@Override
	public Collection<CyNode> getNeighbours(CyNode node) {
		return getProxied().getNeighborList(node, Type.ANY);
	}
}
