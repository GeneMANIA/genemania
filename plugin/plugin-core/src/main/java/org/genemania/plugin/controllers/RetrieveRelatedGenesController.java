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
import java.awt.Window;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.genemania.data.normalizer.GeneCompletionProvider2;
import org.genemania.domain.AttributeGroup;
import org.genemania.domain.Gene;
import org.genemania.domain.InteractionNetwork;
import org.genemania.domain.InteractionNetworkGroup;
import org.genemania.domain.Node;
import org.genemania.domain.Organism;
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
import org.genemania.plugin.data.Colour;
import org.genemania.plugin.data.DataSet;
import org.genemania.plugin.formatters.OrganismFormatter;
import org.genemania.plugin.model.Group;
import org.genemania.plugin.model.ModelElement;
import org.genemania.plugin.model.Network;
import org.genemania.plugin.model.OrganismComparator;
import org.genemania.plugin.model.SearchResult;
import org.genemania.plugin.model.ViewState;
import org.genemania.plugin.model.ViewStateBuilder;
import org.genemania.plugin.model.impl.ViewStateImpl;
import org.genemania.plugin.parsers.Query;
import org.genemania.plugin.proxies.EdgeProxy;
import org.genemania.plugin.proxies.NetworkProxy;
import org.genemania.plugin.proxies.NodeProxy;
import org.genemania.plugin.selection.NetworkSelectionManager;
import org.genemania.plugin.task.GeneManiaTask;
import org.genemania.plugin.task.TaskDispatcher;
import org.genemania.plugin.view.components.WrappedOptionPane;
import org.genemania.type.CombiningMethod;
import org.genemania.util.ChildProgressReporter;
import org.genemania.util.NullProgressReporter;
import org.genemania.util.ProgressReporter;

public class RetrieveRelatedGenesController<NETWORK, NODE, EDGE> {
	private static final int MIN_CATEGORIES = 10;

	private static final double Q_VALUE_THRESHOLD = 0.1;

	private static Map<Long, Integer> sequenceNumbers;

	static {
		sequenceNumbers = new HashMap<>();
	}

	private final CytoscapeUtils<NETWORK, NODE, EDGE> cytoscapeUtils;

	private final GeneMania<NETWORK, NODE, EDGE> plugin;

	private final NetworkUtils networkUtils;

	private final TaskDispatcher taskDispatcher;
	
	public RetrieveRelatedGenesController(GeneMania<NETWORK, NODE, EDGE> plugin, CytoscapeUtils<NETWORK, NODE, EDGE> cytoscapeUtils, NetworkUtils networkUtils, TaskDispatcher taskDispatcher) {
		this.plugin = plugin;
		this.cytoscapeUtils = cytoscapeUtils;
		this.networkUtils = networkUtils;
		this.taskDispatcher = taskDispatcher;
	}
	
	public Vector<ModelElement<Organism>> createModel(final DataSet data) throws DataStoreException {
		Vector<ModelElement<Organism>> organismChoices = new Vector<>();
		OrganismMediator mediator = data.getMediatorProvider().getOrganismMediator();
		Collection<Organism> organisms = mediator.getAllOrganisms(); 
		for (Organism organism : organisms) {
			organismChoices.add(new ModelElement<>(organism, OrganismComparator.getInstance(), OrganismFormatter.getInstance()));
		}
		Collections.sort(organismChoices);
        return organismChoices;
    }

