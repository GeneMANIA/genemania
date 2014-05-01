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
package org.genemania.plugin.delegates;

import org.genemania.exception.ApplicationException;
import org.genemania.plugin.GeneMania;
import org.genemania.plugin.cytoscape.CytoscapeUtils;
import org.genemania.plugin.model.ViewState;
import org.genemania.plugin.selection.NetworkSelectionManager;

public class SelectionDelegate<NETWORK, NODE, EDGE> implements Delegate {

	protected final boolean selected;
	protected NETWORK network;
	protected final NetworkSelectionManager<NETWORK, NODE, EDGE> manager;
	private final GeneMania<NETWORK, NODE, EDGE> plugin;
	protected final CytoscapeUtils<NETWORK, NODE, EDGE> cytoscapeUtils;

	private static Object selectionMutex = new Object();
	
	public SelectionDelegate(boolean selected, NETWORK network, NetworkSelectionManager<NETWORK, NODE, EDGE> manager, GeneMania<NETWORK, NODE, EDGE> plugin, CytoscapeUtils<NETWORK, NODE, EDGE> cytoscapeUtils) {
		this.selected = selected;
		this.network = network;
		this.manager = manager;
		this.plugin = plugin;
		this.cytoscapeUtils = cytoscapeUtils;
	}
	
	@Override
	public void invoke() throws ApplicationException {
		synchronized (selectionMutex) {
			if (!manager.isSelectionListenerEnabled()) {
				return;
			}
			
			ViewState options = manager.getNetworkConfiguration(network);
			if (options == null) {
				return;
			}
			
			handleSelection(options);
			
			boolean listenerState = manager.isSelectionListenerEnabled();
			manager.setSelectionListenerEnabled(false);
			try {
				plugin.updateSelection(options);
			} finally {
				manager.setSelectionListenerEnabled(listenerState);
			}
		}
	}

	protected void handleSelection(ViewState options) throws ApplicationException {
	}
}
