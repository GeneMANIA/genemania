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

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
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
import org.genemania.domain.Organism;
import org.genemania.plugin.Metadata;
import org.genemania.plugin.Strings;
import org.genemania.plugin.controllers.IGeneProvider;
import org.genemania.plugin.model.Group;
import org.genemania.plugin.model.Network;
import org.genemania.plugin.model.SearchResult;
import org.genemania.plugin.model.ViewState;
import org.genemania.plugin.report.ManiaReport.GeneEntry;
import org.genemania.type.CombiningMethod;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

@SuppressWarnings("nls")
public class ManiaReportXMLReader extends AbstractXMLReader {
	private final ManiaReport report;
	private final IGeneProvider provider;
	private Map<Long, Gene> geneCache;

	public ManiaReportXMLReader(ManiaReport report, IGeneProvider provider) {
		this.report = report;
		this.provider = provider;
		geneCache = new HashMap<Long, Gene>();
	}

	public void parse(InputSource input) throws IOException, SAXException {
		startDocument();
		try {
			Metadata metadata = new Metadata();
			String version = metadata.getCytoscapeVersion();
			String buildId = metadata.getBuildId();

			if (version == null) {
				version = "development";
			}
			AttributesImpl attributes = new AttributesImpl();
			attributes.addAttribute("", "version", "version", TYPE_STRING, version);
			
			if (buildId != null) {
				attributes.addAttribute("", "buildId", "buildId", TYPE_STRING, buildId);
			}
			
			attributes.addAttribute("", "date", "date", TYPE_STRING, new Date().toString());
			attributes.addAttribute("", "data", "data", TYPE_STRING, report.getDataVersion());

			startElement("prediction", attributes);
			try {
				startElement("results");
				try {
					exportGenes();
					exportNetworks();
					exportInteractions();
				} finally {
					endElement("results");
				}
				exportParameters();
			} finally {
				endElement("prediction");
			}
		} finally {
			endDocument();
		}
	}
	
