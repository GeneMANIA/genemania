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

import java.util.Collections;

import org.genemania.plugin.FileUtils;
import org.genemania.plugin.NetworkUtils;
import org.genemania.plugin.Strings;
import org.genemania.plugin.cytoscape.CytoscapeUtils;
import org.genemania.plugin.cytoscape2.support.Compatibility;
import org.genemania.plugin.cytoscape26.Cy26Compatibility;
import org.genemania.plugin.data.DataSetManager;
import org.genemania.plugin.data.lucene.LuceneDataSetFactory;
import org.genemania.plugin.task.TaskDispatcher;
import org.genemania.plugin.view.util.UiUtils;

import cytoscape.CyEdge;
import cytoscape.CyNetwork;
import cytoscape.CyNode;
import cytoscape.CytoscapeVersion;
import cytoscape.plugin.CytoscapePlugin;

public class GeneManiaPlugin extends CytoscapePlugin {
	private GeneManiaImpl geneMania;
	private Object mutex = new Object();
	private boolean activated;

	public GeneManiaPlugin() {
		// Cytoscape doesn't seem to use activate/deactivate so we'll trigger
		// it manually for now.
		activate();
	}
	
	@Override
	public void activate() {
		synchronized (mutex) {
			// Make sure we don't activate twice.
			if (activated) {
				return;
			}
			UiUtils uiUtils = new UiUtils();
			FileUtils fileUtils = new FileUtilsImpl();
			NetworkUtils networkUtils = new NetworkUtils();
			CytoscapeVersion version = new CytoscapeVersion();
			Compatibility compatibility = createCompatibility(version);
			CytoscapeUtils<CyNetwork, CyNode, CyEdge> cytoscapeUtils = new CytoscapeUtilsImpl(networkUtils, compatibility);
			
			TaskDispatcher taskDispatcher = new TaskDispatcher(uiUtils);
			DataSetManager dataSetManager = new DataSetManager();
			dataSetManager.addDataSetFactory(new LuceneDataSetFactory<CyNetwork, CyNode, CyEdge>(dataSetManager, uiUtils, fileUtils, cytoscapeUtils, taskDispatcher), Collections.emptyMap());
			
			geneMania = new GeneManiaImpl(dataSetManager, cytoscapeUtils, uiUtils, fileUtils, networkUtils, taskDispatcher);
			geneMania.startUp();
			activated = true;
		}		
	}
	
	private Compatibility createCompatibility(CytoscapeVersion version) {
		String[] parts = version.getMajorVersion().split("[.]"); //$NON-NLS-1$
		if (!parts[0].equals("2")) { //$NON-NLS-1$
			throw new RuntimeException(Strings.cy2_geneManiaPlugin_error);
		}
		
		int minorVersion = Integer.parseInt(parts[1]);
		if (minorVersion < 8) {
			return new Cy26Compatibility();
		}
		return new CompatibilityImpl();
	}

	@Override
	public void deactivate() {
		synchronized (mutex) {
			geneMania.shutDown();
			activated = false;
		}
	}
}
