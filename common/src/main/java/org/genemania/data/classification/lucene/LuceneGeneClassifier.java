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

package org.genemania.data.classification.lucene;

import java.io.IOException;
import java.io.StringReader;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Searcher;
import org.genemania.data.classification.IGeneClassificationHandler;
import org.genemania.data.classification.IGeneClassifier;
import org.genemania.exception.ApplicationException;
import org.genemania.mediator.lucene.AbstractCollector;
import org.genemania.mediator.lucene.LuceneMediator;

public class LuceneGeneClassifier implements IGeneClassifier {
	private Searcher searcher;
	private Analyzer analyzer;

	public LuceneGeneClassifier(Searcher searcher, Analyzer analyzer) {
		this.searcher = searcher;
		this.analyzer = analyzer;
	}

	public void classify(final String symbol, final IGeneClassificationHandler handler) throws ApplicationException {
		try {
			TokenStream tokens = analyze(symbol);
			PhraseQuery query = new PhraseQuery();
			tokens.reset();
			while (tokens.incrementToken()) {
				TermAttribute term = tokens.getAttribute(TermAttribute.class);
				query.add(new Term(LuceneMediator.GENE_SYMBOL, term.term()));
			}
			tokens.end();
			tokens.close();
			
			searcher.search(query, new AbstractCollector() {
				@Override
				public void handleHit(int doc) {
					try {
						Document document = searcher.doc(doc);
						long organismId = Long.parseLong(document.get(LuceneMediator.GENE_ORGANISM_ID));
						handler.handleClassification(symbol, organismId);
					} catch (IOException e) {
						log(e);
					}
				}
			});
		} catch (IOException e) {
			throw new ApplicationException(e);
		}
	}

	void log(Throwable t) {
		Logger logger = Logger.getLogger(LuceneGeneClassifier.class);
		logger.error(t.getMessage(), t);
	}
	TokenStream analyze(String text) throws IOException {
		return analyzer.reusableTokenStream(LuceneMediator.GENE_SYMBOL, new StringReader(text));
	}
}
