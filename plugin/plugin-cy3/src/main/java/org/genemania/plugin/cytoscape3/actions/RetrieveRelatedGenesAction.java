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
import org.cytoscape.view.model.CyNetworkViewManager;
import org.genemania.plugin.FileUtils;
import org.genemania.plugin.GeneMania;
import org.genemania.plugin.NetworkUtils;
import org.genemania.plugin.Strings;
import org.genemania.plugin.controllers.RetrieveRelatedGenesController;
import org.genemania.plugin.cytoscape.CytoscapeUtils;
import org.genemania.plugin.delegates.RetrieveRelatedGenesDelegate;
import org.genemania.plugin.task.TaskDispatcher;
import org.genemania.plugin.view.util.UiUtils;

@SuppressWarnings("serial")
public class RetrieveRelatedGenesAction extends AbstractCyAction {

	private RetrieveRelatedGenesDelegate delegate;

	public RetrieveRelatedGenesAction(
			Map<String, String> properties,
			CyApplicationManager applicationManager,
			GeneMania plugin,
			RetrieveRelatedGenesController controller,
			CytoscapeUtils cytoscapeUtils,
			NetworkUtils networkUtils,
			UiUtils uiUtils,
			FileUtils fileUtils,
			TaskDispatcher taskDispatcher,
			CyNetworkViewManager viewManager
	) {
		super(properties, applicationManager, viewManager);
		
		putValue(NAME, Strings.retrieveRelatedGenes_menuLabel);
		
		delegate = new RetrieveRelatedGenesDelegate(plugin, controller, cytoscapeUtils, networkUtils, uiUtils,
				fileUtils, taskDispatcher);
	}

	public RetrieveRelatedGenesDelegate getDelegate() {
		return delegate;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		delegate.invoke();
	}
}
