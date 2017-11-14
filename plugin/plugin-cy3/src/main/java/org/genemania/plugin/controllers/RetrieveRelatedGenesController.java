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
package org.genemania.plugin.controllers;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.ws.rs.client.AsyncInvoker;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskMonitor.Level;
import org.cytoscape.work.json.JSONResult;
import org.genemania.data.normalizer.GeneCompletionProvider2;
import org.genemania.domain.AttributeGroup;
import org.genemania.domain.Gene;
import org.genemania.domain.InteractionNetwork;
import org.genemania.domain.InteractionNetworkGroup;
import org.genemania.domain.Node;
import org.genemania.domain.Organism;
import org.genemania.domain.ResultGene;
import org.genemania.domain.ResultInteraction;
import org.genemania.domain.ResultInteractionNetwork;
import org.genemania.domain.ResultInteractionNetworkGroup;
import org.genemania.domain.SearchRequest;
import org.genemania.domain.SearchResults;
import org.genemania.dto.EnrichmentEngineRequestDto;
import org.genemania.dto.EnrichmentEngineResponseDto;
import org.genemania.dto.InteractionDto;
import org.genemania.dto.NetworkDto;
import org.genemania.dto.NodeDto;
import org.genemania.dto.RelatedGenesEngineRequestDto;
import org.genemania.dto.RelatedGenesEngineResponseDto;
import org.genemania.engine.IMania;
import org.genemania.engine.Mania2;
import org.genemania.engine.cache.DataCache;
import org.genemania.engine.cache.MemObjectCache;
import org.genemania.exception.ApplicationException;
import org.genemania.exception.DataStoreException;
import org.genemania.mediator.OrganismMediator;
import org.genemania.plugin.GeneMania;
import org.genemania.plugin.LogUtils;
import org.genemania.plugin.NetworkUtils;
import org.genemania.plugin.Strings;
import org.genemania.plugin.cytoscape.CytoscapeUtils;
import org.genemania.plugin.cytoscape.EdgeAttributeProvider;
import org.genemania.plugin.data.DataSet;
import org.genemania.plugin.formatters.OrganismFormatter;
import org.genemania.plugin.model.Group;
import org.genemania.plugin.model.ModelElement;
import org.genemania.plugin.model.Network;
import org.genemania.plugin.model.OrganismComparator;
import org.genemania.plugin.model.SearchResult;
import org.genemania.plugin.model.ViewState;
import org.genemania.plugin.model.ViewStateBuilder;
import org.genemania.plugin.model.impl.InteractionNetworkGroupImpl;
import org.genemania.plugin.model.impl.ViewStateImpl;
import org.genemania.plugin.parsers.Query;
import org.genemania.plugin.selection.NetworkSelectionManager;
import org.genemania.plugin.task.TaskDispatcher;
import org.genemania.plugin.util.TaskMonitorProgressReporter;
import org.genemania.plugin.view.components.WrappedOptionPane;
import org.genemania.type.CombiningMethod;
import org.genemania.util.ChildProgressReporter;
import org.genemania.util.NullProgressReporter;
import org.genemania.util.ProgressReporter;

import com.google.gson.Gson;

public class RetrieveRelatedGenesController {
	
	// TODO Make it a CyProperty?
	private static final String SEARCH_URL = "http://genemania.org/json/search_results";
	private static final int MIN_CATEGORIES = 10;
	private static final double Q_VALUE_THRESHOLD = 0.1;

	private static Map<Long, Integer> sequenceNumbers = new HashMap<>();

	private final CytoscapeUtils cytoscapeUtils;
	private final GeneMania plugin;
	private final NetworkUtils networkUtils;
	private final TaskDispatcher taskDispatcher;
	
	public RetrieveRelatedGenesController(
			GeneMania plugin,
			CytoscapeUtils cytoscapeUtils,
			NetworkUtils networkUtils,
			TaskDispatcher taskDispatcher
	) {
		this.plugin = plugin;
		this.cytoscapeUtils = cytoscapeUtils;
		this.networkUtils = networkUtils;
		this.taskDispatcher = taskDispatcher;
	}
	
