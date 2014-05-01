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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Properties;

import org.genemania.plugin.GeneMania;
import org.genemania.plugin.Strings;
import org.genemania.plugin.cytoscape.CytoscapeUtils;
import org.genemania.plugin.delegates.SessionChangeDelegate;
import org.genemania.plugin.selection.NetworkSelectionManager;
import org.genemania.plugin.task.GeneManiaTask;
import org.genemania.plugin.task.TaskDispatcher;

import cytoscape.CyEdge;
import cytoscape.CyNetwork;
import cytoscape.CyNode;
import cytoscape.Cytoscape;
import cytoscape.CytoscapeInit;
import cytoscape.view.CytoscapeDesktop;

public class SessionChangeListener implements PropertyChangeListener {

	private final CytoscapeUtils<CyNetwork, CyNode, CyEdge> cytoscapeUtils;
	private final GeneMania<CyNetwork, CyNode, CyEdge> plugin;
	private final TaskDispatcher taskDispatcher;

	public SessionChangeListener(GeneMania<CyNetwork, CyNode, CyEdge> plugin, TaskDispatcher taskDispatcher, CytoscapeUtils<CyNetwork, CyNode, CyEdge> cytoscapeUtils) {
		this.plugin = plugin;
		this.taskDispatcher = taskDispatcher;
		this.cytoscapeUtils = cytoscapeUtils;
	}
	
	public void propertyChange(PropertyChangeEvent evt) {
		Properties properties = CytoscapeInit.getProperties();
		String dataSourcePath = properties.getProperty(GeneMania.DATA_SOURCE_PATH_PROPERTY);
		final File path;
		if (dataSourcePath == null) {
			path = null;
		} else {
			path = new File(dataSourcePath);
		}
		
		GeneManiaTask task = new GeneManiaTask(Strings.sessionChangeListener_title) {
			@Override
			protected void runTask() throws Throwable {
				SessionChangeDelegate<CyNetwork, CyNode, CyEdge> delegate = new SessionChangeDelegate<CyNetwork, CyNode, CyEdge>(path, plugin, progress, cytoscapeUtils);
				delegate.invoke();
				
				CyNetwork currentNetwork = cytoscapeUtils.getCurrentNetwork();
				PropertyChangeEvent event = new PropertyChangeEvent(Cytoscape.getDesktop(), CytoscapeDesktop.NETWORK_VIEW_FOCUSED, null, currentNetwork.getIdentifier());
				NetworkSelectionManager<CyNetwork, CyNode, CyEdge> manager = plugin.getNetworkSelectionManager();
				manager.getNetworkChangeListener().propertyChange(event);
			}
		};
		taskDispatcher.executeTask(task, cytoscapeUtils.getFrame(), true, true);
	}
}