	private RelatedGenesEngineRequestDto createRequest(DataSet data, Query query, Collection<Group<?, ?>> groups, ProgressReporter progress) {
		int stage = 0;
		progress.setMaximumProgress(2);
		
		RelatedGenesEngineRequestDto request = new RelatedGenesEngineRequestDto();
		request.setNamespace(GeneMania.DEFAULT_NAMESPACE);
		Organism organism = query.getOrganism();
		long id = organism .getId();
		request.setOrganismId(id);
		

		// Collect the selected networks
		ChildProgressReporter childProgress = new ChildProgressReporter(progress);
		childProgress.setStatus(Strings.retrieveRelatedGenes_status2);
		request.setInteractionNetworks(getInteractionNetworks(data, groups, childProgress));
		childProgress.close();
		stage++;
		if (childProgress.isCanceled()) {
			return null;
		}
		
		// Collect attributes
		request.setAttributeGroups(getAttributeGroups(groups));
		
		// Parse out all the gene names
		childProgress = new ChildProgressReporter(progress);
		progress.setStatus(Strings.retrieveRelatedGenes_status3);
		progress.setProgress(stage++);
		request.setPositiveNodes(getQueryNodes(data, organism, query.getGenes(), progress));
		childProgress.close();
		stage++;
		if (progress.isCanceled()) {
			return null;
		}
		
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
			if (adapted == null) {
				continue;
			}
			for (Network<AttributeGroup> network : adapted.getNetworks()) {
				result.add(network.getModel().getId());
			}
		}
		return result;
	}

	private CombiningMethod computeCombiningMethod(Query query) {
		Organism organism = query.getOrganism();
		CombiningMethod method = query.getCombiningMethod();
		
		if (organism.getId() >= 0) {
			return method;
		}

		// We have a user organism so we have to disable AUTOMATIC_SELECT
		// or the engine might give us a branch-specific weighting method
		if (method.equals(CombiningMethod.AUTOMATIC_SELECT)) {
			return CombiningMethod.AUTOMATIC;
		}
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
			if (nodeId != null) {
				queryNodes.add(nodeId);
			}
			geneCount++;
			progress.setProgress(geneCount);
		}
		return queryNodes;
	}

	private Collection<Collection<Long>> getInteractionNetworks(DataSet data, Collection<Group<?, ?>> selection, ProgressReporter progress) {
		Map<Long, Collection<Long>> groups = new HashMap<>();
		
		progress.setMaximumProgress(selection.size());
		int groupCount = 0;
		
		for (Group<?, ?> selectedGroup : selection) {
			Group<InteractionNetworkGroup, InteractionNetwork> adapted = selectedGroup.adapt(InteractionNetworkGroup.class, InteractionNetwork.class);
			if (adapted == null) {
				continue;
			}
			
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
	
	@SuppressWarnings("unchecked")
	public NETWORK runMania(Window parent, final Query query, final Collection<Group<?, ?>> groups) {
		final Object[] result = new Object[1];
		GeneManiaTask task = new GeneManiaTask(Strings.retrieveRelatedGenes_status) {
			public void runTask() throws DataStoreException {
				result[0] = createNetwork(query, groups, progress);
			}
		};
		taskDispatcher.executeTask(task, parent, true, true);
		LogUtils.log(getClass(), task.getLastError());
		
		return (NETWORK) result[0];
	}
	
	private NETWORK createNetwork(Query query, Collection<Group<?, ?>> selectedGroups, ProgressReporter progress) throws DataStoreException {
		int stage = 0;
		progress.setMaximumProgress(5);
		
		DataSet data = plugin.getDataSetManager().getDataSet();
		
		progress.setStatus(Strings.retrieveRelatedGenes_status4);
		
		ChildProgressReporter childProgress = new ChildProgressReporter(progress);
		RelatedGenesEngineRequestDto request = createRequest(data, query, selectedGroups, childProgress);
		childProgress.close();
		stage++;

		childProgress = new ChildProgressReporter(progress);
		request.setProgressReporter(childProgress);
		RelatedGenesEngineResponseDto response = runQuery(request, data);
		request.setCombiningMethod(response.getCombiningMethodApplied());
		childProgress.close();
		stage++;
		
		if (progress.isCanceled()) {
			return null;
		}

		Map<Long, Double> scores = computeGeneScores(response);
		if (scores.size() == 0) {
			WrappedOptionPane.showConfirmDialog(taskDispatcher.getTaskDialog(), Strings.retrieveRelatedGenesNoResults, Strings.default_title, JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, 40);
			return null;
		}
		
		double[] extrema = computeEdgeWeightExtrema(response);
		
		Organism organism = query.getOrganism();
		EnrichmentEngineRequestDto enrichmentRequest = createEnrichmentRequest(organism, response, data);
		EnrichmentEngineResponseDto enrichmentResponse = null;
		if (enrichmentRequest != null) {
			childProgress = new ChildProgressReporter(progress);
			enrichmentRequest.setProgressReporter(childProgress);
			enrichmentResponse = computeEnrichment(enrichmentRequest, data);
			childProgress.close();
		}
		stage++;
		if (progress.isCanceled()) {
			return null;
		}		
		
		List<String> queryGenes = query.getGenes();
		SearchResult options = networkUtils.createSearchOptions(organism, request, response, enrichmentResponse, data, queryGenes);
		
		EdgeAttributeProvider provider = createEdgeAttributeProvider(data, options);
		
		progress.setStatus(Strings.retrieveRelatedGenes_status5);
		progress.setProgress(stage++);
		ViewStateBuilder builder = new ViewStateImpl(options);
		NETWORK network = cytoscapeUtils.createNetwork(data, getNextNetworkName(organism), options, builder, provider);

		// Set up edge cache
		progress.setStatus(Strings.retrieveRelatedGenes_status6);
		progress.setProgress(stage++);
		NetworkSelectionManager<NETWORK, NODE, EDGE> manager = plugin.getNetworkSelectionManager();
		
		computeGraphCache(network, options, builder, selectedGroups);
		
		manager.addNetworkConfiguration(network, builder.build());

		cytoscapeUtils.registerSelectionListener(network, manager, plugin);
		cytoscapeUtils.applyVisualization(network, filterGeneScores(scores, options), computeColors(data, organism), extrema);
		
		return network;
	}
	
	private EnrichmentEngineResponseDto computeEnrichment(EnrichmentEngineRequestDto request, DataSet data) throws DataStoreException {
		try {
			IMania mania = new Mania2(new DataCache(new MemObjectCache(data.getObjectCache(NullProgressReporter.instance(), false))));
			EnrichmentEngineResponseDto result = mania.computeEnrichment(request);
			return result;
		} catch (ApplicationException e) {
			LogUtils.log(getClass(), e);
			return null;
		}
	}

	private EnrichmentEngineRequestDto createEnrichmentRequest(Organism organism, RelatedGenesEngineResponseDto response, DataSet data) {
		if (organism.getOntology() == null) {
			return null;
		}
		
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
			if (queryGenes.containsKey(entry.getKey())) {
				continue;
			}
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
				if (extrema[0] > weight) {
					extrema[0] = weight;
				}
				if (extrema[1] < weight) {
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

	private Map<String, Color> computeColors(DataSet data, Organism organism) {
		Map<String, Color> colors = new HashMap<>();
		Collection<InteractionNetworkGroup> groups = organism.getInteractionNetworkGroups();
		for (InteractionNetworkGroup group : groups) {
			Colour color = data.getColor(group.getCode());
			colors.put(group.getName(), new Color(color.getRgb())); 
		}
		return colors;
	}

	private EdgeAttributeProvider createEdgeAttributeProvider(DataSet data, SearchResult options) {
		final Map<Long, InteractionNetworkGroup> groupsByNetwork = options.getInteractionNetworkGroups();

		return new EdgeAttributeProvider() {
			public Map<String, Object> getAttributes(InteractionNetwork network) {
				HashMap<String, Object> attributes = new HashMap<>();
				long id = network.getId();
				InteractionNetworkGroup group = groupsByNetwork.get(id);
				if (group != null) {
					attributes.put(CytoscapeUtils.NETWORK_GROUP_NAME_ATTRIBUTE, group.getName());
				}
				return attributes;
			}

			public String getEdgeLabel(InteractionNetwork network) {
				long id = network.getId();
				if (id == -1) {
					return "combined"; //$NON-NLS-1$
				} else {
					InteractionNetworkGroup group = groupsByNetwork.get(id);
					if (group != null) {
						return group.getName();
					}
					return "unknown"; //$NON-NLS-1$
				}
			}
		};
	}
	
	void computeGraphCache(NETWORK currentNetwork, SearchResult result, ViewStateBuilder config, Collection<Group<?, ?>> selectedGroups) {
		// Build edge cache
		NetworkProxy<NETWORK, NODE, EDGE> networkProxy = cytoscapeUtils.getNetworkProxy(currentNetwork);
		for (EDGE edge : networkProxy.getEdges()) {
			EdgeProxy<EDGE, NODE> edgeProxy = cytoscapeUtils.getEdgeProxy(edge, currentNetwork);
			String name = edgeProxy.getAttribute(CytoscapeUtils.NETWORK_GROUP_NAME_ATTRIBUTE, String.class);
			Group<?, ?> group = config.getGroup(name);
			config.addEdge(group, edgeProxy.getIdentifier());
		}
		
		// Build node cache
		for (Gene gene : result.getScores().keySet()) {
			Node node = gene.getNode();
			NODE cyNode = cytoscapeUtils.getNode(currentNetwork, node, null);
			NodeProxy<NODE> nodeProxy = cytoscapeUtils.getNodeProxy(cyNode, currentNetwork);
			config.addNode(node, nodeProxy.getIdentifier());
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
			if (group == null) {
				continue;
			}
			
			String code = group.getCode();
			boolean enabled = !targetGroups.remove(code);
			
			if (enabled) {
				retainedGroups.add(code);
			}
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

	private static String getNextNetworkName(Organism organism) {
		long id = organism.getId();
		int sequenceNumber;
		if (sequenceNumbers.containsKey(id)) {
			sequenceNumber = sequenceNumbers.get(id) + 1;
		} else {
			sequenceNumber = 1;
		}
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
}