	public Vector<ModelElement<Organism>> createModel(DataSet data) throws DataStoreException {
		Vector<ModelElement<Organism>> organismChoices = new Vector<>();
		OrganismMediator mediator = data.getMediatorProvider().getOrganismMediator();
		Collection<Organism> organisms = mediator.getAllOrganisms(); 
		
		for (Organism organism : organisms)
			organismChoices.add(new ModelElement<>(
					organism, OrganismComparator.getInstance(), OrganismFormatter.getInstance()));
		
		Collections.sort(organismChoices);
        
		return organismChoices;
    }

	private RelatedGenesEngineRequestDto createRequest(DataSet data, Query query, Collection<Group<?, ?>> groups,
			ProgressReporter progress) {
		int stage = 0;
		progress.setMaximumProgress(2);
		
		RelatedGenesEngineRequestDto request = new RelatedGenesEngineRequestDto();
		request.setNamespace(GeneMania.DEFAULT_NAMESPACE);
		Organism organism = query.getOrganism();
		long id = organism.getId();
		request.setOrganismId(id);
		
		// Collect the selected networks
		ChildProgressReporter childProgress = new ChildProgressReporter(progress);
		childProgress.setStatus(Strings.retrieveRelatedGenes_status2);
		request.setInteractionNetworks(getInteractionNetworks(groups, childProgress));
		childProgress.close();
		stage++;
		
		if (childProgress.isCanceled())
			return null;
		
		// Collect attributes
		request.setAttributeGroups(getAttributeGroups(groups));
		
		// Parse out all the gene names
		childProgress = new ChildProgressReporter(progress);
		progress.setStatus(Strings.retrieveRelatedGenes_status3);
		progress.setProgress(stage++);
		request.setPositiveNodes(getQueryNodes(data, organism, query.getGenes(), progress));
		childProgress.close();
		stage++;
		
		if (progress.isCanceled())
			return null;
		
		request.setLimitResults(query.getGeneLimit());
		request.setAttributesLimit(query.getAttributeLimit());
		request.setCombiningMethod(computeCombiningMethod(query));
		request.setScoringMethod(query.getScoringMethod());
		
		return request;
	}
	
	private Collection<Long> getAttributeGroups(Collection<Group<?, ?>> selected) {
		Collection<Long> result = new ArrayList<>(selected.size());
		
		for (Group<?, ?> group : selected) {
			Group<Object, AttributeGroup> adapted = group.adapt(Object.class, AttributeGroup.class);
			
			if (adapted == null)
				continue;
			
			for (Network<AttributeGroup> network : adapted.getNetworks())
				result.add(network.getModel().getId());
		}
		
		return result;
	}

	private CombiningMethod computeCombiningMethod(Query query) {
		Organism organism = query.getOrganism();
		CombiningMethod method = query.getCombiningMethod();
		
		if (organism.getId() >= 0)
			return method;

		// We have a user organism so we have to disable AUTOMATIC_SELECT
		// or the engine might give us a branch-specific weighting method
		if (method.equals(CombiningMethod.AUTOMATIC_SELECT))
			return CombiningMethod.AUTOMATIC;
		
		return method;
	}

	private Set<Long> getQueryNodes(DataSet data, Organism organism, List<String> geneNames, ProgressReporter progress) {
		progress.setMaximumProgress(geneNames.size());
		int geneCount = 0;
		
		Set<Long> queryNodes = new HashSet<>();
		GeneCompletionProvider2 geneProvider = data.getCompletionProvider(organism);
		
		for (String name : geneNames) {
			progress.setDescription(name);
			Long nodeId = geneProvider.getNodeId(name);
			
			if (nodeId != null)
				queryNodes.add(nodeId);
			
			geneCount++;
			progress.setProgress(geneCount);
		}
		
		return queryNodes;
	}

