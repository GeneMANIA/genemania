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

public class RetrieveRelatedGenesDelegate implements Delegate {
	
	private static final long MIN_HEAP_SIZE = 900 * 1000000;

	private RetrieveRelatedGenesDialog dialog;

	private final GeneMania plugin;
	private final UiUtils uiUtils;
	private final CytoscapeUtils cytoscapeUtils;
	private final NetworkUtils networkUtils;
	private final  FileUtils fileUtils;
	private final TaskDispatcher taskDispatcher;
	private final DataSetManager dataSetManager;
	
	private final Object lock = new Object();

	public RetrieveRelatedGenesDelegate(
			GeneMania plugin,
			CytoscapeUtils cytoscapeUtils,
			NetworkUtils networkUtils,
			UiUtils uiUtils,
			FileUtils fileUtils,
			TaskDispatcher taskDispatcher
	) {
		this.plugin = plugin;
		this.uiUtils = uiUtils;
		this.cytoscapeUtils = cytoscapeUtils;
		this.networkUtils = networkUtils;
		this.fileUtils = fileUtils;
		this.taskDispatcher = taskDispatcher;
		
		dataSetManager = plugin.getDataSetManager();
    }
	
	public RetrieveRelatedGenesDialog getDialog() {
		synchronized(lock) {
			if (dialog == null) {
				dialog = new RetrieveRelatedGenesDialog(
						null,
						false,
						new RetrieveRelatedGenesController(plugin, cytoscapeUtils, networkUtils, taskDispatcher),
						dataSetManager,
						networkUtils,
						uiUtils,
						cytoscapeUtils,
						fileUtils,
						taskDispatcher,
						plugin
				);
				dialog.setAlwaysOnTop(false);
				dialog.setLocationByPlatform(true);
			}
		}
		
		return dialog;
	}
	
	@Override
	public void invoke() {
		checkHeapSize();
		DataSetManager dataSetManager = plugin.getDataSetManager();
		DataSet data = dataSetManager.getDataSet();
		
		if (data == null) {
			plugin.initializeData(getDialog(), true);
			data = dataSetManager.getDataSet();
		}
		
		if (data != null) {
			try {
				List<Organism> organisms = data.getMediatorProvider().getOrganismMediator().getAllOrganisms();
				if (organisms.size() == 0) {
					IConfiguration configuration = data.getConfiguration();
					configuration.showUi(getDialog());
				}
			} catch (DataStoreException e) {
				LogUtils.log(getClass(), e);
			}
		}
		
		getDialog().setVisible(true);
	}

	private void checkHeapSize() {
		Runtime runtime = Runtime.getRuntime();
		
		if (runtime.maxMemory() < MIN_HEAP_SIZE) {
			String message = String.format(Strings.heapSize_error, (int) (runtime.maxMemory() / 1000000), (int) (MIN_HEAP_SIZE / 1000000));
			JEditorPane editor = uiUtils.createLinkEnabledEditorPane(getDialog(), message);
			JOptionPane.showMessageDialog(getDialog(), editor, Strings.heapSize_title, JOptionPane.WARNING_MESSAGE);
		}
	}
}
