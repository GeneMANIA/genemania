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
package org.genemania.engine.core.integration.gram;

import java.util.ArrayList;
import java.util.List;

import org.genemania.engine.core.integration.Feature;
import org.genemania.engine.core.integration.FeatureList;

import no.uib.cipr.matrix.DenseMatrix;

/*
 * throw out rows/columns of gram/target.
 */
public class GramEditor {

    public static DenseMatrix RemoveNetworkKtK(DenseMatrix KtK, List<Integer> sortedIndexOfNetworksToKeep) {
        DenseMatrix temp = new DenseMatrix(sortedIndexOfNetworksToKeep.size(), sortedIndexOfNetworksToKeep.size());
        for (int i = 0; i < sortedIndexOfNetworksToKeep.size(); i++) {
            for (int j = i; j < sortedIndexOfNetworksToKeep.size(); j++) {
                temp.set(i, j, KtK.get(sortedIndexOfNetworksToKeep.get(i), sortedIndexOfNetworksToKeep.get(j)));
                temp.set(j, i, temp.get(i, j));
            }
        }

        return temp;
    }
 
    static public DenseMatrix RemoveNetworkKtT(DenseMatrix KtT, List<Integer> sortedIndexOfNetworksToKeep) {
        DenseMatrix temp = new DenseMatrix(sortedIndexOfNetworksToKeep.size(), 1);
        for (int i = 0; i < sortedIndexOfNetworksToKeep.size(); i++) {
            temp.set(i, 0, KtT.get(sortedIndexOfNetworksToKeep.get(i), 0));
        }
        return temp;
    }

    public static DenseMatrix RemoveNetworkKtK(DenseMatrix ktK, FeatureList gramFeatureList,
            FeatureList requestedFeatureList) {
        
        List<Integer> indexOfFeaturesToKeep = getIndexOfFeaturesToKeep(gramFeatureList, requestedFeatureList);
        return RemoveNetworkKtK(ktK, indexOfFeaturesToKeep);
    }

    public static DenseMatrix RemoveNetworkKtT(DenseMatrix ktT, FeatureList gramFeatureList,
            FeatureList requestedFeatureList) {
        
        List<Integer> indexOfFeaturesToKeep = getIndexOfFeaturesToKeep(gramFeatureList, requestedFeatureList);
        return RemoveNetworkKtT(ktT, indexOfFeaturesToKeep);
    }

    private static List<Integer> getIndexOfFeaturesToKeep(
            FeatureList gramFeatureList, FeatureList requestedFeatureList) {

        ArrayList<Integer> indices = new ArrayList<Integer>();
        for (Feature feature: requestedFeatureList) {
            int index = gramFeatureList.indexOf(feature);
            if (index < 0) {
                throw new RuntimeException("failed to find index for feature: " + feature);
            }
            indices.add(index);
        }
            
        return indices;
    }    
}
