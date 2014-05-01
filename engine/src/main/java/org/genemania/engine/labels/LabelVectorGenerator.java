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

package org.genemania.engine.labels;

import java.util.Collection;
import no.uib.cipr.matrix.DenseVector;
import org.genemania.engine.core.data.NodeIds;
import org.genemania.exception.ApplicationException;

/**
 * utility code for generator label vectors
 * from collections of node ids
 */
public class LabelVectorGenerator {

    public static DenseVector createLabelsFromIds(NodeIds nodeIds, Collection<Long> positiveNodeIds, Collection<Long> negativeNodeIds,
            double posLabelValue, double negLabelValue, double nonLabelValue) throws ApplicationException {

        // initialize entire label vector to the non labeled nodes value
        int totalNodesForOrganism = nodeIds.getNodeIds().length;
        DenseVector labels = new DenseVector(totalNodesForOrganism);
        for (int i = 0; i < totalNodesForOrganism; i++) {
            labels.set(i, nonLabelValue);
        }

        // set the +ve's
        for (Long nodeId: positiveNodeIds) {
            int index = nodeIds.getIndexForId(nodeId);
            labels.set(index, posLabelValue);
        }

        // set the -ve's
        for (Long nodeId: negativeNodeIds) {
            int index = nodeIds.getIndexForId(nodeId);
            labels.set(index, negLabelValue);
        }

        return labels;
    }
}
