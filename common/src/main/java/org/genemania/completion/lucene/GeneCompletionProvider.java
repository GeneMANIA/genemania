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

package org.genemania.completion.lucene;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.genemania.domain.Gene;
import org.genemania.mediator.lucene.AbstractCollector;
import org.genemania.mediator.lucene.LuceneGeneMediator;

@Deprecated
public class GeneCompletionProvider extends LuceneCompletionProvider {

	// __[constructors]________________________________________________________
	// Spring IOC support  
	private GeneCompletionProvider() {
		this(null, null);
	}

	public GeneCompletionProvider(IndexReader reader, Analyzer analyzer) {
		super(reader, GeneIndexBuilder.GENE_FIELD, analyzer);
	}
	
	// __[public interface]____________________________________________________
	public Set<String> getSynonyms(String symbol) {
		try {
			Query query = parser.parse(String.format("\"%s\"", symbol));
			final Long[] node = new Long[] {-1L};
			searcher.search(query, new AbstractCollector() {
				@Override
				public void handleHit(int id) {
					try {
						Document document = searcher.doc(id);
						node[0] = Long.parseLong(document.get(GeneIndexBuilder.NODE_ID_FIELD));
					} catch (IOException e) {
						log(e);
					}
				}
			});
			if (node[0] == -1L) {
				return Collections.emptySet();
			}
			return getSynonyms(node[0]);
		} catch (ParseException e) {
			log(e);
		} catch (IOException e) {
			log(e);
		}
		return Collections.emptySet();
	}

	public Set<String> getSynonyms(Long nodeId) {
		try {
			Query query = parser.parse(String.format("%s:%d", GeneIndexBuilder.NODE_ID_FIELD, nodeId));
			final Set<String> synonyms = new HashSet<String>();
			searcher.search(query, new AbstractCollector() {
				@Override
				public void handleHit(int id) {
					try {
						Document document = searcher.doc(id);
						synonyms.add(document.get(GeneIndexBuilder.GENE_FIELD));
					} catch (IOException e) {
						log(e);
					}
				}
			});
			return synonyms;
		} catch (ParseException e) {
			log(e);
		} catch (IOException e) {
			log(e);
		}
		return Collections.emptySet();
	}
	
	public Long getNodeId(String symbol) {
		try {
			TokenStream tokens = analyze(symbol);
			PhraseQuery query = new PhraseQuery();
			tokens.reset();
			while (tokens.incrementToken()) {
				TermAttribute term = tokens.getAttribute(TermAttribute.class);
				query.add(new Term(GeneIndexBuilder.GENE_FIELD, term.term()));
			}
			tokens.end();
			tokens.close();
			
			final Set<Long> nodes = new HashSet<Long>();
			searcher.search(query, new AbstractCollector() {
				@Override
				public void handleHit(int id) {
					try {
						Document document = searcher.doc(id);
						nodes.add(Long.parseLong(document.get(GeneIndexBuilder.NODE_ID_FIELD)));
					} catch (IOException e) {
						log(e);
					}
				}
			});
			if (nodes.size() > 0) {
				return nodes.iterator().next();
			}
		} catch (IOException e) {
			log(e);
		}
		return null;
	}
	
	// throws an NPE - needs to be revisited
	public Gene getGene(String symbol) {
		Analyzer analyzer = parser.getAnalyzer();
		LuceneGeneMediator mediator = new LuceneGeneMediator(searcher, analyzer);
		return mediator.getGeneForSymbol(null, symbol);
	}
	
}
