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

package org.genemania.plugin.cytoscape;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.genemania.data.normalizer.GeneCompletionProvider2;
import org.genemania.domain.Attribute;
import org.genemania.domain.AttributeGroup;
import org.genemania.domain.Gene;
import org.genemania.domain.Interaction;
import org.genemania.domain.InteractionNetwork;
import org.genemania.domain.InteractionNetworkGroup;
import org.genemania.domain.Node;
import org.genemania.domain.Organism;
import org.genemania.exception.DataStoreException;
import org.genemania.mediator.AttributeMediator;
import org.genemania.plugin.GeneMania;
import org.genemania.plugin.OneUseIterable;
import org.genemania.plugin.Strings;
import org.genemania.plugin.data.DataSet;
import org.genemania.plugin.model.AnnotationEntry;
import org.genemania.plugin.model.Group;
import org.genemania.plugin.model.Network;
import org.genemania.plugin.model.SearchResultBuilder;
import org.genemania.plugin.model.ViewState;
import org.genemania.plugin.model.ViewStateBuilder;
import org.genemania.plugin.model.impl.SearchResultImpl;
import org.genemania.plugin.model.impl.ViewStateImpl;
import org.genemania.plugin.selection.SessionManager;
import org.genemania.type.CombiningMethod;
import org.genemania.util.ChildProgressReporter;
import org.genemania.util.ProgressReporter;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingJsonFactory;

public class ResultReconstructor {
	
	private final DataSet data;
	private final Set<String> unrecognizedNodes;
	private final Set<String> unrecognizedAnnotations;
	private final CytoscapeUtils cytoscapeUtils;
	private final GeneMania plugin;
	private String version;
	
	private final Map<Node, String> nodeIds;
	private final Set<InteractionNetworkGroup> enabledGroups;
	private final Map<String, Set<Object>> sourceNetworksByEdgeId;
	private final Map<String, Set<Object>> sourceNetworksByNodeId;
	private final Map<String, Set<String>> edgeIdsByGroup;
	
	private final Map<String, Node> nodeCache;

	public ResultReconstructor(DataSet data, GeneMania plugin, CytoscapeUtils cytoscapeUtils) {
		this.data = data;
		this.plugin = plugin;
		this.cytoscapeUtils = cytoscapeUtils;
		
		unrecognizedNodes = new HashSet<>();
		unrecognizedAnnotations = new HashSet<>();
		
		nodeIds = new HashMap<>();
		enabledGroups = new HashSet<>();
		sourceNetworksByEdgeId = new HashMap<>();
		sourceNetworksByNodeId = new HashMap<>();
		edgeIdsByGroup = new HashMap<>();
		
		nodeCache = new HashMap<>();
	}
	
	public Set<String> getUnrecognizedNodes() {
		return Collections.unmodifiableSet(unrecognizedNodes);
	}
	
	public String getVersion() {
		return version;
	}
	
	void addEdgeIdForGroup(String name, String edgeId) {
		Set<String> edgeIds = edgeIdsByGroup.get(name);
		
		if (edgeIds == null) {
			edgeIds = new HashSet<>();
			edgeIdsByGroup.put(name, edgeIds);
		}
		
		edgeIds.add(edgeId);
	}

	void addSourceNetworkForNode(String nodeId, Object network) {
		Set<Object> networks = sourceNetworksByNodeId.get(nodeId);
		
		if (networks == null) {
			networks = new HashSet<>();
			sourceNetworksByNodeId.put(nodeId, networks);
		}
		
		networks.add(network);
	}
	
	void addSourceNetworkForEdge(String edgeId, Object network) {
		Set<Object> networks = sourceNetworksByEdgeId.get(edgeId);
		
		if (networks == null) {
			networks = new HashSet<>();
			sourceNetworksByEdgeId.put(edgeId, networks);
		}
		
		networks.add(network);
	}
	
