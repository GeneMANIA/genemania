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
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.beanutils.BeanUtils;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.genemania.domain.Attribute;
import org.genemania.domain.AttributeGroup;
import org.genemania.domain.Gene;
import org.genemania.domain.GeneNamingSource;
import org.genemania.domain.Interaction;
import org.genemania.domain.InteractionNetwork;
import org.genemania.domain.InteractionNetworkGroup;
import org.genemania.domain.Node;
import org.genemania.domain.Tag;
import org.genemania.plugin.LogUtils;
import org.genemania.plugin.NetworkUtils;
import org.genemania.plugin.Strings;
import org.genemania.plugin.model.AnnotationEntry;
import org.genemania.plugin.model.Group;
import org.genemania.plugin.model.Network;
import org.genemania.plugin.model.SearchResult;
import org.genemania.plugin.model.ViewState;
import org.genemania.plugin.model.ViewStateBuilder;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.MappingJsonFactory;

public abstract class AbstractCytoscapeUtils implements CytoscapeUtils {
	
	private static final String EDGE_TYPE_INTERACTION = "interaction"; //$NON-NLS-1$
	
	protected static final double MINIMUM_NODE_SIZE = 10;
	protected static final double MAXIMUM_NODE_SIZE = 40;
	protected static final double MINIMUM_EDGE_WIDTH = 1;
	protected static final double MAXIMUM_EDGE_WIDTH = 6;
	
	protected static final int DEF_EDGE_TRANSPARENCY = 140;

	private final Map<String, AttributeHandler> attributeHandlerRegistry = createHandlerRegistry();
	
	protected final NetworkUtils networkUtils;

