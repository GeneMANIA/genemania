/**
 * This file is part of GeneMANIA.
 * Copyright (C) 2010 University of Toronto.
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

package org.genemania.data.normalizer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.genemania.data.classification.IGeneClassificationHandler;
import org.genemania.data.classification.IGeneClassifier;
import org.genemania.domain.Organism;
import org.genemania.exception.ApplicationException;

/**
 * Identifies the most likely organism(s) associated with the genes fed to
 * this classifier.
 */
public class OrganismClassifier {
	private static final double DEFAULT_TOLERANCE = 0.25;
	private IGeneClassifier classifier;
	private Map<Long, Integer> votes;
	private Set<String> seenSymbols;
	private String delimiter;
	private Map<Long, Set<Integer>> idColumnsByOrganism;
	
	public OrganismClassifier(IGeneClassifier classifier) {
		this.classifier = classifier;
		votes = new HashMap<Long, Integer>();
		seenSymbols = new HashSet<String>();
		delimiter = "\t"; //$NON-NLS-1$
		idColumnsByOrganism = new HashMap<Long, Set<Integer>>();
	}

	public void classify(DataImportSettings result, Reader source, int maximumLinesToSample) throws IOException, ApplicationException {
		BufferedReader reader = new BufferedReader(source);
		for (int i = 0; i < maximumLinesToSample; i++) {
			String line = reader.readLine();
			if (line == null) {
				break;
			}
			String[] parts = line.split(delimiter);
			for (int column = 0; column < parts.length; column++) {
				addGene(parts[column], column);
			}
		}
		if (votes.size() == 0) {
			result.setOrganism(null);
		} else {
			Organism organism = new Organism();
			long organismId = getMostLikelyOrganismIds().get(0).organismId;
			organism.setId(organismId);
			result.setOrganism(organism);
			result.setOrganismConfidence(getScore(organismId));
			List<Integer> idColumns = new ArrayList<Integer>(idColumnsByOrganism.get(organismId));
			Collections.sort(idColumns);
			result.setIdColumns(idColumns);
		}
	}
	
	public double getScore(long organismId) {
		if (!votes.containsKey(organismId)) {
			return 0;
		}
		return (double) votes.get(organismId) / seenSymbols.size();
	}
	
	public List<Match> getMostLikelyOrganismIds() {
		return getMostLikelyOrganismIds(DEFAULT_TOLERANCE);
	}
	
	public List<Match> getMostLikelyOrganismIds(double tolerance) {
		List<Match> ids = new ArrayList<Match>();
		int maxVotes = 0;
		for (Integer organismVotes : votes.values()) {
			maxVotes = Math.max(maxVotes, organismVotes);
		}
		for (Entry<Long, Integer> entry : votes.entrySet()) {
			double percentDifference = (maxVotes - entry.getValue()) / (double) maxVotes;
			if (percentDifference <= tolerance) {
				double score = (double) entry.getValue() / maxVotes;
				ids.add(new Match(entry.getKey(), score));
			}
		}
		
		Collections.sort(ids, new Comparator<Match>() {
			public int compare(Match o1, Match o2) {
				if (o2.score == o1.score) {
					return 0;
				}
				return o2.score > o1.score ? 1 : -1;
			}
		});
		
		return ids;
	}
	
	public void addGene(String symbol, final int column) throws ApplicationException {
		if (seenSymbols.contains(symbol)) {
			return;
		}
		seenSymbols.add(symbol);
		
		classifier.classify(symbol, new IGeneClassificationHandler() {
			public void handleClassification(String symbol, long organismId) {
				if (votes.containsKey(organismId)) {
					votes.put(organismId, votes.get(organismId) + 1);
				} else {
					votes.put(organismId, 1);
				}
				Set<Integer> idColumns = idColumnsByOrganism.get(organismId);
				if (idColumns == null) {
					idColumns = new HashSet<Integer>();
					idColumnsByOrganism.put(organismId, idColumns);
				}
				idColumns.add(column);
			}
		});
	}
	
	public boolean hasUniqueMatch() {
		int perfectMatches = 0;
		for (int hits : votes.values()) {
			if (hits == seenSymbols.size()) {
				perfectMatches++;
			}
		}
		return perfectMatches == 1;
	}
	
	public static class Match {
		public long organismId;
		public double score;
		
		public Match(long organismId, double score) {
			this.organismId = organismId;
			this.score = score;
		}
	}
}