	private static Collection<Collection<Long>> getInteractionNetworks(Collection<Group<?, ?>> selection,
			ProgressReporter progress) {
		Map<Long, Collection<Long>> groups = new HashMap<>();
		
		progress.setMaximumProgress(selection.size());
		int groupCount = 0;
		
		for (Group<?, ?> selectedGroup : selection) {
			Group<InteractionNetworkGroup, InteractionNetwork> adapted = selectedGroup
					.adapt(InteractionNetworkGroup.class, InteractionNetwork.class);
			
			if (adapted == null)
				continue;
			
			Collection<Long> resultNetworks = new HashSet<>();
			
			for (Network<InteractionNetwork> network : adapted.getNetworks()) {
				InteractionNetwork model = network.getModel();
				progress.setDescription(network.getName());
				long id = model.getId();
				resultNetworks.add(id);
			}
			
			groups.put(adapted.getModel().getId(), resultNetworks);
			
			groupCount++;
			progress.setProgress(groupCount);
		}
		
		List<Long> groupIds = new ArrayList<>(groups.keySet());
		Collections.sort(groupIds);
		
		Collection<Collection<Long>> result = new ArrayList<>();
		
		for (Long groupId : groupIds) {
			List<Long> groupMembers = new ArrayList<>(groups.get(groupId));
			Collections.sort(groupMembers);
			result.add(groupMembers);
		}
		
		return result;
	}
	
	public ObservableTask runMania(Query query, Collection<Group<?, ?>> groups, boolean offline) {
		// Create the WS client here, to avoid a thread/OSGI related issues;
		Client client = offline ? null : ClientBuilder.newClient();
		
		return new RunGeneManiaTask(query, groups, client, offline);
	}
	
	private EnrichmentEngineResponseDto computeEnrichment(EnrichmentEngineRequestDto request, DataSet data)
			throws DataStoreException {
		try {
			IMania mania = new Mania2(new DataCache(new MemObjectCache(data.getObjectCache(NullProgressReporter.instance(), false))));
			EnrichmentEngineResponseDto result = mania.computeEnrichment(request);
			return result;
		} catch (ApplicationException e) {
			LogUtils.log(getClass(), e);
			return null;
		}
	}

	private EnrichmentEngineRequestDto createEnrichmentRequest(Organism organism,
			RelatedGenesEngineResponseDto response) {
		if (organism.getOntology() == null)
			return null;
		
		EnrichmentEngineRequestDto request = new EnrichmentEngineRequestDto();
		request.setProgressReporter(NullProgressReporter.instance());
		request.setMinCategories(MIN_CATEGORIES);
		request.setqValueThreshold(Q_VALUE_THRESHOLD);
		request.setOrganismId(organism.getId());
		request.setOntologyId(organism.getOntology().getId());
		
		Set<Long> nodes = new HashSet<>();
		
		for (NetworkDto network : response.getNetworks()) {
			for (InteractionDto interaction : network.getInteractions()) {
				nodes.add(interaction.getNodeVO1().getId());
				nodes.add(interaction.getNodeVO2().getId());
			}
		}
		
		request.setNodes(nodes);		
		
		return request;
	}

	private Map<Long, Double> filterGeneScores(Map<Long, Double> scores, SearchResult options) {
		Map<Long, Gene> queryGenes = options.getQueryGenes();
		double maxScore = 0;
		
		for (Entry<Long, Double> entry : scores.entrySet()) {
			if (queryGenes.containsKey(entry.getKey()))
				continue;
			
			maxScore = Math.max(maxScore, entry.getValue());
		}
		
		Map<Long, Double> filtered = new HashMap<>();
		
		for (Entry<Long, Double> entry : scores.entrySet()) {
			long nodeId = entry.getKey();
			double score = entry.getValue();
			filtered.put(entry.getKey(), queryGenes.containsKey(nodeId) ? maxScore : score);
		}
		
		return filtered;
	}

	private double[] computeEdgeWeightExtrema(RelatedGenesEngineResponseDto response) {
		double[] extrema = new double[] { 1, 0 };
		
		for (NetworkDto network : response.getNetworks()) {
			for (InteractionDto interaction : network.getInteractions()) {
				double weight = interaction.getWeight() * network.getWeight();
				
				if (extrema[0] > weight)
					extrema[0] = weight;
				if (extrema[1] < weight)
					extrema[1] = weight;
			}
		}
		
		return extrema;
	}
	
