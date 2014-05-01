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

package org.genemania.engine.summary;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.genemania.domain.Gene;
import org.genemania.domain.Node;
import org.genemania.engine.apps.support.DataConnector;
import org.genemania.engine.cache.DataCache;
import org.genemania.exception.ApplicationException;
import org.genemania.mediator.NodeCursor;
import org.genemania.mediator.NodeMediator;
import org.genemania.mediator.OrganismMediator;

/*
 * compute lookups from node ids to preferred names for a given organism 
 * and save for reuse
 */
public class PreferredNames {
	
	Map<Long, String> preferredNames;
	
	public PreferredNames(long organismId, DataConnector dataConnector, String ... preferredSources) throws Exception {
		init(organismId, dataConnector, preferredSources);								
	}

	void init(long organismId, DataConnector dataConnector, String[] preferredSources) throws Exception {
		preferredNames = new HashMap<Long, String>();

		NodeCursor cursor = dataConnector.getOrganismMediator().createNodeCursor(organismId);
        while (cursor.next()) {
        	long id = cursor.getId();
 
        	Node node = dataConnector.getNodeMediator().getNode(id, organismId);
        	Collection<Gene> genes = node.getGenes();
         	
        	preferredNames.put(id, getPreferred(genes, preferredSources));
        }        
	}
	
	String getPreferred(Collection<Gene> genes, String[] preferredSources) throws Exception {
		HashMap<String, String> map = new HashMap<String, String>();
		
		for (Gene gene: genes) {
			map.put(gene.getNamingSource().getName(), gene.getSymbol());
		}
		
		for (String source: preferredSources) {
			String name = map.get(source);
			if (name != null) {
				return name;
			}
		}
		
		throw new ApplicationException("could not find a symbol for gene from preferred sources, known symbols for this gene: " + map);
	}
	
	public String getName(long id) {
		String name = preferredNames.get(id);
		return name;
	}
}
