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

package org.genemania.engine.core.integration;

import org.apache.log4j.Logger;
import org.genemania.engine.Constants.NetworkType;
import org.genemania.engine.config.Config;
import org.genemania.engine.cache.DataCache;
import org.genemania.engine.core.data.Network;
import org.genemania.engine.exception.CancellationException;
import org.genemania.engine.matricks.MatrixAccumulator;
import org.genemania.engine.matricks.SymMatrix;
import org.genemania.exception.ApplicationException;
import org.genemania.util.ProgressReporter;

/**
 * sums a set of weighted networks represented as sparse matrices.
 * ignores weights assigned to other (i.e. attribute) networks),
 * since we handle them in a fancy way for optimization.
 */
public class CombineNetworksOnly {

    public static Logger logger = Logger.getLogger(CombineNetworksOnly.class);

    /*
     * we only need to combine the sparse-networks. portions of the combined network
     * that arise from attribute vectors are computed on the fly.
     * 
     * TODO: put a config switch in here so we can do it the old/slow way for
     * memory constrained users?
     */
    public static SymMatrix combine(FeatureWeightMap weightMap, String namespace, long organismId, DataCache cache, ProgressReporter progress) throws ApplicationException {
//        return basicCombine(weightMap, namespace, organismId, cache,progress);
        return combineWithAdder(weightMap, namespace, organismId, cache,progress);
    }
    

     static SymMatrix basicCombine(FeatureWeightMap weightMap, String namespace, long organismId, DataCache cache, ProgressReporter progress) throws ApplicationException {
        int size = cache.getNodeIds(organismId).getNodeIds().length;
        SymMatrix combined = Config.instance().getMatrixFactory().symSparseMatrix(size);
        
        for (Feature feature: weightMap.keySet()) {

            if (progress.isCanceled()) {
                throw new CancellationException();
            }

            if (feature.getType() == NetworkType.SPARSE_MATRIX) {
                double weight = weightMap.get(feature);
                Network network = cache.getNetwork(namespace, organismId, feature.getId());
                SymMatrix data = network.getData();                
                combined.add(weight, data);
            }            
            else if (feature.getType() == NetworkType.ATTRIBUTE_VECTOR) {
                // skip
            }
            else if (feature.getType() == NetworkType.BIAS) {
                // skip
            }
            else {
                throw new ApplicationException("unsupported network type");                
            }
        }
        
        logger.info("Combine Matrix done!");
        return combined;
    }

     /* 
      * use the optimized accumulator to compute combined network. helps as long as
      * we're not memory constrained ... but worse case is we'll have to reload the
      * networks multiple time! so this suits our server use
      */
    static SymMatrix combineWithAdder(FeatureWeightMap weightMap, String namespace, long organismId, DataCache cache, ProgressReporter progress) throws ApplicationException {
        int size = cache.getNodeIds(organismId).getNodeIds().length;
        SymMatrix combined = Config.instance().getMatrixFactory().symSparseMatrix(size);
        
        MatrixAccumulator adder = combined.accumulator();
        
        while (adder.nextBlock()) {
            for (Feature feature: weightMap.keySet()) {

                if (progress.isCanceled()) {
                    throw new CancellationException();
                }

                if (feature.getType() == NetworkType.SPARSE_MATRIX) {
                    double weight = weightMap.get(feature);
                    Network network = cache.getNetwork(namespace, organismId, feature.getId());
                    SymMatrix data = network.getData();                
                    adder.add(weight, data);
                }            
                else if (feature.getType() == NetworkType.ATTRIBUTE_VECTOR) {
                    // skip
                }
                else if (feature.getType() == NetworkType.BIAS) {
                    // skip
                }
                else {
                    throw new ApplicationException("unsupported network type");                
                }
            }
        }
        
        logger.info("Combine Matrix done!");
        return combined;
    }
}