	private double[] computeEdgeWeightExtrema(SearchResults results) {
		double[] extrema = new double[] { 1, 0 };
		
		for (ResultInteractionNetworkGroup resNetGr : results.getResultNetworkGroups()) {
			for (ResultInteractionNetwork resNet : resNetGr.getResultNetworks()) {
				for (ResultInteraction resInter : resNet.getResultInteractions()) {
					double weight = resInter.getInteraction().getWeight() * resNet.getWeight();// TODO review
					
					if (extrema[0] > weight)
						extrema[0] = weight;
					if (extrema[1] < weight)
						extrema[1] = weight;
				}
			}
		}
		
		return extrema;
	}

	private Map<Long, Double> computeGeneScores(RelatedGenesEngineResponseDto result) {
		Map<Long, Double> scores = new HashMap<>();
		
		for (NetworkDto network : result.getNetworks()) {
			for (InteractionDto interaction : network.getInteractions()) {
				NodeDto node1 = interaction.getNodeVO1();
				scores.put(node1.getId(), node1.getScore());
				NodeDto node2 = interaction.getNodeVO2();
				scores.put(node2.getId(), node2.getScore());
			}
		}
		
		return scores;
	}
	
	private Map<Long, Double> computeGeneScores(Collection<ResultGene> genes) {
		Map<Long, Double> scores = new HashMap<>();
		
		for (ResultGene resGene : genes)
			scores.put(resGene.getGene().getNode().getId(), resGene.getScore());
		
		return scores;
	}

	private Map<String, Color> computeNetworkColors(DataSet data, Organism organism) {
		Map<String, Color> colors = new HashMap<>();
		Collection<InteractionNetworkGroup> groups = organism.getInteractionNetworkGroups();
		
		for (InteractionNetworkGroup group : groups) {
			Color color = networkUtils.getNetworkColor(data, new InteractionNetworkGroupImpl(group));
			colors.put(group.getName(), color);
		}
		
		return colors;
	}
	
	private static EdgeAttributeProvider createEdgeAttributeProvider(SearchResult options) {
		final Map<Long, InteractionNetworkGroup> groupsByNetwork = options.getInteractionNetworkGroups();

		return new EdgeAttributeProvider() {
			@Override
			public Map<String, Object> getAttributes(InteractionNetwork network) {
				HashMap<String, Object> attributes = new HashMap<>();
				long id = network.getId();
				InteractionNetworkGroup group = groupsByNetwork.get(id);
				
				if (group != null)
					attributes.put(CytoscapeUtils.NETWORK_GROUP_NAME_ATTRIBUTE, group.getName());
				
				return attributes;
			}
			
			@Override
			public String getEdgeLabel(InteractionNetwork network) {
				long id = network.getId();
				
				if (id == -1) {
					return "combined"; //$NON-NLS-1$
				} else {
					InteractionNetworkGroup group = groupsByNetwork.get(id);
					
					if (group != null)
						return group.getName();
					
					return "unknown"; //$NON-NLS-1$
				}
			}
		};
	}
	
	void computeGraphCache(CyNetwork currentNetwork, SearchResult result, ViewStateBuilder config,
			Collection<Group<?, ?>> selectedGroups) {
		// Build edge cache
		for (CyEdge edge : currentNetwork.getEdgeList()) {
			String name = cytoscapeUtils.getAttribute(currentNetwork, edge, CytoscapeUtils.NETWORK_GROUP_NAME_ATTRIBUTE,
					String.class);
			Group<?, ?> group = config.getGroup(name);
			config.addEdge(group, cytoscapeUtils.getIdentifier(currentNetwork, edge));
		}
		
		// Build node cache
		for (Gene gene : result.getScores().keySet()) {
			Node node = gene.getNode();
			CyNode cyNode = cytoscapeUtils.getNode(currentNetwork, node, null);
			config.addNode(node, cytoscapeUtils.getIdentifier(currentNetwork, cyNode));
		}
		
		// Cache selected networks
		applyDefaultSelection(config, selectedGroups);
	}

