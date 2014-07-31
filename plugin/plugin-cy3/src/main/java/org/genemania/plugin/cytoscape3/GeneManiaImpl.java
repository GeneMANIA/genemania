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
package org.genemania.plugin.cytoscape3;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CySession;
import org.cytoscape.session.events.SessionAboutToBeSavedEvent;
import org.cytoscape.session.events.SessionAboutToBeSavedListener;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionLoadedListener;
import org.genemania.plugin.AbstractGeneMania;
import org.genemania.plugin.FileUtils;
import org.genemania.plugin.GeneMania;
import org.genemania.plugin.LogUtils;
import org.genemania.plugin.NetworkUtils;
import org.genemania.plugin.cytoscape.CytoscapeUtils;
import org.genemania.plugin.data.DataSet;
import org.genemania.plugin.data.DataSetChangeListener;
import org.genemania.plugin.data.DataSetManager;
import org.genemania.plugin.model.ViewState;
import org.genemania.plugin.selection.NetworkSelectionManager;
import org.genemania.plugin.task.TaskDispatcher;
import org.genemania.plugin.view.util.UiUtils;
import org.genemania.util.ProgressReporter;

public class GeneManiaImpl extends AbstractGeneMania<CyNetwork, CyNode, CyEdge> implements SessionAboutToBeSavedListener, SessionLoadedListener {

	private static final String APP_NAMESPACE = "org.genemania";
	private static final String APP_PROPERTIES = "app.properties";
	
	private CyServiceRegistrar serviceRegistrar;
	private CySwingApplication application;
	private ManiaResultsCytoPanelComponent cytoPanelComponent;
	private boolean resultsVisible;
	
	private Object resultsMutex = new Object();
	private CyProperty<Properties> properties;

	public GeneManiaImpl(
			DataSetManager dataSetManager,
			CytoscapeUtils<CyNetwork, CyNode, CyEdge> cytoscapeUtils,
			UiUtils uiUtils, FileUtils fileUtils,
			NetworkUtils networkUtils,
			TaskDispatcher taskDispatcher, CySwingApplication application,
			CyServiceRegistrar serviceRegistrar,
			NetworkSelectionManager<CyNetwork, CyNode, CyEdge> selectionManager,
			final CyProperty<Properties> properties) {
		super(dataSetManager, cytoscapeUtils, uiUtils, fileUtils, networkUtils, taskDispatcher, selectionManager);
		this.serviceRegistrar = serviceRegistrar;
		this.application = application;
		this.properties = properties;
		
		cytoPanelComponent = new ManiaResultsCytoPanelComponent(dataSetManager, this, cytoscapeUtils, uiUtils, networkUtils);
		dataSetManager.getFactory("");
		dataSetManager.addDataSetChangeListener(new DataSetChangeListener() {
			@Override
			public void dataSetChanged(DataSet dataSet, ProgressReporter progress) {
				Properties properties2 = properties.getProperties();
				if (dataSet == null) {
					properties2.remove(GeneMania.DATA_SOURCE_PATH_PROPERTY);
					return;
				} else {
					properties2.setProperty(GeneMania.DATA_SOURCE_PATH_PROPERTY, dataSet.getBasePath());
				}
			}
		});
	}

	@Override
	protected void shutDown() {
	}

	@Override
	protected void startUp() {
	}

	@Override
	public void applyOptions(ViewState options) {
		cytoPanelComponent.getPanel().applyOptions(options);
	}

	@Override
	public void hideResults() {
		synchronized (resultsMutex) {
			if (resultsVisible) {
				serviceRegistrar.unregisterService(cytoPanelComponent, CytoPanelComponent.class);
				resultsVisible = false;
			}
		}
	}

	@Override
	public void showResults() {
		synchronized (resultsMutex) {
			if (!resultsVisible) {
				serviceRegistrar.registerService(cytoPanelComponent, CytoPanelComponent.class, new Properties());
				resultsVisible = true;
			}
		}
		
		CytoPanel panel = application.getCytoPanel(CytoPanelName.EAST);
		int index = panel.indexOfComponent(cytoPanelComponent.getComponent());
		
		if (index != -1) {
			panel.setSelectedIndex(index);
		}
	}

	@Override
	public void updateSelection(ViewState options) {
		cytoPanelComponent.getPanel().updateSelection(options);
	}
	
	@Override
	public void handleEvent(SessionAboutToBeSavedEvent event) {
		// Workaround for Cytoscape bug #2701
		// https://code.cytoscape.org/redmine/issues/2701
		try {
			File root = File.createTempFile(APP_NAMESPACE, ".tmp");
			root.delete();
			root.mkdir();
			root.deleteOnExit();
			
			File propertyFile = new File(root, APP_PROPERTIES);			
			BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(propertyFile));
			try {
				Properties properties2 = properties.getProperties();
				properties2.store(stream, "");
			} finally {
				stream.close();
			}
			propertyFile.deleteOnExit();

			List<File> files = Collections.singletonList(propertyFile);
			event.addAppFiles(APP_NAMESPACE, files);
		} catch (Exception e) {
			LogUtils.log(getClass(), e);
		}
	}
	
	@Override
	public void handleEvent(SessionLoadedEvent event) {
		// Workaround for Cytoscape bug #2701
		// https://code.cytoscape.org/redmine/issues/2701
		CySession session = event.getLoadedSession();
		Map<String, List<File>> fileListMap = session.getAppFileListMap();
		List<File> list = fileListMap.get(APP_NAMESPACE);
		if (list == null) {
			return;
		}
		
		for (File file : list) {
			if (file.getName().equals(APP_PROPERTIES)) {
				Properties properties2 = properties.getProperties();
				properties2.clear();
				try {
					properties2.load(new BufferedInputStream(new FileInputStream(file)));
				} catch (IOException e) {
					LogUtils.log(getClass(), e);
				}
			}
		}
	}
}
