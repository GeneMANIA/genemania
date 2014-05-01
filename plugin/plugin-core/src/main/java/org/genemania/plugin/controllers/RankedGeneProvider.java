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

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.genemania.domain.Gene;
import org.genemania.domain.GeneNamingSource;
import org.genemania.domain.Node;

public class RankedGeneProvider implements IGeneProvider {
	protected final Map<Long, Integer> rankings;
	private final Comparator<Gene> comparator;

	public RankedGeneProvider(List<GeneNamingSource> allSources, List<GeneNamingSource> namingSourcePreferences) {
		rankings = createNamingSourceRankings(allSources, namingSourcePreferences);
		comparator = createComparator();
	}
	
	public Gene getGene(Node node) {
		return Collections.min(node.getGenes(), comparator);
	}
	
	Map<Long, Integer> createNamingSourceRankings(List<GeneNamingSource> baseRankings, List<GeneNamingSource> userPreferences) {
		HashMap<Long, Integer> rankings = new HashMap<Long, Integer>();
		for (GeneNamingSource source : baseRankings) {
			rankings.put(source.getId(), (int) source.getRank());
		}
		Integer maxRank = Collections.max(rankings.values());
		if (maxRank == null) {
			maxRank = 0;
		}
		int nextRank = maxRank + userPreferences.size();
		for (GeneNamingSource source : userPreferences) {
			rankings.put(source.getId(), nextRank);
			nextRank--;
		}
		return rankings;
	}

	protected Comparator<Gene> createComparator() {
		return new Comparator<Gene>() {
			public int compare(Gene gene1, Gene gene2) {
				GeneNamingSource source1 = gene1.getNamingSource();
				GeneNamingSource source2 = gene2.getNamingSource();
				Integer rank1 = rankings.get(source1.getId());
				Integer rank2 = rankings.get(source2.getId());
				return rank2 - rank1;
			}
		};
	}
}
