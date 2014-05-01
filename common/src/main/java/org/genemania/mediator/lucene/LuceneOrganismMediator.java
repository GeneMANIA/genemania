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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.LazyLoader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.search.Searcher;
import org.genemania.domain.Gene;
import org.genemania.domain.InteractionNetwork;
import org.genemania.domain.Organism;
import org.genemania.exception.DataStoreException;
import org.genemania.mediator.NodeCursor;
import org.genemania.mediator.OrganismMediator;

public class LuceneOrganismMediator extends LuceneMediator implements OrganismMediator {

	public LuceneOrganismMediator(Searcher searcher, Analyzer analyzer) {
		super(searcher, analyzer);
	}

	public NodeCursor createNodeCursor(long organismId) {
		final List<ObjectProvider<Document>> result = new ArrayList<ObjectProvider<Document>>();
		search(String.format("%s:%d", NODE_ORGANISM_ID, organismId), new AbstractCollector() {
			@SuppressWarnings("unchecked")
			@Override
			public void handleHit(final int doc) {
				result.add((ObjectProvider<Document>) Enhancer.create(ObjectProvider.class, new LazyLoader() {
					public Object loadObject() throws Exception {
						return new ObjectProvider<Document>(searcher.doc(doc));
					}
				}));
			}
		});
		return new LuceneNodeCursor(result);
	}

	public List<Organism> getAllOrganisms() {
		return createOrganisms();
	}

	private List<Organism> createOrganisms() {
		final List<Organism> result = new ArrayList<Organism>();
		search(String.format("%s:%s", LuceneMediator.TYPE, LuceneMediator.ORGANISM), new AbstractCollector() {
			@Override
			public void handleHit(int doc) {
				try {
					Document document = searcher.doc(doc);
					result.add(createOrganism(document));
				} catch (CorruptIndexException e) {
					log(e);
				} catch (IOException e) {
					log(e);
				}
			}
		});
		return result;
	}

	public List<Gene> getDefaultGenes(long organismId) {
		final Organism organism = createOrganism(organismId);
		final List<Gene> result = new ArrayList<Gene>();
		search(String.format("+%s:%s +%s:%s", LuceneMediator.GENE_ORGANISM_ID, String.valueOf(organismId), LuceneMediator.GENE_DEFAULT_SELECTED, String.valueOf(true)), new AbstractCollector() {
			@Override
			public void handleHit(int doc) {
				try {
					Document document = searcher.doc(doc);
					result.add(createGene(document, null, organism, null));
				} catch (CorruptIndexException e) {
					log(e);
				} catch (IOException e) {
					log(e);
				}
			}
		});
		return result;
	}

	public Organism getOrganism(long organismId) {
		return createOrganism(organismId);
	}

	public List<?> hqlSearch(String queryString) {
		return null;
	}

	public List<InteractionNetwork> getDefaultNetworks(long organismId) throws DataStoreException {
		final Set<Long> groupIds = new HashSet<Long>();
		search(String.format("%s:%s", LuceneMediator.GROUP_ORGANISM_ID, String.valueOf(organismId)), new AbstractCollector() {
			@Override
			public void handleHit(int doc) {
				try {
					Document document = searcher.doc(doc);
					groupIds.add(Long.parseLong(document.get(LuceneMediator.GROUP_ID)));
				} catch (CorruptIndexException e) {
					log(e);
				} catch (IOException e) {
					log(e);
				}
			}
		});

		final List<InteractionNetwork> result = new ArrayList<InteractionNetwork>();
		search(String.format("%s:%s", LuceneMediator.NETWORK_DEFAULT_SELECTED, String.valueOf(true)), new AbstractCollector() {
			@Override
			public void handleHit(int doc) {
				try {
					Document document = searcher.doc(doc);
					long groupId = Long.parseLong(document.get(LuceneMediator.NETWORK_GROUP_ID));
					if (groupIds.contains(groupId)) {
						result.add(createNetwork(document));
					}
				} catch (CorruptIndexException e) {
					log(e);
				} catch (IOException e) {
					log(e);
				}
			}
		});
		return result;
	}
	
	public Organism getOrganismForGroup(long groupId) throws DataStoreException {
        final Organism[] result = new Organism[1];
        search(String.format("+%s:\"%d\"", LuceneMediator.GROUP_ID, groupId), new AbstractCollector() {
            @Override
            public void handleHit(int doc) {
                try {
                    Document document = searcher.doc(doc);
                    long organismId = Long.parseLong(document.get(LuceneMediator.GROUP_ORGANISM_ID));
					result[0] = createOrganism(organismId);
                } catch (CorruptIndexException e) {
                    log(e);
                } catch (IOException e) {
                    log(e);
                }
            }
        });
        return result[1];
	}
}
