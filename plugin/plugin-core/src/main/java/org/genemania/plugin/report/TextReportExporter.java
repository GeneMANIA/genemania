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

package org.genemania.plugin.report;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.genemania.domain.Attribute;
import org.genemania.domain.AttributeGroup;
import org.genemania.domain.Gene;
import org.genemania.domain.Interaction;
import org.genemania.domain.InteractionNetwork;
import org.genemania.domain.InteractionNetworkGroup;
import org.genemania.domain.NetworkMetadata;
import org.genemania.domain.Node;
import org.genemania.domain.Tag;
import org.genemania.plugin.Metadata;
import org.genemania.plugin.Strings;
import org.genemania.plugin.controllers.IGeneProvider;
import org.genemania.plugin.model.AnnotationEntry;
import org.genemania.plugin.model.Group;
import org.genemania.plugin.model.Network;
import org.genemania.plugin.model.SearchResult;
import org.genemania.plugin.model.ViewState;
import org.genemania.plugin.model.impl.QueryAttributeGroupImpl;
import org.genemania.plugin.model.impl.QueryAttributeNetworkImpl;
import org.genemania.plugin.report.ManiaReport.GeneEntry;
import org.genemania.type.CombiningMethod;

public class TextReportExporter implements ReportExporter {

	private final IGeneProvider geneProvider;
	private Group<?, ?> attributeGroup;
	private Map<Long, Gene> geneCache;

	public TextReportExporter(IGeneProvider geneProvider) {
		this.geneProvider = geneProvider;
		
		Collection<Network<AttributeGroup>> networks = Collections.emptyList();
		attributeGroup = new QueryAttributeGroupImpl(networks);
		geneCache = new HashMap<Long, Gene>();
	}
	
	@Override
	public void export(ManiaReport report, OutputStream stream) {
		PrintWriter writer = new PrintWriter(stream);
		try {
			writer.print("GeneMANIA Results\n"); //$NON-NLS-1$
			exportVersion(report, writer);
			exportGenes(report, writer);
			exportNetworks(report, writer);
			exportInteractions(report, writer);
			exportEnrichment(report, writer);
			exportEnrichmentSummary(report, writer);
			exportParameters(report, writer);
		} finally {
			writer.close();
		}
	}

	private void exportEnrichment(ManiaReport report, PrintWriter writer) {
		SearchResult options = report.getViewState().getSearchResult();

		writer.println("Gene\tGO ids"); //$NON-NLS-1$
		List<GeneEntry> genes = new ArrayList<GeneEntry>(report.getGenes());
		Collections.sort(genes, new Comparator<GeneEntry>() {
			@Override
			public int compare(GeneEntry o1, GeneEntry o2) {
				return o1.getGene().getSymbol().compareToIgnoreCase(o2.getGene().getSymbol());
			}
		});
		for (GeneEntry entry : genes) {
			try {
				Gene gene = entry.getGene();
				gene = findGene(gene.getNode(), options);
				writer.print(getGeneLabel(gene));
				
				Collection<AnnotationEntry> annotations = options.getAnnotations(gene.getNode().getId());
				if (annotations == null) {
					continue;
				}
				List<AnnotationEntry> sortedAnnotations = new ArrayList<AnnotationEntry>(annotations);
				Collections.sort(sortedAnnotations, new Comparator<AnnotationEntry>() {
					@Override
					public int compare(AnnotationEntry o1, AnnotationEntry o2) {
						return o1.getName().compareToIgnoreCase(o2.getName());
					}
				});
				for (AnnotationEntry annotation : sortedAnnotations) {
					writer.print("\t"); //$NON-NLS-1$
					writer.print(annotation.getName());
				}
			} finally {
				writer.println();
			}
		}
		writer.println();
	}

	private void exportEnrichmentSummary(ManiaReport report, PrintWriter writer) {
		SearchResult options = report.getViewState().getSearchResult();
		List<AnnotationEntry> enrichment = options.getEnrichmentSummary();
		
		writer.println("GO id\tDescription\tq-value\tOccurrences in Sample\tOccurrences in Genome"); //$NON-NLS-1$
		for (AnnotationEntry annotation : enrichment) {
			writer.print(annotation.getName());
			writer.print("\t"); //$NON-NLS-1$
			writer.print(annotation.getDescription());
			writer.print("\t"); //$NON-NLS-1$
			writer.print(annotation.getQValue());
			writer.print("\t"); //$NON-NLS-1$
			writer.print(annotation.getSampleOccurrences());
			writer.print("\t"); //$NON-NLS-1$
			writer.print(annotation.getTotalOccurrences());
			writer.println();
		}
		writer.println();
	}

