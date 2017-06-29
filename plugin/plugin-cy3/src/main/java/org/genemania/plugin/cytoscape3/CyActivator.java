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

import static org.cytoscape.work.ServiceProperties.COMMAND;
import static org.cytoscape.work.ServiceProperties.COMMAND_DESCRIPTION;
import static org.cytoscape.work.ServiceProperties.COMMAND_NAMESPACE;
import static org.cytoscape.work.ServiceProperties.INSERT_SEPARATOR_AFTER;
import static org.cytoscape.work.ServiceProperties.INSERT_SEPARATOR_BEFORE;
import static org.cytoscape.work.ServiceProperties.MENU_GRAVITY;
import static org.cytoscape.work.ServiceProperties.TITLE;

import java.awt.Component;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.search.NetworkSearchTaskFactory;
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
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.undo.UndoSupport;
import org.genemania.plugin.FileUtils;
import org.genemania.plugin.NetworkUtils;
import org.genemania.plugin.controllers.RetrieveRelatedGenesController;
import org.genemania.plugin.cytoscape3.actions.AboutAction;
import org.genemania.plugin.cytoscape3.actions.CheckForUpdatesAction;
import org.genemania.plugin.cytoscape3.actions.DownloadDataSetAction;
import org.genemania.plugin.cytoscape3.actions.RetrieveRelatedGenesAction;
import org.genemania.plugin.cytoscape3.actions.SwitchDataSetAction;
import org.genemania.plugin.cytoscape3.layout.GeneManiaFDLayout;
import org.genemania.plugin.cytoscape3.task.SearchCommandTaskFactory;
import org.genemania.plugin.cytoscape3.task.SimpleSearchTaskFactory;
import org.genemania.plugin.data.DataSetManager;
import org.genemania.plugin.data.IDataSetFactory;
import org.genemania.plugin.view.util.UiUtils;
import org.osgi.framework.BundleContext;

public class CyActivator extends AbstractCyActivator {
	
	private CySwingApplication swingApplication;
	private CyServiceRegistrar serviceRegistrar;
	private RetrieveRelatedGenesAction retrieveRelatedGenesAction;
	
