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
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import org.cytoscape.application.swing.search.NetworkSearchTaskFactory;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskObserver;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.util.UserAction;
import org.genemania.data.normalizer.GeneCompletionProvider2;
import org.genemania.domain.InteractionNetworkGroup;
import org.genemania.domain.Organism;
import org.genemania.plugin.GeneMania;
import org.genemania.plugin.controllers.RetrieveRelatedGenesController;
import org.genemania.plugin.cytoscape.CytoscapeUtils;
import org.genemania.plugin.cytoscape3.actions.RetrieveRelatedGenesAction;
import org.genemania.plugin.cytoscape3.model.OrganismManager;
import org.genemania.plugin.cytoscape3.view.QueryBar;
import org.genemania.plugin.data.DataSetManager;
import org.genemania.plugin.model.Group;
import org.genemania.plugin.model.ViewState;
import org.genemania.plugin.model.impl.InteractionNetworkGroupImpl;
import org.genemania.plugin.parsers.Query;
import org.genemania.plugin.selection.NetworkSelectionManager;
import org.genemania.type.CombiningMethod;
import org.genemania.type.ScoringMethod;

public class SimpleSearchTaskFactory implements NetworkSearchTaskFactory, ActionListener {

	@Tunable(description = "Max Resultant Genes:", groups = { "_Default" }, gravity = 1.0)
	public int geneLimit = 20;
	
	public boolean offline;
	@Tunable(description = "Offline Search:", groups = { "_Default" }, gravity = 1.1)
	public boolean getOffline() {
		return offline;
	}
	public void setOffline(boolean offline) {
		if (this.offline != offline) {
			this.offline = offline;
			organismManager.setOffline(offline);
		}
	}
	
	@Tunable(description="Advanced Search...", gravity = 2.0)
	public UserAction advancedSearchAction = new UserAction(this);
	
	private static final String ID = "ca.utoronto.GeneMANIA";
	private static final String NAME = "GeneMANIA";
	private static final String DESCRIPTION = "Search related genes on GeneMANIA";
	private static final String WEBSITE_URL = "http://genemania.org/";
	
	private final Icon icon;
	private URL website;
	
	private QueryBar queryBar;
	
	private final GeneMania<CyNetwork, CyNode, CyEdge> plugin;
	private final RetrieveRelatedGenesController<CyNetwork, CyNode, CyEdge> controller;
	private final RetrieveRelatedGenesAction retrieveRelatedGenesAction;
	private final OrganismManager organismManager;
	private final CytoscapeUtils<CyNetwork, CyNode, CyEdge> cytoscapeUtils;
	private final CyServiceRegistrar serviceRegistrar;

	public SimpleSearchTaskFactory(
			GeneMania<CyNetwork, CyNode, CyEdge> plugin,
			RetrieveRelatedGenesController<CyNetwork, CyNode, CyEdge> controller,
			RetrieveRelatedGenesAction retrieveRelatedGenesAction, 
			OrganismManager organismManager, 
			CytoscapeUtils<CyNetwork, CyNode, CyEdge> cytoscapeUtils, 
			CyServiceRegistrar serviceRegistrar
	) {
		this.plugin = plugin;
		this.controller = controller;
		this.retrieveRelatedGenesAction = retrieveRelatedGenesAction;
		this.organismManager = organismManager;
		this.cytoscapeUtils = cytoscapeUtils;
		this.serviceRegistrar = serviceRegistrar;
		icon = new ImageIcon(getClass().getClassLoader().getResource("/img/logo_squared.png"));
		
		try {
			website = new URL(WEBSITE_URL);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		setOffline(organismManager.isOffline());
		organismManager.addPropertyChangeListener("offline", evt -> setOffline(organismManager.isOffline()));
	}
	
	@Override
	public JComponent getQueryComponent() {
		if (queryBar == null) {
			queryBar = new QueryBar(plugin, organismManager, serviceRegistrar);
		}
		
		return queryBar;
	}
	
	@Override
	public JComponent getOptionsComponent() {
		return null;
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
				if (!hasValidGenes(query.getOrganism(), query.getGenes(), plugin))
					throw new RuntimeException("Please specify a set of valid gene names and try again.");
					
				tm.setStatusMessage("Searching...");
				
				List<Group<?, ?>> groups = getGroups(query.getOrganism());
				
				new Thread(() -> {
					CyNetwork network = controller.runMania(SwingUtilities.getWindowAncestor(queryBar), query, groups);
	
					cytoscapeUtils.handleNetworkPostProcessing(network);
					cytoscapeUtils.performLayout(network);
					cytoscapeUtils.maximize(network);
					
					NetworkSelectionManager<CyNetwork, CyNode, CyEdge> selManager = plugin.getNetworkSelectionManager();
					ViewState options = selManager.getNetworkConfiguration(network);
					plugin.applyOptions(options);
					plugin.showResults();
				}).start();
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
		return null;
	}

	@Override
	public boolean isReady() {
		return ((QueryBar) getQueryComponent()).isReady();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		retrieveRelatedGenesAction.getDelegate().invoke();
	}
	
	protected static boolean hasValidGenes(Organism organism, List<String> geneList,
			GeneMania<CyNetwork, CyNode, CyEdge> plugin) {
		DataSetManager dataSetManager = plugin.getDataSetManager();
		GeneCompletionProvider2 provider = dataSetManager.getDataSet().getCompletionProvider(organism);
		
		for (String gene : geneList) {
			if (provider.isValid(gene))
				return true;
		}
		
		return false;
	}
	
	protected static List<Group<?, ?>> getGroups(Organism organism) {
		List<Group<?, ?>> groups = new ArrayList<>();
		
		// TODO get only default or selected groups
		for (InteractionNetworkGroup group : organism.getInteractionNetworkGroups())
			groups.add(new InteractionNetworkGroupImpl(group));
		
		return groups;
	}
	
	private Query getQuery() {
		final Query query = new Query();
		query.setOrganism(queryBar.getSelectedOrganism());
		query.setGenes(new ArrayList<>(queryBar.getQueryGenes()));
		query.setGeneLimit(geneLimit);
		query.setAttributeLimit(0);
		query.setCombiningMethod(CombiningMethod.AUTOMATIC_SELECT);
		query.setScoringMethod(ScoringMethod.DISCRIMINANT);
		
		return query;
	}
}