	private void exportInteractions(ManiaReport report, PrintWriter writer) {
		final SearchResult options = report.getViewState().getSearchResult();

		writer.println("Gene 1\tGene 2\tWeight\tType\tSource"); //$NON-NLS-1$

		List<InteractionEntry> entries = new ArrayList<InteractionEntry>();
		for (Entry<Long, Collection<Attribute>> entry : options.getAttributesByNodeId().entrySet()) {
			Gene gene = options.getGene(entry.getKey());
			for (Attribute attribute : entry.getValue()) {
				AttributeGroup group = options.getAttributeGroup(attribute.getId());
				entries.add(new InteractionEntry(attribute.getName(), gene.getSymbol(), group.getName(), 0D));
			}
		}
		Collections.sort(entries, new Comparator<InteractionEntry>() {
			@Override
			public int compare(InteractionEntry entry1, InteractionEntry entry2) {
				int result = String.CASE_INSENSITIVE_ORDER.compare(entry1.getType(), entry2.getType());
				if (result != 0) {
					return result;
				}
				result = String.CASE_INSENSITIVE_ORDER.compare(entry1.getFrom(), entry2.getFrom());
				if (result != 0) {
					return result;
				}
				return String.CASE_INSENSITIVE_ORDER.compare(entry1.getTo(), entry2.getTo());
			}
		});
		for (InteractionEntry entry : entries) {
			writer.print(entry.getFrom());
			writer.print("\t"); //$NON-NLS-1$
			writer.print(entry.getTo());
			writer.print("\t"); //$NON-NLS-1$
			writer.print(entry.getWeight() * 100);
			writer.print("\t"); //$NON-NLS-1$
			writer.print(entry.getType());
			writer.print("\t"); //$NON-NLS-1$
			writer.println();
		}
		
		Map<InteractionNetwork, Double> networkWeights = options.getNetworkWeights();
		Map<Long, Double> weights = remapWeights(networkWeights);
		
		List<InteractionNetwork> keys = new ArrayList<InteractionNetwork>(networkWeights.keySet());
		Collections.sort(keys, new Comparator<InteractionNetwork>() {
			@Override
			public int compare(InteractionNetwork o1, InteractionNetwork o2) {
				InteractionNetworkGroup group1 = options.getInteractionNetworkGroup(o1.getId());
				InteractionNetworkGroup group2 = options.getInteractionNetworkGroup(o2.getId());
				if (group1.getId() == group2.getId()) {
					return o1.getName().compareToIgnoreCase(o2.getName());
				}
				return group1.getName().compareToIgnoreCase(group2.getName());
			}
		});
		
		for (InteractionNetwork network : keys) {
			InteractionNetworkGroup group = options.getInteractionNetworkGroup(network.getId());
			
			List<Interaction> interactions = new ArrayList<Interaction>(network.getInteractions());
			Collections.sort(interactions, new Comparator<Interaction>() {
				@Override
				public int compare(Interaction o1, Interaction o2) {
					Gene gene1 = findGene(o1.getFromNode(), options);
					Gene gene2 = findGene(o2.getFromNode(), options);
					int ordering = gene1.getSymbol().compareToIgnoreCase(gene2.getSymbol());
					if (ordering != 0) {
						return ordering;
					}
					gene1 = findGene(o1.getToNode(), options);
					gene2 = findGene(o2.getToNode(), options);
					return gene1.getSymbol().compareToIgnoreCase(gene2.getSymbol());
				}
			});
			for (Interaction interaction : interactions) {
				writer.print(findGene(interaction.getFromNode(), options).getSymbol());
				writer.print("\t"); //$NON-NLS-1$
				writer.print(findGene(interaction.getToNode(), options).getSymbol());
				writer.print("\t"); //$NON-NLS-1$
				writer.print(interaction.getWeight() * weights.get(network.getId()) * 100);
				writer.print("\t"); //$NON-NLS-1$
				writer.print(group.getName());
				writer.print("\t"); //$NON-NLS-1$
				writer.print(network.getName());
				writer.println();
			}
		}
		writer.println();
	}

