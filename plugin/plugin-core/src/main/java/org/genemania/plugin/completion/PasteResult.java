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
/**
 * 
 */
package org.genemania.plugin.completion;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.genemania.domain.Gene;
import org.genemania.plugin.NetworkUtils;

public class PasteResult {
	private final Set<String> unrecognizedSymbols;
	private final Map<String, Set<String>> synonyms;
	private final Map<String, Integer> duplicates;
	private final NetworkUtils networkUtils;
	
	public PasteResult(NetworkUtils networkUtils) {
		unrecognizedSymbols = new HashSet<String>();
		synonyms = new HashMap<String, Set<String>>();
		duplicates = new HashMap<String, Integer>();
		this.networkUtils = networkUtils;
	}
	
	public Map<String, Integer> getDuplicates() {
		Map<String, Integer> result = new HashMap<String, Integer>();
		for (Entry<String, Integer> entry : duplicates.entrySet()) {
			Integer count = entry.getValue();
			if (count > 1) {
				result.put(entry.getKey(), count);
			}
		}
		return result;
	}
	
	public Map<String, Set<String>> getSynonyms() {
		Map<String, Set<String>> result = new HashMap<String, Set<String>>();
		for (Entry<String, Set<String>> entry : synonyms.entrySet()) {
			String key = entry.getKey();
			Set<String> value = entry.getValue();
			if (value.size() > 1) {
				result.put(key, value);
			}
		}
		return result;
	}

	public Collection<String> getUnrecognizedSymbols() {
		return Collections.unmodifiableSet(unrecognizedSymbols);
	}

	public boolean hasIssues() {
		if (unrecognizedSymbols.size() > 0) {
			return true;
		}
		for (Set<String> geneSynonyms : synonyms.values()) {
			if (geneSynonyms.size() > 1) {
				return true;
			}
		}
		for (Integer count : duplicates.values()) {
			if (count > 1) {
				return true;
			}
		}
		return false;
	}

	public void addUnrecognizedSymbol(String symbol) {
		unrecognizedSymbols.add(symbol);
	}
	
	public void addSynonym(Gene gene) {
		Gene preferredGene = networkUtils.getPreferredGene(gene.getNode());
		String preferredSymbol = preferredGene.getSymbol();
		Set<String> geneSynonyms = synonyms.get(preferredSymbol);
		if (geneSynonyms == null) {
			geneSynonyms = new HashSet<String>();
			synonyms.put(preferredSymbol, geneSynonyms);
		}
		geneSynonyms.add(gene.getSymbol());
	}
	
	public void addDuplicate(String symbol) {
		Integer count = duplicates.get(symbol);
		if (count == null) {
			count = 0;
		}
		count++;
		duplicates.put(symbol, count);
	}
}