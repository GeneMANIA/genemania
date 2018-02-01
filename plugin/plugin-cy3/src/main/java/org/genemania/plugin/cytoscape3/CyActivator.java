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
import static org.cytoscape.work.ServiceProperties.COMMAND_EXAMPLE_JSON;
import static org.cytoscape.work.ServiceProperties.COMMAND_LONG_DESCRIPTION;
import static org.cytoscape.work.ServiceProperties.COMMAND_NAMESPACE;
import static org.cytoscape.work.ServiceProperties.COMMAND_SUPPORTS_JSON;
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
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.SimpleCyProperty;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.visualize.ApplyPreferredLayoutTaskFactory;
import org.cytoscape.util.swing.IconManager;
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
import org.genemania.plugin.cytoscape3.controllers.RetrieveRelatedGenesControllerImpl;
import org.genemania.plugin.cytoscape3.layout.GeneManiaFDLayout;
import org.genemania.plugin.cytoscape3.model.OrganismManager;
import org.genemania.plugin.cytoscape3.task.ListOrganismsCommandTaskFactory;
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
		IconManager iconManager = getService(bc, IconManager.class);

		closeAppPanels();
		
		UiUtils uiUtils = new UiUtils(iconManager);
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
		DefaultDataSetFactory luceneDataSetFactory = new DefaultDataSetFactory(dataSetManager, uiUtils, fileUtils,
				cytoscapeUtils, taskDispatcher);

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
		
		RetrieveRelatedGenesController controller =
				new RetrieveRelatedGenesControllerImpl(geneMania, cytoscapeUtils, networkUtils, taskDispatcher);

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
		
		OrganismManager organismManager = new OrganismManager(geneMania, serviceRegistrar);
		
		{
			SimpleSearchTaskFactory factory = new SimpleSearchTaskFactory(geneMania, controller,
					retrieveRelatedGenesAction, organismManager, networkUtils, uiUtils, cytoscapeUtils,
					serviceRegistrar);
			registerService(bc, factory, NetworkSearchTaskFactory.class);
		}
		{
			SearchCommandTaskFactory factory = new SearchCommandTaskFactory(geneMania, controller,
					organismManager, cytoscapeUtils);
			Properties p = new Properties();
			p.setProperty(COMMAND, "search");
			p.setProperty(COMMAND_NAMESPACE, "genemania");
			p.setProperty(COMMAND_DESCRIPTION, "Searches GeneMANIA");
			p.setProperty(COMMAND_LONG_DESCRIPTION, "Finds related genes from a locally installed data set (i.e. offline search) or by querying the GeneMANIA server (i.e. online search).");
			p.setProperty(COMMAND_SUPPORTS_JSON, "true");
			p.setProperty(COMMAND_EXAMPLE_JSON,
					"{\n" + 
					"    \"combiningMethod\": \"AUTOMATIC_SELECT\",\n" + 
					"    \"genes\": [\n" + 
					"      {\n" + 
					"        \"symbol\": \"ybcN\",\n" + 
					"        \"description\": \"DLP12 prophage; putative protein\",\n" + 
					"        \"score\": 0.7550105796324988\n" + 
					"      },\n" + 
					"      {\n" + 
					"        \"symbol\": \"ybcO\",\n" + 
					"        \"description\": \"DLP12 prophage; putative protein\",\n" + 
					"        \"score\": 0.007381754018362385\n" + 
					"      },\n" + 
					"      {\n" + 
					"        \"symbol\": \"ninE\",\n" + 
					"        \"description\": \"DLP12 prophage; conserved protein\",\n" + 
					"        \"score\": 0.009564379669797896\n" + 
					"      }\n" + 
					"    ],\n" + 
					"    \"network\": 2526\n" + 
					"}"
			);
			registerService(bc, factory, TaskFactory.class, p);
		}
		{
			ListOrganismsCommandTaskFactory factory = new ListOrganismsCommandTaskFactory(organismManager);
			Properties p = new Properties();
			p.setProperty(COMMAND, "organisms");
			p.setProperty(COMMAND_NAMESPACE, "genemania");
			p.setProperty(COMMAND_DESCRIPTION, "Lists supported GeneMANIA organisms and interaction networks");
			p.setProperty(COMMAND_LONG_DESCRIPTION, "Lists all available organisms--and their interaction networks--that have been installed or are supported for online searches.");
			p.setProperty(COMMAND_SUPPORTS_JSON, "true");
			p.setProperty(COMMAND_EXAMPLE_JSON,
					"{\n" + 
					"    \"organisms\": [\n" + 
					"      {\n" + 
					"        \"taxonomyId\": 3702,\n" + 
					"        \"scientificName\": \"Arabidopsis thaliana\",\n" + 
					"        \"abbreviatedName\": \"A. thaliana\",\n" + 
					"        \"commonName\": \"arabidopsis\",\n" + 
					"        \"interactionNetworkGroups\": [\n" + 
					"          {\n" + 
					"            \"code\": \"spd\",\n" + 
					"            \"name\": \"Shared protein domains\",\n" + 
					"            \"description\": \"\",\n" + 
					"            \"interactionNetworks\": [\n" + 
					"              {\n" + 
					"                \"id\": 55,\n" + 
					"                \"name\": \"Lee-Rhee-2010 Shared protein domains\",\n" + 
					"                \"description\": \"\",\n" + 
					"                \"metadata\": {\n" + 
					"                  \"id\": 55,\n" + 
					"                  \"source\": \"SUPPLEMENTARY_MATERIAL\",\n" + 
					"                  \"reference\": \"\",\n" + 
					"                  \"pubmedId\": \"20118918\",\n" + 
					"                  \"authors\": \"Lee,Rhee\",\n" + 
					"                  \"publicationName\": \"Nat Biotechnol\",\n" + 
					"                  \"yearPublished\": \"2010.0\",\n" + 
					"                  \"processingDescription\": \"Direct interaction\",\n" + 
					"                  \"networkType\": \"Shared protein domains\",\n" + 
					"                  \"alias\": \"\",\n" + 
					"                  \"interactionCount\": 50665,\n" + 
					"                  \"dynamicRange\": \"\",\n" + 
					"                  \"edgeWeightDistribution\": \"\",\n" + 
					"                  \"accessStats\": 0,\n" + 
					"                  \"comment\": \"\",\n" + 
					"                  \"other\": \"\",\n" + 
					"                  \"title\": \"Rational association of genes with traits using a genome-scale gene network for Arabidopsis thaliana.\",\n" + 
					"                  \"url\": \"http://www.ncbi.nlm.nih.gov/pubmed/20118918\",\n" + 
					"                  \"sourceUrl\": \"\"\n" + 
					"                },\n" + 
					"                \"defaultSelected\": true\n" + 
					"              },\n" + 
					"              {\n" + 
					"                \"id\": 74,\n" + 
					"                \"name\": \"INTERPRO\",\n" + 
					"                \"description\": \"\",\n" + 
					"                \"metadata\": {\n" + 
					"                  \"id\": 74,\n" + 
					"                  \"source\": \"INTERPRO\",\n" + 
					"                  \"reference\": \"\",\n" + 
					"                  \"pubmedId\": \"0\",\n" + 
					"                  \"authors\": \"\",\n" + 
					"                  \"publicationName\": \"\",\n" + 
					"                  \"yearPublished\": \"\",\n" + 
					"                  \"processingDescription\": \"sharedneighbour\",\n" + 
					"                  \"networkType\": \"Shared protein domains\",\n" + 
					"                  \"alias\": \"\",\n" + 
					"                  \"interactionCount\": 743516,\n" + 
					"                  \"dynamicRange\": \"\",\n" + 
					"                  \"edgeWeightDistribution\": \"\",\n" + 
					"                  \"accessStats\": 0,\n" + 
					"                  \"comment\": \"\",\n" + 
					"                  \"other\": \"\",\n" + 
					"                  \"title\": \"\",\n" + 
					"                  \"url\": \"\",\n" + 
					"                  \"sourceUrl\": \"http://www.ebi.ac.uk/interpro/\"\n" + 
					"                },\n" + 
					"                \"defaultSelected\": true\n" + 
					"              }\n" + 
					"            ]\n" + 
					"          },\n" + 
					"          {\n" + 
					"            \"code\": \"coexp\",\n" + 
					"            \"name\": \"Co-expression\",\n" + 
					"            \"description\": \"\",\n" + 
					"            \"interactionNetworks\": [\n" + 
					"              {\n" + 
					"                \"id\": 164,\n" + 
					"                \"name\": \"Adrian-Bergmann-2015\",\n" + 
					"                \"description\": \"\",\n" + 
					"                \"metadata\": {\n" + 
					"                  \"id\": 164,\n" + 
					"                  \"source\": \"GEO\",\n" + 
					"                  \"reference\": \"GSE58855\",\n" + 
					"                  \"pubmedId\": \"25850675\",\n" + 
					"                  \"authors\": \"Adrian,Bergmann\",\n" + 
					"                  \"publicationName\": \"Dev Cell\",\n" + 
					"                  \"yearPublished\": \"2015.0\",\n" + 
					"                  \"processingDescription\": \"Pearson correlation\",\n" + 
					"                  \"networkType\": \"Co-expression\",\n" + 
					"                  \"alias\": \"\",\n" + 
					"                  \"interactionCount\": 486602,\n" + 
					"                  \"dynamicRange\": \"\",\n" + 
					"                  \"edgeWeightDistribution\": \"\",\n" + 
					"                  \"accessStats\": 0,\n" + 
					"                  \"comment\": \"\",\n" + 
					"                  \"other\": \"\",\n" + 
					"                  \"title\": \"Transcriptome dynamics of the stomatal lineage: birth, amplification, and termination of a self-renewing population.\",\n" + 
					"                  \"url\": \"http://www.ncbi.nlm.nih.gov/pubmed/25850675\",\n" + 
					"                  \"sourceUrl\": \"http://www.ncbi.nlm.nih.gov/projects/geo/query/acc.cgi?acc=GSE58855\"\n" + 
					"                },\n" + 
					"                \"defaultSelected\": false\n" + 
					"              }\n" + 
					"            ]\n" + 
					"          }\n" + 
					"        ]\n" + 
					"      },\n" + 
					"      {\n" + 
					"        \"taxonomyId\": 6239,\n" + 
					"        \"scientificName\": \"Caenorhabditis elegans\",\n" + 
					"        \"abbreviatedName\": \"C. elegans\",\n" + 
					"        \"commonName\": \"worm\",\n" + 
					"        \"interactionNetworkGroups\": [\n" + 
					"          {\n" + 
					"            \"code\": \"predict\",\n" + 
					"            \"name\": \"Predicted\",\n" + 
					"            \"description\": \"\",\n" + 
					"            \"interactionNetworks\": [\n" + 
					"              {\n" + 
					"                \"id\": 429,\n" + 
					"                \"name\": \"I2D-BIND-Fly2Worm\",\n" + 
					"                \"description\": \"\",\n" + 
					"                \"metadata\": {\n" + 
					"                  \"id\": 429,\n" + 
					"                  \"source\": \"I2D\",\n" + 
					"                  \"reference\": \"\",\n" + 
					"                  \"pubmedId\": \"10871269\",\n" + 
					"                  \"authors\": \"Bader,Hogue\",\n" + 
					"                  \"publicationName\": \"Bioinformatics\",\n" + 
					"                  \"yearPublished\": \"2000.0\",\n" + 
					"                  \"processingDescription\": \"Direct interaction\",\n" + 
					"                  \"networkType\": \"Predicted\",\n" + 
					"                  \"alias\": \"\",\n" + 
					"                  \"interactionCount\": 380,\n" + 
					"                  \"dynamicRange\": \"\",\n" + 
					"                  \"edgeWeightDistribution\": \"\",\n" + 
					"                  \"accessStats\": 0,\n" + 
					"                  \"comment\": \"\",\n" + 
					"                  \"other\": \"\",\n" + 
					"                  \"title\": \"BIND--a data specification for storing and describing biomolecular interactions, molecular complexes and pathways.\",\n" + 
					"                  \"url\": \"http://www.ncbi.nlm.nih.gov/pubmed/10871269\",\n" + 
					"                  \"sourceUrl\": \"http://ophid.utoronto.ca/\"\n" + 
					"                },\n" + 
					"                \"defaultSelected\": true\n" + 
					"              },\n" + 
					"              {\n" + 
					"                \"id\": 408,\n" + 
					"                \"name\": \"I2D-BIND-Human2Worm\",\n" + 
					"                \"description\": \"\",\n" + 
					"                \"metadata\": {\n" + 
					"                  \"id\": 408,\n" + 
					"                  \"source\": \"I2D\",\n" + 
					"                  \"reference\": \"\",\n" + 
					"                  \"pubmedId\": \"10871269\",\n" + 
					"                  \"authors\": \"Bader,Hogue\",\n" + 
					"                  \"publicationName\": \"Bioinformatics\",\n" + 
					"                  \"yearPublished\": \"2000.0\",\n" + 
					"                  \"processingDescription\": \"Direct interaction\",\n" + 
					"                  \"networkType\": \"Predicted\",\n" + 
					"                  \"alias\": \"\",\n" + 
					"                  \"interactionCount\": 257,\n" + 
					"                  \"dynamicRange\": \"\",\n" + 
					"                  \"edgeWeightDistribution\": \"\",\n" + 
					"                  \"accessStats\": 0,\n" + 
					"                  \"comment\": \"\",\n" + 
					"                  \"other\": \"\",\n" + 
					"                  \"title\": \"BIND--a data specification for storing and describing biomolecular interactions, molecular complexes and pathways.\",\n" + 
					"                  \"url\": \"http://www.ncbi.nlm.nih.gov/pubmed/10871269\",\n" + 
					"                  \"sourceUrl\": \"http://ophid.utoronto.ca/\"\n" + 
					"                },\n" + 
					"                \"defaultSelected\": true\n" + 
					"              }\n" + 
					"            ]\n" + 
					"          }\n" + 
					"        ]\n" + 
					"      }\n" + 
					"    ]\n" + 
					"}"
			);
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
