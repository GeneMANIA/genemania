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

package org.genemania.plugin.data.lucene;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TermQuery;
import org.genemania.domain.InteractionNetwork;
import org.genemania.exception.ApplicationException;
import org.genemania.mediator.NetworkMediator;
import org.genemania.mediator.lucene.AbstractCollector;
import org.genemania.mediator.lucene.LuceneMediator;
import org.genemania.plugin.LogUtils;

public class Queries {
	public long getNextAvailableUserId(final Searcher searcher, String type, final String field) throws ApplicationException {
		// First available id is -2 because -1 is reserved, and counts down.
		final long[] minId = new long[] { -1L };
		
		Query query = new TermQuery(new Term(LuceneMediator.TYPE, type));
		try {
			searcher.search(query, new AbstractCollector() {
				@Override
				public void handleHit(int doc) {
					try {
						Document document = searcher.doc(doc);
						long id = Long.parseLong(document.get(field));
						minId[0] = Math.min(minId[0], id);
					} catch (IOException e) {
						LogUtils.log(getClass(), e);
					}
				}
			});
			return minId[0] - 1;
		} catch (IOException e) {
			throw new ApplicationException(e);
		}
	}

	public long getNextAvailableCoreId(final Searcher searcher, String type, final String field) throws ApplicationException {
		// First available id is 1, and counts up.
		final long[] maxId = new long[] { 0L };
		
		Query query = new TermQuery(new Term(LuceneMediator.TYPE, type));
		try {
			searcher.search(query, new AbstractCollector() {
				@Override
				public void handleHit(int doc) {
					try {
						Document document = searcher.doc(doc);
						long id = Long.parseLong(document.get(field));
						maxId[0] = Math.max(maxId[0], id);
					} catch (IOException e) {
						LogUtils.log(getClass(), e);
					}
				}
			});
			return maxId[0] + 1;
		} catch (IOException e) {
			throw new ApplicationException(e);
		}
	}

	public long getOrganismIdFromGroup(final Searcher searcher, long groupId) throws ApplicationException {
		String term = getUniqueTerm(searcher, LuceneMediator.GROUP_ID, String.valueOf(groupId), LuceneMediator.GROUP_ORGANISM_ID);
		if (term == null) {
			throw new ApplicationException();
		}
		return Long.parseLong(term);
	}
	
	public long getGroupIdFromNetwork(final Searcher searcher, long networkId) throws ApplicationException {
		String term = getUniqueTerm(searcher, LuceneMediator.NETWORK_ID, String.valueOf(networkId), LuceneMediator.NETWORK_GROUP_ID);
		if (term == null) {
			throw new ApplicationException();
		}
		return Long.parseLong(term);
	}
	
	public Collection<InteractionNetwork> getUserDefinedNetworks(final Searcher searcher, final NetworkMediator mediator) throws ApplicationException {
		final List<InteractionNetwork> networks = new ArrayList<InteractionNetwork>();
		Query query = new TermQuery(new Term(LuceneMediator.TYPE, LuceneMediator.NETWORK));
		try {
			searcher.search(query, new AbstractCollector() {
				@Override
				public void handleHit(int doc) {
					try {
						Document document = searcher.doc(doc);
						long id = Long.parseLong(document.get(LuceneMediator.NETWORK_ID));
						if (id < -1) {
							networks.add(mediator.getNetwork(id));
						}
					} catch (IOException e) {
						LogUtils.log(getClass(), e);
					}
				}
			});
			return networks;
		} catch (IOException e) {
			throw new ApplicationException(e);
		}
	}

	public String getUniqueTerm(final Searcher searcher, String lookupField, String lookupValue, final String targetField) throws ApplicationException {
		final String[] term = new String[1];
		Query query = new TermQuery(new Term(lookupField, lookupValue));
		try {
			searcher.search(query, new AbstractCollector() {
				@Override
				public void handleHit(int doc) {
					try {
						Document document = searcher.doc(doc);
						term[0] = document.get(targetField);
					} catch (IOException e) {
						LogUtils.log(getClass(), e);
					}
				}
			});
			return term[0];
		} catch (IOException e) {
			throw new ApplicationException(e);
		}
	}
}
