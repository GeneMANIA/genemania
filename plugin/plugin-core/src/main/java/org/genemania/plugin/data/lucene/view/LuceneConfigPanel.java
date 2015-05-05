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

package org.genemania.plugin.data.lucene.view;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.genemania.plugin.FileUtils;
import org.genemania.plugin.Strings;
import org.genemania.plugin.cytoscape.CytoscapeUtils;
import org.genemania.plugin.data.DataSet;
import org.genemania.plugin.data.DataSetManager;
import org.genemania.plugin.data.lucene.LuceneDataSet;
import org.genemania.plugin.task.TaskDispatcher;
import org.genemania.plugin.view.util.UiUtils;

@SuppressWarnings("serial")
public class LuceneConfigPanel<NETWORK, NODE, EDGE> extends JPanel {
	
	private final ImportedDataPanel importedDataPanel;
	private final DataSetManager dataSetManager;
	private final ImportCyNetworkPanel<NETWORK, NODE, EDGE> importCyNetworkPanel;
	private final NetManiaPanel<NETWORK, NODE, EDGE> netManiaPanel;
	
	public LuceneConfigPanel(
			final LuceneDataSet<NETWORK, NODE, EDGE> data,
			final DataSetManager dataSetManager,
			final UiUtils uiUtils,
			final FileUtils fileUtils,
			final CytoscapeUtils<NETWORK, NODE, EDGE> cytoscapeUtils,
			final TaskDispatcher taskDispatcher
	) {
		if (uiUtils.isAquaLAF())
			setOpaque(false);
		
		this.dataSetManager = dataSetManager;
		
		netManiaPanel = new NetManiaPanel<NETWORK, NODE, EDGE>(dataSetManager, uiUtils, taskDispatcher);
		importedDataPanel = new ImportedDataPanel(dataSetManager, uiUtils, fileUtils, taskDispatcher);
		importCyNetworkPanel = new ImportCyNetworkPanel<NETWORK, NODE, EDGE>(dataSetManager, uiUtils, cytoscapeUtils, taskDispatcher);
		
		addComponments();
		validate();
	}

	public void close() {
		importedDataPanel.close();
	}

	public DataSet getDataSet() {
		return dataSetManager.getDataSet();
	}
	
	private void addComponments() {
		final JTabbedPane tabs = new JTabbedPane();
		tabs.addTab(Strings.luceneConfigNetManiaTab_title, netManiaPanel);
		tabs.addTab(Strings.luceneConfigUserDefinedTab_title, importedDataPanel);
		tabs.addTab(Strings.luceneConfigCyNetworkTab_title, importCyNetworkPanel);
		
		setLayout(new BorderLayout());
		add(tabs, BorderLayout.CENTER);
	}
}
