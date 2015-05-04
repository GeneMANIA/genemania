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
import org.genemania.plugin.data.DataSetManager;
import org.genemania.plugin.task.TaskDispatcher;
import org.genemania.plugin.view.util.UiUtils;

@SuppressWarnings("serial")
public class ImportedDataPanel extends JPanel {
	
	private final ImportedNetworkDataPanel importNetworkPanel;
	private final ImportOrganismPanel importOrganismPanel;

	public ImportedDataPanel(DataSetManager dataSetManager, UiUtils uiUtils, FileUtils fileUtils, TaskDispatcher taskDispatcher) {
		if (uiUtils.isAquaLAF())
			setOpaque(false);
		
		setLayout(new GridBagLayout());
		JTabbedPane tabPane = new JTabbedPane();
		
		importNetworkPanel = new ImportedNetworkDataPanel(dataSetManager, uiUtils, fileUtils, taskDispatcher);
		tabPane.add(Strings.importedDataPanelNetworkTab_title, importNetworkPanel);
		
		importOrganismPanel = new ImportOrganismPanel(dataSetManager, fileUtils, uiUtils, taskDispatcher);
		tabPane.add(Strings.importedDataPanelOrganismTab_title, importOrganismPanel);
		add(tabPane, new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
	}

	public void close() {
		importNetworkPanel.close();
		importOrganismPanel.close();
	}
}