	public ViewState reconstructCache(CyNetwork cyNetwork, ProgressReporter progress)
			throws DataStoreException, IOException {
		progress.setStatus(Strings.resultReconstructor_status);
		int currentProgress = 0;
		progress.setMaximumProgress(6);
		progress.setProgress(currentProgress);
		
		// Check data version
		String dataVersion = cytoscapeUtils.getDataVersion(cyNetwork);
		
		if (dataVersion == null)
			return null;
		
		version = dataVersion;
		boolean isWeb = dataVersion.endsWith(CytoscapeUtils.WEB_VERSION_TAG);
		
		if (isWeb || !dataVersion.equals(data.getVersion().toString()))
			return null;
		
		// Locate organism
		final Organism organism = reconstructOrganism(cyNetwork);
		
		if (organism == null)
			return null;

		SearchResultBuilder builder = new SearchResultImpl();
		builder.setOrganism(organism);
		builder.setCombiningMethod(reconstructCombiningMethod(cyNetwork));
		builder.setGeneSearchLimit(reconstructGeneSearchLimit(cyNetwork));
		builder.setAttributeSearchLimit(reconstructAttributeSearchLimit(cyNetwork));
		progress.setProgress(++currentProgress);
		
		ChildProgressReporter childProgress = new ChildProgressReporter(progress);
		reconstructNodeCache(builder, cyNetwork, organism, childProgress);
		childProgress.close();
		
		childProgress = new ChildProgressReporter(progress);
		reconstructNetworkCache(builder, cyNetwork, organism, childProgress);
		childProgress.close();
		
		childProgress = new ChildProgressReporter(progress);
		reconstructEnrichmentCache(builder, cyNetwork, organism, childProgress);
		childProgress.close();
		
		childProgress = new ChildProgressReporter(progress);
		reconstructAttributeCache(builder, cyNetwork, organism, childProgress);
		childProgress.close();
		
		ViewStateBuilder viewStateBuilder = new ViewStateImpl(builder);
		childProgress = new ChildProgressReporter(progress);
		reconstructViewState(viewStateBuilder, childProgress);
		childProgress.close();
		
		SessionManager manager = plugin.getSessionManager();
		cytoscapeUtils.registerSelectionListener(cyNetwork, manager, plugin);

		return viewStateBuilder.build();
	}
	
	private Organism reconstructOrganism(CyNetwork network) throws DataStoreException {
		String organismName = cytoscapeUtils.getAttribute(network, network, CytoscapeUtils.ORGANISM_NAME_ATTRIBUTE,
				String.class);
		
		if (organismName == null)
			return null;
		
		Collection<Organism> allOrganisms = data.getMediatorProvider().getOrganismMediator().getAllOrganisms();
	
		for (Organism organism : allOrganisms) {
			if (organismName.equals(organism.getName()))
				return organism;
		}
		
		return null;
	}

	private void reconstructViewState(ViewStateBuilder builder, ProgressReporter progress) {
		progress.setDescription(Strings.resultReconstructorViewState_description);
		int maximum = 0;
		maximum += nodeIds.size();
		maximum += enabledGroups.size();
		maximum += builder.getAllGroups().size();
		maximum += sourceNetworksByEdgeId.size();
		maximum += sourceNetworksByNodeId.size();
		maximum += edgeIdsByGroup.size();
		progress.setMaximumProgress(maximum);
		
		int count = 0;
		
		for (Entry<Node, String> entry : nodeIds.entrySet()) {
			builder.addNode(entry.getKey(), entry.getValue());
			progress.setProgress(++count);
		}
		
		for (InteractionNetworkGroup group : enabledGroups) {
			Group<?, ?> group2 = builder.getGroup(group.getName());
			builder.setEnabled(group2, true);
			progress.setProgress(++count);
		}

		Map<Object, Network<?>> networksByModel = new HashMap<>();
		
		for (Group<?, ?> group : builder.getAllGroups()) {
			for (Network<?> network : group.getNetworks()) {
				networksByModel.put(network.getModel(), network);
			}
			
			progress.setProgress(++count);
		}
		
		for (Entry<String, Set<Object>> entry : sourceNetworksByEdgeId.entrySet()) {
			for (Object model : entry.getValue()) {
				Network<?> network = networksByModel.get(model);
				builder.addSourceNetworkForEdge(entry.getKey(), network);
			}
			
			progress.setProgress(++count);
		}
		
		for (Entry<String, Set<Object>> entry : sourceNetworksByNodeId.entrySet()) {
			for (Object model : entry.getValue()) {
				Network<?> network = networksByModel.get(model);
				builder.addSourceNetworkForNode(entry.getKey(), network);
			}
			
			progress.setProgress(++count);
		}

		for (Entry<String, Set<String>> entry : edgeIdsByGroup.entrySet()) {
			Group<?, ?> group = builder.getGroup(entry.getKey());
			
			for (String edgeId : entry.getValue())
				builder.addEdge(group, edgeId);
			
			progress.setProgress(++count);
		}
	}

