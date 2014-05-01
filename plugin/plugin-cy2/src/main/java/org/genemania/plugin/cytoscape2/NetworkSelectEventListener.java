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
package org.genemania.plugin.cytoscape2;

import java.util.Set;

import org.genemania.exception.ApplicationException;
import org.genemania.plugin.GeneMania;
import org.genemania.plugin.LogUtils;
import org.genemania.plugin.cytoscape.CytoscapeUtils;
import org.genemania.plugin.delegates.NodeSelectionDelegate;
import org.genemania.plugin.delegates.NodeSetSelectionDelegate;
import org.genemania.plugin.delegates.SelectionDelegate;
import org.genemania.plugin.selection.NetworkSelectionManager;

import cytoscape.CyEdge;
import cytoscape.CyNetwork;
import cytoscape.CyNode;
import cytoscape.data.SelectEvent;
import cytoscape.data.SelectEventListener;

public class NetworkSelectEventListener implements SelectEventListener {
	private final CyNetwork network;
	private final NetworkSelectionManager<CyNetwork, CyNode, CyEdge> manager;
	private final GeneMania<CyNetwork, CyNode, CyEdge> plugin;
	private final CytoscapeUtils<CyNetwork, CyNode, CyEdge> cytoscapeUtils;
	private final SelectionDelegate<CyNetwork, CyNode, CyEdge> defaultDelegate;

	public NetworkSelectEventListener(CyNetwork network, NetworkSelectionManager<CyNetwork, CyNode, CyEdge> manager, GeneMania<CyNetwork, CyNode, CyEdge> plugin, CytoscapeUtils<CyNetwork, CyNode, CyEdge> cytoscapeUtils) {
		this.network = network;
		this.manager = manager;
		this.plugin = plugin;
		this.cytoscapeUtils = cytoscapeUtils;
		defaultDelegate = new SelectionDelegate<CyNetwork, CyNode, CyEdge>(true, network, manager, plugin, cytoscapeUtils);
	}

	@SuppressWarnings("unchecked")
	public void onSelectEvent(SelectEvent event) {
		SelectionDelegate<CyNetwork, CyNode, CyEdge> delegate;
		boolean selected = event.getEventType();
		
		switch (event.getTargetType()) {
		case SelectEvent.SINGLE_EDGE:
		case SelectEvent.EDGE_SET:
			delegate = defaultDelegate;
			break;
		case SelectEvent.NODE_SET:
			delegate = new NodeSetSelectionDelegate<CyNetwork, CyNode, CyEdge>((Set<CyNode>) event.getTarget(), selected, network, manager, plugin, cytoscapeUtils);
			break;
		case SelectEvent.SINGLE_NODE:
			delegate = new NodeSelectionDelegate<CyNetwork, CyNode, CyEdge>((CyNode) event.getTarget(), selected, network, manager, plugin, cytoscapeUtils);
			break;
		default:
			return;
		}
		
		try {
			delegate.invoke();
		} catch (ApplicationException e) {
			LogUtils.log(getClass(), e);
		}
	}
}
