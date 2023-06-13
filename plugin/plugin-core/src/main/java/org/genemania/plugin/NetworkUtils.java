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
package org.genemania.plugin;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.genemania.data.normalizer.GeneCompletionProvider2;
import org.genemania.domain.Attribute;
import org.genemania.domain.AttributeGroup;
import org.genemania.domain.Gene;
import org.genemania.domain.GeneData;
import org.genemania.domain.GeneNamingSource;
import org.genemania.domain.Interaction;
import org.genemania.domain.InteractionNetwork;
import org.genemania.domain.InteractionNetworkGroup;
import org.genemania.domain.NetworkMetadata;
import org.genemania.domain.Node;
import org.genemania.domain.OntologyCategory;
import org.genemania.domain.Organism;
import org.genemania.domain.ResultAttribute;
import org.genemania.domain.ResultAttributeGroup;
import org.genemania.domain.ResultGene;
import org.genemania.domain.ResultInteraction;
import org.genemania.domain.ResultInteractionNetwork;
import org.genemania.domain.ResultInteractionNetworkGroup;
import org.genemania.domain.ResultOntologyCategory;
import org.genemania.domain.SearchParameters;
import org.genemania.domain.SearchResults;
import org.genemania.domain.Tag;
import org.genemania.dto.AttributeDto;
import org.genemania.dto.EnrichmentEngineResponseDto;
import org.genemania.dto.InteractionDto;
import org.genemania.dto.NetworkDto;
import org.genemania.dto.NodeDto;
import org.genemania.dto.OntologyCategoryDto;
import org.genemania.dto.RelatedGenesEngineRequestDto;
import org.genemania.dto.RelatedGenesEngineResponseDto;
import org.genemania.exception.DataStoreException;
import org.genemania.mediator.AttributeMediator;
import org.genemania.mediator.GeneMediator;
import org.genemania.mediator.NodeMediator;
import org.genemania.mediator.OntologyMediator;
import org.genemania.plugin.cytoscape.CytoscapeUtils;
import org.genemania.plugin.data.Colour;
import org.genemania.plugin.data.DataSet;
import org.genemania.plugin.data.IMediatorProvider;
import org.genemania.plugin.model.AnnotationEntry;
import org.genemania.plugin.model.Group;
import org.genemania.plugin.model.Network;
import org.genemania.plugin.model.SearchResult;
import org.genemania.plugin.model.SearchResultBuilder;
import org.genemania.plugin.model.impl.SearchResultImpl;
import org.genemania.util.GeneLinkoutGenerator;

/**
 * A collection of utility functions for converting GeneMANIA domain
 * objects to Cytoscape objects.
 */
public class NetworkUtils {
	
	public NetworkUtils() {
		
	}
	
	/**
	 * Returns a <code>Map</code> of <code>Gene</code>s for the given organism
	 * corresponding to the given gene names, keyed by the id of the node to
	 * which the <code>Gene</code> belongs. 
	 * @param geneMediator
	 * @param geneNames
	 * @param organism
	 * @return
	 */
	public Map<Long, Gene> createQueryNodes(GeneMediator geneMediator, List<String> geneNames, Organism organism) {
		try {
			List<Gene> genes = geneMediator.getGenes(geneNames, organism.getId());
			Map<Long, Gene> nodes = new HashMap<>();
			for (Gene gene : genes) {
				Node node = gene.getNode();
				nodes.put(node.getId(), gene);
			}
			return nodes;
		} catch (DataStoreException e) {
			return Collections.emptyMap();
		}
	}

	public Map<Long, Collection<Interaction>> createInteractionMap(Map<InteractionNetwork, Collection<Interaction>> sourceInteractions) {
		Map<Long, Collection<Interaction>> networks = new HashMap<>();
		
		for (Entry<InteractionNetwork, Collection<Interaction>> entry : sourceInteractions.entrySet()) {
			InteractionNetwork network = entry.getKey();
			networks.put(network.getId(), entry.getValue());
		}
		
		return networks;
	}

	/**
	 * Returns the highest ranking gene symbol for the given <code>Node</code>.
	 */
	public Gene getPreferredGene(Node node) {
		Gene best = null;
		byte bestRank = Byte.MIN_VALUE;
		var genes = node.getGenes();
		
		for (var gene : genes) {
			var namingSource = gene.getNamingSource();
			
			if (namingSource == null)
				continue;
			
			try {
				byte rank = namingSource.getRank();
				
				if (rank > bestRank) {
					best = gene;
					bestRank = rank;
				}
			} catch (Exception e) {
				if (best == null)
					best = gene;
//				e.printStackTrace();
			}
		}
		
		return best;
	}

	public double[] sortScores(Map<?, Double> scores) {
		double[] values = new double[scores.size()];
		int i = 0;
		
		for (Entry<?, Double> entry : scores.entrySet()) {
			values[i] = entry.getValue();
			i++;
		}
		
		Arrays.sort(values);
		
		return values;
	}
	