	@SuppressWarnings("unchecked")
	private void reconstructEnrichmentCache(SearchResultBuilder builder, CyNetwork cyNetwork, Organism targetOrganism,
			ProgressReporter progress) throws IOException {
		progress.setDescription(Strings.resultReconstructorEnrichmentCache_description);
		String rawAnnotations = cytoscapeUtils.getAttribute(cyNetwork, cyNetwork, CytoscapeUtils.ANNOTATIONS_ATTRIBUTE, String.class);
		
		if (rawAnnotations == null)
			return;
		
		Map<Long, Collection<AnnotationEntry>> annotationsByNode = new HashMap<>();
		Map<String, AnnotationEntry> annotationsByCategory = new HashMap<>();
		JsonFactory factory = new MappingJsonFactory();
		JsonParser parser = factory.createJsonParser(rawAnnotations);
		JsonNode root = parser.readValueAsTree();
		
		for (JsonNode node : new OneUseIterable<JsonNode>(root.elements())) {
			AnnotationEntry entry = new AnnotationEntry(node);
			annotationsByCategory.put(entry.getName(), entry);
		}
		
		GeneCompletionProvider2 completionProvider = data.getCompletionProvider(targetOrganism);
		
		for (CyNode node : cyNetwork.getNodeList()) {
			String symbol = cytoscapeUtils.getAttribute(cyNetwork, node, CytoscapeUtils.GENE_NAME_ATTRIBUTE, String.class);
			
			if (symbol == null)
				continue;
			
			Gene gene = completionProvider.getGene(symbol);
			
			if (gene == null) {
				unrecognizedNodes.add(symbol);
				continue;
			}
			
			long nodeId = gene.getNode().getId();			
			List<String> annotations = cytoscapeUtils.getAttribute(cyNetwork, node, CytoscapeUtils.ANNOTATION_ID_ATTRIBUTE, List.class);
			
			if (annotations == null)
				continue;
			
			for (String annotation : annotations) {
				AnnotationEntry entry = annotationsByCategory.get(annotation);

				if (entry == null) {
					unrecognizedAnnotations.add(annotation);
					continue;
				}

				Collection<AnnotationEntry> entries = annotationsByNode.get(nodeId);
				
				if (entries == null) {
					entries = new HashSet<>();
					annotationsByNode.put(nodeId, entries);
				}
				
				entries.add(entry);
			}
		}
		
		builder.setEnrichment(annotationsByNode);
	}

	private void reconstructNetworkCache(SearchResultBuilder builder, CyNetwork cyNetwork, Organism organism,
			ProgressReporter progress) throws IOException {
		progress.setDescription(Strings.resultReconstructorNetworkCache_description);
		// Reconstruct edge and network group cache
		Map<Long, InteractionNetworkGroup> allGroupsByNetwork = new HashMap<>();
		Map<String, InteractionNetworkGroup> allGroupsByName = new HashMap<>();
		Map<String, InteractionNetwork> allNetworksByName = new HashMap<>();
		
		Collection<InteractionNetworkGroup> groups = organism.getInteractionNetworkGroups();
		
		for (InteractionNetworkGroup group : groups) {
			for (InteractionNetwork network2 : (Collection<InteractionNetwork>) group.getInteractionNetworks()) {
				allNetworksByName.put(String.format("%s|%s", group.getName(), network2.getName()), network2); //$NON-NLS-1$
				allGroupsByNetwork.put(network2.getId(), group);
			}
			
			// Remove all networks from group; populate later with only relevant networks.
			group.setInteractionNetworks(new HashSet<>());
			allGroupsByName.put(group.getName(), group);
		}
		
		Map<InteractionNetwork, Double> networkWeights = reconstructNetworkWeights(cyNetwork, allNetworksByName);
		Map<InteractionNetwork, Collection<Interaction>> allInteractions = reconstructSourceNetworks(builder, organism, cyNetwork, allNetworksByName, allGroupsByName, progress);
		Map<Long, InteractionNetworkGroup> groupsByNetwork = new HashMap<>();
		
		// Link interactions to model
		for (InteractionNetwork network : networkWeights.keySet()) {
			Collection<Interaction> interactions = allInteractions.get(network);
			if (interactions == null) {
				interactions = Collections.emptySet();
				allInteractions.put(network, interactions);
			}
			network.setInteractions(interactions);
			long networkId = network.getId();
			InteractionNetworkGroup group = allGroupsByNetwork.get(networkId);
			groupsByNetwork.put(networkId, group);
			
			// Reconnect network to group.
			group.getInteractionNetworks().add(network);
		}
		
		builder.setGroups(groupsByNetwork);
		builder.setNetworkWeights(networkWeights);
	}
	
