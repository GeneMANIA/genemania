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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;
import org.genemania.domain.Organism;
import org.genemania.plugin.GeneMania;
import org.genemania.plugin.controllers.RetrieveRelatedGenesController;
import org.genemania.plugin.cytoscape.CytoscapeUtils;
import org.genemania.plugin.cytoscape3.model.OrganismManager;
import org.genemania.plugin.model.Group;
import org.genemania.plugin.model.ViewState;
import org.genemania.plugin.parsers.Query;
import org.genemania.plugin.selection.NetworkSelectionManager;
import org.genemania.type.CombiningMethod;
import org.genemania.type.ScoringMethod;

public class SearchCommandTask extends AbstractTask {

	@Tunable(
			description = "Organism",
			longDescription = "The name or NCBI taxonomy id of an organism whose genes should be considered.",
			exampleStringValue = "83333",
			required = true,
			context = "nogui"
	)
	public String organism;
	
	@Tunable(
			description = "Query genes",
			longDescription = "One or more input genes. The gene symbols must be separated by a \"vertical bar\" character (```|```).",
			exampleStringValue = "ybcN|vsr|mutS|mutH|ssb|mutL",
			required = true,
			context = "nogui"
	)
	public String genes;
	
	@Tunable(
			description = "Offline search",
			longDescription = 
					"If ```true```, it runs GeneMANIA against a local data set, so at least one data set and organism data must be installed. "
					+ "If ```false``` (default falue), the genes search is done by the GeneMANIA server, which means it requires an internet connection.",
			exampleStringValue = "false",
			context = "nogui"
	)
	public boolean offline;
	
	@Tunable(
			description = "Maximum number of resultant genes",
			longDescription = "The maximum number of genes returned by the search. The default value is ```20```.",
			exampleStringValue = "20",
			context = "nogui"
	)
	public int geneLimit = 20;
	
	@Tunable(
			description = "Maximum number of resultant attributes",
			longDescription = "The maximum number of attributes returned by GeneMANIA. The default value is ```10```.",
			exampleStringValue = "10",
			context = "nogui"
	)
	public int attrLimit = 10;
	
	@Tunable(
			description = "Combining method",
			longDescription = 
					"GeneMANIA can use a few different methods to weight networks when combining all networks to form "
					+ "the final composite network that results from a search. The default settings (i.e. ```AUTOMATIC_SELECT```) "
					+ "are usually appropriate, but you can choose a weighting method.",
			exampleStringValue = "AUTOMATIC_SELECT",
			context = "nogui"
	)
	public ListSingleSelection<CombiningMethod> combiningMethod = new ListSingleSelection<>(CombiningMethod.values());
	
	// TODO not supported by ONLINE search!
	@Tunable(
			description = "Scoring method",
			longDescription = 
					"The method used to compute the gene scores. "
					+ "This option is only supported by the offline search. "
					+ "The default value is ```DISCRIMINANT```.",
			exampleStringValue = "DISCRIMINANT",
			context = "nogui"
	)
	public ListSingleSelection<ScoringMethod> scoringMethod = new ListSingleSelection<>(ScoringMethod.values());
	
	private final GeneMania plugin;
	private final RetrieveRelatedGenesController controller;
	private final OrganismManager organismManager;
	private final CytoscapeUtils cytoscapeUtils;

	public SearchCommandTask(
			GeneMania plugin,
			RetrieveRelatedGenesController controller,
			OrganismManager organismManager,
			CytoscapeUtils cytoscapeUtils
	) {
		this.plugin = plugin;
		this.controller = controller;
		this.organismManager = organismManager;
		this.cytoscapeUtils = cytoscapeUtils;
		
		combiningMethod.setSelectedValue(CombiningMethod.AUTOMATIC_SELECT);
		scoringMethod.setSelectedValue(ScoringMethod.DISCRIMINANT);
	}
	
	@Override
	public void run(TaskMonitor tm) throws Exception {
		tm.setTitle("GeneMANIA");
		
		tm.setStatusMessage("Validating search...");
		tm.setProgress(-1);
		
		Query query = getQuery();
		
		if (query.getOrganism() == null)
			throw new RuntimeException("Please specify a valid organism.");
		if (query.getGenes() == null || query.getGenes().isEmpty())
			throw new RuntimeException("Please enter one or more genes.");
		if (offline && !SimpleSearchTaskFactory.hasValidGenes(query.getOrganism(), query.getGenes(), plugin))
			throw new RuntimeException("Please specify a set of valid gene names and try again.");
			
		tm.setStatusMessage("Searching " + (offline ? "installed data set" : "ONLINE") + "...");
		
		List<Group<?, ?>> groups = SimpleSearchTaskFactory.getGroups(query.getOrganism());
		
		ObservableTask nextTask = controller.runMania(query, groups, offline);
		
		AbstractTask finalTask = new AbstractTask() {
			@Override
			public void run(TaskMonitor tm) throws Exception {
				CyNetwork network = nextTask.getResults(CyNetwork.class);
				
				if (network != null) {
					tm.setStatusMessage("Applying layout...");
					
					cytoscapeUtils.handleNetworkPostProcessing(network);
					cytoscapeUtils.performLayout(network);
					cytoscapeUtils.maximize(network);
					
					NetworkSelectionManager selManager = plugin.getNetworkSelectionManager();
					ViewState options = selManager.getNetworkConfiguration(network);
					plugin.applyOptions(options);
					plugin.showResults();
				}
			}
		};
		
		insertTasksAfterCurrentTask(finalTask);
		insertTasksAfterCurrentTask(nextTask);
	}

	private Query getQuery() {
		// Create list of gene names
		List<String> geneList = new ArrayList<>();
		
		if (genes != null) {
			String[] arr = genes.split("\\|");
			
			for (String s : arr) {
				s = s.trim();
				
				if (!s.isEmpty())
					geneList.add(s);
			}
		}
		
		// Retrieve Organism object
		if (organism != null)
			organism = organism.trim();
		
		Set<Organism> organismSet = offline ? organismManager.getLocalOrganisms()
				: organismManager.getRemoteOrganisms();
		
		// Create Query object
		final Query query = new Query();
		query.setOrganism(findOrganism(organismSet));
		query.setGenes(geneList);
		query.setGeneLimit(geneLimit);
		query.setAttributeLimit(0);
		query.setCombiningMethod(combiningMethod.getSelectedValue() != null ?
				combiningMethod.getSelectedValue() : CombiningMethod.AUTOMATIC_SELECT);
		query.setScoringMethod(scoringMethod.getSelectedValue() != null ?
				scoringMethod.getSelectedValue() : ScoringMethod.DISCRIMINANT);
		
		return query;
	}

	private Organism findOrganism(Set<Organism> organismsList) {
		for (Organism org : organismsList) {
			if (org.getName().equalsIgnoreCase(organism) || org.getAlias().equalsIgnoreCase(organism)
					|| String.valueOf(org.getTaxonomyId()).equals(organism))
				return org;
		}
		
		return null;
	}
}