	private Map<Long, Double> remapWeights(Map<InteractionNetwork, Double> weights) {
		Map<Long, Double> result = new HashMap<Long, Double>();
		for (Entry<InteractionNetwork, Double> entry : weights.entrySet()) {
			result.put(entry.getKey().getId(), entry.getValue());
		}
		return result;
	}
	
	private Gene findGene(Node node, SearchResult options) {
		long nodeId = node.getId();
		
		if (options.isQueryNode(nodeId)) {
			return options.getGene(node.getId());
		}
		
		var gene = geneCache.get(node.getId());
		
		if (gene == null) {
			if (geneProvider != null)
				gene = geneProvider.getGene(node);
			else
				gene = options.getGene(node.getId());
			
			geneCache.put(node.getId(), gene);
		}
		
		return gene;
	}
	
	private void exportVersion(ManiaReport report, PrintWriter writer) {
		Metadata metadata = new Metadata();
		String version = metadata.getCytoscapeVersion();
		String buildId = metadata.getBuildId();
		
		writer.print("Plugin Version\t"); //$NON-NLS-1$
		writer.print(version == null ? "development" : version); //$NON-NLS-1$
		if (buildId != null) {
			writer.print(" ("); //$NON-NLS-1$
			writer.print(buildId);
			writer.print(")"); //$NON-NLS-1$
		}
		writer.print("\n"); //$NON-NLS-1$
		writer.print("Data Version\t"); //$NON-NLS-1$
		writer.print(report.getDataVersion());
		writer.print("\n"); //$NON-NLS-1$
		writer.print("Report Generated\t"); //$NON-NLS-1$
		writer.print(new Date());
		writer.print("\n\n"); //$NON-NLS-1$
	}

	private void exportParameters(ManiaReport report, PrintWriter writer) {
		writer.print("Query Parameters\n"); //$NON-NLS-1$
		
		final ViewState viewState = report.getViewState();
		SearchResult options = viewState.getSearchResult();
		writer.print("Organism\t"); //$NON-NLS-1$
		writer.print(options.getOrganism().getName());
		writer.print("\n"); //$NON-NLS-1$

		writer.print("Network Weighting\t"); //$NON-NLS-1$
		writer.print(formatCombiningMethod(options.getCombiningMethod()));
		writer.print("\n"); //$NON-NLS-1$

		writer.print("Related Genes Limit\t"); //$NON-NLS-1$
		writer.print(options.getGeneSearchLimit());
		writer.print("\n"); //$NON-NLS-1$
		
		writer.print("Related Attributes Limit\t"); //$NON-NLS-1$
		writer.print(options.getAttributeSearchLimit());
		writer.print("\n"); //$NON-NLS-1$

		writer.print("Input Genes"); //$NON-NLS-1$
		for (Gene gene : options.getQueryGenes().values()) {
			writer.print("\t"); //$NON-NLS-1$
			writer.print(gene.getSymbol());
		}
		writer.print("\n"); //$NON-NLS-1$

		writer.print("Networks\n"); //$NON-NLS-1$
		List<Network<?>> networks = new ArrayList<Network<?>>(report.getNetworks());
		filterAttributes(networks, viewState);
		
		Collections.sort(networks, new Comparator<Network<?>>() {
			@Override
			public int compare(Network<?> network1, Network<?> network2) {
				Group<?, ?> group1 = getGroup(network1, viewState);
				Group<?, ?> group2 = getGroup(network2, viewState);
				int result = String.CASE_INSENSITIVE_ORDER.compare(group1.getName(), group2.getName());
				if (result != 0) {
					return result;
				}
				return String.CASE_INSENSITIVE_ORDER.compare(network1.getName(), network2.getName());
			}
		});
		for (Network<?> entry : networks) {
			writer.print("\t"); //$NON-NLS-1$
			writer.print(entry.getName());
			writer.print("\t"); //$NON-NLS-1$
			Group<?, ?> group = getGroup(entry, viewState);
			writer.print(group.getName());
			writer.print("\n"); //$NON-NLS-1$
		}
		writer.print("\n"); //$NON-NLS-1$
	}

