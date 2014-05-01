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

package org.genemania.connector;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiSearcher;
import org.apache.lucene.search.Searchable;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.store.FSDirectory;
import org.genemania.Constants;
import org.genemania.domain.Attribute;
import org.genemania.domain.AttributeGroup;
import org.genemania.domain.Gene;
import org.genemania.domain.InteractionNetwork;
import org.genemania.domain.InteractionNetworkGroup;
import org.genemania.domain.Node;
import org.genemania.domain.Organism;
import org.genemania.domain.Statistics;
import org.genemania.exception.DataStoreException;
import org.genemania.mediator.lucene.LuceneAttributeMediator;
import org.genemania.mediator.lucene.LuceneGeneMediator;
import org.genemania.mediator.lucene.LuceneMediator;
import org.genemania.mediator.lucene.LuceneNetworkMediator;
import org.genemania.mediator.lucene.LuceneNodeMediator;
import org.genemania.mediator.lucene.LuceneOrganismMediator;
import org.genemania.mediator.lucene.LuceneStatsMediator;
import org.genemania.util.ApplicationConfig;

public class LuceneConnector {

	// __[static]______________________________________________________________
	private static LuceneConnector instance = new LuceneConnector();
	private static Logger LOG = Logger.getLogger(LuceneConnector.class);

	// __[attributes]__________________________________________________________
	private LuceneOrganismMediator organismMediator = null;
	private LuceneGeneMediator geneMediator = null;
	private LuceneNetworkMediator networkMediator = null;
	private LuceneNodeMediator nodeMediator = null;
	private LuceneStatsMediator statsMediator = null;
	private LuceneAttributeMediator attributeMediator = null;

	private Searcher searcher;
	private Analyzer analyzer;

	// __[constructors]________________________________________________________
	private LuceneConnector() {
		try {
			String indexPath = ApplicationConfig.getInstance().getProperty(
					Constants.CONFIG_PROPERTIES.GENE_INDEX_DIR);
			searcher = createSearcher(indexPath);
			analyzer = LuceneMediator.createDefaultAnalyzer();
			organismMediator = new LuceneOrganismMediator(searcher, analyzer);
			geneMediator = new LuceneGeneMediator(searcher, analyzer);
			networkMediator = new LuceneNetworkMediator(searcher, analyzer);
			nodeMediator = new LuceneNodeMediator(searcher, analyzer);
			statsMediator = new LuceneStatsMediator(searcher, analyzer);
			attributeMediator = new LuceneAttributeMediator(searcher, analyzer);
		} catch (IOException e) {
			LOG.error(e);
		}
	}
    
	// __[public helpers]______________________________________________________
	public static LuceneConnector getInstance() {
		return instance;
	}

	public List<InteractionNetworkGroup> findNetworkGroupsByOrganism(
			long organismId) throws DataStoreException {
		return networkMediator.getNetworkGroupsByOrganism(organismId);
	}

	public List<Organism> retrieveAllOrganisms() throws DataStoreException {
		return organismMediator.getAllOrganisms();
	}

	public Organism findOrganismById(long organismId) throws DataStoreException {
		return organismMediator.getOrganism(organismId);
	}

	public List<Gene> retrieveDefaultGenesFor(long organismId)
			throws DataStoreException {
		return organismMediator.getDefaultGenes(organismId);
	}

	public List<InteractionNetwork> retrieveDefaultNetworksFor(long organismId)
			throws DataStoreException {
		return organismMediator.getDefaultNetworks(organismId);
	}

	public List<Gene> findGenesBySymbol(long organismId,
			List<String> geneSymbols) throws DataStoreException {
		return geneMediator.getGenes(geneSymbols, organismId);
	}

	public InteractionNetwork findNetworkById(long networkId)
			throws DataStoreException {
		return networkMediator.getNetwork(networkId);
	}

	public Node findNodeById(long nodeId, long organismId) {
		return nodeMediator.getNode(nodeId, organismId);
	}

	public Statistics getLatestStatistics() {
		return statsMediator.getLatestStatistics();
	}

	public boolean isValid(long organismId, String geneSymbol) {
		return geneMediator.isValid(organismId, geneSymbol);
	}

	public Long getNodeId(long organismId, String symbol) {
		return geneMediator.getNodeId(organismId, symbol);
	}

	public InteractionNetworkGroup getNetworkGroupByName(long organismId,
			String groupName) {
		return networkMediator.getNetworkGroupByName(groupName, organismId);
	}
	
	public Organism getOrganismForGroup(long id) throws DataStoreException {
		return organismMediator.getOrganismForGroup(id);
	}
	
	public InteractionNetworkGroup getGroupForNetwork(long networkId){
		return networkMediator.getNetworkGroupForNetwork(networkId);
	}
	
	public boolean isValidNetwork(long organismId, long networkId){
		return networkMediator.isValidNetwork(organismId, networkId);
	}

	public Searcher getSearcher() {
		return searcher;
	}

	public void setSearcher(Searcher searcher) {
		this.searcher = searcher;
	}

	public Analyzer getAnalyzer() {
		return analyzer;
	}

	public void setAnalyzer(Analyzer analyzer) {
		this.analyzer = analyzer;
	}

	// __[private helpers]_____________________________________________________
	private static Searcher createSearcher(String indexPath) throws IOException {
		ArrayList<Searcher> searchers = new ArrayList<Searcher>();
		File indices = new File(indexPath);
		File[] fileList = indices.listFiles();
		if (fileList == null) {
			throw new IOException(
					String
							.format(
									"Unable to load indices from path '%s', not a directory or I/O error",
									indexPath));
		}

		for (File file : fileList) {
			try {
				if (!LuceneMediator.indexExists(file)) {
					continue;
				}
				FSDirectory directory = FSDirectory.open(file);
				searchers.add(new IndexSearcher(directory));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (searchers.size() == 0) {
			throw new IOException("No indices found");
		}
		return new MultiSearcher(searchers
				.toArray(new Searchable[searchers.size()]));
	}
    
    public Attribute findAttribute(long organismId, long attributeId) {
        return attributeMediator.findAttribute(organismId, attributeId);
    }
    
    public boolean isValidAttribute(long organismId, long attributeId) {
        return attributeMediator.isValidAttribute(organismId, attributeId);
    }
    
    public List<Attribute> findAttributesByGroup(long organismId,
            long attributeGroupId) {
        return attributeMediator.findAttributesByGroup(organismId, attributeGroupId);
    }
    
    public List<AttributeGroup> findAttributeGroupsByOrganism(long organismId) {
        return attributeMediator.findAttributeGroupsByOrganism(organismId);
    }
    
    public AttributeGroup findAttributeGroup(long organismId, long attributeGroupId) {
        return attributeMediator.findAttributeGroup(organismId, attributeGroupId);
    }
}
