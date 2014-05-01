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

package org.genemania.plugin.cytoscape2.actions;

import javax.swing.JMenuItem;
import javax.swing.SwingConstants;

import org.genemania.plugin.GeneMania;
import org.genemania.plugin.NetworkUtils;
import org.genemania.plugin.Strings;
import org.genemania.plugin.controllers.ManiaResultsController;
import org.genemania.plugin.cytoscape.CytoscapeUtils;
import org.genemania.plugin.data.DataSetManager;
import org.genemania.plugin.view.ManiaResultsPanel;
import org.genemania.plugin.view.util.UiUtils;

import cytoscape.Cytoscape;
import cytoscape.view.cytopanels.CytoPanel;

public class ManiaResultsAction<NETWORK, NODE, EDGE> extends ShowCytoPanelAction<ManiaResultsPanel<NETWORK, NODE, EDGE>> {
	private static final long serialVersionUID = 1L;

	private ManiaResultsPanel<NETWORK, NODE, EDGE> panel;

	private JMenuItem menu;

	private final CytoscapeUtils<NETWORK, NODE, EDGE> cytoscapeUtils;

	private final GeneMania<NETWORK, NODE, EDGE> plugin;

	private final NetworkUtils networkUtils;

	private final UiUtils uiUtils;
	
	public ManiaResultsAction(String name, GeneMania<NETWORK, NODE, EDGE> plugin, CytoscapeUtils<NETWORK, NODE, EDGE> cytoscapeUtils, NetworkUtils networkUtils, UiUtils uiUtils) {
		super(name);
		this.plugin = plugin;
		this.cytoscapeUtils = cytoscapeUtils;
		this.networkUtils = networkUtils;
		this.uiUtils = uiUtils;
	}

	@Override
	protected ManiaResultsPanel<NETWORK, NODE, EDGE> createPanel() {
		DataSetManager dataSetManager = plugin.getDataSetManager();
		panel = new ManiaResultsPanel<NETWORK, NODE, EDGE>(new ManiaResultsController<NETWORK, NODE, EDGE>(dataSetManager, cytoscapeUtils, uiUtils, networkUtils), plugin, cytoscapeUtils, networkUtils, uiUtils);
		panel.setName(Strings.maniaResults_title);
		return panel;
	}

	@Override
	public CytoPanel getCytoPanel() {
	    return Cytoscape.getDesktop().getCytoPanel(SwingConstants.EAST);
	}

	public void setMenuItem(JMenuItem menu) {
		this.menu = menu;
	}
	
	@Override
	protected void handleVisibilityChange(boolean visible) {
		if (menu == null) {
			return;
		}
		if (visible) {
			menu.setText(Strings.maniaResults_menuLabel2);
		} else {
			menu.setText(Strings.maniaResults_menuLabel);
		}
	}
}