	private void applyDefaultSelection(ViewState config, Collection<Group<?, ?>> selectedGroups) {
		Set<String> targetGroups = new HashSet<>();
		//targetGroups.add("coloc"); //$NON-NLS-1$
		//targetGroups.add("coexp"); //$NON-NLS-1$
		
		// By default, disable colocation/coexpression networks.
		Set<String> retainedGroups = new HashSet<>();
		
		for (Group<?, ?> group : selectedGroups) {
			group = config.getGroup(group.getName());
			
			if (group == null)
				continue;
			
			String code = group.getCode();
			boolean enabled = !targetGroups.remove(code);
			
			if (enabled)
				retainedGroups.add(code);
			
			config.setEnabled(group, enabled);
		}
		
		// If we only have colocation/coexpression networks, enabled them.
		if (retainedGroups.size() == 0) {
			for (Group<?, ?> group : selectedGroups) {
				group = config.getGroup(group.getName());
				config.setEnabled(group, true);
			}
		}
	}

	public static String getNextNetworkName(Organism organism) {
		long id = organism.getId();
		int sequenceNumber = sequenceNumbers.containsKey(id) ? sequenceNumbers.get(id) + 1 : 1;
		sequenceNumbers.put(id, sequenceNumber);
		
		return String.format(Strings.retrieveRelatedGenesNetworkName_label, organism.getName(), sequenceNumber);
	}

	RelatedGenesEngineResponseDto runQuery(RelatedGenesEngineRequestDto request, DataSet data) throws DataStoreException {
		try {
			IMania mania = new Mania2(new DataCache(new MemObjectCache(data.getObjectCache(NullProgressReporter.instance(), false))));
			RelatedGenesEngineResponseDto result = mania.findRelated(request);
			networkUtils.normalizeNetworkWeights(result);
			
			return result;
		} catch (ApplicationException e) {
			LogUtils.log(getClass(), e);
			
			return null;
		}
	}
	
	private class RunGeneManiaTask extends AbstractTask implements ObservableTask {

		private final Query query;
		private Collection<Group<?, ?>> groups;
		private final Client client;
		private final boolean offline;
		
		private Future<String> future;
		private CyNetwork network;

		public RunGeneManiaTask(Query query, Collection<Group<?, ?>> groups, Client client, boolean offline) {
			this.query = query;
			this.groups = groups;
			this.client = client;
			this.offline = offline;
		}

