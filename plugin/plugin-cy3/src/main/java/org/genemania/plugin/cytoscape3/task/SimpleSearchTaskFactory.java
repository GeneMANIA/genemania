/**
 * This file is part of GeneMANIA.
 * Copyright (C) 2017 University of Toronto.
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
package org.genemania.plugin.cytoscape3.task;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.cytoscape.application.swing.search.NetworkSearchTaskFactory;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.TextIcon;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskObserver;
import org.genemania.data.normalizer.GeneCompletionProvider2;
import org.genemania.domain.InteractionNetwork;
import org.genemania.domain.InteractionNetworkGroup;
import org.genemania.domain.Organism;
import org.genemania.plugin.GeneMania;
import org.genemania.plugin.NetworkUtils;
import org.genemania.plugin.Strings;
import org.genemania.plugin.controllers.RetrieveRelatedGenesController;
import org.genemania.plugin.cytoscape.CytoscapeUtils;
import org.genemania.plugin.cytoscape3.actions.RetrieveRelatedGenesAction;
import org.genemania.plugin.cytoscape3.model.OrganismManager;
import org.genemania.plugin.cytoscape3.view.QueryBar;
import org.genemania.plugin.data.DataSetManager;
import org.genemania.plugin.model.Group;
import org.genemania.plugin.model.Network;
import org.genemania.plugin.model.ViewState;
import org.genemania.plugin.model.impl.InteractionNetworkGroupImpl;
import org.genemania.plugin.parsers.Query;
import org.genemania.plugin.selection.SessionManager;
import org.genemania.plugin.view.components.WrappedOptionPane;
import org.genemania.plugin.view.util.IconUtil;
import org.genemania.plugin.view.util.UiUtils;
import org.genemania.type.ScoringMethod;

public class SimpleSearchTaskFactory implements NetworkSearchTaskFactory, ActionListener {

	private static final String ID = "ca.utoronto.GeneMANIA";
	private static final String NAME = "GeneMANIA";
	private static final String DESCRIPTION = "Search related genes on GeneMANIA";
	private static final String WEBSITE_URL = "http://genemania.org/";
	
	private final Icon icon;
	private URL website;
	
	private QueryBar queryBar;
	
	private final GeneMania plugin;
	private final RetrieveRelatedGenesController controller;
	private final RetrieveRelatedGenesAction retrieveRelatedGenesAction;
	private final OrganismManager organismManager;
	private final NetworkUtils networkUtils;
	private final UiUtils uiUtils;
	private final CytoscapeUtils cytoscapeUtils;
	private final CyServiceRegistrar serviceRegistrar;

	public SimpleSearchTaskFactory(
			GeneMania plugin,
			RetrieveRelatedGenesController controller,
			RetrieveRelatedGenesAction retrieveRelatedGenesAction, 
			OrganismManager organismManager, 
			NetworkUtils networkUtils, 
			UiUtils uiUtils, 
			CytoscapeUtils cytoscapeUtils, 
			CyServiceRegistrar serviceRegistrar
	) {
		this.plugin = plugin;
		this.controller = controller;
		this.retrieveRelatedGenesAction = retrieveRelatedGenesAction;
		this.networkUtils = networkUtils;
		this.uiUtils = uiUtils;
		this.organismManager = organismManager;
		this.cytoscapeUtils = cytoscapeUtils;
		this.serviceRegistrar = serviceRegistrar;
		icon = new TextIcon(IconUtil.GENEMANIA_LOGO, IconUtil.getIconFont(32.0f), IconUtil.GENEMANIA_LOGO_COLOR, 36, 36);
		
		try {
			website = new URL(WEBSITE_URL);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public JComponent getQueryComponent() {
		if (queryBar == null) {
			queryBar = new QueryBar(organismManager, networkUtils, uiUtils, serviceRegistrar);
		}
		
		return queryBar;
	}
	
	@Override
	public JComponent getOptionsComponent() {
		return ((QueryBar) getQueryComponent()).getOptionsPanel();
	}
	
	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new AbstractTask() {
			@Override
			public void run(TaskMonitor tm) throws Exception {
				tm.setTitle("GeneMANIA");
				tm.setStatusMessage("Validating search...");
				tm.setProgress(-1);
				
				Query query = getQuery();
				
				if (query.getOrganism() == null)
					throw new RuntimeException("Please select an organism.");
				if (query.getGenes().isEmpty())
					throw new RuntimeException("Please enter one or more genes.");
					
				tm.setStatusMessage("Searching...");

				ObservableTask nextTask = controller.runMania(query, false);
				insertTasksAfterCurrentTask(nextTask);
			}
		});
	}
	
	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String getDescription() {
		return DESCRIPTION;
	}

	@Override
	public Icon getIcon() {
		return icon;
	}

	@Override
	public URL getWebsite() {
		return website;
	}

	@Override
	public TaskObserver getTaskObserver() {
		return new TaskObserver() {
			
			private CyNetwork network;
			
			@Override
			public void taskFinished(ObservableTask task) {
				if (task.getResultClasses().contains(CyNetwork.class))
					network = task.getResults(CyNetwork.class);
			}
			
			@Override
			public void allFinished(FinishStatus finishStatus) {
				if (finishStatus != FinishStatus.getSucceeded())
					return;
				
				if (network == null) {
					SwingUtilities.invokeLater(() -> WrappedOptionPane.showConfirmDialog(
							cytoscapeUtils.getFrame(),
							Strings.retrieveRelatedGenesNoResults,
							Strings.default_title,
							JOptionPane.DEFAULT_OPTION,
							JOptionPane.INFORMATION_MESSAGE,
							60
					));
				} else {
					// Show results
					cytoscapeUtils.handleNetworkPostProcessing(network);
					cytoscapeUtils.performLayout(network);
					cytoscapeUtils.maximize(network);
					
					SessionManager sessionManager = plugin.getSessionManager();
					ViewState options = sessionManager.getNetworkConfiguration(network);
					plugin.applyOptions(options);
					plugin.showResults();
				}
			}
		};
	}

	@Override
	public boolean isReady() {
		return ((QueryBar) getQueryComponent()).isReady();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		retrieveRelatedGenesAction.getDelegate().invoke();
	}
	
	protected static boolean hasValidGenes(Organism organism, List<String> geneList, GeneMania plugin) {
		DataSetManager dataSetManager = plugin.getDataSetManager();
		GeneCompletionProvider2 provider = dataSetManager.getDataSet().getCompletionProvider(organism);
		
		for (String gene : geneList) {
			if (provider.isValid(gene))
				return true;
		}
		
		return false;
	}
	
	protected static List<Group<?, ?>> getDefaultGroups(Organism organism) {
		List<Group<?, ?>> groups = new ArrayList<>();
		
		// Only default or selected groups
		for (InteractionNetworkGroup netGroup : organism.getInteractionNetworkGroups()) {
			Group<InteractionNetworkGroup, InteractionNetwork> gr = new InteractionNetworkGroupImpl(netGroup);
			Collection<? extends Network<InteractionNetwork>> networks = gr.getNetworks();
			
			if (networks != null) {
				Collection<Network<?>> defNetworks = new ArrayList<>();
				
				for (Network<?> n : networks) {
					if (n.isDefaultSelected())
						defNetworks.add(n);
				}
				
				Group<InteractionNetworkGroup, InteractionNetwork> filter = gr.filter(defNetworks);
				
				if (filter != null && filter.getNetworks() != null && !filter.getNetworks().isEmpty())
					groups.add(filter);
			}
		}
		
		return groups;
	}
	
	private Query getQuery() {
		final Query query = new Query();
		query.setOrganism(queryBar.getSelectedOrganism());
		query.setGenes(new ArrayList<>(queryBar.getQueryGenes()));
		query.setGeneLimit(queryBar.getGeneLimit());
		query.setAttributeLimit(queryBar.getAttributeLimit());
		query.setCombiningMethod(queryBar.getCombiningMethod());
		query.setScoringMethod(ScoringMethod.DISCRIMINANT);
		query.setGroups(queryBar.getSelectedGroups());
		
		return query;
	}
}
