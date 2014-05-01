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

import java.util.ArrayList;

import no.uib.cipr.matrix.DenseVector;

import org.apache.log4j.Logger;
import org.genemania.engine.Constants.NetworkType;
import org.genemania.engine.cache.DataCache;
import org.genemania.engine.core.MatrixUtils;
import org.genemania.engine.core.data.AttributeData;
import org.genemania.engine.core.data.AttributeGroups;
import org.genemania.engine.core.data.CoAnnotationSet;
import org.genemania.engine.core.data.Data;
import org.genemania.engine.matricks.Matrix;
import org.genemania.engine.matricks.SymMatrix;
import org.genemania.exception.ApplicationException;

/* 
 * Compute correlation score between each attribute and the given
 * target vector, returning the given max-number of most-correlated
 * attributes.
 * 
 * if max-number of requested attributes is <= 0, return all
 * 
 */
public class CorrelatedAttributeSelector extends AbstractAttributeSelector {
    private static Logger logger = Logger.getLogger(CorrelatedAttributeSelector.class);

    DataCache cache;
    String goBranch;
    private int maxAttributes;
    
    private String namespace = Data.CORE; // maybe we'll need to control this later
    
    public CorrelatedAttributeSelector(DataCache cache, String goBranch, int maxAttributes) {
        this.cache  = cache;
        this.goBranch = goBranch;
        this.maxAttributes = maxAttributes;
    }

    /*
     * So we want to order all the attributes by pearson-correlation to the target vector. But not
     * any old target vector, its the target vector computed by summing & scaling GO co-annotation matrices.
     * This is a dense n*n matrix, represented using a sparse matrix, dense vector, and constant.
     * Its also already has mean 0 by construction, though that's including the diagonal which we don't normally
     * care about.
     * 
     * The attributes we are comparing to are networks computed as the outer product of a binary vector x with
     * s 1's and n-s zero's, diagonal removed, and scaled as our other interaction networks (divide each 
     * interaction by the sqrt of the product of the row and col sums). This boils down to each network X being
     * 
     *    X = (x*x' - diag(x)) / (s-1)
     *    
     * The mean and variance of this attribute network is 
     *    
     *    meanX = s*(s-1)/(n*n)
     *    varX = meanX*(1-meanX)
     *    
     * Which we need to scale to z-scores. Since we are comparing multiple attributes to the same
     * target vector, scaling the target by its variance doesn't change the order of the correlations,
     * and similarly its mean will vanish upon inner-product with the attribute z-scores. So we can
     * just compute 
     * 
     *   score_i = ZX_i'*T
     *  
     * for the z-score ZX_i for the i'th attribute, over all the attributes, against the untransformed
     * target T, which is convenient because we are representing T in this weird form anyway. But one
     * technical point, The actual T we represent has non-zero diagonal, and this can actually affect
     * the order of the correlation scores. So we adjust for this diagonal by adding a correction
     * term.
     * 
     * Recalling that to compute the inner-product of some matrix W with the target T represented with
     * the triplet (Ahat, yhat, const) we compute:
     * 
     *   sum(sum(W .* Ahat)) + sum(W*yhat) + sum(sum(W))*const;
     *   
     * by matrix inner-product of X and Y we just mean vec(X)'*vec(Y) = sum(sum(X.*Y)). Okay this bit above deserves
     * a comment of its own (did i already write that in the fast-weights code? TODO: check for comment adequacy,
     * given my memories complete inadequacy)
     * 
     * The final calculation of score for each attribute becomes
     *  
     *     term1 = sum(sum(X .* Ahat)) + sum(X*yhat) + sum(sum(X))*const
     *     term2 = sum(sum(Ahat))+sum(yhat)*n + n*n*const;
     *     correlation = (term1 - term2)/(sqrt(varX)*n*n);
     *
     *     correction = -meanV/sqrt(varV)*(sum(diag(Ahat)) + sum(yhat) + n*const)/(n*n);
     *
     *     score = correlation - correction 
     *
     * Just remember this is not the actual Pearson Correlation since we didn't bother scaling the target.
     * Really should write out the intermediate steps cause this will all seem mysterious in about 24 hrs <sigh>.
     * 
     * (non-Javadoc)
     * @see org.genemania.engine.core.integration.IAttributeSelector#selectAttributes(long, long)
     */
    @Override
    public FeatureList selectAttributes(long organismId, long attributeGroupId)
            throws ApplicationException {
        
        // load attribute group data
        AttributeData attributeSet = cache.getAttributeData(namespace, organismId, attributeGroupId);    
        Matrix data = attributeSet.getData();
               
        // compute normalization params, we don't want to turn the matrix dense
        // so don't actually normalize. also since the attributes are binary
        // we can quickly compute the results in a vector
        int numAttributes = data.numCols();
        int numGenes = data.numRows();
        
        DenseVector sums = new DenseVector(numAttributes);
        data.columnSums(sums.getData());
        
        boolean oldMode = false;
        boolean scaled = false;
        FeatureLoader featureLoader = new FeatureLoader(cache, namespace, organismId, oldMode, scaled);
        CoAnnotationSet annoSet = cache.getCoAnnotationSet(organismId, goBranch);
        DenseVector scores = new DenseVector(numAttributes);

        FeatureList candidateFeatures = getCandidateFeatures(organismId, attributeGroupId);

        double n = numGenes; // because it reads better

        final double yhatSum = MatrixUtils.sum(annoSet.GetBHalf());
        double correctionFactor = yhatSum + n*annoSet.GetConstant();
        double diagSum = 0;
        for (int i=0; i<numGenes; i++) {
            diagSum += annoSet.GetCoAnnotationMatrix().get(i, i);
        }
        correctionFactor += diagSum;
        correctionFactor = correctionFactor/(n*n);
        
        double annoSetSum = annoSet.GetCoAnnotationMatrix().elementSum();
        
        int i=0;
        DenseVector tempVec = new DenseVector(numGenes);
        for (Feature feature: candidateFeatures) {
            double score = 0d;
            double s = sums.get(i);
            double mean = s*(s-1)/(n*n);
            double var = mean*(1-mean);
            
            SymMatrix network = featureLoader.load(feature);
            
            double attributeCoannoProd = network.elementMultiplySum(annoSet.GetCoAnnotationMatrix());

            // if zero let's just skip the rest
            if (attributeCoannoProd == 0) {
                score = Double.MIN_VALUE;
            }
            else {
                double networkSum =  s*(s-1); // == network.elementSum();
            
                network.mult(annoSet.GetBHalf().getData(), tempVec.getData());
                double tempVecSum = MatrixUtils.sum(tempVec);
    
                double term1 = attributeCoannoProd
                    + tempVecSum 
                    + networkSum*annoSet.GetConstant();
    
                double term2 = annoSetSum 
                    + n*yhatSum
                    + n*n*annoSet.GetConstant();
    
                score = (term1-term2)/(Math.sqrt(var)*n*n);
    
                double correction = -mean/Math.sqrt(var)*correctionFactor;
    
                score = score - correction;
            }
            scores.set(i, score);
            
            i += 1;
            
            if (i % 1000 == 0) {
                logger.debug("i: " + i);
            }
        }
                
        FeatureList featureList = selectTopFeatures(organismId, attributeGroupId, candidateFeatures, scores, sums);
        return featureList;
    }