		@Override
		public void run(TaskMonitor tm) throws Exception {
			tm.setTitle(Strings.retrieveRelatedGenes_status);
			tm.setStatusMessage(Strings.retrieveRelatedGenes_status4);
			
			ProgressReporter progress = new TaskMonitorProgressReporter(tm, 5);
			int stage = 0;
			
			ChildProgressReporter childProgress = new ChildProgressReporter(progress);
			childProgress.close();
			stage++;

			Organism organism = query.getOrganism();
			List<String> queryGenes = query.getGenes();
			
			SearchResult options = null;
			String dataVersion = null; // TODO server should also return this when searching online
			double[] extrema =  null;
			Map<String, Color> networkColors = CytoscapeUtils.NETWORK_COLORS;
			Map<Long, Double> scores = null;
			
			if (offline) {
				DataSet data = plugin.getDataSetManager().getDataSet();
				dataVersion = data.getVersion().toString();
				networkColors = computeNetworkColors(data, organism);
				
				childProgress = new ChildProgressReporter(progress);
				RelatedGenesEngineRequestDto request = createRequest(data, query, groups, childProgress);
				request.setProgressReporter(childProgress);
				RelatedGenesEngineResponseDto response = runQuery(request, data);
				request.setCombiningMethod(response.getCombiningMethodApplied());
				childProgress.close();
				stage++;
				
				if (cancelled == true)
					return;
		
				scores = computeGeneScores(response);
				
				if (scores.size() == 0) {
					showNoResultsWarning(tm);
					tm.setProgress(1.0);
					
					return;
				}
				
				extrema = computeEdgeWeightExtrema(response);
				
				EnrichmentEngineRequestDto enrichmentRequest = createEnrichmentRequest(organism, response);
				EnrichmentEngineResponseDto enrichmentResponse = null;
				
				if (enrichmentRequest != null) {
					childProgress = new ChildProgressReporter(progress);
					enrichmentRequest.setProgressReporter(childProgress);
					enrichmentResponse = computeEnrichment(enrichmentRequest, data);
					childProgress.close();
				}
				
				stage++;
				
				if (cancelled == true)
					return;
				
				options = networkUtils.createSearchOptions(organism, request, response, enrichmentResponse, data,
						queryGenes);
			} else {
				SearchRequest req = new SearchRequest(
						query.getOrganism().getId(),
						query.getGenes().stream().collect(Collectors.joining("\n"))
				);
				req.setWeightingFromEnum(query.getCombiningMethod());
				req.setGeneThreshold(query.getGeneLimit());
				req.setAttrThreshold(query.getAttributeLimit());
				// TODO
//				req.setAttrGroups(new Long[] {});
//				req.setNetworks(new Long[] {});
				
				Gson gson = new Gson();
				String jsonReq = gson.toJson(req);
				
				WebTarget target = client.target(SEARCH_URL);
				AsyncInvoker invoker = target.request().async();
				future = invoker.post(Entity.json(jsonReq), String.class);
				String json = future.get();
				SearchResults searchResults = gson.fromJson(json, SearchResults.class);
				
				if (cancelled == true)
					return;
		
				scores = computeGeneScores(searchResults.getResultGenes());
				
				if (scores.size() == 0) {
					showNoResultsWarning(tm);
					tm.setProgress(1.0);
					
					return;
				}
				
				extrema = computeEdgeWeightExtrema(searchResults);
				options = networkUtils.createSearchOptions(searchResults);
				
				// On online searches, the user cannot select network groups yet, so we need to get all groups
				// returned with the response, otherwise all of them will be unchecked on the Networks panel by the default.
				if (groups == null || groups.isEmpty()) {
					groups = new HashSet<>();
					
					for (InteractionNetworkGroup group : options.getInteractionNetworkGroups().values())
						groups.add(new InteractionNetworkGroupImpl(group));
				}
			}
			
			if (options != null) {
				EdgeAttributeProvider provider = createEdgeAttributeProvider(options);
				
				tm.setStatusMessage(Strings.retrieveRelatedGenes_status5);
				progress.setProgress(stage++);
				ViewStateBuilder builder = new ViewStateImpl(options);
				String netName = getNextNetworkName(organism);
				
				network = cytoscapeUtils.createNetwork(netName, dataVersion, options, builder, provider);
		
				// Set up edge cache
				tm.setStatusMessage(Strings.retrieveRelatedGenes_status6);
				progress.setProgress(stage++);
				NetworkSelectionManager manager = plugin.getNetworkSelectionManager();
				
				computeGraphCache(network, options, builder, groups);
				manager.addNetworkConfiguration(network, builder.build());
		
				cytoscapeUtils.registerSelectionListener(network, manager, plugin);
				cytoscapeUtils.applyVisualization(network, filterGeneScores(scores, options), networkColors, extrema);
			}
		}
		
		@Override
		@SuppressWarnings({ "rawtypes", "unchecked" })
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
		
		@Override
		public List<Class<?>> getResultClasses() {
			return Arrays.asList(CyNetwork.class, CySubNetwork.class, String.class, JSONResult.class);
		}
		
		@Override
		public void cancel() {
			super.cancel();
			
			if (future != null)
				future.cancel(true);
		}
		
		private void showNoResultsWarning(TaskMonitor tm) {
			tm.showMessage(Level.WARN, Strings.retrieveRelatedGenesNoResults);
			
			SwingUtilities.invokeLater(() -> WrappedOptionPane.showConfirmDialog(
					cytoscapeUtils.getFrame(),
					Strings.retrieveRelatedGenesNoResults,
					Strings.default_title,
					JOptionPane.DEFAULT_OPTION,
					JOptionPane.WARNING_MESSAGE,
					40
			));
		}
	}
}
