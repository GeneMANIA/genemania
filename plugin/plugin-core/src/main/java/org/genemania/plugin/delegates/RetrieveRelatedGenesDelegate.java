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

import java.util.List;

import javax.swing.JEditorPane;
import javax.swing.JOptionPane;

import org.genemania.domain.Organism;
import org.genemania.exception.DataStoreException;
import org.genemania.plugin.FileUtils;
import org.genemania.plugin.GeneMania;
import org.genemania.plugin.LogUtils;
import org.genemania.plugin.NetworkUtils;
import org.genemania.plugin.Strings;
import org.genemania.plugin.controllers.RetrieveRelatedGenesController;
import org.genemania.plugin.cytoscape.CytoscapeUtils;
import org.genemania.plugin.data.DataSet;
import org.genemania.plugin.data.DataSetManager;
import org.genemania.plugin.data.IConfiguration;
import org.genemania.plugin.task.TaskDispatcher;
import org.genemania.plugin.view.RetrieveRelatedGenesDialog;
import org.genemania.plugin.view.util.UiUtils;

public class RetrieveRelatedGenesDelegate<NETWORK, NODE, EDGE> implements Delegate {
	private static final long MIN_HEAP_SIZE = 900 * 1000000;

	private final RetrieveRelatedGenesDialog<NETWORK, NODE, EDGE> dialog;

	private final GeneMania<NETWORK, NODE, EDGE> plugin;
	private final UiUtils uiUtils;

	public RetrieveRelatedGenesDelegate(GeneMania<NETWORK, NODE, EDGE> plugin, CytoscapeUtils<NETWORK, NODE, EDGE> cytoscapeUtils, NetworkUtils networkUtils, UiUtils uiUtils, FileUtils fileUtils, TaskDispatcher taskDispatcher) {
		this.plugin = plugin;
		this.uiUtils = uiUtils;
		
		DataSetManager dataSetManager = plugin.getDataSetManager();
		dialog = new RetrieveRelatedGenesDialog<NETWORK, NODE, EDGE>(null, false, new RetrieveRelatedGenesController<NETWORK, NODE, EDGE>(plugin, cytoscapeUtils, networkUtils, taskDispatcher), dataSetManager , networkUtils, uiUtils, cytoscapeUtils, fileUtils, taskDispatcher, plugin);
		dialog.setAlwaysOnTop(false);
		dialog.setResizable(true);
		dialog.setSize(900, 600);
		dialog.setLocationByPlatform(true);
    }
	
	@Override
	public void invoke() {
		checkHeapSize();
		DataSetManager dataSetManager = plugin.getDataSetManager();
		DataSet data = dataSetManager.getDataSet();
		if (data == null) {
			plugin.initializeData(dialog, true);
			data = dataSetManager.getDataSet();
		}
		
		if (data != null) {
			try {
				List<Organism> organisms = data.getMediatorProvider().getOrganismMediator().getAllOrganisms();
				if (organisms.size() == 0) {
					IConfiguration configuration = data.getConfiguration();
					configuration.showUi(dialog);
				}
			} catch (DataStoreException e) {
				LogUtils.log(getClass(), e);
			}
		}
		dialog.setVisible(true);
	}

	private void checkHeapSize() {
		Runtime runtime = Runtime.getRuntime();
		if (runtime.maxMemory() < MIN_HEAP_SIZE) {
			String message = String.format(Strings.heapSize_error, (int) (runtime.maxMemory() / 1000000), (int) (MIN_HEAP_SIZE / 1000000));
			JEditorPane editor = uiUtils.createLinkEnabledEditorPane(dialog, message);
			JOptionPane.showMessageDialog(dialog, editor, Strings.heapSize_title, JOptionPane.WARNING_MESSAGE);
		}
	}

	public RetrieveRelatedGenesDialog<NETWORK, NODE, EDGE> getDialog() {
		return dialog;
	}
}
