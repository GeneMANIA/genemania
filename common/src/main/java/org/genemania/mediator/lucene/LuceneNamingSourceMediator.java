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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.Searcher;
import org.genemania.domain.GeneNamingSource;

public class LuceneNamingSourceMediator extends LuceneMediator {
	public LuceneNamingSourceMediator(Searcher searcher, Analyzer analyzer) {
		super(searcher, analyzer);
	}
	
	public List<GeneNamingSource> getAllNamingSources() {
		final List<GeneNamingSource> result = new ArrayList<GeneNamingSource>();
		search(String.format("%s:%s", TYPE, NAMINGSOURCE), new AbstractCollector() {
			@Override
			public void handleHit(int doc) {
				try {
					result.add(createNamingSource(searcher.doc(doc)));
				} catch (IOException e) {
					log(e);
				}
			}
		});
		Collections.sort(result, new Comparator<GeneNamingSource>() {
			public int compare(GeneNamingSource source1, GeneNamingSource source2) {
				return source2.getRank() - source1.getRank();
			}
		});
		return result;
	}
}
