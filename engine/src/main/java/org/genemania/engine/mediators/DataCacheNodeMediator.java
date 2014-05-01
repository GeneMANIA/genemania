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

import java.util.ArrayList;
import java.util.Collection;
import org.genemania.domain.Gene;
import org.genemania.domain.Node;
import org.genemania.domain.Organism;
import org.genemania.engine.cache.DataCache;
import org.genemania.engine.core.data.NodeIds;
import org.genemania.mediator.NodeMediator;

/**
 * testing, only works for a single organism
 */
public class DataCacheNodeMediator implements NodeMediator {

    private DataCache cache;
    Organism organism;

    // only works for the given organism
    public DataCacheNodeMediator(DataCache cache, Organism organism) {
        this.cache = cache;
        this.organism = organism;
    }

    public Node getNode(long nodeId, long organismId) {

        try {
            // check given id exists
            NodeIds nodeIds = cache.getNodeIds(organismId);
            int index = nodeIds.getIndexForId(nodeId);

            Node node = new Node();
            node.setName("node" + nodeId);
            node.setId(nodeId);

            Collection<Gene> genes = new ArrayList<Gene>();

            Gene gene = new Gene("gene" + nodeId, null, null, node, organism, false);
            gene.setId(nodeId); // use node id also as gene id
            genes.add(gene);

            node.setGenes(genes);
            return node;
        }
        catch (Exception e) {
            return null;
        }
    }
}