    /* 
     * this should probably be a util method somewhere
     */
    private FeatureList getCandidateFeatures(long organismId, long attributeGroupId) throws ApplicationException {
        AttributeGroups groups = cache.getAttributeGroups(namespace, organismId);
        FeatureList list = new FeatureList();
        
        ArrayList<Long> attributeIds = groups.getAttributesForGroup(attributeGroupId);
        for (long attributeId: attributeIds) {
            Feature feature = new Feature(NetworkType.ATTRIBUTE_VECTOR, attributeGroupId, attributeId);
            list.add(feature);
        }
        
        return list;
    }
    
    private FeatureList selectTopFeatures(long organismId, long attributeGroupId, FeatureList candidateFeatures, DenseVector correlations, DenseVector columnSums) throws ApplicationException {
        AttributeGroups groups = cache.getAttributeGroups(namespace, organismId);
        FeatureList list = new FeatureList();
        
        DenseVector ranks = correlations.copy();
        
        // ranking is from smallest to largest, flip correlation scores so largest
        // +ve'ly ranked correlation will be smallest (-ve). TODO: maybe we should threshold so
        // as not to pick up poorly correlated if nothing strongly correlated? but we don't
        // know where to threshold since we haven't computed a proper pearson and the correlations
        // aren't between -1/1. 
        ranks.scale(-1d);
        MatrixUtils.add(ranks, 1d);
        
        MatrixUtils.rank(ranks);
        
        // build ordered list
        DenseVector ordered = new DenseVector(correlations.size());
        int [] unrank = new int[correlations.size()];
        for (int i=0; i<ranks.size(); i++) {
            int p = (int) Math.round(ranks.get(i))-1;
            ordered.set(p, correlations.get(i));
            unrank[p] = i;
        }
        
        int max = ranks.size();
        if (maxAttributes > 0) {
            max = Math.min(max, maxAttributes);
        }

        for (int i=0; i<max; i++) {
            int attributeIndex = unrank[i];
            double count = columnSums.get(attributeIndex);
            if (count >= 1) { // must have at least one gene with the attribute
                long attributeId = groups.getAttributeIdForIndex(attributeGroupId, attributeIndex);
                Feature feature = new Feature(NetworkType.ATTRIBUTE_VECTOR, attributeGroupId, attributeId);
                list.add(feature);
            }
        }
                
        logger.debug(String.format("selected %d attributes", list.size()));
        return list;
    }
}
