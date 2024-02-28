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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.genemania.domain.Gene;
import org.genemania.domain.OntologyCategory;
import org.genemania.exception.DataStoreException;
import org.genemania.plugin.data.DataSet;
import org.genemania.plugin.model.Group;
import org.genemania.plugin.model.Network;
import org.genemania.plugin.model.SearchResult;
import org.genemania.plugin.model.ViewState;

public class ManiaReport {
	
	private List<GeneEntry> genes;
	private List<Network<?>> networks;
	private ViewState options;
	private List<Group<?, ?>> groups;
	private Map<String, OntologyCategory> categories;
	
	private final DataSet data;
	private final String dataVersion;
	
	public ManiaReport(ViewState options, DataSet data) {
		this(options, data, data.getVersion().toString());
	}
	
	public ManiaReport(ViewState options, DataSet data, String dataVersion) {
		this.options = options;
		this.data = data;
		this.dataVersion = dataVersion;
		
		var result = options.getSearchResult();
		groups = computeGroups(options);
		networks = populateNetworks(options, groups);
		genes = populateGenes(result);
		categories = populateCategories(result);
	}
	
	private List<Group<?, ?>> computeGroups(ViewState options) {
		List<Group<?, ?>> groups = new ArrayList<Group<?, ?>>(options.getAllGroups());
		Collections.sort(groups, new Comparator<Group<?, ?>>() {
			@Override
			public int compare(Group<?, ?> group1, Group<?, ?> group2) {
				int result = Double.compare(group1.getWeight(), group2.getWeight());
				if (result != 0) {
					return result;
				}
				return String.CASE_INSENSITIVE_ORDER.compare(group1.getName(), group2.getName());
			}
		});
		return groups;
	}

	private Map<String, OntologyCategory> populateCategories(SearchResult options) {
		var result = new HashMap<String, OntologyCategory>();
		
		if (data != null) {
			var mediator = data.getMediatorProvider().getOntologyMediator();
			
			for (var annotation : options.getEnrichmentSummary()) {
				String name = annotation.getName();
				try {
					result.put(name, mediator.getCategory(name));
				} catch (DataStoreException e) {
					Logger logger = Logger.getLogger(getClass());
					logger.error(String.format("Can't find category: %s", name, e)); //$NON-NLS-1$
				}
			}
		}
		
		return result;
	}

	private List<GeneEntry> populateGenes(SearchResult options) {
		List<GeneEntry> result = new ArrayList<GeneEntry>();
		
		final Map<Gene, Double> scores = options.getScores();
		List<Gene> genes = new ArrayList<Gene>(scores.keySet());
		Collections.sort(genes, new Comparator<Gene>() {
			@Override
			public int compare(Gene gene1, Gene gene2) {
				return scores.get(gene2).compareTo(scores.get(gene1));
			}
		});
		
		Map<Long, Gene> queryGenes = options.getQueryGenes();
		for (Gene gene : genes) {
			double score;
			if (queryGenes.containsKey(gene.getNode().getId())) {
				score = Double.MAX_VALUE;
			} else {
				score = scores.get(gene) * 100;
			}
			result.add(new GeneEntry(gene, score));
		}
		return result;
	}

	private List<Network<?>> populateNetworks(final ViewState options, List<Group<?, ?>> groups) {
		List<Network<?>> result = new ArrayList<Network<?>>();
		for (Group<?, ?> group : groups) {
			result.addAll(group.getNetworks());
		}
		
		Collections.sort(result, new Comparator<Network<?>>() {
			@Override
			public int compare(Network<?> network1, Network<?> network2) {
				// Sort by group weight first
				Group<?, ?> group1 = options.getGroup(network1);
				Group<?, ?> group2 = options.getGroup(network2);
				int result = Double.compare(group2.getWeight(), group1.getWeight());
				if (result != 0) {
					return result;
				}
				// Then group name
				result = String.CASE_INSENSITIVE_ORDER.compare(group1.getName(), group2.getName());
				if (result != 0) {
					return result;
				}
				// Then network weight
				return Double.compare(network2.getWeight(), network1.getWeight());
			}
		});
		return result;
	}

	public List<GeneEntry> getGenes() {
		return genes;
	}
	
	public List<Network<?>> getNetworks() {
		return networks;
	}
	
	public String getDataVersion() {
		return dataVersion;
	}

	public ViewState getViewState() {
		return options;
	}
	
	public OntologyCategory getCategory(String name) {
		return categories.get(name);
	}
	
	public static class GeneEntry {
		Gene gene;
		double score;
		
		public GeneEntry(Gene gene, double score) {
			this.gene = gene;
			this.score = score;
		}
		
		public Gene getGene() {
			return gene;
		}
		
		public double getScore() {
			return score;
		}
	}
}