	@Override
	public void start(BundleContext bc) {
		serviceRegistrar = getService(bc, CyServiceRegistrar.class);
		swingApplication = getService(bc, CySwingApplication.class);
		CyApplicationManager applicationManager = getService(bc, CyApplicationManager.class);
		CyNetworkManager networkManager = getService(bc, CyNetworkManager.class);
		CyNetworkFactory networkFactory = getService(bc, CyNetworkFactory.class);
		CyNetworkViewManager networkViewManager = getService(bc, CyNetworkViewManager.class);
		CyNetworkViewFactory networkViewFactory = getService(bc, CyNetworkViewFactory.class);
		VisualMappingManager visualMappingManager = getService(bc, VisualMappingManager.class);
		VisualStyleFactory visualStyleFactory = getService(bc, VisualStyleFactory.class);
		VisualMappingFunctionFactory discreteMappingFactory = getService(bc, VisualMappingFunctionFactory.class, "(mapping.type=discrete)");
		VisualMappingFunctionFactory passthroughMappingFactory = getService(bc, VisualMappingFunctionFactory.class, "(mapping.type=passthrough)");
		VisualMappingFunctionFactory continuousMappingFactory = getService(bc, VisualMappingFunctionFactory.class, "(mapping.type=continuous)");
		TaskManager<?, ?> taskManager = getService(bc, TaskManager.class);
		ApplyPreferredLayoutTaskFactory applyPreferredLayoutTaskFactory = getService(bc, ApplyPreferredLayoutTaskFactory.class);
		CyEventHelper eventHelper = getService(bc, CyEventHelper.class);
		UndoSupport undoSupport = getService(bc, UndoSupport.class);
		RenderingEngineManager renderingEngineManager = getService(bc, RenderingEngineManager.class);
		StreamUtil streamUtil = getService(bc, StreamUtil.class);

		closeAppPanels();
		
		UiUtils uiUtils = new UiUtils();
		FileUtils fileUtils = new CyFileUtils(streamUtil);
		NetworkUtils networkUtils = new NetworkUtils();
		CytoscapeUtilsImpl cytoscapeUtils = new CytoscapeUtilsImpl(
				networkUtils, swingApplication, applicationManager,
				networkManager, networkViewManager,
				networkFactory, networkViewFactory,
				visualStyleFactory, visualMappingManager,
				discreteMappingFactory,
				passthroughMappingFactory,
				continuousMappingFactory,
				taskManager, eventHelper,
				applyPreferredLayoutTaskFactory, renderingEngineManager,
				serviceRegistrar);
		DataSetManager dataSetManager = new DataSetManager();
		OsgiTaskDispatcher taskDispatcher = new OsgiTaskDispatcher(uiUtils);
		DefaultDataSetFactory<CyNetwork, CyNode, CyEdge> luceneDataSetFactory = new DefaultDataSetFactory<>(
				dataSetManager, uiUtils, fileUtils, cytoscapeUtils, taskDispatcher);
		
		SimpleCyProperty<Properties> properties = new SimpleCyProperty<>("org.genemania", new Properties(),
				Properties.class, CyProperty.SavePolicy.SESSION_FILE);
		registerService(bc, properties, CyProperty.class);
		
		NetworkSelectionManagerImpl selectionManager = new NetworkSelectionManagerImpl(cytoscapeUtils, taskDispatcher,
				properties);
		GeneManiaImpl geneMania = new GeneManiaImpl(dataSetManager, cytoscapeUtils, uiUtils, fileUtils, networkUtils,
				taskDispatcher, swingApplication, serviceRegistrar, selectionManager, properties);
		selectionManager.setGeneMania(geneMania);
		registerAllServices(bc, selectionManager);
		geneMania.startUp();
		
		GeneManiaFDLayout fdLayout = new GeneManiaFDLayout(undoSupport);
		registerLayoutAlgorithms(bc, fdLayout);
		
		RetrieveRelatedGenesController<CyNetwork, CyNode, CyEdge> controller =
				new RetrieveRelatedGenesController<>(geneMania, cytoscapeUtils, networkUtils, taskDispatcher);

		Map<String, String> props;
		{
			props = new HashMap<>();
			props.put("inMenuBar", "true");
			props.put("preferredMenu", "Apps.GeneMANIA");
			props.put(MENU_GRAVITY, "1.0");
			props.put(INSERT_SEPARATOR_AFTER, "true");
			retrieveRelatedGenesAction = new RetrieveRelatedGenesAction(props, applicationManager, geneMania,
					cytoscapeUtils, networkUtils, uiUtils, fileUtils, taskDispatcher, networkViewManager);
			registerService(bc, retrieveRelatedGenesAction, CyAction.class);
		}
		{
			props = new HashMap<>();
			props.put("inMenuBar", "true");
			props.put("preferredMenu", "Apps.GeneMANIA");
			props.put(MENU_GRAVITY, "2.0");
			DownloadDataSetAction action = new DownloadDataSetAction(props, applicationManager, geneMania,
					networkViewManager);
			registerService(bc, action, CyAction.class);
		}
		{
			props = new HashMap<>();
			props.put("inMenuBar", "true");
			props.put("preferredMenu", "Apps.GeneMANIA");
			props.put(MENU_GRAVITY, "3.0");
			SwitchDataSetAction action = new SwitchDataSetAction(props, applicationManager, geneMania,
					networkViewManager);
			registerService(bc, action, CyAction.class);
		}
		{
			props = new HashMap<>();
			props.put("inMenuBar", "true");
			props.put("preferredMenu", "Apps.GeneMANIA");
			props.put(MENU_GRAVITY, "4.0");
			CheckForUpdatesAction action = new CheckForUpdatesAction(props, applicationManager, geneMania,
					networkViewManager);
			registerService(bc, action, CyAction.class);
		}
		{
			props = new HashMap<>();
			props.put("inMenuBar", "true");
			props.put("preferredMenu", "Apps.GeneMANIA");
			props.put(MENU_GRAVITY, "5.0");
			props.put(INSERT_SEPARATOR_BEFORE, "true");
			AboutAction action = new AboutAction(props, applicationManager, swingApplication, uiUtils,
					networkViewManager);
			registerService(bc, action, CyAction.class);
		}
		
		registerService(bc, cytoscapeUtils, RowsSetListener.class);
		registerService(bc, luceneDataSetFactory, IDataSetFactory.class);

		registerServiceListener(bc, dataSetManager::addDataSetFactory, dataSetManager::removeDataSetFactory,
				IDataSetFactory.class);
		
		{
			SimpleSearchTaskFactory factory = new SimpleSearchTaskFactory(geneMania, controller, cytoscapeUtils,
					retrieveRelatedGenesAction, serviceRegistrar);
			registerService(bc, factory, NetworkSearchTaskFactory.class);
		}
		{
			SearchCommandTaskFactory factory = new SearchCommandTaskFactory(geneMania, controller, cytoscapeUtils,
					retrieveRelatedGenesAction, serviceRegistrar);
			Properties p = new Properties();
			p.put(COMMAND, "search");
			p.put(COMMAND_NAMESPACE, "genemania");
			p.put(COMMAND_DESCRIPTION, "Search GeneMANIA");
			registerService(bc, factory, TaskFactory.class, p);
		}
	}
	
	@Override
	public void shutDown() {
		closeAppPanels();
		super.shutDown();
	}
	
	private void registerLayoutAlgorithms(BundleContext bc, CyLayoutAlgorithm... algorithms) {
		for (int i = 0; i < algorithms.length; i++) {
			Properties props = new Properties();
			props.setProperty("preferredTaskManager", "menu");
			props.setProperty(TITLE, algorithms[i].toString());
			props.setProperty(MENU_GRAVITY, "30." + (i+1));
			
			if (i == 0)
				props.setProperty(INSERT_SEPARATOR_BEFORE, "true");
			if (i == algorithms.length-1)
				props.setProperty(INSERT_SEPARATOR_AFTER, "true");
			
			registerService(bc, algorithms[i], CyLayoutAlgorithm.class, props);
		}
	}
	
	private void closeAppPanels() {
		// First, unregister result panels...
		final CytoPanel resPanel = swingApplication.getCytoPanel(CytoPanelName.EAST);
		
		if (resPanel != null) {
			int count = resPanel.getCytoPanelComponentCount();
			
			try {
				for (int i = 0; i < count; i++) {
					final Component comp = resPanel.getComponentAt(i);
					
					// Compare the class names to also get panels that may have been left by old versions of GeneMANIA
					if (comp.getClass().getName().equals(ManiaResultsCytoPanelComponent.class.getName()))
						serviceRegistrar.unregisterAllServices(comp);
				}
			} catch (Exception e) {
			}
		}
		
		// Then dispose dialogs
		if (retrieveRelatedGenesAction != null)
			retrieveRelatedGenesAction.getDelegate().getDialog().dispose();
	}
}
