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
import java.util.Vector;
import java.util.stream.Collectors;

import javax.swing.JFrame;
import javax.ws.rs.core.Response;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.json.JSONResult;
import org.cytoscape.work.util.ListSingleSelection;
import org.genemania.domain.Organism;
import org.genemania.domain.SearchRequest;
import org.genemania.exception.DataStoreException;
import org.genemania.plugin.GeneMania;
import org.genemania.plugin.NetworkUtils;
import org.genemania.plugin.controllers.RetrieveRelatedGenesController;
import org.genemania.plugin.cytoscape.CytoscapeUtils;
import org.genemania.plugin.data.DataSet;
import org.genemania.plugin.data.DataSetManager;
import org.genemania.plugin.model.Group;
import org.genemania.plugin.model.ModelElement;
import org.genemania.plugin.model.ViewState;
import org.genemania.plugin.parsers.Query;
import org.genemania.plugin.selection.NetworkSelectionManager;
import org.genemania.type.CombiningMethod;
import org.genemania.type.ScoringMethod;

import com.google.gson.Gson;

public class SearchCommandTask extends AbstractTask implements ObservableTask {

	@Tunable(description = "Offline search:", context = "nogui")
	public boolean offline;
	
	@Tunable(description = "Organism", context = "nogui")
	public ListSingleSelection<Organism> organism = new ListSingleSelection<>();
	
	@Tunable(description = "List of query genes", context = "nogui")
	public String genes;
	
	@Tunable(description = "Maximum number of resultant genes", context = "nogui")
	public int geneLimit = 20;
	
	@Tunable(description = "Maximum number of resultant attributes", context = "nogui")
	public int attrLimit = 10;
	
	@Tunable(description = "Combining method", context = "nogui")
	public ListSingleSelection<CombiningMethod> combiningMethod = new ListSingleSelection<>(CombiningMethod.values());
	
	// TODO not supported by ONLINE search!
	@Tunable(description = "Scoring method", context = "nogui")
	public ListSingleSelection<ScoringMethod> scoringMethod = new ListSingleSelection<>(ScoringMethod.values());
	
	private CyNetwork network;
	
	private final GeneMania plugin;
	private final RetrieveRelatedGenesController controller;
	private final NetworkUtils networkUtils;
	private final CytoscapeUtils cytoscapeUtils;
	private final CyServiceRegistrar serviceRegistrar;

	public SearchCommandTask(
			GeneMania plugin,
			RetrieveRelatedGenesController controller,
			NetworkUtils networkUtils,
			CytoscapeUtils cytoscapeUtils, 
			CyServiceRegistrar serviceRegistrar
	) {
		this.plugin = plugin;
		this.controller = controller;
		this.networkUtils = networkUtils;
		this.cytoscapeUtils = cytoscapeUtils;
		this.serviceRegistrar = serviceRegistrar;
		
		combiningMethod.setSelectedValue(CombiningMethod.AUTOMATIC_SELECT);
		scoringMethod.setSelectedValue(ScoringMethod.DISCRIMINANT);
		
		DataSetManager dataSetManager = plugin.getDataSetManager();
		DataSet data = dataSetManager.getDataSet();
		
		try {
			Vector<ModelElement<Organism>> orgVector = data != null ? controller.createModel(data) : new Vector<>();
			List<Organism> orgList = new ArrayList<>();
			
			for (ModelElement<Organism> me : orgVector)
				orgList.add(me.getItem());
			
			organism.setPossibleValues(orgList);
		} catch (DataStoreException e) {
			throw new RuntimeException("Cannot retrieve available organisms", e);
		}
	}
	
