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

import org.genemania.engine.core.integration.Feature;
import org.genemania.engine.core.integration.FeatureList;
import org.genemania.exception.ApplicationException;

import no.uib.cipr.matrix.DenseMatrix;

/*
 * Our network weight calculation methods usually involve
 * constructing and solving a system of linear equations in
 * the style of normal equations:
 *  
 *    K'Kw = K't
 * 
 * for the weight vector w, given a target vector t and a matrix
 * of vectorized networks K. Our code usually denotes K'K (the
 * normal matrix, or gram matrix) as KtK, and similarly K't as
 * KtT (target vector). 
 * 
 * The details of the computation of the system element vary from
 * method to method (e.g. if we subset the original network), this
 * class omits details of the computation and just consolidates the
 * book-keeping associated with how the set of networks are mapped
 * to the rows/cols of the system.
 * 
 * The book-keeping has two parts, each row has a type id (network, 
 * attribute), and the types corresponding id (network-id, attribute-id).
 * 
 * TODO: not sure but maybe its convenient to track groups here as well,
 * e.g. attribute group? engine currently doesn't know about network groups
 * though
 * 
 * TODO: don't like the name of the class, haven't thought of a better
 * one
 * 
 * TODO: why did we make KtT a 1-col matrix instead of using the vector type?
 */
public class GramTarget {
    private int size;
    private FeatureList featureList;
    private int biasOffset = 1; // 1 if we have bias, 0 otherwise. this is not the bias col #, which is always 0 if there is a bias 
    private DenseMatrix KtK;
    private DenseMatrix KtT;

    public GramTarget(FeatureList featureList, boolean hasBias) {
        this.size = featureList.size();
        this.featureList = featureList;
        this.biasOffset = hasBias ? 1 : 0;
    }
    
    public void alloc() {
        KtK = new DenseMatrix(size + biasOffset, size + biasOffset);
        KtT = new DenseMatrix(size + biasOffset, 1);
    }

    public void setGramBias(double bias) throws ApplicationException {
        if (bias == 0) {
            throw new ApplicationException("bias value specified but not allowed");
        }

        KtK.set(0, 0, bias);
    }
    
    public void setGramBias(Feature feature, double bias) throws ApplicationException {
        if (bias == 0) {
            throw new ApplicationException("bias value specified but not allowed");
        }
    }
    
    public void setTargetBias(double bias) throws ApplicationException {
        if (bias == 0) {
            throw new ApplicationException("bias value specified but not allowed");
        }

        KtT.set(0, 0, bias);
    }
    
    public int getIndex(Feature feature) throws ApplicationException {
        return 0;
    }
    
    public void setKtT(Feature feature, double val) {
        
    }
    
    public void setKtK(Feature feature1, Feature feature2, double val) {
        
    }
    
    public void solve() {
        
    }
    
    public void getWeights() {
        
    }
    
    public DenseMatrix getKtK() {
        return KtK;
    }
    
    public DenseMatrix getKtT() {
        return KtT;
    }
}
