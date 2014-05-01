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

import java.io.IOException;
import java.io.StringReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery.TooManyClauses;
import org.genemania.completion.CompletionConsumer;
import org.genemania.completion.CompletionProvider;
import org.genemania.domain.Gene;
import org.genemania.domain.Organism;
import org.genemania.mediator.lucene.AbstractCollector;
import org.genemania.mediator.lucene.LuceneGeneMediator;
import org.genemania.mediator.lucene.LuceneMediator;

public class GeneCompletionProvider2 implements CompletionProvider {
	private Organism organism;
	private LuceneGeneMediator mediator;
	private Searcher searcher;
	private Analyzer analyzer;
	
	public GeneCompletionProvider2(Searcher searcher, Analyzer analyzer, Organism organism) {
		this.organism = organism;
		this.searcher = searcher;
		this.mediator = new LuceneGeneMediator(searcher, analyzer);
		this.analyzer = analyzer;
	}	
	
	public void close() {
	}

	public void computeProposals(final CompletionConsumer consumer, String queryString) {
		try {
			if (queryString.length() == 0) {
				return;
			}
			
			TokenStream stream = analyzer.tokenStream(LuceneGeneMediator.GENE_SYMBOL, new StringReader(queryString));
			TermAttribute term = stream.getAttribute(TermAttribute.class);
			if (!stream.incrementToken()) {
				return;
			}
			
			BooleanQuery query = new BooleanQuery();
			query.add(new TermQuery(new Term(LuceneMediator.GENE_ORGANISM_ID, String.valueOf(organism.getId()))), Occur.MUST);
			query.add(new PrefixQuery(new Term(LuceneMediator.GENE_SYMBOL, term.term())), Occur.MUST);
			searcher.search(query, new AbstractCollector() {
				@Override
				public void handleHit(int id) {
					try {
						Document document = searcher.doc(id);
						consumer.consume(document.get(LuceneMediator.GENE_SYMBOL));
					} catch (IOException e) {
						log(e);
					}
				}
			});
		} catch (IOException e) {
			log(e);
		} catch (TooManyClauses e) {
			consumer.tooManyCompletions();
		} finally {
			consumer.finish();
		}
	}

	public String getCanonicalForm(String proposal) {
		return mediator.getCanonicalSymbol(organism.getId(), proposal);
	}

	public boolean isValid(String proposal) {
		return mediator.isValid(organism.getId(), proposal);
	}

	public Gene getGene(String symbol) {
		return mediator.getGeneForSymbol(organism, symbol);
	}

	public Long getNodeId(String symbol) {
		return mediator.getNodeId(organism.getId(), symbol);
	}

	private void log(IOException e) {
		e.printStackTrace();
	}
}
