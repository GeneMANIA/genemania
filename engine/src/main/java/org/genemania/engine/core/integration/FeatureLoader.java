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

import org.genemania.engine.Constants.NetworkType;
import org.genemania.engine.cache.DataCache;
import org.genemania.engine.config.Config;
import org.genemania.engine.core.data.AttributeData;
import org.genemania.engine.core.data.AttributeGroups;
import org.genemania.engine.core.utils.Normalization;
import org.genemania.engine.matricks.MatricksException;
import org.genemania.engine.matricks.Matrix;
import org.genemania.engine.matricks.SymMatrix;
import org.genemania.engine.matricks.custom.Outer1View;
import org.genemania.engine.matricks.custom.Outer2View;
import org.genemania.exception.ApplicationException;

/*
 * return a network representation of the given
 * feature.
 */
public class FeatureLoader {

	private DataCache cache;
	private String namespace;
	private long organismId;
	
	// change to true to test by converting the feature arrays to
	// actual sparse matrices
	boolean oldMode = false;
	
	// apply the normalization
	boolean scaled = true;
	
	// optimization
    org.genemania.engine.matricks.Vector sumsCache; 
	long sumsCacheGroupId;
	
	public FeatureLoader(DataCache cache, String namespace, long organismId) {
		this(cache, namespace, organismId, false, false);
	}

	public FeatureLoader(DataCache cache, String namespace, long organismId, boolean oldMode, boolean scaled) {
		this.cache = cache;
		this.namespace = namespace;
		this.organismId = organismId;
		this.oldMode = oldMode;
		this.scaled = scaled;
	}
	
    public SymMatrix load(Feature feature) throws ApplicationException {
        return load(feature, null);
    }
    
    /* 
     * pull in appropriate feature. for attribute vector's we do a bit of extra
     * work wrapping particular columns in an outer-product matrix object so it
     * can be processed like a symmetric matrix in the same way as regular networks.
     */
    public SymMatrix load(Feature feature, int [] ixPos) throws ApplicationException {
        if (feature.getType() == NetworkType.SPARSE_MATRIX) {
            SymMatrix network = cache.getNetwork(namespace, organismId, feature.getId()).getData();
            
            if (ixPos != null) {
            	return network.subMatrix(ixPos);
            }
            else {
            	return network;
            }
        }
        else if (feature.getType() == NetworkType.ATTRIBUTE_VECTOR) {
            AttributeData attributeSet = cache.getAttributeData(namespace, organismId, feature.getGroupId());
            AttributeGroups attributeGroups = cache.getAttributeGroups(namespace, organismId);
            int index = attributeGroups.getIndexForAttributeId(feature.getGroupId(), feature.getId());

            org.genemania.engine.matricks.Vector sums = getColumnSums(attributeSet);
            
            double scale = 1d/(sums.get(index) - 1d);

            if (oldMode) {
                Matrix data = attributeSet.getData();
                SymMatrix tmp = attributeToSparse(data, index);
                return tmp.subMatrix(ixPos);
            }
            else {
                Matrix data = attributeSet.getData();
                if (ixPos != null) {
                	return Outer1View.fromColumn(data, index, ixPos, scale, true);
                }
                else {
                	return Outer1View.fromColumn(data, index, scale, true);
                }
            }
        }
        else {
            throw new ApplicationException("unexpected feature type: " + feature.getType());
        }
    }
    
    /*
     * quick & dirty caching of sums
     */
    org.genemania.engine.matricks.Vector getColumnSums(AttributeData attributeSet) {
        if (sumsCacheGroupId == attributeSet.getAttributeGroupId() && sumsCache != null) {
            return sumsCache;
        }
        else {
            sumsCache = attributeSet.getData().columnSums();
            sumsCacheGroupId = attributeSet.getAttributeGroupId();
            return sumsCache;
        }       
    }
    
    /*
     * load & subset features appropriately. for matrices, we extract the actual submatrix so
     * we can release the underlying memory if under pressure. for the more compact attributes,
     * we provide views.
     */
    public Matrix load(Feature feature, int [] ixPos, int [] ixNeg) throws ApplicationException {
        if (feature.getType() == NetworkType.SPARSE_MATRIX) {
            SymMatrix network = cache.getNetwork(namespace, organismId, feature.getId()).getData();
            return network.subMatrix(ixPos, ixNeg);
        }
        else if (feature.getType() == NetworkType.ATTRIBUTE_VECTOR) {
            AttributeData attributeSet = cache.getAttributeData(namespace, organismId, feature.getGroupId());
            AttributeGroups attributeGroups = cache.getAttributeGroups(namespace, organismId);
            int index = attributeGroups.getIndexForAttributeId(feature.getGroupId(), feature.getId());

            // TODO: bad to sum every time!
            org.genemania.engine.matricks.Vector sums = attributeSet.getData().columnSums();
            double scale;
            if (scaled) {
                scale = 1d/(sums.get(index) - 1d);
            }
            else {
                scale = 1d;
            }
            
            if (oldMode) {
//                OuterProductSymMatrixFromMatrixColView symMatrix = new OuterProductSymMatrixFromMatrixColView(attributeSet.getData(), index, scale, true);
//                return new MatrixView(symMatrix, ixPos, ixNeg);
                Matrix data = attributeSet.getData();
                SymMatrix tmp = attributeToSparse(data, index);
                return tmp.subMatrix(ixPos, ixNeg);
            }
            else {
                Matrix data = attributeSet.getData();

                // we do want zero diagonal, but since we are selecting indices
                // so that no elements correspond to diagonals, we can set this
                // to false. this is convenient, since its zeroDiag = true
                // isnt' implemented for this Outer2View thingy.
                boolean zeroDiag = false; 
                return Outer2View.fromColumn(data,index, ixPos, ixNeg, scale, zeroDiag);
            }
        }
        else {
            throw new MatricksException("unkown feature type: " + feature.getType());
        }
    }
      
    // for comparison testing, materialize an attribute vector to a sparse matrix
    private SymMatrix attributeToSparse(Matrix data, int column) throws ApplicationException {
        
        SymMatrix sparse = Config.instance().getMatrixFactory().symSparseMatrix(data.numRows());
        
        // dumb and slow, but should be correct
        for (int i1=0; i1<data.numRows(); i1++) {
            double d1 = data.get(i1, column);
            if (d1 == 0) {
                continue;
            }
            
            for (int i2 =0; i2<data.numRows(); i2++) {
                double d2 = data.get(i2, column);
                if (d2 == 0) {
                    continue;
                }
                
                if (i1 == i2) {
                    continue;
                }
                
                sparse.set(i1, i2, d1*d2);
            }
        }
        
        Normalization.normalizeNetwork(sparse);
        return sparse;        
    }
   
}
