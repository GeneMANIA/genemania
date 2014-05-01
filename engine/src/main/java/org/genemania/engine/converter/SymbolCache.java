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

package org.genemania.engine.converter;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.genemania.domain.Gene;
import org.genemania.domain.Organism;
import org.genemania.engine.cache.DataCache;
import org.genemania.engine.core.data.NodeIds;
import org.genemania.exception.ApplicationException;
import org.genemania.mediator.GeneMediator;

/**
 * So symbol strings are used to lookup genes, from genes we
 * lookup nodes, and then use the node id to lookup the corresponding
 * index for that gene in our network matrices. This function
 * does the lookup, and as an optimization stores the
 * symbol string -> matrix index map because the db queries aren't
 * the fastest thing in the world, and the mapping table ought to
 * be small enough to comfortably fit in memory.
 *
 * this function returns null for symbols that could not be found.
 *
 * note we always put and check upper case for the cache, so search
 * is safely case insensitive
 *
 * TODO: investigate/remove the duplication
 */
public class SymbolCache {
    private static Logger logger = Logger.getLogger(SymbolCache.class);
    private GeneMediator geneMediator;
    private Map<String, Integer> symbolToIndexCache = new HashMap<String, Integer>();
    private static final int SYMBOL_NOT_FOUND = -1;
    private Organism organism;
    private NodeIds nodeIds;

    public SymbolCache(Organism oragnism, GeneMediator geneMediator, DataCache cache) throws ApplicationException {
        this.organism = oragnism;
        this.geneMediator = geneMediator;
        this.nodeIds = cache.getNodeIds(organism.getId());
    }

    public Integer getIndexForSymbol(String symbol) {

        // check cache first
        Integer index = symbolToIndexCache.get(symbol.toUpperCase());

        // look in db if not found
        if (index == null) {
            Gene gene = geneMediator.getGeneForSymbol(organism, symbol);
            if (gene == null) {
                // not in db either, put a marker in the cache
                logger.info("symbol not in db: " + symbol);
                symbolToIndexCache.put(symbol.toUpperCase(), SYMBOL_NOT_FOUND);
            }
            else {
                // we got the gene, look up index, and again mark the
                // cache if not found. otherwise, cache it
                int nodeId = (int) gene.getNode().getId();

                try {
                    index = nodeIds.getIndexForId(nodeId);
                    symbolToIndexCache.put(symbol.toUpperCase(), index);
                }
                catch (ApplicationException e) {
                    logger.warn("gene not in mappings for " + gene.getSymbol());
                    symbolToIndexCache.put(symbol.toUpperCase(), SYMBOL_NOT_FOUND);
                    index = null;
                }
            }
        }
        else if (index == SYMBOL_NOT_FOUND) {
            index = null;
        }

        return index;
    }
}