	private void exportParameters() throws SAXException {
		startElement("query");
		try {
			ViewState viewState = report.getViewState();
			SearchResult result = viewState.getSearchResult();
			AttributesImpl attributes = new AttributesImpl();
			Organism organism = result.getOrganism();
			attributes.addAttribute("", "name", "name", TYPE_STRING, organism.getName());
			attributes.addAttribute("", "taxid", "taxid", TYPE_NUMBER, String.valueOf(organism.getTaxonomyId()));
			simpleElement("organism", attributes);
			

			attributes.clear();
			attributes.addAttribute("", "method", "method", TYPE_STRING, formatCombiningMethod(result.getCombiningMethod()));
			simpleElement("network-weighting", attributes);
	
			attributes.clear();
			attributes.addAttribute("", "limit", "limit", TYPE_STRING, String.valueOf(result.getGeneSearchLimit()));
			simpleElement("related-genes", attributes);

			attributes.clear();
			attributes.addAttribute("", "limit", "limit", TYPE_STRING, String.valueOf(result.getAttributeSearchLimit()));
			simpleElement("related-attributes", attributes);

			startElement("genes");
			try {
				for (Gene gene : result.getQueryGenes().values()) {
					attributes.clear();
					attributes.addAttribute("", "symbol", "symbol", TYPE_STRING, gene.getSymbol());
					simpleElement("gene", attributes);
				}
			} finally {
				endElement("genes");
			}

			startElement("networks");
			try {
				for (Network<?> entry : report.getNetworks()) {
					attributes.clear();
					attributes.addAttribute("", "name", "name", TYPE_STRING, entry.getName());
					Group<?, ?> group = viewState.getGroup(entry);
					attributes.addAttribute("", "group", "group", TYPE_STRING, group.getCode());
					simpleElement("network", attributes);
				}
			} finally {
				endElement("networks");
			}
		} finally {
			endElement("query");
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

	private void exportNetworks() throws SAXException {
		startElement("networks");
		try {
			ViewState viewState = report.getViewState();
			AttributesImpl attributes = new AttributesImpl();
			for (Network<?> entry : report.getNetworks()) {
				{
					InteractionNetwork network = entry.adapt(InteractionNetwork.class);
					if (network == null) {
						continue;
					}
					Group<?, ?> group = viewState.getGroup(entry);
					NetworkMetadata metadata = network.getMetadata();
					attributes.clear();
					attributes.addAttribute("", "name", "name", TYPE_STRING, network.getName());
					attributes.addAttribute("", "group", "group", TYPE_STRING, group.getCode());
					attributes.addAttribute("", "weight", "weight", TYPE_NUMBER, String.format("%.2f", entry.getWeight() * 100));
					attributes.addAttribute("", "interactions","interactions", TYPE_NUMBER, String.valueOf(metadata.getInteractionCount()));
					startElement("network", attributes);
					try {
						attributes.clear();
						attributes.addAttribute("", "title","title", TYPE_STRING, metadata.getTitle());
						attributes.addAttribute("", "authors","authors", TYPE_STRING, metadata.getAuthors());
						attributes.addAttribute("", "year","year", TYPE_STRING, metadata.getYearPublished());
						attributes.addAttribute("", "publication","publication", TYPE_STRING, metadata.getPublicationName());
						attributes.addAttribute("", "pmid","pmid", TYPE_STRING, metadata.getPubmedId());
						attributes.addAttribute("", "url","url", TYPE_STRING, metadata.getUrl());
						simpleElement("reference", attributes);
						
						attributes.clear();
						attributes.addAttribute("", "name", "name", TYPE_STRING, metadata.getSource());
						attributes.addAttribute("", "url", "url", TYPE_STRING, metadata.getSourceUrl());
						attributes.addAttribute("", "processing-method", "processing-method", TYPE_STRING, metadata.getProcessingDescription());
						simpleElement("source", attributes);
						
						simpleElement("comment", metadata.getComment());
					} finally {
						endElement("network");
					}
				}
			}
		} finally {
			endElement("networks");
		}
	}

	private void exportGenes() throws SAXException {
		SearchResult options = report.getViewState().getSearchResult();
		startElement("genes");
		try {
			AttributesImpl attributes = new AttributesImpl();
			for (GeneEntry entry : report.getGenes()) {
				attributes.clear();
				Gene gene = findGene(entry.getGene().getNode(), options);
				attributes.addAttribute("", "symbol", "symbol", TYPE_STRING, gene.getSymbol());
				if (entry.getScore() != Double.MAX_VALUE) {
					attributes.addAttribute("", "score", "score", TYPE_NUMBER, String.format("%.2f", entry.getScore()));
				}
				attributes.addAttribute("", "description", "description", TYPE_STRING, gene.getNode().getGeneData().getDescription());
				simpleElement("gene", attributes);
			}
		} finally {
			endElement("genes");
		}
	}
	
	private void exportInteractions() throws SAXException {
		SearchResult options = report.getViewState().getSearchResult();
		startElement("interactions");	
		try {
			AttributesImpl attributes = new AttributesImpl();
			
			Map<InteractionNetwork, Double> networkWeights = options.getNetworkWeights();
			Map<Long, Double> weights = remapWeights(networkWeights);
			
			for (Entry<Long, Collection<Attribute>> entry : options.getAttributesByNodeId().entrySet()) {
				Gene gene = options.getGene(entry.getKey());
				for (Attribute attribute : entry.getValue()) {
					AttributeGroup group = options.getAttributeGroup(attribute.getId());
					attributes.clear();
					attributes.addAttribute("", "from", "from", TYPE_STRING, attribute.getName());
					attributes.addAttribute("", "to", "to", TYPE_STRING, gene.getSymbol());
					attributes.addAttribute("", "weight", "weight", TYPE_NUMBER, String.valueOf(0));
					attributes.addAttribute("", "type", "type", TYPE_STRING, group.getName());
					startElement("interaction", attributes);
					endElement("interaction");
				}
			}
			
			for (InteractionNetwork network : networkWeights.keySet()) {
				for (Interaction interaction : network.getInteractions()) {
					InteractionNetworkGroup group = options.getInteractionNetworkGroup(network.getId());
					attributes.clear();
					attributes.addAttribute("", "from", "from", TYPE_STRING, findGene(interaction.getFromNode(), options).getSymbol());
					attributes.addAttribute("", "to", "to", TYPE_STRING, findGene(interaction.getToNode(), options).getSymbol());
					attributes.addAttribute("", "weight", "weight", TYPE_NUMBER, String.valueOf(interaction.getWeight() * weights.get(network.getId()) * 100));
					attributes.addAttribute("", "type", "type", TYPE_STRING, group.getName());
					attributes.addAttribute("", "source", "source", TYPE_STRING, network.getName());
					startElement("interaction", attributes);
					endElement("interaction");
				}
			}
		} finally {
			endElement("interactions");
		}
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
		
		Gene gene = geneCache.get(node.getId());
		if (gene == null) {
			gene = provider.getGene(node);
			geneCache.put(node.getId(), gene);
		}
		return gene;
	}
}