	@SuppressWarnings("unchecked")
	private Map<InteractionNetwork, Collection<Interaction>> reconstructSourceNetworks(SearchResultBuilder builder,
			Organism organism, CyNetwork cyNetwork, Map<String, InteractionNetwork> allNetworksByName,
			Map<String, InteractionNetworkGroup> allGroupsByName, ProgressReporter progress) {
		progress.setDescription(Strings.resultReconstructorSourceNetworks_description);
		Map<InteractionNetwork, Collection<Interaction>> allInteractions = new HashMap<>();
		Map<Long, Node> allNodes = new HashMap<>();
		
		GeneCompletionProvider2 completionProvider = data.getCompletionProvider(organism);
		Collection<CyEdge> edges = cyNetwork.getEdgeList();
		progress.setMaximumProgress(edges.size());
		int count = 0;
		
		for (CyEdge cyEdge : edges) {
			progress.setProgress(++count);
			String groupName = cytoscapeUtils.getAttribute(cyNetwork, cyEdge, CytoscapeUtils.NETWORK_GROUP_NAME_ATTRIBUTE, String.class);
			InteractionNetworkGroup group = allGroupsByName.get(groupName);
			
			if (group == null)
				continue;
			
			enabledGroups.add(group);
			
			List<String> sourceNetworks = cytoscapeUtils.getAttribute(cyNetwork, cyEdge, CytoscapeUtils.NETWORK_NAMES_ATTRIBUTE, List.class);
			List<Double> networkWeights = cytoscapeUtils.getAttribute(cyNetwork, cyEdge, CytoscapeUtils.RAW_WEIGHTS_ATTRIBUTE, List.class);
			
			if (sourceNetworks == null || networkWeights == null)
				continue;
			
			String edgeId = cytoscapeUtils.getIdentifier(cyNetwork, cyEdge);
			
			for (int i = 0; i < sourceNetworks.size(); i++) {
				String name = sourceNetworks.get(i);
				double weight = networkWeights.get(i);
				InteractionNetwork key = allNetworksByName.get(String.format("%s|%s", groupName, name)); //$NON-NLS-1$
				
				if (key != null) {
					Interaction interaction = new Interaction();
					Node fromNode = getNode(cyNetwork, allNodes, cyEdge.getSource(), completionProvider);
					Node toNode = getNode(cyNetwork, allNodes, cyEdge.getTarget(), completionProvider);
					
					if (fromNode == null || toNode == null) 
						continue;
					
					interaction.setFromNode(fromNode);
					interaction.setToNode(toNode);
					
					interaction.setWeight((float) weight);
					Collection<Interaction> interactions = allInteractions.get(key);
					
					if (interactions == null) {
						interactions = new HashSet<>();
						allInteractions.put(key, interactions);
					}
					
					interactions.add(interaction);
					addSourceNetworkForEdge(edgeId, key);
				}
			}
			
			addEdgeIdForGroup(group.getName(), edgeId);
		}
		
		return allInteractions;
	}