	private Group<?, ?> getGroup(Network<?> entry, ViewState viewState) {
		Group<?, ?> group = viewState.getGroup(entry);
		if (group == null) {
			AttributeGroup adapted = entry.adapt(AttributeGroup.class);
			if (adapted == null) {
				return null;
			}
			return attributeGroup;
		}
		return group;
	}

	private void filterAttributes(List<Network<?>> networks, ViewState viewState) {
		Map<String, Network<AttributeGroup>> newNetworks = new HashMap<String, Network<AttributeGroup>>();
		
		Iterator<Network<?>> iterator = networks.iterator();
		while (iterator.hasNext()) {
			Network<?> network = iterator.next();
			Group<?, ?> group = viewState.getGroup(network);
			Group<AttributeGroup, Attribute> adapted = group.adapt(AttributeGroup.class, Attribute.class);
			if (adapted == null) {
				continue;
			}
			AttributeGroup model = adapted.getModel();
			if (newNetworks.containsKey(model.getName())) {
				continue;
			}
			newNetworks.put(model.getName(), new QueryAttributeNetworkImpl(model, 0));
		}
		
		for (Network<AttributeGroup> network : newNetworks.values()) {
			networks.add(network);
		}
	}

	private String formatCombiningMethod(CombiningMethod combiningMethod) {
		switch (combiningMethod) {
		case AUTOMATIC:
			return Strings.automatic;
		case AVERAGE:
			return Strings.average;
		case AVERAGE_CATEGORY:
			return Strings.average_category;
		case BP:
			return Strings.bp;
		case MF:
			return Strings.mf;
		case CC:
			return Strings.cc;
		default:
			return Strings.default_combining_method;
		}
	}

