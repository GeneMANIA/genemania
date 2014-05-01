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

package org.genemania.engine.core.utils;

import java.util.Map;

import org.genemania.engine.core.integration.Feature;
import org.genemania.engine.matricks.Vector;
import org.genemania.engine.matricks.MatricksException;
import org.genemania.engine.matricks.SymMatrix;
import org.genemania.engine.matricks.VectorCursor;

/**
 * Network normalization related utilities
 */
public class Normalization {

    /*
     * normalize a symmetric interaction network containing only
     * +ve weights by dividing each value by the sqrt of the product
     * of the row/col sums.
     */
    public static void normalizeNetwork(SymMatrix m) throws MatricksException {
        Vector sums = m.columnSums();
        VectorCursor cursor = sums.cursor();
        while (cursor.next()) {
            double val = cursor.val();
            if (val > 0.0d) {
                cursor.set(Math.sqrt(val));
            }
            else {
                cursor.set(1d);
            }
        }

        m.dotDivOuterProd(sums);
    }
        
    /*
     * Make all feature weights sum to 1. 
     * TODO: get rid by just normalizing the weight vector duh
     */
    public static void normalizeFeatureWeights(Map<Feature, Double> featureToWeightMap) {
        double sum = 0;
        
        for (double weight: featureToWeightMap.values()) {
            sum += weight;
        }
        
        for (Feature feature: featureToWeightMap.keySet()) {
            double weight = featureToWeightMap.get(feature);
            weight = weight / sum;
            featureToWeightMap.put(feature, weight);
        }
    }
}