	public <T> List<T> createSortedList(Map<T,Double> scoredMap) {
		ArrayList<T> list = new ArrayList<>();
		list.addAll(scoredMap.keySet());
		Collections.sort(list, new Comparator<T>() {
			@Override
			public int compare(T o1,T o2) {
				double score1 = scoredMap.get(o1);
				double score2 = scoredMap.get(o2);
				return (int) Math.signum(score2 - score1);
			}
		});
		return list;
	}
	
	public Comparator<Group<?, ?>> getNetworkGroupComparator() {
		return new Comparator<Group<?, ?>>() {
			@Override
			public int compare(Group<?, ?> group1, Group<?, ?> group2) {
				return group1.getName().compareToIgnoreCase(group2.getName());
			}
		};	
	}
	
	public Comparator<Network<?>> getNetworkComparator() {
		return new Comparator<Network<?>>() {
			@Override
			public int compare(Network<?> network1, Network<?> network2) {
				return network1.getName().compareToIgnoreCase(network2.getName());
			}
		};	
	}
	
	public Color getNetworkColor(DataSet data, Group<?, ?> group) {
		Colour colour = data != null ? data.getColor(group.getCode()) : null;
		Color color = colour != null ? new Color(colour.getRgb()) : CytoscapeUtils.NETWORK_COLORS.get(group.getName());
		
		return color != null ? color : CytoscapeUtils.DEFAULT_NETWORK_COLOUR;
	}
	
	public String buildDescriptionHtml(Network<?> network, Group<?, ?> group) {
		{
			InteractionNetwork adapted = network.adapt(InteractionNetwork.class);
			if (adapted != null) {
				return buildDescriptionHtml(adapted);
			}
		}
		{
			AttributeGroup adapted = network.adapt(AttributeGroup.class);
			if (adapted != null) {
				return buildDescriptionHtml(adapted);
			}
		}
		{
			Attribute adapted = network.adapt(Attribute.class);
			Group<AttributeGroup, Attribute> adaptedGroup = group.adapt(AttributeGroup.class, Attribute.class);
			if (adapted != null && adaptedGroup != null) {
				return buildDescriptionHtml(adapted, adaptedGroup.getModel());
			}
		}
		return ""; //$NON-NLS-1$
	}
	
	private String buildDescriptionHtml(Attribute attribute, AttributeGroup group) {
		StringBuilder builder = new StringBuilder();
		builder.append("<div>"); //$NON-NLS-1$
		builder.append(attribute.getDescription());
		builder.append("</div>"); //$NON-NLS-1$
		
		builder.append(String.format("<div><strong>%s</strong> ", Strings.networkDetailPanelSource_label)); //$NON-NLS-1$
		builder.append(String.format(Strings.networkDetailPanelAttribute_description, group.getDescription(), formatLink(group.getPublicationName(), group.getPublicationUrl())));
		builder.append("</div>"); //$NON-NLS-1$
		
		builder.append(String.format("<div><strong>%s</strong> ", Strings.networkDetailPanelMoreAt_label)); //$NON-NLS-1$
		builder.append(formatLink(group.getLinkoutLabel(), group.getLinkoutUrl()));
		builder.append("</div>"); //$NON-NLS-1$

		return builder.toString();
	}

	private String buildDescriptionHtml(AttributeGroup group) {
		StringBuilder builder = new StringBuilder();
		builder.append("<div>"); //$NON-NLS-1$
		builder.append(String.format(Strings.networkDetailPanelAttribute_description, group.getDescription(), formatLink(group.getPublicationName(), group.getPublicationUrl())));
		builder.append("</div>"); //$NON-NLS-1$
		return builder.toString();
	}