	private void exportNetworks(ManiaReport report, PrintWriter writer) {
		Group<?, ?> lastGroup = null;
		
		writer.print("Network Group"); //$NON-NLS-1$
		writer.print("\t"); //$NON-NLS-1$
		writer.print("Network"); //$NON-NLS-1$
		writer.print("\t"); //$NON-NLS-1$
		writer.print("Weight"); //$NON-NLS-1$
		writer.print("\t"); //$NON-NLS-1$
		writer.print("Title"); //$NON-NLS-1$
		writer.print("\t"); //$NON-NLS-1$
		writer.print("Authors"); //$NON-NLS-1$
		writer.print("\t"); //$NON-NLS-1$
		writer.print("Year"); //$NON-NLS-1$
		writer.print("\t"); //$NON-NLS-1$
		writer.print("Publication"); //$NON-NLS-1$
		writer.print("\t"); //$NON-NLS-1$
		writer.print("PMID"); //$NON-NLS-1$
		writer.print("\t"); //$NON-NLS-1$
		writer.print("URL"); //$NON-NLS-1$
		writer.print("\t"); //$NON-NLS-1$
		writer.print("Processing Method"); //$NON-NLS-1$
		writer.print("\t"); //$NON-NLS-1$
		writer.print("Interactions"); //$NON-NLS-1$
		writer.print("\t"); //$NON-NLS-1$
		writer.print("Source"); //$NON-NLS-1$
		writer.print("\t"); //$NON-NLS-1$
		writer.print("Source URL"); //$NON-NLS-1$
		writer.print("\t"); //$NON-NLS-1$
		writer.print("Tags"); //$NON-NLS-1$
		writer.print("\n"); //$NON-NLS-1$
		
		ViewState viewState = report.getViewState();
		for (Network<?> entry : report.getNetworks()) {
			Group<?, ?> group = viewState.getGroup(entry); 
			if (lastGroup == null || group != lastGroup) {
				writer.print(group.getName());
				writer.print("\t\t"); //$NON-NLS-1$
				writer.print(String.format("%.2f", group.getWeight() * 100)); //$NON-NLS-1$
				writer.print("\n"); //$NON-NLS-1$
			}
			{
				InteractionNetwork network = entry.adapt(InteractionNetwork.class);
				if (network != null) {
					NetworkMetadata metadata = network.getMetadata();
					writer.print("\t"); //$NON-NLS-1$
					writer.print(network.getName());
					writer.print("\t"); //$NON-NLS-1$
					writer.print(String.format("%.2f", entry.getWeight() * 100)); //$NON-NLS-1$
					writer.print("\t"); //$NON-NLS-1$
					writer.print(metadata.getTitle());
					writer.print("\t"); //$NON-NLS-1$
					writer.print(metadata.getAuthors());
					writer.print("\t"); //$NON-NLS-1$
					writer.print(metadata.getYearPublished());
					writer.print("\t"); //$NON-NLS-1$
					writer.print(metadata.getPublicationName());
					writer.print("\t"); //$NON-NLS-1$
					writer.print(metadata.getPubmedId());
					writer.print("\t"); //$NON-NLS-1$
					writer.print(metadata.getUrl());
					writer.print("\t"); //$NON-NLS-1$
					writer.print(metadata.getProcessingDescription());
					writer.print("\t"); //$NON-NLS-1$
					writer.print(metadata.getInteractionCount());
					writer.print("\t"); //$NON-NLS-1$
					writer.print(metadata.getSource());
					writer.print("\t"); //$NON-NLS-1$
					writer.print(metadata.getSourceUrl());
					writer.print("\t"); //$NON-NLS-1$
					boolean first = true;
					for (Tag tag : network.getTags()) {
						if (!first) {
							writer.print(","); //$NON-NLS-1$
						}
						writer.print(tag.getName());
						first = false;
					}
				}
			}
			{
				Group<AttributeGroup, Attribute> attributeGroup = group.adapt(AttributeGroup.class, Attribute.class);
				Attribute network = entry.adapt(Attribute.class);
				if (attributeGroup != null && network != null) {
					AttributeGroup groupModel = attributeGroup.getModel();
					writer.print("\t"); //$NON-NLS-1$
					writer.print(network.getName());
					writer.print("\t"); //$NON-NLS-1$
					writer.print(String.format("%.2f", entry.getWeight() * 100)); //$NON-NLS-1$
					writer.print("\t"); //$NON-NLS-1$
					writer.print(network.getDescription());
					writer.print("\t"); //$NON-NLS-1$
					writer.print("\t"); //$NON-NLS-1$
					writer.print("\t"); //$NON-NLS-1$
					writer.print(groupModel.getPublicationName());
					writer.print("\t"); //$NON-NLS-1$
					writer.print("\t"); //$NON-NLS-1$
					writer.print(groupModel.getPublicationUrl());
					writer.print("\t"); //$NON-NLS-1$
					writer.print("\t"); //$NON-NLS-1$
					writer.print("\t"); //$NON-NLS-1$
					writer.print(groupModel.getLinkoutLabel());
					writer.print("\t"); //$NON-NLS-1$
					writer.print(groupModel.getLinkoutUrl());
					writer.print("\t"); //$NON-NLS-1$
				}
			}
			writer.print("\n"); //$NON-NLS-1$
			lastGroup = group;
		}
		writer.print("\n"); //$NON-NLS-1$
	}

	private void exportGenes(ManiaReport report, PrintWriter writer) {
		SearchResult options = report.getViewState().getSearchResult();
		writer.print("Gene\tScore\tDescription\n"); //$NON-NLS-1$
		for (GeneEntry entry : report.getGenes()) {
			Gene gene = findGene(entry.getGene().getNode(), options);
			writer.print(getGeneLabel(gene));
			writer.print("\t"); //$NON-NLS-1$
			
			double score = entry.getScore();
			if (score != Double.MAX_VALUE) {
				writer.print(String.format("%.2f", score)); //$NON-NLS-1$
			}
			
			writer.print("\t"); //$NON-NLS-1$
			writer.print(gene.getNode().getGeneData().getDescription());
			writer.print("\n"); //$NON-NLS-1$
		}
		writer.print("\n"); //$NON-NLS-1$
	}
	
	protected String getGeneLabel(Gene gene) {
		return gene.getSymbol();
	}

	private static class InteractionEntry {
		String from;
		String to;
		String type;
		Double weight;
		
		public InteractionEntry(String from, String to, String type, Double weight) {
			this.from = from;
			this.to = to;
			this.type = type;
			this.weight = weight;
		}
		
		public Double getWeight() {
			return weight;
		}

		public String getFrom() {
			return from;
		}
		
		public String getTo() {
			return to;
		}
		
		public String getType() {
			return type;
		}
	}
	
}