	private Map<InteractionNetwork, Double> reconstructNetworkWeights(CyNetwork cyNetwork,
			Map<String, InteractionNetwork> allNetworksByName) throws IOException {
		HashMap<InteractionNetwork, Double> weights = new HashMap<>();
		String rawNetworks = cytoscapeUtils.getAttribute(cyNetwork, cyNetwork, CytoscapeUtils.NETWORKS_ATTRIBUTE, String.class);
		
		JsonFactory factory = new MappingJsonFactory();
		JsonParser parser = factory.createJsonParser(rawNetworks);
		JsonNode root = parser.readValueAsTree();
		
		for (JsonNode node : new OneUseIterable<JsonNode>(root.elements())) {
			String groupName = node.get("group").textValue(); //$NON-NLS-1$
			String name = node.get("name").textValue(); //$NON-NLS-1$
			double weight = node.get("weight").doubleValue(); //$NON-NLS-1$
			InteractionNetwork network = allNetworksByName.get(String.format("%s|%s", groupName, name)); //$NON-NLS-1$
			
			if (network == null) // TODO: Keep track of networks that are no longer available
				continue;
			
			weights.put(network, weight);
		}
		
		return weights;
	}

	private void reconstructAttributeCache(SearchResultBuilder builder, CyNetwork cyNetwork, Organism organism,
			ProgressReporter progress) {
		progress.setDescription(Strings.resultReconstructorAttributeCache_description);
		Map<String, Attribute> attributesByName = new HashMap<>();
		Map<Long, AttributeGroup> groupsByAttribute = new HashMap<>();
		AttributeMediator mediator = data.getMediatorProvider().getAttributeMediator();
		
		for (AttributeGroup group : mediator.findAttributeGroupsByOrganism(organism.getId())) {
			for (Attribute attribute : mediator.findAttributesByGroup(organism.getId(), group.getId())) {
				attributesByName.put(attribute.getName(), attribute);
				long id = attribute.getId();
				groupsByAttribute.put(id, group);
			}
		}
		
		builder.setGroupsByAttribute(groupsByAttribute);
		
		Map<Long, Collection<Attribute>> attributesByNode = new HashMap<>();
		Map<Attribute, Double> weights = new HashMap<>();
		
		GeneCompletionProvider2 completionProvider = data.getCompletionProvider(organism);

		// Find each attribute node...
		for (CyNode cyNode : cyNetwork.getNodeList()) {
			String type = cytoscapeUtils.getAttribute(cyNetwork, cyNode, CytoscapeUtils.NODE_TYPE_ATTRIBUTE, String.class);
			
			if (!CytoscapeUtils.NODE_TYPE_ATTRIBUTE_NODE.equals(type))
				continue;
			
			String name = cytoscapeUtils.getAttribute(cyNetwork, cyNode, CytoscapeUtils.GENE_NAME_ATTRIBUTE, String.class);
			Double weight = cytoscapeUtils.getAttribute(cyNetwork, cyNode, CytoscapeUtils.SCORE_ATTRIBUTE, Double.class);
			Attribute attribute = attributesByName.get(name);
			
			if (attribute == null)
				continue;
			
			weights.put(attribute, weight);
			
			// ...and track down its neighbours.
			for (CyNode cyNode2 : cytoscapeUtils.getNeighbours(cyNode, cyNetwork)) {
				String symbol = cytoscapeUtils.getAttribute(cyNetwork, cyNode2, CytoscapeUtils.GENE_NAME_ATTRIBUTE, String.class);
				
				if (symbol == null)
					continue;
				
				Gene gene = completionProvider.getGene(symbol);
				
				if (gene == null)
					continue;
				
				long nodeId = gene.getNode().getId();
				Collection<Attribute> attributes = attributesByNode.get(nodeId);
				
				if (attributes == null) {
					attributes = new HashSet<>();
					attributesByNode.put(nodeId, attributes);
				}
				
				attributes.add(attribute);
			}
			
			addSourceNetworkForNode(cytoscapeUtils.getIdentifier(cyNetwork, cyNode), attribute);
		}
		
		// Find each attribute edge
		for (CyEdge cyEdge : cyNetwork.getEdgeList()) {
			String name = cytoscapeUtils.getAttribute(cyNetwork, cyEdge, CytoscapeUtils.ATTRIBUTE_NAME_ATTRIBUTE, String.class);
			Attribute attribute = attributesByName.get(name);
			
			if (attribute == null)
				continue;
			
			String edgeId = cytoscapeUtils.getIdentifier(cyNetwork, cyEdge);
			AttributeGroup group = builder.getAttributeGroup(attribute.getId());
			addSourceNetworkForEdge(edgeId, attribute);
			addEdgeIdForGroup(group.getName(), edgeId);
		}
		
		builder.setAttributes(attributesByNode);
		builder.setAttributeWeights(weights);
	}

