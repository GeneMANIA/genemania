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

package org.genemania.engine.converter.sym;


import org.apache.log4j.Logger;
import org.genemania.engine.config.Config;
import org.genemania.engine.core.data.NodeIds;
import org.genemania.engine.core.utils.Normalization;
import org.genemania.engine.matricks.SymMatrix;
import org.genemania.exception.ApplicationException;
import org.genemania.mediator.InteractionCursor;
import org.genemania.util.ProgressReporter;

/**
 * build a matrix representation of interactions from the given network.
 *
 */
public abstract class CursorNetworkSymMatrixProvider implements INetworkSymMatrixProvider {

    private static Logger logger = Logger.getLogger(CursorNetworkSymMatrixProvider.class);
    protected InteractionCursor cursor;
    protected NodeIds nodeIds;

    /**
     * construct sparse matrix of interaction weights.
     *
     * @param network
     * @param progress
     * @param nodeIdMap
     * @return
     */
    protected SymMatrix convertNetworkToMatrix(ProgressReporter progress) throws ApplicationException {
//        logger.debug(String.format("loading interactions for network %d from db using cursor", cursor.getNetworkId()));
        progress.setMaximumProgress((int) cursor.getTotalInteractions());
        int count = 0;

        int n = nodeIds.getNodeIds().length;
        
        SymMatrix matrix = Config.instance().getMatrixFactory().symSparseMatrix(n);

        try {
            while (cursor.next()) {
                progress.setProgress(count);
                int fromNodeIndex = nodeIds.getIndexForId(cursor.getFromNodeId());
                int toNodeIndex = nodeIds.getIndexForId(cursor.getToNodeId());
                double weight = cursor.getWeight();

                matrix.set(fromNodeIndex, toNodeIndex, weight);
                count++;
            }
        }
        finally {
            cursor.close();
        }

        // normalization
        matrix.setDiag(0d);
        Normalization.normalizeNetwork(matrix);
        return matrix;
    }
}
