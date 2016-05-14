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

/**
 * LuceneGeneDao: Lucene gene data access object implementation   
 * Created Jun 15, 2010
 * @author Ovi Comes
 */
package org.genemania.dao.impl;

import java.util.Collection;
import java.util.List;

import org.genemania.connector.LuceneConnector;
import org.genemania.dao.GeneDao;
import org.genemania.domain.Gene;
import org.genemania.domain.GeneNamingSource;
import org.genemania.domain.Node;
import org.genemania.exception.DataStoreException;

import com.googlecode.ehcache.annotations.Cacheable;

public class LuceneGeneDao implements GeneDao {

	// __[attributes]__________________________________________________________
	private LuceneConnector connector;

	// __[constructors]________________________________________________________
	public LuceneGeneDao() {
		connector = LuceneConnector.getInstance();
	}

	// __[interface implementation]____________________________________________
	// @Cacheable(cacheName = "genesForSymbolsCache")
	public List<Gene> getGenesForSymbols(long organismId,
			List<String> geneSymbols) throws DataStoreException {
		return connector.findGenesBySymbol(organismId, geneSymbols);
	}

	// @Cacheable(cacheName = "geneIsValidCache")
	public boolean isValid(long organismId, String nextSymbol) {
		return connector.isValid(organismId, nextSymbol);
	}

	// @Cacheable(cacheName = "nodeIdCache")
	public Long getNodeId(long organismId, String symbol) {
		return connector.getNodeId(organismId, symbol);
	}

	// @Cacheable(cacheName = "geneForIdCache")
	public Gene findGeneForId(long organismId, long id) {
		Node node = connector.findNodeById(id, organismId);
		Gene ret = null;
		byte bestRank = Byte.MIN_VALUE;

		Collection<Gene> genesForNode = node.getGenes();
		for (Gene gene : genesForNode) {
			GeneNamingSource namingSource = gene.getNamingSource();
			if (namingSource != null) {
				if (namingSource.getRank() > bestRank) {
					bestRank = namingSource.getRank();
					ret = gene;
				}
			}
		}

		return ret;
	}

}
