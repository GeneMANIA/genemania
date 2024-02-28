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
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.search.Searcher;
import org.genemania.domain.InteractionNetwork;
import org.genemania.domain.InteractionNetworkGroup;
import org.genemania.mediator.InteractionCursor;
import org.genemania.mediator.NetworkMediator;

public class LuceneNetworkMediator extends LuceneMediator implements NetworkMediator {

	public LuceneNetworkMediator(Searcher searcher, Analyzer analyzer) {
		super(searcher, analyzer);
	}

	@Override
	public InteractionCursor createInteractionCursor(long networkId) {
		return null;
	}

	@Override
	public List<InteractionNetwork> getAllNetworks() {
		var result = new ArrayList<InteractionNetwork>();
		
		search(String.format("%s:\"%s\"", LuceneMediator.TYPE, LuceneMediator.NETWORK), new AbstractCollector() {
			@Override
			public void handleHit(int doc) {
				try {
					var document = searcher.doc(doc);
					result.add(createNetwork(document));
				} catch (CorruptIndexException e) {
					log(e);
				} catch (IOException e) {
					log(e);
				}
			}
		});
		
		return result;
	}

	@Override
	public InteractionNetwork getNetwork(long networkId) {
		return createNetwork(networkId);
	}

	@Override
	public InteractionNetworkGroup getNetworkGroupByName(String groupName, long organismId) {
		return createNetworkGroup(organismId, groupName);
	}

	@Override
	public void saveNetwork(InteractionNetwork network) {
		// Nothing to do here...
	}

	@Override
	public void saveNetworkGroup(InteractionNetworkGroup group) {
		// Nothing to do here...
	}

	@Override
	public List<InteractionNetworkGroup> getNetworkGroupsByOrganism(long organismId) {
		var groups = new ArrayList<InteractionNetworkGroup>(createNetworkGroups(organismId));
		
		return groups;
	}

	@Override
	public InteractionNetworkGroup getNetworkGroupForNetwork(long networkId) {
		var result = new InteractionNetworkGroup[1];
		
		search(String.format("+%s:\"%d\"", LuceneMediator.NETWORK_ID, networkId), new AbstractCollector() {
			@Override
			public void handleHit(int doc) {
				try {
					var document = searcher.doc(doc);
					var groupId = Long.parseLong(document.get(LuceneMediator.NETWORK_GROUP_ID));
					result[0] = createNetworkGroup(groupId);
				} catch (CorruptIndexException e) {
					log(e);
				} catch (IOException e) {
					log(e);
				}
			}
		});
		
		return result[0];
	}

	@Override
	public boolean isValidNetwork(long organismId, long networkId) {
		var groupId = new Long[1];
		
		search(String.format("+%s:\"%d\"", LuceneMediator.NETWORK_ID, networkId), new AbstractCollector() {
			@Override
			public void handleHit(int doc) {
				try {
					var document = searcher.doc(doc);
					groupId[0] = Long.parseLong(document.get(LuceneMediator.NETWORK_GROUP_ID));
				} catch (CorruptIndexException e) {
					log(e);
				} catch (IOException e) {
					log(e);
				}
			}
		});
		
		if (groupId[0] == null)
			return false;

		var organism = new Long[1];
		
		search(String.format("+%s:\"%d\"", LuceneMediator.GROUP_ID, groupId[0]), new AbstractCollector() {
			@Override
			public void handleHit(int doc) {
				try {
					var document = searcher.doc(doc);
					organism[0] = Long.parseLong(document.get(LuceneMediator.GROUP_ORGANISM_ID));
				} catch (CorruptIndexException e) {
					log(e);
				} catch (IOException e) {
					log(e);
				}
			}
		});
		
		if (organism[0] == null)
			return false;
		
		return organism[0].equals(organismId);
	}
}
