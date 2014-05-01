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

import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Vector;

import org.apache.log4j.Logger;
import org.genemania.engine.Constants.NetworkType;
import org.genemania.engine.cache.DataCache;
import org.genemania.engine.config.Config;
import org.genemania.engine.core.MatrixUtils;
import org.genemania.engine.core.data.AttributeGroups;
import org.genemania.engine.core.data.AttributeData;
import org.genemania.engine.core.integration.FeatureList;
import org.genemania.engine.core.integration.Feature;
import org.genemania.engine.core.integration.FeatureLoader;
import org.genemania.engine.core.integration.Solver;
import org.genemania.engine.core.integration.FeatureWeightMap;
import org.genemania.engine.core.utils.Normalization;
import org.genemania.engine.exception.CancellationException;
import org.genemania.engine.matricks.MatricksException;
import org.genemania.engine.matricks.Matrix;
import org.genemania.engine.matricks.SymMatrix;
import org.genemania.engine.matricks.custom.Outer1View;
import org.genemania.engine.matricks.custom.Outer2View;
import org.genemania.exception.ApplicationException;
import org.genemania.util.ProgressReporter;

/*
 * build linear system for smart a.k.a. automatic a.k.a
 * GeneMANIA network weighting.
 * 
 * The system is defined as follows: 
 * 
 * given a list of d features, defined as networks over a set of n genes, 
 * represented as n-by-n symmetric matrices
 * (with no self interactions so diagonals are zero. the matrices are
 * sparse).
 * 
 * and given a subset of genes of interest, represented as a binary vector b
 * with 1's in the positions of the genes of interest, and 0's elsewhere.
 * we'll just refer the genes of interest as positives and the other genes negatives.
 *
 * let the # of 1's in b be n_pos, and the numbers of 0's be n_neg so 
 * n = n_pos + n_neg.
 * 
 * we compute a set of weights by constructing and solving the following
 * optimization problem:
 * 
 * Let K be a matrix with n rows and d+1 columns. column 0 is a bias column
 * containing the constant value z = 1/(n*n), and columns 1 through d contain the
 * vectorized feature matrices (stacking the columns).
 * 
 * Let t be a column vector of length n, and defined by:
 * 
 *   t = z*vec(bhat*bhat')
 *   
 * where bhat = b-n_neg/n. We form the system:
 * 
 *   K'*K*w = K'*t 
 *
 * The scaling by z in the bias column and target above is simply intended try
 * to keep the problem well conditioned.
 *
 * Each row of the system corresponds to a gene pair i & j. These pairs can belong to 
 * one of three classes: both genes i & j are positives
 * (that is, genes of interest), both i & j are negatives, or one of i or j is a positive
 * and the other negative. call these classes +/+, -/-, +/-. There are n_pos*n_pos pairs
 * of type +/+, n_neg*n_neg pairs of type -/-, and 2*n_pos*n_neg pairs of type +/-.
 * 
 * In the system above, throw out all the -/- pairs, we are not interested in that class.
 * Also throw out the pairs corresponding to self-interactions i=j, this leaves us with
 * n_pos*(n_pos-1) +/+ pairs, and 2*n_pos*N-neg -/- pairs.
 *  
 * We solve the resulting system for the weights w, and discard w_0 associated with the bias column. 
 * (we actually solve iteratively throwing out negatively weighted features, but
 * that's another comment).
 *
 * In the code we refer to K'*K as KtK, and similarly to K'*t as KtT.
 * 
 *  
 *
 * 
 */
public class AutomaticGramBuilder {
    private static Logger logger = Logger.getLogger(AutomaticGramBuilder.class);

    DataCache cache;
    String namespace;
    long organismId;
    FeatureList featureList;
    Vector labels;
    ProgressReporter progress;
    
