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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TermQuery;
import org.genemania.domain.Gene;
import org.genemania.domain.GeneData;
import org.genemania.domain.GeneNamingSource;
import org.genemania.domain.InteractionNetwork;
import org.genemania.domain.InteractionNetworkGroup;
import org.genemania.domain.NetworkMetadata;
import org.genemania.domain.Node;
import org.genemania.domain.Ontology;
import org.genemania.domain.Organism;
import org.genemania.exception.ApplicationException;
import org.genemania.mediator.lucene.AbstractCollector;
import org.genemania.mediator.lucene.LuceneMediator;
import org.genemania.mediator.lucene.exporter.Generic2LuceneExporter;
import org.genemania.plugin.data.IModelWriter;

public class LuceneModelWriter implements IModelWriter {

	private final IndexWriter writer;
	private final Generic2LuceneExporter exporter;
	
	public LuceneModelWriter(IndexWriter indexWriter) {
		writer = indexWriter;
		exporter = new Generic2LuceneExporter();
	}

	@Override
	public void close() throws ApplicationException {
		try {
			writer.commit();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void addNetwork(InteractionNetwork network, InteractionNetworkGroup group) throws ApplicationException {
		NetworkMetadata metadata = network.getMetadata();
		try {
			exporter.exportNetwork(writer, new String[] {
				String.valueOf(network.getId()),
				network.getName(),
				String.valueOf(metadata.getId()),
				network.getDescription(),
				network.isDefaultSelected() ? "true" : "0",  //$NON-NLS-1$//$NON-NLS-2$
				String.valueOf(group.getId()),
			});
			exporter.exportNetworkMetadata(writer, new String[] {
				String.valueOf(metadata.getId()),
				filter(metadata.getSource()),
				filter(metadata.getReference()),
				filter(metadata.getPubmedId()),
				filter(metadata.getAuthors()),
				filter(metadata.getPublicationName()),
				filter(metadata.getYearPublished()),
				filter(metadata.getProcessingDescription()),
				filter(metadata.getNetworkType()),
				filter(metadata.getAlias()),
				String.valueOf(metadata.getInteractionCount()),
				filter(metadata.getDynamicRange()),
				filter(metadata.getEdgeWeightDistribution()),
				String.valueOf(metadata.getAccessStats()),
				filter(metadata.getComment()),
				filter(metadata.getOther()),
				filter(metadata.getTitle()),
				filter(metadata.getUrl()),
				filter(metadata.getSourceUrl()),
			});
		} catch (IOException e) {
			throw new ApplicationException(e);
		}
	}

	@Override
	public void addGroup(InteractionNetworkGroup group, Organism organism, String colour) throws ApplicationException {
		try {
			Map<String, String> colourMap = new HashMap<String, String>();
			colourMap.put(group.getCode(), colour);
			exporter.setNetworkGroupColours(colourMap);
			
			exporter.exportGroup(writer, new String[] {
				String.valueOf(group.getId()),
				group.getName(),
				group.getCode(),
				group.getDescription(),
				String.valueOf(organism.getId()),
			});
		} catch (IOException e) {
			throw new ApplicationException(e);
		}
	}
	
	@Override
	public void addOrganism(Organism organism) throws ApplicationException {
		try {
			Ontology ontology = organism.getOntology();
			long ontologyId = ontology == null ? -1 : ontology.getId(); 
			exporter.exportOrganism(writer, new String[] {
				String.valueOf(organism.getId()),
				organism.getName(),
				filter(organism.getDescription()),
				organism.getAlias(),
				String.valueOf(ontologyId),
				String.valueOf(organism.getTaxonomyId()),
			});
		} catch (IOException e) {
			throw new ApplicationException(e);
		}
	}
	
	@Override
	public void addNode(Node node, Organism organism) throws ApplicationException {
		GeneData geneData = node.getGeneData();
		try {
			exporter.exportNode(writer, new String[] {
				String.valueOf(node.getId()),
				node.getName(),
				String.valueOf(geneData.getId()),
			}, String.valueOf(organism.getId()));
			addGeneData(writer, geneData);
			for (Gene gene : node.getGenes()) {
				addGene(gene);
			}
		} catch (IOException e) {
			throw new ApplicationException(e);
		}
	}
	
	@Override
	public void addGene(Gene gene) throws ApplicationException {
		GeneNamingSource namingSource = gene.getNamingSource();
		long namingSourceId = namingSource == null ? 0 : namingSource.getId();

		try {
			exporter.exportGene(writer, new String[] {
				String.valueOf(gene.getId()),
				gene.getSymbol(),
				null,
				String.valueOf(namingSourceId),
				String.valueOf(gene.getNode().getId()),
				String.valueOf(gene.getOrganism().getId()),
				String.valueOf(gene.isDefaultSelected()),
			});
		} catch (IOException e) {
			throw new ApplicationException(e);
		}
	}
	
	void addGeneData(IndexWriter writer, GeneData data) throws IOException {
		GeneNamingSource namingSource = data.getLinkoutSource();
		long namingSourceId = namingSource == null ? 0 : namingSource.getId();
		
		exporter.exportGeneData(writer, new String[] {
			String.valueOf(data.getId()),
			filter(data.getDescription()),
		}, data.getExternalId(), namingSourceId);
	}
	
	private String filter(String value) {
		return value == null ? "" : value; //$NON-NLS-1$
	}

	@Override
	public void addNamingSource(GeneNamingSource source) throws ApplicationException {
		try {
			exporter.exportNamingSource(writer, new String[] {
				String.valueOf(source.getId()),
				source.getName(),
				String.valueOf(source.getRank()),
				source.getShortName(),
			});
		} catch (IOException e) {
			throw new ApplicationException(e);
		}
	}

	@Override
	public void deleteNetwork(InteractionNetwork network) throws ApplicationException {
		try {
			try {
				writer.deleteDocuments(new TermQuery(new Term(LuceneMediator.NETWORK_ID, String.valueOf(network.getId()))));
				NetworkMetadata metadata = network.getMetadata();
				if (metadata != null) {
					writer.deleteDocuments(new TermQuery(new Term(LuceneMediator.NETWORKMETADATA_ID, String.valueOf(metadata.getId()))));
				}
			} finally {
				writer.commit();
			}
		} catch (IOException e) {
			throw new ApplicationException(e);
		}
	}
	
	@Override
	public void deleteOrganism(Organism organism) throws ApplicationException {
		try {
			try {
				writer.deleteDocuments(new TermQuery(new Term(LuceneMediator.ORGANISM_ID, String.valueOf(organism.getId()))));
			} finally {
				writer.commit();
			}
		} catch (IOException e) {
			throw new ApplicationException(e);
		}
	}
	
	@Override
	public void deleteOrganismNodesAndGenes(Organism organism) throws ApplicationException {
		try {
			try {
				IndexReader reader = writer.getReader();
				final Searcher searcher = new IndexSearcher(reader);
				final Set<Long> nodeIds = new HashSet<Long>();
				final Set<Long> geneIds = new HashSet<Long>();
				final Set<Long> geneDataIds = new HashSet<Long>();
				try {
					searcher.search(new TermQuery(new Term(LuceneMediator.NODE_ORGANISM_ID, String.valueOf(organism.getId()))), new AbstractCollector() {
						@Override
						public void handleHit(int doc) throws IOException {
							Document document = searcher.doc(doc);
							nodeIds.add(Long.parseLong(document.get(LuceneMediator.NODE_ID)));
							geneDataIds.add(Long.parseLong(document.get(LuceneMediator.NODE_GENEDATA_ID)));
						}
					});
					searcher.search(new TermQuery(new Term(LuceneMediator.GENE_ORGANISM_ID, String.valueOf(organism.getId()))), new AbstractCollector() {
						@Override
						public void handleHit(int doc) throws IOException {
							Document document = searcher.doc(doc);
							geneIds.add(Long.parseLong(document.get(LuceneMediator.GENE_ID)));
						}
					});
				} finally {
					searcher.close();
				}
				for (Long geneDataId : geneDataIds) {
					writer.deleteDocuments(new TermQuery(new Term(LuceneMediator.GENEDATA_ID, String.valueOf(geneDataId))));
				}
				for (Long geneId : geneIds) {
					writer.deleteDocuments(new TermQuery(new Term(LuceneMediator.GENE_ID, String.valueOf(geneId))));
				}
				for (Long nodeId : nodeIds) {
					writer.deleteDocuments(new TermQuery(new Term(LuceneMediator.NODE_ID, String.valueOf(nodeId))));
				}
				writer.deleteDocuments(new TermQuery(new Term(LuceneMediator.ORGANISM_ID, String.valueOf(organism.getId()))));
			} finally {
				writer.commit();
			}
		} catch (IOException e) {
			throw new ApplicationException(e);
		}
	}
}
