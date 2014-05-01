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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

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

public class LuceneConfigPanel<NETWORK, NODE, EDGE> extends JPanel {
	private static final long serialVersionUID = 1L;
	private final ImportedDataPanel importedDataPanel;
	private final DataSetManager dataSetManager;
	
	public LuceneConfigPanel(LuceneDataSet<NETWORK, NODE, EDGE> data, DataSetManager dataSetManager, UiUtils uiUtils, FileUtils fileUtils, CytoscapeUtils<NETWORK, NODE, EDGE> cytoscapeUtils, TaskDispatcher taskDispatcher) {
		setOpaque(false);
		setLayout(new GridBagLayout());
		
		this.dataSetManager = dataSetManager;
		importedDataPanel = new ImportedDataPanel(dataSetManager, uiUtils, fileUtils, taskDispatcher);
		
		JTabbedPane tabs = new JTabbedPane();
		tabs.addTab(Strings.luceneConfigNetManiaTab_title, new NetManiaPanel<NETWORK, NODE, EDGE>(dataSetManager, uiUtils, taskDispatcher));
		tabs.addTab(Strings.luceneConfigUserDefinedTab_title, importedDataPanel);
		tabs.addTab(Strings.luceneConfigCyNetworkTab_title, new ImportCyNetworkPanel<NETWORK, NODE, EDGE>(dataSetManager, uiUtils, cytoscapeUtils, taskDispatcher));
		
		add(tabs, new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		
		validate();
	}
	
	public void close() {
		importedDataPanel.close();
	}

	public DataSet getDataSet() {
		return dataSetManager.getDataSet();
	}
}
