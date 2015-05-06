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
package org.genemania.plugin.cytoscape3.actions;

import java.awt.event.ActionEvent;
import java.util.Map;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.genemania.plugin.FileUtils;
import org.genemania.plugin.GeneMania;
import org.genemania.plugin.NetworkUtils;
import org.genemania.plugin.Strings;
import org.genemania.plugin.cytoscape.CytoscapeUtils;
import org.genemania.plugin.delegates.RetrieveRelatedGenesDelegate;
import org.genemania.plugin.task.TaskDispatcher;
import org.genemania.plugin.view.util.UiUtils;

@SuppressWarnings("serial")
public class RetrieveRelatedGenesAction extends AbstractCyAction {

	private RetrieveRelatedGenesDelegate<CyNetwork, CyNode, CyEdge> delegate;

	public RetrieveRelatedGenesAction(
			Map<String, String> properties,
			CyApplicationManager applicationManager,
			GeneMania<CyNetwork, CyNode, CyEdge> plugin,
			CytoscapeUtils<CyNetwork, CyNode, CyEdge> cytoscapeUtils,
			NetworkUtils networkUtils,
			UiUtils uiUtils,
			FileUtils fileUtils,
			TaskDispatcher taskDispatcher,
			CyNetworkViewManager viewManager
	) {
		super(properties, applicationManager, viewManager);
		putValue(NAME, Strings.retrieveRelatedGenes_menuLabel);
		delegate = new RetrieveRelatedGenesDelegate<CyNetwork, CyNode, CyEdge>(plugin, cytoscapeUtils, networkUtils, uiUtils, fileUtils, taskDispatcher);
	}

	public RetrieveRelatedGenesDelegate<CyNetwork, CyNode, CyEdge> getDelegate() {
		return delegate;
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		delegate.invoke();
	}
}