	String buildDescriptionHtml(InteractionNetwork network) {
		StringBuilder builder = new StringBuilder();
		NetworkMetadata data = network.getMetadata();
		
		if (data == null) {
			// No metadata; fallback to whatever
			return network.getDescription();
		}
		
		
		// TODO: Change to match website
		String title = data.getTitle();
		if (!isEmpty(title)) {
			builder.append("<div>"); //$NON-NLS-1$
			builder.append(formatLink(title, data.getUrl()));
			builder.append(". "); //$NON-NLS-1$
			
			String authors = data.getAuthors();
			if (!isEmpty(authors)) {
				builder.append(htmlEscape(formatAuthors(authors)));
				builder.append(". "); //$NON-NLS-1$
			}
			
			String yearPublished = data.getYearPublished();
			if (!isEmpty(yearPublished)) {
				builder.append("("); //$NON-NLS-1$
				builder.append(htmlEscape(yearPublished));
				builder.append("). "); //$NON-NLS-1$
			}
			
			String publication = data.getPublicationName();
			if (!isEmpty(publication)) {
				builder.append(htmlEscape(data.getPublicationName()));
				builder.append("."); //$NON-NLS-1$
			}
			builder.append("</div>"); //$NON-NLS-1$
		}
		
		String other = data.getOther();
		if (!isEmpty(other)) {
			builder.append("<div>"); //$NON-NLS-1$
			builder.append(htmlEscape(other));
			builder.append("</div>"); //$NON-NLS-1$
		}

		String comment = data.getComment();
		if (!isEmpty(comment)) {
			builder.append(String.format("<div><strong>%s</strong> ", Strings.networkDetailPanelComment_label)); //$NON-NLS-1$
			builder.append(htmlEscape(comment));
			builder.append("</div>"); //$NON-NLS-1$
		}

		builder.append(String.format("<div><strong>%s</strong> ", Strings.networkDetailPanelSource_label)); //$NON-NLS-1$
		builder.append(String.format(Strings.networkDetailPanelSource_description, formatProcessingDescription(data.getProcessingDescription()), data.getInteractionCount(), formatLink(data.getSource(), data.getSourceUrl())));
		builder.append("</div>"); //$NON-NLS-1$
		
		Collection<Tag> tags = network.getTags();
		if (tags.size() > 0) {
			builder.append("<div>"); //$NON-NLS-1$
			builder.append(String.format("<strong>%s</strong> ", Strings.networkDetailPanelTags_label)); //$NON-NLS-1$
			int i = 0;
			for (Tag tag : tags) {
				if (i > 0) {
					builder.append(", "); //$NON-NLS-1$
				}
				builder.append(tag.getName().toLowerCase());
				i++;
			}
			builder.append("</div>"); //$NON-NLS-1$
		}
		return builder.toString();
	}

	private String formatProcessingDescription(String processingDescription) {
		return processingDescription;
	}

	private String formatAuthors(String authors) {
		String[] parts = authors.split(","); //$NON-NLS-1$
		if (parts.length == 1) {
			return parts[0];
		}
		
		return parts[0] + ", et al"; //$NON-NLS-1$
	}

