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



package org.genemania.engine.mediators;

import java.util.List;
import java.util.Set;

import org.genemania.domain.Gene;
import org.genemania.domain.GeneData;
import org.genemania.domain.GeneNamingSource;
import org.genemania.domain.Node;
import org.genemania.domain.Organism;
import org.genemania.engine.cache.DataCache;
import org.genemania.engine.core.data.NodeIds;
import org.genemania.exception.DataStoreException;
import org.genemania.mediator.GeneMediator;

/**
 * for testing
 *
 * DataCache doesn't have genes! make up a gene with id = node_id and name = id
 */
public class DataCacheGeneMediator implements GeneMediator {
    private DataCache cache;

    public DataCacheGeneMediator(DataCache cache) {
        this.cache = cache;
    }

    public GeneNamingSource findNamingSourceByName(String namingSourceName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public List<Gene> getAllGenes(long organismId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Gene getGeneForSymbol(Organism organism, String geneSymbol) {
        try {

            // must start with "gene", requirement for datacache driven mediators
            if (!geneSymbol.startsWith("gene")) {
                return null;
            }
            
            // check given id exists
            long geneId = Long.parseLong(geneSymbol.substring(4));
            long nodeId = geneId; // our assumption for datacache driven mediators
            NodeIds nodeIds = cache.getNodeIds(organism.getId());
            int index = nodeIds.getIndexForId(nodeId);
            
            Node node = new Node();
            node.setId(nodeId);

            Gene gene = new Gene(geneSymbol, null, null, node, organism, false);
            gene.setId(geneId);
            return gene;
        }
        catch (Exception e) {
            return null;
        }
    }

    public List<Gene> getGenes(List<String> geneSymbols, long organismId) throws DataStoreException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void updateGeneData(Organism organism, String geneSymbol, GeneData geneData) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

	public String getCanonicalSymbol(long organismId, String symbol) {
        throw new UnsupportedOperationException("Not supported yet.");
	}

	public Long getNodeId(long organismId, String symbol) {
        // must start with "gene", requirement for datacache driven mediators
        if (!symbol.startsWith("gene")) {
            return null;
        }
        
        // check given id exists
        long geneId = Long.parseLong(symbol.substring(4));
        long nodeId = geneId; // our assumption for datacache driven mediators
        return nodeId;
	}

	public Set<String> getSynonyms(long organismId, String symbol) {
        throw new UnsupportedOperationException("Not supported yet.");
	}

	public Set<String> getSynonyms(long organismId, long nodeId) {
        throw new UnsupportedOperationException("Not supported yet.");
	}

	public boolean isValid(long organismId, String symbol) {
        throw new UnsupportedOperationException("Not supported yet.");
	}

}
