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
import java.io.StringReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.BooleanQuery.TooManyClauses;
import org.apache.lucene.util.Version;
import org.genemania.completion.CompletionConsumer;
import org.genemania.completion.CompletionProvider;
import org.genemania.mediator.lucene.AbstractCollector;

@Deprecated
public abstract class LuceneCompletionProvider implements CompletionProvider {
	protected final IndexSearcher searcher;
	protected final QueryParser parser;
	protected final IndexReader reader;
	protected final String defaultField;
	private final Analyzer analyzer;

	public LuceneCompletionProvider(IndexReader reader, String defaultField, Analyzer analyzer) {
		this.reader = reader;
		if (reader != null) {
			this.searcher = new IndexSearcher(reader);
		} else {
			searcher = null;
		}
		if (analyzer != null) {
			this.parser = new QueryParser(Version.LUCENE_29, defaultField, analyzer);
		} else {
			this.parser = null;
		}
		this.defaultField = defaultField;
		this.analyzer = analyzer;
	}

	public void computeProposals(final CompletionConsumer consumer, String queryString) {
		try {
			if (queryString.length() == 0) {
				return;
			}
			Query query = new PrefixQuery(new Term(defaultField, queryString));
			searcher.search(query, new AbstractCollector() {
				@Override
				public void handleHit(int id) {
					try {
						Document document = searcher.doc(id);
						consumer.consume(document.get(defaultField));
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
	
	protected void log(Throwable e) {
		// Eat these for now.  We might want to log it later if we
		// run into problems.
	}

	public void close() {
		try {
			searcher.close();
		} catch (IOException e) {
			log(e);
		}
		try {
			reader.close();
		} catch (IOException e) {
			log(e);
		}
	}

	public boolean isValid(String proposal) {
		return getCanonicalForm(proposal) != null;
	}

	public String getCanonicalForm(String proposal) {
		try {
			if (proposal.length() == 0) {
				return null;
			}
			Query query = parser.parse(String.format("\"%s\"", proposal));
			TopDocs topDocs = searcher.search(query, 1);
			if (topDocs.totalHits > 0) {
				Document document = searcher.doc(topDocs.scoreDocs[0].doc);
				return document.get(defaultField);
			} else {
				return null;
			}
		} catch (IOException e) {
			log(e);
		} catch (ParseException e) {
			log(e);
		} catch (TooManyClauses e) {
			log(e);
		}
		return null;
	}
	
	public TokenStream analyze(String text) throws IOException {
		return analyzer.reusableTokenStream(defaultField, new StringReader(text));
	}
}