    public AutomaticGramBuilder(DataCache cache, String namespace, long organismId, FeatureList featureList, Vector labels, ProgressReporter progress) {
        this.cache = cache;
        this.namespace = namespace;
        this.organismId = organismId;
        this.featureList = new FeatureList(featureList, true);
        this.labels = labels;
        this.progress = progress;
    }
    
 
    /*
     * oops, we combined the building with the solving. TODO: split
     */
    public FeatureWeightMap build(ProgressReporter reporter) throws ApplicationException {
        
        featureList.validate(); // TODO: probably safe to remove this, we control construction of featurelist not user
        
        // setup constants used for the remainder of the calculation.
        // these shouldn't depend on the features, but can depend on  
        // number of positives & negatives in the label vector
        int[] ixPos = MatrixUtils.find(labels, 1d);
        int[] ixNeg = MatrixUtils.find(labels, -1d);
        final int numPos = ixPos.length;
        final int numNeg = ixNeg.length;
        
        final int numFeatures = featureList.size();
        
        // counts of pairs of positively labeled nodes,
        // and positively and negatively labeled nodes. includes symmetry, 
        // but excludes self-interactions.
        final int numPosPos = numPos * (numPos - 1);
        final int numPosNeg = 2 * numPos * numNeg;
        
        // we scale the bias column by the size of the system (# of pairs)
        final double biasVal = 1d/ (numPosPos + numPosNeg);
        
        // each element of the target vector corresponds to either
        // pos-pos pairs or pos-neg pairs. this works out as follows
        // except for the constant of 4 which eludes me, but i don't
        // think a constant scaling of the entire target vector matters
        final double posConst = (2d * numNeg) / (numPos + numNeg);
        final double negConst = (-2d * numPos) / (numPos + numNeg);

        final double posPosTarget = posConst * posConst;
        final double posNegTarget = posConst * negConst;
        
        DenseMatrix KtK = new DenseMatrix(numFeatures, numFeatures);
        DenseVector KtT = new DenseVector(numFeatures);
  
        KtK.set(0, 0, biasVal);     
        double sumOfTargets = posPosTarget * numPosPos + posNegTarget * numPosNeg;
        KtT.set(0, biasVal * sumOfTargets);

        SymMatrix[] Wpp = new SymMatrix[numFeatures];
        Matrix[] Wpn = new Matrix[numFeatures];

        boolean oldMode = false;  // comparison testing, TODO: remove, want to keep new mode
        boolean scaled = true;
        FeatureLoader featureLoader = new FeatureLoader(cache, namespace, organismId, oldMode, scaled);

        for (int ii = 1; ii<numFeatures; ii++) {
            Feature feature_ii = featureList.get(ii);
            
            Wpp[ii] = featureLoader.load(feature_ii, ixPos);
            Wpn[ii] = featureLoader.load(feature_ii, ixPos, ixNeg);            
            
            double ssWpp = Wpp[ii].elementSum();
            double ssWpn = Wpn[ii].elementSum();
            
            KtT.set(ii, posPosTarget * ssWpp + 2d * posNegTarget * ssWpn);
            KtK.set(ii, 0, biasVal * (ssWpp + 2 * ssWpn));
            KtK.set(0, ii, KtK.get(ii, 0));

            for (int jj = 1; jj <= ii; jj++) {

                if (progress.isCanceled()) {
                    throw new CancellationException();
                }
                
                double sumOfProds = 0;
                
                sumOfProds += Wpp[ii].elementMultiplySum(Wpp[jj]);
                sumOfProds += 2 * Wpn[ii].elementMultiplySum(Wpn[jj]);
                
                KtK.set(ii, jj, sumOfProds);
                KtK.set(jj, ii, sumOfProds);                
            }
        }
        
        logger.debug("solving system of size " + featureList.size());
        return Solver.solve(KtK, KtT, featureList, progress);
    }  

    void logAttributeCounts(long groupId) throws ApplicationException {
        AttributeData attributeSet = cache.getAttributeData(namespace, organismId, groupId);    
        Matrix data = attributeSet.getData();
        org.genemania.engine.matricks.Vector colSums = data.columnSums();
        for (int i=0; i<colSums.getSize(); i++) {
            if (colSums.get(i)>0) {
                logger.debug(String.format("attribute %d has col sum %f", i, colSums.get(i)));
            }
        }  
    }
}