	@Override
	public void run(TaskMonitor tm) throws Exception {
		tm.setTitle("GeneMANIA");
		tm.setStatusMessage("Validating search...");
		tm.setProgress(-1);
		
		Query query = getQuery();
		
		if (query.getOrganism() == null)
			throw new RuntimeException("Please specify an organism.");
		if (query.getGenes() == null || query.getGenes().isEmpty())
			throw new RuntimeException("Please enter one or more genes.");
		if (offline && !SimpleSearchTaskFactory.hasValidGenes(query.getOrganism(), query.getGenes(), plugin))
			throw new RuntimeException("Please specify a set of valid gene names and try again.");
			
		tm.setStatusMessage("Searching " + (offline ? "installed data set" : "ONLINE") + "...");
		
		List<Group<?, ?>> groups = SimpleSearchTaskFactory.getGroups(query.getOrganism());
		
		JFrame parent = serviceRegistrar.getService(CySwingApplication.class).getJFrame();
		network = controller.runMania(parent, query, groups, offline);

		tm.setStatusMessage("Applying layout...");
		
		cytoscapeUtils.handleNetworkPostProcessing(network);
		cytoscapeUtils.performLayout(network);
		cytoscapeUtils.maximize(network);
		
		NetworkSelectionManager selManager = plugin.getNetworkSelectionManager();
		ViewState options = selManager.getNetworkConfiguration(network);
		plugin.applyOptions(options);
		plugin.showResults();
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object getResults(Class type) {
		if (type == CyNetwork.class || type == CySubNetwork.class)
			return network;
		
		if (type == String.class)
			return network == null ? 
					"Search returned no results." :
					String.format("Created network '%s' (SUID=%d)", 
							network.getRow(network).get(CyNetwork.NAME, String.class), network.getSUID());
		
		if (type == JSONResult.class) {
			JSONResult res = () -> { return network != null ? "" + network.getSUID() : null; };
			return res;
		}
			
		return null;
	}
	
//	private void searchOffline(Query query, TaskMonitor tm) {
//		List<Group<?, ?>> groups = SimpleSearchTaskFactory.getGroups(query.getOrganism());
//		
//		JFrame parent = serviceRegistrar.getService(CySwingApplication.class).getJFrame();
//		network = controller.runMania(parent, query, groups, true);
//
//		tm.setStatusMessage("Applying layout...");
//		
//		cytoscapeUtils.handleNetworkPostProcessing(network);
//		cytoscapeUtils.performLayout(network);
//		cytoscapeUtils.maximize(network);
//		
//		NetworkSelectionManager selManager = plugin.getNetworkSelectionManager();
//		ViewState options = selManager.getNetworkConfiguration(network);
//		plugin.applyOptions(options);
//		plugin.showResults();
//	}
	
	private void searchOnline(Query query, TaskMonitor tm) {
		SearchRequest req = new SearchRequest(
				query.getOrganism().getId(),
				query.getGenes().stream().collect(Collectors.joining("\n"))
		);
		req.setWeightingFromEnum(query.getCombiningMethod());
		req.setGeneThreshold(query.getGeneLimit());
		req.setAttrThreshold(query.getAttributeLimit());
		// TODO
//		req.setAttrGroups(new Long[] {});
//		req.setNetworks(new Long[] {});
		
		Gson gson = new Gson();
		String jsonReq = gson.toJson(req);
		
		Response res = null;
		
		try {
//			Client client = ClientBuilder.newClient();
//			WebTarget target = client.target(URL);
//			res = target.request(MediaType.APPLICATION_JSON).post(Entity.json(jsonReq));
//			
//			if (res.getStatus() != 200)
//				throw new RuntimeException(
//						res.getStatusInfo().getStatusCode() + ": " + res.getStatusInfo().getReasonPhrase());
//			
//			String json = res.readEntity(String.class);
//			SearchResults searchResults = gson.fromJson(json, SearchResults.class);
//			System.out.println(searchResults);
			
//			RelatedGenesEngineRequestDto request,
//			RelatedGenesEngineResponseDto response,
//			EnrichmentEngineResponseDto enrichmentResponse,
//			SearchResult options
			
			// TODO:
			// A) ============
//			DataSet data = plugin.getDataSetManager().getDataSet();
//			List<Group<?, ?>> groups = SimpleSearchTaskFactory.getGroups(query.getOrganism());
//			SearchResult options = networkUtils.createSearchOptions(organism, request, response, enrichmentResponse, data, queryGenes);
//			
//			SearchResult options = new Searchres
//			EdgeAttributeProvider provider = RetrieveRelatedGenesController.createEdgeAttributeProvider(data, options);
//			
////			progress.setStatus(Strings.retrieveRelatedGenes_status5);
////			progress.setProgress(stage++);
//			ViewStateBuilder builder = new ViewStateImpl(options);
//			CyNetwork network = cytoscapeUtils.createNetwork(data, RetrieveRelatedGenesController.getNextNetworkName(query.getOrganism()), options, builder, provider);
//
//			// Set up edge cache
////			progress.setStatus(Strings.retrieveRelatedGenes_status6);
////			progress.setProgress(stage++);
//			NetworkSelectionManager manager = plugin.getNetworkSelectionManager();
//			
//			computeGraphCache(network, options, builder, selectedGroups);
//			
//			manager.addNetworkConfiguration(network, builder.build());
//
//			cytoscapeUtils.registerSelectionListener(network, manager, plugin);
//			cytoscapeUtils.applyVisualization(network, filterGeneScores(scores, options), computeColors(data, organism), extrema);
			
			// B) ============
//			cytoscapeUtils.handleNetworkPostProcessing(network);
//			cytoscapeUtils.performLayout(network);
//			cytoscapeUtils.maximize(network);
//			
//			ViewState viewState = manager.getNetworkConfiguration(network);
//			plugin.applyOptions(viewState);
//			plugin.showResults();
		} finally {
			if (res != null)
				res.close();
		}
	}
	
	private Query getQuery() {
		List<String> geneList = new ArrayList<>();
		
		if (genes != null) {
			String[] arr = genes.split("\\|");
			
			for (String s : arr) {
				s = s.trim();
				
				if (!s.isEmpty())
					geneList.add(s);
			}
		}
		
		final Query query = new Query();
		query.setGenes(geneList);
		query.setOrganism(organism.getSelectedValue());
		query.setGeneLimit(geneLimit);
		query.setAttributeLimit(0);
		query.setCombiningMethod(combiningMethod.getSelectedValue() != null ?
				combiningMethod.getSelectedValue() : CombiningMethod.AUTOMATIC_SELECT);
		query.setScoringMethod(scoringMethod.getSelectedValue() != null ?
				scoringMethod.getSelectedValue() : ScoringMethod.DISCRIMINANT);
		
		return query;
	}
}