	private String formatLink(String title, String url) {
		if (isEmpty(url)) {
			return htmlEscape(title);
		}
		
		return "<a href=\"" + url + "\">" + htmlEscape(title) + "</a>";  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	private String htmlEscape(String comment) {
		return comment.replaceAll("&", "&amp;").replaceAll("<", "&lt;"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}

	public String buildDescriptionReport(InteractionNetwork network) {
		StringBuilder builder = new StringBuilder();
		NetworkMetadata data = network.getMetadata();
		
		if (data == null) {
			// No metadata; fallback to whatever
			return network.getDescription();
		}
		
		builder.append(String.format(Strings.reportMethod_label, data.getProcessingDescription()));
	
		String comment = data.getComment();
		if (!isEmpty(comment)) {
			if (builder.length() > 0) {
				builder.append("|"); //$NON-NLS-1$
			}
			builder.append(comment);
		}
		
		String authors = data.getAuthors();
		if (!isEmpty(authors)) {
			if (builder.length() > 0) {
				builder.append("|"); //$NON-NLS-1$
			}
			builder.append(String.format(Strings.reportAuthors_label,authors));
		}
		
		String pubMed = data.getPubmedId();
		if (!isEmpty(pubMed)) {
			if (builder.length() > 0) {
				builder.append("|"); //$NON-NLS-1$
			}
			builder.append(String.format(Strings.reportPubMed_label, pubMed));
		}
		
		if (builder.length() > 0) {
			builder.append("|"); //$NON-NLS-1$
		}
		builder.append(String.format(Strings.reportInteraction_label, data.getInteractionCount()));

		String source = data.getSource();
		if (!isEmpty(source)) {
			if (builder.length() > 0) {
				builder.append("|"); //$NON-NLS-1$
			}
			builder.append(String.format(Strings.reportSource_label, source));
		}
		
		Collection<Tag> tags = network.getTags();
		if (tags.size() > 0) {
			if (builder.length() > 0) {
				builder.append("|"); //$NON-NLS-1$
			}
			builder.append(Strings.reportTags_label);
			int i = 0;
			for (Tag tag : tags) {
				if (i > 0) {
					builder.append(","); //$NON-NLS-1$
				}
				builder.append(tag.getName());
				i++;
			}
		}
		return builder.toString();
	}
	
	private boolean isEmpty(String string) {
		return string == null || string.length() == 0;
	}

	public String getGeneLabel(Gene gene) {
		Gene preferredGene = getPreferredGene(gene.getNode());
		
		if (preferredGene == null)
			return null;
		
		if (preferredGene.getId() == gene.getId())
			return gene.getSymbol();
		
		return String.format("%s (%s)", preferredGene.getSymbol(), gene.getSymbol()); //$NON-NLS-1$
	}

	public Collection<Interaction> computeCombinedInteractions(Map<InteractionNetwork, Collection<Interaction>> source) {
		List<Interaction> interactions = new ArrayList<>();
		Map<Long, Set<Long>> seenNodes = new HashMap<>();
		
		for (Collection<Interaction> network : source.values()) {
			for (Interaction interaction : network) {
				long fromId = interaction.getFromNode().getId();
				long toId = interaction.getToNode().getId();
				
				// Canonicalize our lookup data by ensuring the fromId is
				// smaller than the toId.
				if (fromId > toId) {
					fromId = toId;
					toId = interaction.getFromNode().getId();
				}
				
				Set<Long> toIds = seenNodes.get(fromId);
				if (toIds == null) {
					toIds = new HashSet<>();
					seenNodes.put(fromId, toIds);
					toIds.add(toId);
					interactions.add(interaction);
				} else {
					if (toIds.contains(toId)) {
						continue;
					}
					toIds.add(toId);
					interactions.add(interaction);
				}
			}
		}
		
		return interactions;
	}
	
	public Map<InteractionNetwork, Double> computeNetworkWeights(List<NetworkDto> networks,
			Map<Long, InteractionNetwork> canonicalNetworks, Map<Attribute, Double> attributeWeights) {
		double totalAttributeWeight = 0;
		
		if (attributeWeights != null) {
			for (Double weight : attributeWeights.values())
				totalAttributeWeight += weight;
		}
		
		double scaleFactor = 1 - totalAttributeWeight;
		Map<InteractionNetwork, Double> networkWeights = new HashMap<>();
		
		for (NetworkDto networkVo : networks) {
			InteractionNetwork network = canonicalNetworks.get(networkVo.getId());
			
			if (network == null) {
				network = new InteractionNetwork();
				network.setId(networkVo.getId());
			}
			
			networkWeights.put(network, networkVo.getWeight() * scaleFactor);
		}
		
		return networkWeights;
	}
	
	public Map<InteractionNetwork, Double> computeNetworkWeights(Collection<ResultInteractionNetworkGroup> resNetGroups,
			Map<Long, InteractionNetwork> canonicalNetworks, Map<Attribute, Double> attributeWeights) {
		double totalAttributeWeight = 0;
		
		if (attributeWeights != null) {
			for (Double weight : attributeWeights.values())
				totalAttributeWeight += weight;
		}
		
		double scaleFactor = 1 - totalAttributeWeight;
		Map<InteractionNetwork, Double> networkWeights = new HashMap<>();
		
		for (ResultInteractionNetworkGroup resGr : resNetGroups) {
			for (ResultInteractionNetwork resNet : resGr.getResultNetworks()) {
				InteractionNetwork network = canonicalNetworks.get(resNet.getNetwork().getId());
				
				if (network == null) {
					network = new InteractionNetwork();
					network.setId(resNet.getNetwork().getId());
				}
				
				networkWeights.put(network, resNet.getWeight() * scaleFactor);// TODO Is it necessary when results come from GeneMANIA webapp???
			}
		}
		
		return networkWeights;
	}

	public Map<Long, Gene> computeQueryGenes(List<String> geneSymbols, GeneCompletionProvider2 geneProvider) {
		Map<Long, Gene> genesByNodeId = new HashMap<>();
		
		for (String symbol : geneSymbols) {
			Gene gene = geneProvider.getGene(symbol);
			
			if (gene == null)
				continue;
			
			genesByNodeId.put(gene.getNode().getId(), gene);
		}
		
		return genesByNodeId;
	}

	/**
	 * Used by offline search.
	 */
	public SearchResult createSearchOptions(Organism organism, RelatedGenesEngineRequestDto request,
			RelatedGenesEngineResponseDto response, EnrichmentEngineResponseDto enrichmentResponse, DataSet data,
			List<String> genes) {
		SearchResultBuilder config = new SearchResultImpl();
		
		config.setOrganism(organism);
		config.setCombiningMethod(request.getCombiningMethod());
		config.setGeneSearchLimit(request.getLimitResults());
		config.setAttributeSearchLimit(request.getAttributesLimit());
		
		GeneCompletionProvider2 geneProvider = data.getCompletionProvider(organism);
		Map<Long, Gene> queryGenes = computeQueryGenes(genes, geneProvider);
		config.setSearchQuery(queryGenes);
		
		Map<Long, InteractionNetworkGroup> groupsByNetwork = computeGroupsByNetwork(response, data);
		config.setGroups(groupsByNetwork);

		IMediatorProvider provider = data.getMediatorProvider();
		NodeMediator nodeMediator = provider.getNodeMediator();
		List<NetworkDto> sourceNetworks = response.getNetworks();
		config.setGeneScores(computeGeneScores(response.getNodes(), queryGenes, organism, nodeMediator));
		
		Map<Long, InteractionNetwork> canonicalNetworks = computeCanonicalNetworks(groupsByNetwork);
		computeSourceInteractions(sourceNetworks, canonicalNetworks, organism, data);
		
		AttributeMediator attributeMediator = provider.getAttributeMediator();
		computeAttributes(config, organism, response.getAttributes(), response.getNodeToAttributes(), attributeMediator);
		
		config.setNetworkWeights(computeNetworkWeights(sourceNetworks, canonicalNetworks, config.getAttributeWeights()));
		
		if (enrichmentResponse != null)
			config.setEnrichment(processAnnotations(enrichmentResponse.getAnnotations(), data));
		
		return config.build();
	}
	
	/**
	 * Used by online search.
	 */
	public SearchResult createSearchOptions(SearchResults res) {
		SearchResultBuilder config = new SearchResultImpl();
		
		SearchParameters params = res.getParameters();
		
		if (params != null) {
			Organism organism = params.getOrganism();
			
			config.setOrganism(organism);
			config.setCombiningMethod(params.getWeighting());
			config.setGeneSearchLimit(params.getResultsSize());
			config.setAttributeSearchLimit(params.getAttributeResultsSize());
			config.setSearchQuery(params.getGenes().stream().collect(
					Collectors.toMap(
							g -> g.getNode().getId(),
							g -> g,
							(g1, g2) -> {
								// If two genes have same node id, just use the second one
								// (usually happens when two or more query genes are synonyms)
								return g2;
							}
					)));
		}
		
		Collection<ResultInteractionNetworkGroup> resNetGroups = res.getResultNetworkGroups();
		
		if (resNetGroups != null) {
			Map<Long, InteractionNetworkGroup> groupsByNetwork = computeGroupsByNetwork(resNetGroups);
			config.setGroups(groupsByNetwork);
		
			Map<Long, Node> uniqueNodes = new HashMap<>();
			sanitize(res, uniqueNodes);
			
			Map<Gene, Double> geneScores = computeGeneScores(res.getResultGenes());
			config.setGeneScores(geneScores);
			
			Map<Long, InteractionNetwork> canonicalNetworks = computeCanonicalNetworks(groupsByNetwork);
			computeSourceInteractions(resNetGroups, canonicalNetworks, uniqueNodes);
			computeAttributes(config, res);
			config.setNetworkWeights(computeNetworkWeights(resNetGroups, canonicalNetworks, config.getAttributeWeights()));
			
			if (res.getResultOntologyCategories() != null)
				config.setEnrichment(processAnnotations(res));
		}
		
		return config.build();
	}

	/**
	 * Fixes the search results by replacing duplicated nodes and genes with unique instances and
	 * setting missing attributes as required by this Cytoscape app. 
	 */
	private void sanitize(SearchResults res, Map<Long, Node> uniqueNodes) {
		Map<Long, Gene> uniqueGenes = new HashMap<>();
		
		for (ResultGene resGene : res.getResultGenes()) {
			Gene gene = resGene.getGene();
			
			if (gene != null) {
				uniqueGenes.put(gene.getId(), gene);
				Node node = gene.getNode();
				
				if (node != null)
					uniqueNodes.put(node.getId(), node);
			}
		}
		
		Collection<ResultInteractionNetworkGroup> resNetGroups = res.getResultNetworkGroups();
		
		for (ResultInteractionNetworkGroup resGr : resNetGroups) {
			for (ResultInteractionNetwork resNet : resGr.getResultNetworks()) {
				for (ResultInteraction resInter : resNet.getResultInteractions()) {
					Gene fromGene = resInter.getFromGene().getGene();
					Gene toGene = resInter.getToGene().getGene();
					
					if (fromGene != null) {
						uniqueGenes.put(fromGene.getId(), fromGene);
						Node fromNode = fromGene.getNode();
						
						if (fromNode != null)
							uniqueNodes.put(fromNode.getId(), fromNode);
					}
					if (toGene != null) {
						uniqueGenes.put(toGene.getId(), toGene);
						Node toNode = toGene.getNode();
						
						if (toNode != null)
							uniqueNodes.put(toNode.getId(), toNode);
					}
				}
			}
		}
		
		if (res.getParameters() == null)
			return;
		
		// Fixes resultGenes and Node's genes
		Map<Long, Gene> queryGenesByNode = new HashMap<>();
		
		for (Gene gene : res.getParameters().getGenes()) {
			Node node = gene.getNode();
			
			if (node != null)
				queryGenesByNode.put(node.getId(), gene);
		}
		
		for (ResultGene resGene : res.getResultGenes()) {
			Gene gene = resGene.getGene();
			
			if (gene != null) {
				gene = uniqueGenes.get(gene.getId());
				gene.setOrganism(res.getParameters().getOrganism());
				resGene.setGene(gene);
				
				Node node = uniqueNodes.get(gene.getNode().getId());
				
				if (node != null) {
					gene.setNode(node);
					// Set genes to each Node, because this relationship is missing
					// when the data comes from the web service
					Collection<Gene> geneList = node.getGenes();
					
					if (geneList == null)
						node.setGenes(geneList = new ArrayList<>());
					
					geneList.add(gene);
					
					// Also add genes that were deserialized from the query parameters
					// (they usually contain the input gene names)
					Gene queryGene = queryGenesByNode.get(node.getId());
					
					if (queryGene != null && queryGene.getId() != gene.getId()) {
						// So add it to the list, because it's a synonym, probably the input one
						geneList.add(queryGene);
						// Don't forget to "fix" the query gene as well
						queryGene.setNode(node);
						queryGene.setOrganism(res.getParameters().getOrganism());
						// And, finally, the query gene must replace the result gene here
						resGene.setGene(queryGene);
					}
				}
			}
		}
	}
	
	public Map<Long, InteractionNetworkGroup> computeGroupsByNetwork(RelatedGenesEngineResponseDto response, DataSet data) {
		Map<Long/*group-id*/, InteractionNetworkGroup> groups = new HashMap<>();
		Map<Long/*network-id*/, InteractionNetworkGroup> groupsByNetwork = new HashMap<>();
		List<NetworkDto> networks = response.getNetworks();
		
		for (NetworkDto network : networks) {
			long networkId = network.getId();
			
			InteractionNetworkGroup group = data.getNetworkGroup(networkId);
			
			if (group == null)
				continue;
			
			InteractionNetworkGroup canonicalGroup = groups.get(group.getId());
			
			if (canonicalGroup == null) {
				groups.put(group.getId(), group);
				canonicalGroup = group;
			}
			
			groupsByNetwork.put(networkId, canonicalGroup);
		}
		
		return groupsByNetwork;
	}
	
	public Map<Long, InteractionNetworkGroup> computeGroupsByNetwork(Collection<ResultInteractionNetworkGroup> list) {
		Map<Long, InteractionNetworkGroup> groups = new HashMap<>();
		Map<Long, InteractionNetworkGroup> groupsByNetwork = new HashMap<>();

		// TODO
		for (ResultInteractionNetworkGroup grRes : list) {
			InteractionNetworkGroup group = grRes.getNetworkGroup();
			
			if (group == null)
				continue;
			
			for (ResultInteractionNetwork network : grRes.getResultNetworks()) {
				long networkId = network.getNetwork().getId();
			
				InteractionNetworkGroup canonicalGroup = groups.get(group.getId());
				
				if (canonicalGroup == null) {
					groups.put(group.getId(), group);
					canonicalGroup = group;
				}
			
				groupsByNetwork.put(networkId, canonicalGroup);
			}
		}
		
		return groupsByNetwork;
	}

	private Map<Long, InteractionNetwork> computeCanonicalNetworks(Map<Long, InteractionNetworkGroup> groupsByNetwork) {
		Map<Long, InteractionNetwork> canonicalNetworks = new HashMap<>();
		
		for (InteractionNetworkGroup group : groupsByNetwork.values()) {
			for (InteractionNetwork network : group.getInteractionNetworks())
				canonicalNetworks.put(network.getId(), network);
		}
		
		return canonicalNetworks;
	}

	/**
	 * Offline Search version.
	 */
	private void computeAttributes(SearchResultBuilder config, Organism organism, Collection<AttributeDto> attributes,
			Map<Long, Collection<AttributeDto>> nodeToAttributes, AttributeMediator mediator) {
		if (attributes == null || nodeToAttributes == null)
			return;
		
		Map<Long, Attribute> attributesById = new HashMap<>();
		Map<Long, AttributeGroup> groupsByAttribute = new HashMap<>();
		Map<Long, AttributeGroup> groups = new HashMap<>();
		Map<Long, Collection<Attribute>> attributesByNode = new HashMap<>();
		Map<Attribute, Double> weights = new HashMap<>();
		
		long organismId = organism.getId();
		
		for (AttributeDto item : attributes) {
			Attribute attr = mediator.findAttribute(organismId, item.getId());
			attributesById.put(attr.getId(), attr);
			
			AttributeGroup group = groups.get(item.getGroupId());
			
			if (group == null)
				group = mediator.findAttributeGroup(organismId, item.getGroupId()); 
				
			groupsByAttribute.put(item.getId(), group);
			groups.put(item.getGroupId(), group);
			weights.put(attr, item.getWeight());
		}

		for (Entry<Long, Collection<AttributeDto>> entry : nodeToAttributes.entrySet()) {
			Collection<AttributeDto> sourceAttributes = entry.getValue();
			Collection<Attribute> nodeAttributes = new ArrayList<>(sourceAttributes.size());
			
			for (AttributeDto item : sourceAttributes) {
				Attribute attr = attributesById.get(item.getId());
				nodeAttributes.add(attr);
			}
			
			attributesByNode.put(entry.getKey(), nodeAttributes);
		}
		
		config.setGroupsByAttribute(groupsByAttribute);
		config.setAttributeWeights(weights);
		config.setAttributes(attributesByNode);
	}
	
	/**
	 * Online Search version.
	 */
	private void computeAttributes(SearchResultBuilder config, SearchResults res) {
		if (res.getResultAttributeGroups() == null || res.getResultAttributeGroups().isEmpty())
			return;
		
		Map<Long, Attribute> attributesById = new HashMap<>();
		Map<Long, AttributeGroup> groupsByAttribute = new HashMap<>();
		Map<Long, AttributeGroup> groups = new HashMap<>();
		Map<Long, Collection<Attribute>> attributesByNode = new HashMap<>();
		Map<Attribute, Double> weights = new HashMap<>();
		
		// FIXME always returns empty list???
		for (ResultAttributeGroup resGr : res.getResultAttributeGroups()) {
			for (ResultAttribute resAttr : resGr.getResultAttributes()) {
				Attribute attr = resAttr.getAttribute();
				attributesById.put(attr.getId(), attr);
				
				AttributeGroup group = groups.get(resGr.getAttributeGroup().getId());
				
				if (group == null)
					group = resGr.getAttributeGroup(); 
				
				groupsByAttribute.put(attr.getId(), group);
				groups.put(group.getId(), group);
				weights.put(attr, resGr.getWeight());
			}
		}
		
		// TODO	Probably wrong:
		for (ResultGene resGene : res.getResultGenes()) {
			Gene gene = resGene.getGene();
			
			if (gene == null)
				continue;
			
			Collection<Attribute> nodeAttributes = new ArrayList<>();
			GeneData geneData = gene.getNode().getGeneData();
			GeneNamingSource namingSource = gene.getNamingSource();
			
			Attribute attr = new Attribute();
			attr.setId(gene.getId()); // TODO ???
			attr.setName(gene.getSymbol());
			attr.setDescription(geneData.getDescription());
			attr.setExternalId(geneData.getExternalId());
			nodeAttributes.add(attr);
			
			attributesByNode.put(gene.getNode().getId(), nodeAttributes);
		}
		
		config.setGroupsByAttribute(groupsByAttribute);
		config.setAttributeWeights(weights);
		config.setAttributes(attributesByNode);
	}
	
	/**
	 * Offline Search.
	 */
	private Map<Gene, Double> computeGeneScores(List<NodeDto> nodes, Map<Long, Gene> queryGenes, Organism organism,
			NodeMediator nodeMediator) {
		// Figure out what the unique set of nodes so we don't end up creating model objects unnecessarily.
		Map<Long, NodeDto> uniqueNodes = new HashMap<>();
		
		for (NodeDto nodeDto : nodes)
			uniqueNodes.put(nodeDto.getId(), nodeDto);
		
		double maxScore = 0;
		Map<Gene, Double> scores = new HashMap<>();
		
		for (Entry<Long, NodeDto> entry : uniqueNodes.entrySet()) {
			long nodeId = entry.getKey();
			Gene gene = queryGenes.get(nodeId);
			
			if (gene == null) {
				Node node = nodeMediator.getNode(nodeId, organism.getId());
				gene = getPreferredGene(node);
			}
			
			if (gene == null)
				continue;
			
			double score = entry.getValue().getScore();
			maxScore = Math.max(maxScore, score);
			scores.put(gene, score);
		}
		
		for (Gene gene : queryGenes.values()) {
			if (!scores.containsKey(gene))
				scores.put(gene, maxScore);
		}
		
		return scores;
	}
	
	/**
	 * Online Search.
	 */
	private Map<Gene, Double> computeGeneScores(Collection<ResultGene> resultGenes) {
		double maxScore = 0;
		Map<Gene, Double> scores = new HashMap<>();
		
		if (resultGenes != null) {
			for (ResultGene resGene : resultGenes) {
				Gene gene = resGene.getGene();
				
				if (gene != null) {
					double score = resGene.getScore();
					maxScore = Math.max(maxScore, score);
					scores.put(gene, score);
				}
			}
		}
		
		return scores;
	}
	
	private void computeSourceInteractions(List<NetworkDto> networks, Map<Long, InteractionNetwork> canonicalNetworks,
			Organism organism, DataSet data) {
		IMediatorProvider mediatorProvider = data.getMediatorProvider();
		NodeMediator nodeMediator = mediatorProvider.getNodeMediator();
		
		for (NetworkDto networkVo : networks) {
			InteractionNetwork network = canonicalNetworks.get(networkVo.getId());
			
			if (network == null)
				continue;
			
			List<Interaction> interactions = new ArrayList<>();
			
			for (InteractionDto interactionVo : networkVo.getInteractions()) {
				Node fromNode = nodeMediator.getNode(interactionVo.getNodeVO1().getId(), organism.getId());
				Node toNode = nodeMediator.getNode(interactionVo.getNodeVO2().getId(), organism.getId());
				Interaction interaction = new Interaction(fromNode, toNode, (float) interactionVo.getWeight(), null);
				interactions.add(interaction);
			}
			
			network.setInteractions(interactions);
		}
	}
	
	private void computeSourceInteractions(Collection<ResultInteractionNetworkGroup> groups,
			Map<Long, InteractionNetwork> canonicalNetworks, Map<Long, Node> uniqueNodes) {
		for (ResultInteractionNetworkGroup resGr : groups) {
			for (ResultInteractionNetwork resNet : resGr.getResultNetworks()) {
				InteractionNetwork network = canonicalNetworks.get(resNet.getNetwork().getId());
				
				if (network == null)
					continue;
				
				List<Interaction> interactionList = new ArrayList<>();
				
				for (ResultInteraction resInter : resNet.getResultInteractions()) {
					Interaction interaction = resInter.getInteraction();
					interactionList.add(interaction);
					
					// Fix the interaction Nodes: These Nodes (from ResultGene) have all attributes set,
					// whereas the ones from interaction.getFromNode() don't!
					Node fromNode = resInter.getFromGene().getGene().getNode();
					fromNode = uniqueNodes.get(fromNode.getId());
					interaction.setFromNode(fromNode);
					
					Node toNode = resInter.getToGene().getGene().getNode();
					toNode = uniqueNodes.get(toNode.getId());
					interaction.setToNode(toNode);
				}
				
				network.setInteractions(interactionList);
			}
		}
	}

	private Map<Long, Collection<AnnotationEntry>> processAnnotations(Map<Long, Collection<OntologyCategoryDto>> annotations, DataSet data) {
		OntologyMediator mediator = data.getMediatorProvider().getOntologyMediator();
		Map<Long, Collection<AnnotationEntry>> result = new HashMap<>();
		Map<Long, AnnotationEntry> annotationCache = new HashMap<>();
		
		for (Entry<Long, Collection<OntologyCategoryDto>> entry : annotations.entrySet()) {
			long nodeId = entry.getKey();
			Set<AnnotationEntry> nodeAnnotations = new HashSet<>();
			
			for (OntologyCategoryDto dto : entry.getValue()) {
				long catId = dto.getId();
				AnnotationEntry annotation = annotationCache.get(catId);
				
				if (annotation == null) {
					try {
						OntologyCategory cat = mediator.getCategory(catId);
						annotation = new AnnotationEntry(cat, dto);
						annotationCache.put(catId, annotation);
					} catch (DataStoreException e) {
						Logger logger = Logger.getLogger(NetworkUtils.class);
						logger.error(String.format("Can't find category: %d", catId), e); //$NON-NLS-1$
						continue;
					}
				}
				
				nodeAnnotations.add(annotation);
			}
			
			if (!nodeAnnotations.isEmpty())
				result.put(nodeId, nodeAnnotations);
		}
		
		return result;
	}
	
	private Map<Long, Collection<AnnotationEntry>> processAnnotations(SearchResults res) {
		Map<Long, Collection<AnnotationEntry>> result = new HashMap<>();
		
		if (res.getResultGenes() == null)
			return result;
		
		Map<Long, AnnotationEntry> annotationCache = new HashMap<>();
		
		for (ResultGene resGene : res.getResultGenes()) {
			long nodeId = resGene.getGene().getNode().getId();
			Collection<ResultOntologyCategory> resultOntologyCategories = resGene.getResultOntologyCategories();
			Set<AnnotationEntry> nodeAnnotations = new HashSet<>();
			
			if (resultOntologyCategories == null)
				continue;
			
			for (ResultOntologyCategory resCat : resultOntologyCategories) {
				OntologyCategory cat = resCat.getOntologyCategory();
				long catId = cat.getId();
				AnnotationEntry annotation = annotationCache.get(catId);
					
				if (annotation == null) {
					OntologyCategoryDto dto = new OntologyCategoryDto();
					dto.setId(catId);
					dto.setNumAnnotatedInSample(resCat.getNumAnnotatedInSample());
					dto.setNumAnnotatedInTotal(resCat.getNumAnnotatedInTotal());
					dto.setpValue(resCat.getpValue());
					dto.setqValue(resCat.getqValue());
					
					annotation = new AnnotationEntry(cat, dto);
					annotationCache.put(catId, annotation);
				}
				
				nodeAnnotations.add(annotation);
			}
				
			if (!nodeAnnotations.isEmpty())
				result.put(nodeId, nodeAnnotations);
		}
		
		return result;
	}

	public void normalizeNetworkWeights(RelatedGenesEngineResponseDto result) {
		double totalWeight = 0;
		
		for (NetworkDto network : result.getNetworks())
			totalWeight += network.getWeight();
		
		if (totalWeight == 0)
			return;

		double correctionFactor = 1 / totalWeight;
		
		for (NetworkDto network : result.getNetworks())
			network.setWeight(network.getWeight() * correctionFactor);
	}

	public String buildGeneDescription(Gene gene) {
		Node node = gene.getNode();
		GeneData data = node.getGeneData();
		
		boolean first = true;
		StringBuilder builder = new StringBuilder();
		Map<String, String> linkouts = GeneLinkoutGenerator.instance().getLinkouts(gene.getOrganism(), node);
		
		for (Entry<String, String> entry : linkouts.entrySet()) {
			if (!first)
				builder.append(", "); //$NON-NLS-1$
			
			builder.append(String.format("<a href=\"%s\">%s</a>", htmlEscape(entry.getValue()), entry.getKey())); //$NON-NLS-1$
			first = false;
		}
		
		if (builder.length() == 0)
			return String.format(Strings.geneDetailPanelDescription_label, htmlEscape(data.getDescription()), builder.toString());
		
		return String.format(Strings.geneDetailPanelDescription2_label, htmlEscape(data.getDescription()), builder.toString());
	}
}
