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

package org.genemania.mediator.lucene;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanQuery.TooManyClauses;
import org.apache.lucene.search.Searcher;
import org.genemania.completion.lucene.GeneIndexBuilder;
import org.genemania.domain.Gene;
import org.genemania.domain.GeneData;
import org.genemania.domain.GeneNamingSource;
import org.genemania.domain.Organism;
import org.genemania.mediator.GeneMediator;

public class LuceneGeneMediator extends LuceneMediator implements GeneMediator {

	public LuceneGeneMediator(Searcher searcher, Analyzer analyzer) {
		super(searcher, analyzer);
	}

	@Override
	public GeneNamingSource findNamingSourceByName(String namingSourceName) {
		return createNamingSource(namingSourceName);
	}

	@Override
	public List<Gene> getAllGenes(long organismId) {
		return createGenes(organismId);
	}

	@Override
	public Gene getGeneForSymbol(Organism organism, String geneSymbol) {
		if (organism == null) {
			return createGene(geneSymbol);
		} else {
			return createGene(organism.getId(), geneSymbol);
		}
	}

	@Override
	public List<Gene> getGenes(List<String> geneSymbols, long organismId) {
		return createGenes(organismId, geneSymbols);
	}

	@Override
	public void updateGeneData(Organism organism, String geneSymbol, GeneData geneData) {
	}

	@Override
	public boolean isValid(long organismId, String proposal) {
		return getCanonicalSymbol(organismId, proposal) != null;
	}

	@Override
	public String getCanonicalSymbol(long organismId, String proposal) {
		try {
			if (proposal.length() == 0)
				return null;
			
			var topDocs = search(String.format("+%s:\"%d\" +%s:\"%s\"", LuceneMediator.GENE_ORGANISM_ID, organismId, LuceneMediator.GENE_SYMBOL, QueryParser.escape(proposal)), 1);
			
			if (topDocs != null && topDocs.totalHits > 0) {
				var document = searcher.doc(topDocs.scoreDocs[0].doc);
				return document.get(LuceneMediator.GENE_SYMBOL);
			} else {
				return null;
			}
		} catch (IOException e) {
			log(e);
		} catch (TooManyClauses e) {
			log(e);
		}
		
		return null;
	}

	@Override
	public Set<String> getSynonyms(long organismId, String symbol) {
		Long nodeId = getNodeId(organismId, symbol);
		
		if (nodeId == null)
			return Collections.emptySet();
		
		return getSynonyms(organismId, nodeId);
	}

	@Override
	public Set<String> getSynonyms(long organismId, long nodeId) {
		var query = String.format("+%s:\"%d\" +%s:\"%d\"", LuceneMediator.GENE_ORGANISM_ID, organismId, LuceneMediator.GENE_NODE_ID, nodeId);
		var synonyms = new HashSet<String>();
		
		search(query, new AbstractCollector() {
			@Override
			public void handleHit(int id) {
				try {
					var document = searcher.doc(id);
					synonyms.add(document.get(GeneIndexBuilder.GENE_FIELD));
				} catch (IOException e) {
					log(e);
				}
			}
		});
		
		return synonyms;
	}
	
	@Override
	public Long getNodeId(long organismId, String symbol) {
		try {
			var query = String.format("+%s:\"%d\" +%s:\"%s\"", LuceneMediator.GENE_ORGANISM_ID, organismId, LuceneMediator.GENE_SYMBOL, QueryParser.escape(symbol));
			var topDocs = search(query, 1);
			
			if (topDocs != null && topDocs.totalHits >= 1) {
				var document = searcher.doc(topDocs.scoreDocs[0].doc);
				return Long.parseLong(document.get(GeneIndexBuilder.NODE_ID_FIELD));
			}
		} catch (IOException e) {
			log(e);
		}
		
		return null;
	}
}
