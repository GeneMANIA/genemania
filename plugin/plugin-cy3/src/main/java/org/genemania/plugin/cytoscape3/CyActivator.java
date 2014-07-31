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

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.SimpleCyProperty;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.visualize.ApplyPreferredLayoutTaskFactory;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.TaskManager;
import org.genemania.plugin.FileUtils;
import org.genemania.plugin.NetworkUtils;
import org.genemania.plugin.cytoscape3.actions.AboutAction;
import org.genemania.plugin.cytoscape3.actions.CheckForUpdatesAction;
import org.genemania.plugin.cytoscape3.actions.DownloadDataSetAction;
import org.genemania.plugin.cytoscape3.actions.RetrieveRelatedGenesAction;
import org.genemania.plugin.cytoscape3.actions.SwitchDataSetAction;
import org.genemania.plugin.data.DataSetManager;
import org.genemania.plugin.data.IDataSetFactory;
import org.genemania.plugin.view.util.UiUtils;
import org.osgi.framework.BundleContext;

public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {
		CySwingApplication cySwingApplicationRef = getService(bc,
				CySwingApplication.class);
		CyApplicationManager cyApplicationManagerRef = getService(bc,
				CyApplicationManager.class);
		CyNetworkManager cyNetworkManagerRef = getService(bc,
				CyNetworkManager.class);
		CyNetworkFactory cyNetworkFactoryRef = getService(bc,
				CyNetworkFactory.class);
		CyNetworkViewManager cyNetworkViewManagerRef = getService(bc,
				CyNetworkViewManager.class);
		CyNetworkViewFactory cyNetworkViewFactoryRef = getService(bc,
				CyNetworkViewFactory.class);
		VisualMappingManager visualMappingManagerRef = getService(bc,
				VisualMappingManager.class);
		VisualStyleFactory visualStyleFactoryRef = getService(bc,
				VisualStyleFactory.class);
		VisualMappingFunctionFactory discreteMappingFunctionFactoryRef = getService(
				bc, VisualMappingFunctionFactory.class,
				"(mapping.type=discrete)");
		VisualMappingFunctionFactory passthroughMappingFunctionFactoryRef = getService(
				bc, VisualMappingFunctionFactory.class,
				"(mapping.type=passthrough)");
		VisualMappingFunctionFactory continuousMappingFunctionFactoryRef = getService(
				bc, VisualMappingFunctionFactory.class,
				"(mapping.type=continuous)");
		TaskManager<?, ?> taskManagerRef = getService(bc, TaskManager.class);
		ApplyPreferredLayoutTaskFactory applyPreferredLayoutTaskFactory = getService(bc, ApplyPreferredLayoutTaskFactory.class);
		CyServiceRegistrar cyServiceRegistrarRef = getService(bc,
				CyServiceRegistrar.class);
		CyEventHelper cyEventHelperRef = getService(bc, CyEventHelper.class);
		RenderingEngineManager renderingEngineManager = getService(bc, RenderingEngineManager.class);

		StreamUtil streamUtil = getService(bc, StreamUtil.class);

		UiUtils uiUtils = new UiUtils();
		FileUtils fileUtils = new CyFileUtils(streamUtil);
		NetworkUtils networkUtils = new NetworkUtils();
		CytoscapeUtilsImpl cytoscapeUtils = new CytoscapeUtilsImpl(
				networkUtils, cySwingApplicationRef, cyApplicationManagerRef,
				cyNetworkManagerRef, cyNetworkViewManagerRef,
				cyNetworkFactoryRef, cyNetworkViewFactoryRef,
				visualStyleFactoryRef, visualMappingManagerRef,
				discreteMappingFunctionFactoryRef,
				passthroughMappingFunctionFactoryRef,
				continuousMappingFunctionFactoryRef,
				taskManagerRef, cyEventHelperRef, applyPreferredLayoutTaskFactory, renderingEngineManager);
		DataSetManager dataSetManager = new DataSetManager();
		OsgiTaskDispatcher taskDispatcher = new OsgiTaskDispatcher(uiUtils);
		DefaultDataSetFactory<CyNetwork, CyNode, CyEdge> luceneDataSetFactory = new DefaultDataSetFactory<CyNetwork, CyNode, CyEdge>(
				dataSetManager, uiUtils, fileUtils, cytoscapeUtils,
				taskDispatcher);

		// Don't register this until fix for bug #2701 is released and workaround is removed.
		// https://code.cytoscape.org/redmine/issues/2701
		SimpleCyProperty<Properties> properties = new SimpleCyProperty<Properties>("org.genemania", new Properties(), Properties.class, CyProperty.SavePolicy.SESSION_FILE);
		
		NetworkSelectionManagerImpl selectionManager = new NetworkSelectionManagerImpl(cytoscapeUtils, taskDispatcher, properties);
		GeneManiaImpl geneMania = new GeneManiaImpl(dataSetManager,
				cytoscapeUtils, uiUtils, fileUtils, networkUtils,
				taskDispatcher, cySwingApplicationRef, cyServiceRegistrarRef,
				selectionManager, properties);
		selectionManager.setGeneMania(geneMania);
		registerAllServices(bc, selectionManager, new Properties());
		registerAllServices(bc, geneMania, new Properties());
		geneMania.startUp();

		Map<String, String> serviceProperties;

		serviceProperties = new HashMap<String, String>();
		serviceProperties.put("inMenuBar", "true");
		serviceProperties.put("preferredMenu", "Apps.GeneMANIA");
		RetrieveRelatedGenesAction retrieveRelatedGenesAction = new RetrieveRelatedGenesAction(
				serviceProperties, cyApplicationManagerRef, geneMania,
				cytoscapeUtils, networkUtils, uiUtils, fileUtils,
				taskDispatcher, cyNetworkViewManagerRef);

		serviceProperties = new HashMap<String, String>();
		serviceProperties.put("inMenuBar", "true");
		serviceProperties.put("preferredMenu", "Apps.GeneMANIA");
		DownloadDataSetAction downloadDataSetAction = new DownloadDataSetAction(
				serviceProperties, cyApplicationManagerRef, geneMania, cyNetworkViewManagerRef);

		serviceProperties = new HashMap<String, String>();
		serviceProperties.put("inMenuBar", "true");
		serviceProperties.put("preferredMenu", "Apps.GeneMANIA");
		SwitchDataSetAction switchDataSetAction = new SwitchDataSetAction(
				serviceProperties, cyApplicationManagerRef, geneMania, cyNetworkViewManagerRef);

		serviceProperties = new HashMap<String, String>();
		serviceProperties.put("inMenuBar", "true");
		serviceProperties.put("preferredMenu", "Apps.GeneMANIA");
		AboutAction aboutAction = new AboutAction(serviceProperties,
				cyApplicationManagerRef, cySwingApplicationRef, uiUtils, cyNetworkViewManagerRef);

		serviceProperties = new HashMap<String, String>();
		serviceProperties.put("inMenuBar", "true");
		serviceProperties.put("preferredMenu", "Apps.GeneMANIA");
		CheckForUpdatesAction checkForUpdatesAction = new CheckForUpdatesAction(
				serviceProperties, cyApplicationManagerRef, geneMania, cyNetworkViewManagerRef);

		registerService(bc, aboutAction, CyAction.class, new Properties());
		registerService(bc, retrieveRelatedGenesAction, CyAction.class,
				new Properties());
		registerService(bc, downloadDataSetAction, CyAction.class,
				new Properties());
		registerService(bc, switchDataSetAction, CyAction.class,
				new Properties());
		registerService(bc, checkForUpdatesAction, CyAction.class,
				new Properties());
		registerService(bc, cytoscapeUtils, RowsSetListener.class,
				new Properties());
		registerService(bc, luceneDataSetFactory, IDataSetFactory.class,
				new Properties());

		registerServiceListener(bc, dataSetManager, "addDataSetFactory",
				"removeDataSetFactory", IDataSetFactory.class);
	}
}