	private void reconstructNodeCache(SearchResultBuilder resultBuilder, CyNetwork cyNetwork, Organism organism, ProgressReporter progress) {
		progress.setDescription(Strings.resultReconstructorNodeCache_description);
		// Reconstruct node cache
		Map<Gene, Double> geneScores = new HashMap<>();
		Map<Long, Gene> queryGenes = new HashMap<>();
	
		GeneCompletionProvider2 completionProvider = data.getCompletionProvider(organism);
		Collection<CyNode> nodes = cyNetwork.getNodeList();
		progress.setMaximumProgress(nodes.size());
		int count = 0;
		
		for (CyNode cyNode : nodes) {
			progress.setProgress(++count);
			String symbol = cytoscapeUtils.getAttribute(cyNetwork, cyNode, CytoscapeUtils.GENE_NAME_ATTRIBUTE, String.class);
			
			if (symbol == null)
				continue;
			
			Gene gene = completionProvider.getGene(symbol);
			
			if (gene == null) {
				unrecognizedNodes.add(symbol);
				continue;
			}
			
			Double score = cytoscapeUtils.getAttribute(cyNetwork, cyNode, CytoscapeUtils.SCORE_ATTRIBUTE, Double.class);
			
			if (score == null) {
				unrecognizedNodes.add(symbol);
				continue;
			}
			
			geneScores.put(gene, score);
			String type = cytoscapeUtils.getAttribute(cyNetwork, cyNode, CytoscapeUtils.NODE_TYPE_ATTRIBUTE, String.class);
			Node node = gene.getNode();
			
			if (type == null) {
				unrecognizedNodes.add(symbol);
				continue;
			}
			
			if (CytoscapeUtils.NODE_TYPE_QUERY.equals(type))
				queryGenes.put(node.getId(), gene);
			
			nodeIds.put(node, cytoscapeUtils.getIdentifier(cyNetwork, cyNode));
		}
		
		resultBuilder.setGeneScores(geneScores);
		resultBuilder.setSearchQuery(queryGenes);
	}

	private CombiningMethod reconstructCombiningMethod(CyNetwork cyNetwork) {
		String combiningMethod = cytoscapeUtils.getAttribute(cyNetwork, cyNetwork, CytoscapeUtils.COMBINING_METHOD_ATTRIBUTE, String.class);
		
		if (combiningMethod == null)
			return null;
		
		return CombiningMethod.fromCode(combiningMethod);		
	}
	
	private int reconstructGeneSearchLimit(CyNetwork cyNetwork) {
		return cytoscapeUtils.getAttribute(cyNetwork, cyNetwork, CytoscapeUtils.GENE_SEARCH_LIMIT_ATTRIBUTE, Integer.class);
	}

	private int reconstructAttributeSearchLimit(CyNetwork cyNetwork) {
		return cytoscapeUtils.getAttribute(cyNetwork, cyNetwork, CytoscapeUtils.ATTRIBUTE_SEARCH_LIMIT_ATTRIBUTE, Integer.class);
	}

	Node getNode(CyNetwork network, Map<Long, Node> allNodes, CyNode source, GeneCompletionProvider2 completionProvider) {
		String symbol = cytoscapeUtils.getAttribute(network, source, CytoscapeUtils.GENE_NAME_ATTRIBUTE, String.class);
		
		if (symbol == null)
			return null;
		
		Node node = nodeCache.get(symbol);
		
		if (node != null)
			return node;
		
		Long nodeId = completionProvider.getNodeId(symbol);
		
		if (nodeId == null)
			return null;
		
		node = allNodes.get(nodeId);
		
		if (node != null) {
			nodeCache.put(symbol, node);
			return node;
		}
		
		Gene gene = completionProvider.getGene(symbol);
		
		if (gene == null)
			return null;
		
		node = gene.getNode();
		nodeCache.put(symbol, node);
		
		return node;
	}
}
