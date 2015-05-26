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

package org.genemania.plugin.view;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.util.Comparator;

import org.genemania.plugin.GeneMania;
import org.genemania.plugin.NetworkUtils;
import org.genemania.plugin.Strings;
import org.genemania.plugin.cytoscape.CytoscapeUtils;
import org.genemania.plugin.model.Group;
import org.genemania.plugin.model.Network;
import org.genemania.plugin.model.ViewState;
import org.genemania.plugin.selection.NetworkSelectionManager;
import org.genemania.plugin.selection.SelectionEvent;
import org.genemania.plugin.selection.SelectionListener;
import org.genemania.plugin.view.components.ToggleInfoPanel;
import org.genemania.plugin.view.util.UiUtils;

@SuppressWarnings("serial")
public class NetworkGroupInfoPanel<NETWORK, NODE, EDGE> extends ToggleInfoPanel<Group<?, ?>, NetworkGroupDetailPanel<NETWORK, NODE, EDGE>> {
	
	private final CytoscapeUtils<NETWORK, NODE, EDGE> cytoscapeUtils;
	private final GeneMania<NETWORK, NODE, EDGE> plugin;
	private final NetworkUtils networkUtils;
	private SelectionListener<Group<?, ?>> selectionListener;

	public NetworkGroupInfoPanel(GeneMania<NETWORK, NODE, EDGE> plugin, CytoscapeUtils<NETWORK, NODE, EDGE> cytoscapeUtils, NetworkUtils networkUtils, UiUtils uiUtils) {
		super(uiUtils);
		this.plugin = plugin;
		this.cytoscapeUtils = cytoscapeUtils;
		this.networkUtils = networkUtils;
	}
	
	@Override
	public void applyOptions(final ViewState options) {
		removeAll();
		if (selectionListener != null) {
			removeSelectionListener(selectionListener);
		}

		selectionListener = new SelectionListener<Group<?, ?>>() {
			public void selectionChanged(SelectionEvent<Group<?, ?>> event) {
				// Only handle deselections
				if (event.selected) {
					return;
				}
				
				for (Group<?, ?> group : event.items) {
					for (Network<?> network : group.getNetworks()) {
						options.setNetworkHighlighted(network, false);
					}
				}
				for (NetworkGroupDetailPanel<NETWORK, NODE, EDGE> panel : dataModel) {
					panel.getNetworkInfoPanel().updateSelection(options);
				}
			}
		};
		
		addSelectionListener(selectionListener);
		
		int index = 0;
		NETWORK network = cytoscapeUtils.getCurrentNetwork();
		for (Group<?, ?> group : options.getAllGroups()) {
			if (group.getWeight() == 0) {
				continue;
			}
			
			boolean enabledByDefault = options.isEnabled(group);
			NetworkGroupDetailPanel<NETWORK, NODE, EDGE> panel = new NetworkGroupDetailPanel<NETWORK, NODE, EDGE>(group, this, plugin, networkUtils, uiUtils, enabledByDefault, options);
			addDetailPanel(panel, index);
			index++;
			
			cytoscapeUtils.setHighlight(options, group, network, enabledByDefault);
		}
		add(uiUtils.createFillerPanel(), new GridBagConstraints(0, index, 1, 1, 1, 1, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		
		sort(1, true);
		invalidate();
	}
	
	@Override
	public void updateSelection(ViewState options) {
		final NETWORK cyNetwork = cytoscapeUtils.getCurrentNetwork();
		
		if (cyNetwork == null)
			return;
		
		int totalEnabled = 0;
		NetworkGroupDetailPanel<NETWORK, NODE, EDGE> mostRecent = null;
		final Group<?, ?> mostRecentGroup = options.getMostRecentGroup();
		
		for (NetworkGroupDetailPanel<NETWORK, NODE, EDGE> panel : dataModel) {
			final Group<?, ?> group = panel.getSubject();
			final boolean enabled = options.isGroupHighlighted(group);
			
			if (enabled) {
				totalEnabled++;
				panel.showDetails(true, 1);
			}

			panel.setItemEnabled(options.isEnabled(group));
			panel.setSelected(enabled);
			
			if (group.equals(mostRecentGroup))
				mostRecent = panel;
			
			panel.getNetworkInfoPanel().updateSelection(options);
		}
		
		if (totalEnabled == 0) {
			mostRecent = null;
		}
		if (mostRecent != null) {
			ensureVisible(mostRecent);
			mostRecent.showDetails(true, 1);
		}
	}

	@Override
	protected void setAllEnabled(boolean enabled) {
		for (NetworkGroupDetailPanel<NETWORK, NODE, EDGE> panel : dataModel) {
			panel.setItemEnabled(enabled);
		}

		NETWORK cyNetwork = cytoscapeUtils.getCurrentNetwork();
		NetworkSelectionManager<NETWORK, NODE, EDGE> selectionManager = plugin.getNetworkSelectionManager();
		ViewState options = selectionManager.getNetworkConfiguration(cyNetwork);
		cytoscapeUtils.setHighlight(options, null, cyNetwork, enabled);
	}

	@Override
	protected Comparator<NetworkGroupDetailPanel<NETWORK, NODE, EDGE>> getComparator(final int column, Boolean descending) {
		if (descending == null) {
			isDescending = !isDescending;
		} else {
			isDescending = descending;
		}
		
		return new Comparator<NetworkGroupDetailPanel<NETWORK, NODE, EDGE>>() {
			public int compare(NetworkGroupDetailPanel<NETWORK, NODE, EDGE> o1, NetworkGroupDetailPanel<NETWORK, NODE, EDGE> o2) {
				switch (column) {
				case 0:
					return o1.getSubject().getName().compareTo(o2.getSubject().getName()) * (isDescending ? -1 : 1);
				case 1:
					return Double.compare(o1.getWeight(), o2.getWeight()) * (isDescending ? -1 : 1);
				default:
					throw new RuntimeException(String.format(Strings.tableModelInvalidColumn_error, column));
				}
			}
		};
	}
	
	@Override
	public void sort(int column, Boolean descending) {
		super.sort(column, descending);

		for (NetworkGroupDetailPanel<NETWORK, NODE, EDGE> panel : dataModel) {
			panel.getNetworkInfoPanel().sort(column, descending);
		}
	}
}