	public AbstractCytoscapeUtils(NetworkUtils networkUtils) {
		this.networkUtils = networkUtils;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void expandAttributes(CyNetwork cyNetwork, ViewState options, List<String> attributes) {
		if (attributes.isEmpty())
			return;
		
		for (CyEdge edge : cyNetwork.getEdgeList()) {
			String edgeId = getIdentifier(cyNetwork, edge);
			Set<Network<?>> networks = options.getNetworksByEdge(edgeId);
			List<String> networkNames = getAttribute(cyNetwork, edge, NETWORK_NAMES_ATTRIBUTE, List.class);
			
			for (String attribute : attributes) {
				List<Object> values = new ArrayList<>();
				AttributeHandler handler = attributeHandlerRegistry.get(attribute);
				
				for (String networkName : networkNames) {
					InteractionNetwork network = findNetwork(networkName, networks);
					values.add(handler.getValue(network));
				}
				
				setAttribute(cyNetwork, edge, attribute, values);
			}
		}
	}
	
	private InteractionNetwork findNetwork(String networkName, Set<Network<?>> networks) {
		for (Network<?> network : networks) {
			InteractionNetwork adapted = network.adapt(InteractionNetwork.class);
			
			if (adapted == null)
				continue;
			
			if (adapted.getName().equals(networkName))
				return adapted;
		}
		
		return null;
	}
	
	interface AttributeHandler {
		
		public abstract Object getValue(InteractionNetwork network);
	}
	
	static class TagAttributeHandler implements AttributeHandler {
		@Override
		public Object getValue(InteractionNetwork network) {
			StringBuilder builder = new StringBuilder();
			
			for (Tag tag : network.getTags()) {
				if (builder.length() > 0)
					builder.append("|"); //$NON-NLS-1$
				builder.append(tag.getName());
			}
			
			return builder.toString();
		}
	}
	
	static class MetadataAttributeHandler implements AttributeHandler {
		private String name;

		public MetadataAttributeHandler(String attributeName) {
			name = attributeName;
		}
		
		@Override
		public Object getValue(InteractionNetwork network) {
			try {
				return BeanUtils.getProperty(network.getMetadata(), name);
			} catch (IllegalAccessException e) {
				return null;
			} catch (InvocationTargetException e) {
				return null;
			} catch (NoSuchMethodException e) {
				return null;
			}
		}
	}
	
	private Map<String, AttributeHandler> createHandlerRegistry() {
		Map<String, AttributeHandler> map = new HashMap<>();
		map.put(TAGS, new TagAttributeHandler());
		
		for (String name : new String[] {
			AUTHORS,
			INTERACTION_COUNT,
			PUBMED_ID,
			PROCESSING_DESCRIPTION,
			PUBLICATION_NAME,
			YEAR_PUBLISHED,
			SOURCE,
			SOURCE_URL,
			TITLE,
			URL,
		}) {
			map.put(name, new MetadataAttributeHandler(name));
		}
		
		return map;
	}
	
	/**
	 * Returns the <code>CyNode</code> that corresponds to the given <code>Node</code>.
	 * If the <code>CyNode</code> does not already exist, a new one is created.
	 */
	@Override
	public CyNode getNode(CyNetwork network, Node node, final String preferredSymbol) {
		String id = getNodeId(network, node);
		CyNode target = getNode(id, network);
		
		if (target != null)
			return target;
		
		String name = null;
		
		if (preferredSymbol == null) {
			Gene gene = networkUtils.getPreferredGene(node);
			
			if (gene == null)
				name = Strings.missingGeneName;
			else
				name = gene.getSymbol();
		} else {
			name = preferredSymbol;
		}

		target = createNode(id, network);
		
		if (name != null)
			setAttribute(network, target, GENE_NAME_ATTRIBUTE, name);
		if (preferredSymbol != null)
			setAttribute(network, target, QUERY_TERM_ATTRIBUTE, preferredSymbol);
		
		exportSynonyms(network, target, node);
		
		return target;
	}
	
	protected String getNodeId(CyNetwork network, Node node) {
		return String.format("%s-%s", filterTitle(getTitle(network)), node.getName()); //$NON-NLS-1$
	}
	
	protected String getNodeId(CyNetwork network, Attribute attribute) {
		return String.format("%s-%s", filterTitle(getTitle(network)), attribute.getName()); //$NON-NLS-1$
	}

	private String filterTitle(String title) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0 ; i < title.length(); i++) {
			char character = title.charAt(i);
			if (Character.isLetterOrDigit(character)) {
				builder.append(character);
			} else {
				builder.append("_"); //$NON-NLS-1$
			}
		}
		return builder.toString();
	}

	private void exportSynonyms(CyNetwork cyNetwork, CyNode cyNode, Node node) {
		Collection<Gene> genes = node.getGenes();
		
		for (Gene gene : genes) {
			String name = null;
			
			try {
				GeneNamingSource source = gene.getNamingSource();
				name = source != null ? source.getName() : null;
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
			
			if (name != null)
				setAttribute(cyNetwork, cyNode, name, gene.getSymbol());
		}
	}

	protected abstract CyNode getNode(String id, CyNetwork network);
	protected abstract CyNode createNode(String id, CyNetwork network);
	protected abstract CyNetwork createNetwork(String title);
	protected abstract CyEdge getEdge(CyNode from, CyNode to, String type, String label, CyNetwork network);
	protected abstract CyEdge getEdge(String id, CyNetwork network);

	/**
	 * Creates a Cytoscape network from the interaction network given by
	 * <code>RelatedResult</code>.
	 * 
	 * @param name the name of the new <code>CyNetwork</code>
	 * @param result the results from running the GeneMANIA algorithm
	 * @param queryGenes the genes used to 
	 * @return
	 */
	@Override
	public CyNetwork createNetwork(String name, String dataVersion, SearchResult res, ViewStateBuilder builder,
			EdgeAttributeProvider attributeProvider) {
		CyNetwork currentNetwork = createNetwork(name);
		setAttribute(currentNetwork, currentNetwork, TYPE_ATTRIBUTE, GENEMANIA_NETWORK_TYPE);
		setAttribute(currentNetwork, currentNetwork, ORGANISM_NAME_ATTRIBUTE, res.getOrganism().getName());
		setAttribute(currentNetwork, currentNetwork, NETWORKS_ATTRIBUTE, serializeNetworks(res));
		setAttribute(currentNetwork, currentNetwork, COMBINING_METHOD_ATTRIBUTE, res.getCombiningMethod().getCode());
		setAttribute(currentNetwork, currentNetwork, GENE_SEARCH_LIMIT_ATTRIBUTE, res.getGeneSearchLimit());
		setAttribute(currentNetwork, currentNetwork, ATTRIBUTE_SEARCH_LIMIT_ATTRIBUTE, res.getAttributeSearchLimit());
		setAttribute(currentNetwork, currentNetwork, ANNOTATIONS_ATTRIBUTE, serializeAnnotations(res));
		
		if (dataVersion != null)
			setAttribute(currentNetwork, currentNetwork, DATA_VERSION_ATTRIBUTE, dataVersion);

		for (Group<?, ?> group : builder.getAllGroups()) {
			Group<InteractionNetworkGroup, InteractionNetwork> adapted = group.adapt(InteractionNetworkGroup.class, InteractionNetwork.class);
			
			if (adapted == null)
				continue;
			
			for (Network<InteractionNetwork> network : adapted.getNetworks()) {
				Collection<Interaction> sourceInteractions = network.getModel().getInteractions();
				
				if (sourceInteractions == null || sourceInteractions.size() == 0)
					continue;
				
				buildGraph(currentNetwork, sourceInteractions, network, attributeProvider, res, builder);
			}
		}
		
		// Add all query genes in case they don't show up in the results
		for (Gene gene : res.getQueryGenes().values()) {
			Node node = gene.getNode();
			getNode(currentNetwork, node, getSymbol(gene));
		}
		
		// Create attributes
		Map<Long, Network<Attribute>> attributesById = new HashMap<>();
		
		for (Group<?, ?> group : builder.getAllGroups()) {
			Group<AttributeGroup, Attribute> adapted = group.adapt(AttributeGroup.class, Attribute.class);
			
			if (adapted == null)
				continue;
			
			for (Network<Attribute> network : adapted.getNetworks()) {
				attributesById.put(network.getModel().getId(), network);
			}
		}
		
		Map<Attribute, Double> weights = res.getAttributeWeights();
		
		for (Entry<Long, Collection<Attribute>> entry : res.getAttributesByNodeId().entrySet()) {
			Gene gene = res.getGene(entry.getKey());
			CyNode to = getNode(currentNetwork, gene.getNode(), null);
			
			for (Attribute attribute : entry.getValue()) {
				String id = getNodeId(currentNetwork, attribute);
				Network<Attribute> network = attributesById.get(attribute.getId());
				
				CyNode from = getNode(id, currentNetwork);
				
				if (from == null) {
					from = createNode(id, currentNetwork);
					setAttribute(currentNetwork, from, GENE_NAME_ATTRIBUTE, attribute.getName());
					setAttribute(currentNetwork, from, QUERY_TERM_ATTRIBUTE, attribute.getName());
					setAttribute(currentNetwork, from, NODE_TYPE_ATTRIBUTE, NODE_TYPE_ATTRIBUTE_NODE);
					setAttribute(currentNetwork, from, SCORE_ATTRIBUTE, weights.get(attribute));
					builder.addSourceNetworkForNode(getIdentifier(currentNetwork, from), network);
				}
				
				String edgeLabel = attribute.getName();
				CyEdge edge = getEdge(from, to, EDGE_TYPE_INTERACTION, edgeLabel, currentNetwork);
				
				AttributeGroup group = res.getAttributeGroup(attribute.getId());
				setAttribute(currentNetwork, edge, NETWORK_GROUP_NAME_ATTRIBUTE, group.getName());
				setAttribute(currentNetwork, edge, ATTRIBUTE_NAME_ATTRIBUTE, edgeLabel);
				setAttribute(currentNetwork, edge, HIGHLIGHT_ATTRIBUTE, 1);
				
				String edgeId = getIdentifier(currentNetwork, edge);
				builder.addEdge(builder.getGroup(network), edgeId);
				builder.addSourceNetworkForEdge(edgeId, network);
			}
		}

		decorateNodes(currentNetwork, res);
		
		return currentNetwork;
	}
	
	private String serializeAnnotations(SearchResult options) {
		StringWriter writer = new StringWriter();
		
		JsonFactory jsonFactory = new MappingJsonFactory();
		try {
			JsonGenerator generator = jsonFactory.createJsonGenerator(writer);
			
			generator.writeStartArray();
			List<AnnotationEntry> enrichmentSummary = options.getEnrichmentSummary();
			for (AnnotationEntry entry : enrichmentSummary) {
				generator.writeStartObject();
				generator.writeFieldName("name"); //$NON-NLS-1$
				generator.writeString(entry.getName());
				generator.writeFieldName("description"); //$NON-NLS-1$
				generator.writeString(entry.getDescription());
				generator.writeFieldName("qValue"); //$NON-NLS-1$
				generator.writeNumber(entry.getQValue());
				generator.writeFieldName("sample"); //$NON-NLS-1$
				generator.writeNumber(entry.getSampleOccurrences());
				generator.writeFieldName("total"); //$NON-NLS-1$
				generator.writeNumber(entry.getTotalOccurrences());
				generator.writeEndObject();
			}
			generator.writeEndArray();
			generator.close();
		} catch (IOException e) {
			LogUtils.log(getClass(), e);
			return ""; //$NON-NLS-1$
		}
		return writer.toString();
	}

	private String serializeNetworks(SearchResult options) {
		JsonFactory factory = new MappingJsonFactory();
		StringWriter writer = new StringWriter();
		
		try {
			JsonGenerator generator = factory.createJsonGenerator(writer);
			generator.writeStartArray();
			Map<InteractionNetwork, Double> networkWeights = options.getNetworkWeights();
			for (Entry<InteractionNetwork, Double> entry : networkWeights.entrySet()) {
				generator.writeStartObject();
				InteractionNetwork network = entry.getKey();
				generator.writeFieldName("group"); //$NON-NLS-1$
				generator.writeString(options.getInteractionNetworkGroup(network.getId()).getName());
				generator.writeFieldName("name"); //$NON-NLS-1$
				generator.writeString(network.getName());
				generator.writeFieldName("weight"); //$NON-NLS-1$
				generator.writeNumber(entry.getValue());
				generator.writeEndObject();
			}
			generator.writeEndArray();
			generator.close();
		} catch (IOException e) {
			LogUtils.log(getClass(), e);
			return ""; //$NON-NLS-1$
		}
		return writer.toString();
	}

	@SuppressWarnings("unchecked")
	private void buildGraph(CyNetwork currentNetwork, Collection<Interaction> interactions,
			Network<InteractionNetwork> network, EdgeAttributeProvider attributeProvider, SearchResult res,
			ViewStateBuilder builder) {
		Map<Long, Gene> queryGenes = res.getQueryGenes();
		InteractionNetwork model = network.getModel();
		
		for (Interaction interaction : interactions) {
			Node fromNode = interaction.getFromNode();
			CyNode from = getNode(currentNetwork, fromNode, getSymbol(queryGenes.get(fromNode.getId())));
			
			Node toNode = interaction.getToNode();
			CyNode to = getNode(currentNetwork, toNode, getSymbol(queryGenes.get(toNode.getId())));
			
			String edgeLabel = attributeProvider.getEdgeLabel(model);
			CyEdge edge = getEdge(from, to, EDGE_TYPE_INTERACTION, edgeLabel, currentNetwork);
			
			String edgeId = getIdentifier(currentNetwork, edge);
			Double rawWeight = (double) interaction.getWeight();

			builder.addSourceNetworkForEdge(edgeId, network);
			Double weight = rawWeight * network.getWeight();
			
			List<String> networkNames = getAttribute(currentNetwork, edge, NETWORK_NAMES_ATTRIBUTE, List.class);
			
			if (networkNames == null)
				networkNames = new ArrayList<>();
			
			networkNames.add(network.getName());
			setAttribute(currentNetwork, edge, NETWORK_NAMES_ATTRIBUTE, networkNames);
			
			List<Double> edgeWeights = getAttribute(currentNetwork, edge, RAW_WEIGHTS_ATTRIBUTE, List.class);
			
			if (edgeWeights == null)
				edgeWeights = new ArrayList<>();
			
			edgeWeights.add((double) interaction.getWeight());
			setAttribute(currentNetwork, edge, RAW_WEIGHTS_ATTRIBUTE, edgeWeights);

			Double oldWeight = getAttribute(currentNetwork, edge, MAX_WEIGHT_ATTRIBUTE, Double.class);
			
			if (oldWeight == null || oldWeight < weight)
				setAttribute(currentNetwork, edge, MAX_WEIGHT_ATTRIBUTE, weight);
			
			setAttribute(currentNetwork, edge, HIGHLIGHT_ATTRIBUTE, 1);
			
			for (Entry<String, Object> entry : attributeProvider.getAttributes(model).entrySet()) {
				setAttribute(currentNetwork, edge, entry.getKey(), entry.getValue());
			}
		}
	}
	
	/**
	 * Returns the symbol for the given <code>Gene</code> or <code>null</code>
	 * if the <code>Gene</code> is <code>null</code>.
	 */
	private String getSymbol(Gene gene) {
		return gene == null ? null : gene.getSymbol();
	}

	/**
	 * Decorates the nodes in the active NETWORK with the results of
	 * the GeneMANIA algorithm.  For example, scores are assigned to the nodes.
	 */
	private void decorateNodes(CyNetwork currentNetwork, SearchResult options) {
		// Assign scores.
		Map<Long, Gene> queryGenes = options.getQueryGenes();
		Map<Gene, Double> scores = options.getScores();
		
		for (Entry<Gene, Double> entry : scores.entrySet()) {
			double score = entry.getValue();
			Node node = entry.getKey().getNode();
			
			CyNode cyNode = getNode(currentNetwork, node, getSymbol(queryGenes.get(node)));
	
			setAttribute(currentNetwork, cyNode, LOG_SCORE_ATTRIBUTE, Math.log(score));
			setAttribute(currentNetwork, cyNode, SCORE_ATTRIBUTE, score);
			final String type;
			
			if (queryGenes.containsKey(node.getId()))
				type = NODE_TYPE_QUERY;
			else
				type = NODE_TYPE_RESULT;
			
			Collection<AnnotationEntry> nodeAnnotations = options.getAnnotations(node.getId());
			
			if (nodeAnnotations != null) {
				List<String> annotationIds = new ArrayList<>();
				List<String> annotationNames = new ArrayList<>();
				
				for (AnnotationEntry annotation : nodeAnnotations) {
					annotationIds.add(annotation.getName());
					annotationNames.add(annotation.getDescription());
				}
				
				setAttribute(currentNetwork, cyNode, ANNOTATION_ID_ATTRIBUTE, annotationIds);
				setAttribute(currentNetwork, cyNode, ANNOTATION_NAME_ATTRIBUTE, annotationNames);
			}
			
			setAttribute(currentNetwork, cyNode, NODE_TYPE_ATTRIBUTE, type);
		}
	}

	@Override
	public void setHighlighted(ViewState options, CyNetwork cyNetwork, boolean visible) {
		for (CyEdge edge : cyNetwork.getEdgeList()) {
			String groupName = getAttribute(cyNetwork, edge, NETWORK_GROUP_NAME_ATTRIBUTE, String.class);
			
			if (groupName == null)
				continue;
			
			Group<?, ?> group = options.getGroup(groupName);
			
			if (group == null)
				continue;
			
			Integer value = options.isEnabled(group) || visible ? 1 : 0;
			setAttribute(cyNetwork, edge, HIGHLIGHT_ATTRIBUTE, value);
		}
		
		updateVisualStyles(cyNetwork);
		repaint();
	}
	
	protected String getVisualStyleName(CyNetwork network) {
		return getTitle(network).replace(".", ""); //$NON-NLS-1$ //$NON-NLS-2$;
	}
	
	@Override
	public void setHighlight(ViewState config, Group<?, ?> source, CyNetwork network, boolean selected) {
		Set<String> edgeIds = config.getEdgeIds(source);
		
		if (edgeIds == null)
			return;
		
		config.setEnabled(source, selected);
		
		if (selected) {
			for (String edgeId : edgeIds) {
				CyEdge edge = getEdge(edgeId, network);
				setAttribute(network, edge, HIGHLIGHT_ATTRIBUTE, 1);
			}
		} else {
			for (String edgeId : edgeIds) {
				CyEdge edge = getEdge(edgeId, network);
				setAttribute(network, edge, HIGHLIGHT_ATTRIBUTE, 0);
			}
		}
		
		updateVisualStyles(network);
		repaint();
	}
	
	@Override
	public String getIdentifier(CyNetwork network, CyIdentifiable entry) {
		return network.getRow(entry).get(CyNetwork.NAME, String.class);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <U> U getAttribute(CyNetwork network, CyIdentifiable entry, String name, Class<U> type) {
		CyRow row = network.getRow(entry);
		
		if (type.equals(List.class)) {
			CyTable table = row.getTable();
			CyColumn column = table.getColumn(name);
			
			if (column == null)
				return null;
			
			Class<?> elementType = column.getListElementType();
			return (U) row.getList(name, elementType);
		}
		
		if (type.equals(Object.class)) {
			CyTable table = row.getTable();
			CyColumn column = table.getColumn(name);
			type = (Class<U>) column.getType();
		}
		
		return row.get(name, type);
	}
	
	@Override
	public <U> void setAttribute(CyNetwork network, CyIdentifiable entry, String name, U value) {
		CyRow row = network.getRow(entry);
		CyTable table = row.getTable();
		CyColumn column = table.getColumn(name);
		
		if (column == null) {
			if (value instanceof List<?>) {
				List<?> list = (List<?>) value;
				Class<?> elementType = list.size() == 0 ? String.class : list.get(0).getClass();
				table.createListColumn(name, elementType, false);
			} else {
				table.createColumn(name, value.getClass(), false);
			}
		}
		
		row.set(name, value);
	}
	
	@Override
	public Class<?> getAttributeType(CyNetwork network, CyIdentifiable entry, String name) {
		return network.getRow(entry).getTable().getColumn(name).getType();
	}
}
