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

import org.apache.log4j.Logger;
import org.genemania.engine.Constants;
import org.genemania.engine.Constants.NetworkType;
import org.genemania.engine.cache.DataCache;
import org.genemania.engine.core.MatrixUtils;
import org.genemania.engine.core.data.CoAnnotationSet;
import org.genemania.engine.core.data.DatasetInfo;
import org.genemania.engine.core.integration.Feature;
import org.genemania.engine.core.integration.FeatureList;
import org.genemania.engine.core.integration.FeatureLoader;
import org.genemania.engine.exception.CancellationException;
import org.genemania.engine.matricks.SymMatrix;
import org.genemania.exception.ApplicationException;
import org.genemania.util.ProgressReporter;

public class BasicGramBuilder {
    private static Logger logger = Logger.getLogger(BasicGramBuilder.class);
    DataCache cache;
    String namespace;
    long organismId;
    ProgressReporter progress;
    
    public BasicGramBuilder(DataCache cache, String namespace, long organismId, ProgressReporter progress) {
        this.cache = cache;
        this.namespace = namespace;
        this.organismId = organismId;
        this.progress = progress;
    }
  
    /*
     * maybe later we put in the GramTarget container instead of
     * only building KtK here?
     */
    public DenseMatrix buildBasicKtK(FeatureList featureList, ProgressReporter reporter) throws ApplicationException {
        checkFeatureList(featureList, true);
        
        int size = featureList.size();
        int numGenes = cache.getNodeIds(organismId).getNodeIds().length; 
        
        DenseMatrix KtK = new DenseMatrix(size, size);
        FeatureLoader featureLoader = new FeatureLoader(cache, namespace, organismId);
       
        KtK.set(0, 0, numGenes*numGenes);
        for (int i = 1; i < size; i++) {
            Feature iFeature = featureList.get(i);
//            logger.debug(String.format("processing %d'th feature, type %s", i, iFeature.getType().toString()));
            SymMatrix network_i = featureLoader.load(iFeature);
            
            double networkSum = network_i.elementSum();
            KtK.set(i, 0, networkSum);
            KtK.set(0, i, networkSum);

            for (int j = 1; j <= i; j++) {
                if (progress.isCanceled()) {
                    throw new CancellationException();
                }
                
                Feature jFeature = featureList.get(j);
                SymMatrix network_j = featureLoader.load(jFeature);
                
                double prodSum = network_j.elementMultiplySum(network_i);
//                double prodSum = network_i.elementMultiplySum(network_j);
                KtK.set(i, j, prodSum);
                KtK.set(j, i, prodSum);
            }
        }        
   
        return KtK;      
    }
    
    public DenseMatrix buildKtT(FeatureList featureList, CoAnnotationSet annoSet, ProgressReporter reporter) throws ApplicationException {
        checkFeatureList(featureList, true);
        
        DatasetInfo info = cache.getDatasetInfo(organismId);
        int goBranchNum = Constants.getIndexForGoBranch(annoSet.getGoBranch());        
        int numberOfCategories = info.getNumCategories()[goBranchNum];
        int numberOfGenes = info.getNumGenes();
                
        SymMatrix CoAnnotationMatrix = annoSet.GetCoAnnotationMatrix();
        DenseVector BHalf = annoSet.GetBHalf();

        double constant = annoSet.GetConstant();
        CoAnnotationMatrix.setDiag(0d); // TODO: note we are altering this structure, if its cached other users might be surprised! 
        int size = featureList.size();

        logger.debug("Number of Genes " + numberOfGenes + ", Number of Categories " + numberOfCategories + ", Number of networks: " + (size-1));
        double biasValue = (double) numberOfGenes * (double) numberOfGenes * (double) numberOfCategories;
        logger.debug("biasValue: " + biasValue);

        DenseMatrix Ktt = new DenseMatrix(size, 1);
        Ktt.set(0, 0, MatrixUtils.sum(BHalf) * numberOfGenes + CoAnnotationMatrix.elementSum() + constant * numberOfGenes * numberOfGenes);

        FeatureLoader featureLoader = new FeatureLoader(cache, namespace, organismId);
        
        logger.debug("Ktt bias value is " + Ktt.get(0, 0));
        for (int i = 1; i < size; i++) {
            if (progress.isCanceled()) {
                throw new CancellationException();
            }
            
            Feature iFeature = featureList.get(i);
            SymMatrix network_i = featureLoader.load(iFeature);
            
            double val = computeKttElement(numberOfGenes, network_i, CoAnnotationMatrix, BHalf, constant);
            Ktt.set(i, 0, val);
        }
        
        return Ktt;
    }
    
    /*
     * note the coAnnotation has non-zero diag, but it doesn't matter since we are
     * multiplying it by a network which (should always) have zero self-interactions.
     */
    public static double computeKttElement(int numberOfGenes, SymMatrix network, SymMatrix CoAnnotationMatrix, DenseVector BHalf, double constant) {
        double result = 0d;

        double networkSum = network.elementSum();

        DenseVector tempVec = new DenseVector(numberOfGenes);
        network.mult(BHalf.getData(), tempVec.getData());
        double tempVecSum = MatrixUtils.sum(tempVec);

        result = network.elementMultiplySum(CoAnnotationMatrix) +
                 tempVecSum + networkSum * constant;
        
        return result;
    }    

    public DenseMatrix updateBasicKtK(DenseMatrix KtK, FeatureList featureList, FeatureList featuresToAdd, ProgressReporter reporter) throws ApplicationException {
        checkFeatureList(featureList, true);
        checkFeatureList(featuresToAdd, false);
        
        // allocate new KtK, copy over data
        final int oldSize = featureList.size(); // == KtK.numRows() == KtK.numCols()
        final int numFeaturesToAdd = featuresToAdd.size();
        final int newSize = oldSize + numFeaturesToAdd;
        
        logger.debug("allocating new KtK and copying data over");
        DenseMatrix KtKNew = new DenseMatrix(newSize, newSize);        
        for (int i=0; i<oldSize; i++) {
            for (int j=0; j<oldSize; j++) {
                KtKNew.set(i, j, KtK.get(i, j));
            }
        }
        
        // compute products between previous features, and new
        FeatureLoader featureLoader = new FeatureLoader(cache, namespace, organismId);

        logger.debug("preloading new features");
        
        SymMatrix [] newFeatures = new SymMatrix[numFeaturesToAdd];
        for (int j=0; j<numFeaturesToAdd; j++) {
            Feature jFeature = featuresToAdd.get(j);
            SymMatrix network_j = featureLoader.load(jFeature);    
            newFeatures[j] = network_j;
        }

        logger.debug(String.format("computing products between %d new and %d old features", numFeaturesToAdd, oldSize));

        for (int i=1; i<oldSize; i++) {
            Feature iFeature = featureList.get(i);
            SymMatrix network_i = featureLoader.load(iFeature);
            
            for (int j=0; j<numFeaturesToAdd; j++) {
                if (progress.isCanceled()) {
                    throw new CancellationException();
                }
                
//                Feature jFeature = featuresToAdd.get(j);
//                SymMatrix network_j = featureLoader.load(jFeature); 
                SymMatrix network_j = newFeatures[j];
                
                double prodSum = network_i.elementMultiplySum(network_j);
                KtKNew.set(i, j + oldSize, prodSum);
                KtKNew.set(j + oldSize, i, prodSum);
            }
        }
        
        // compute products of new features amongst themselves, and biases
        logger.debug(String.format("computing products between %d new features, and their biases", numFeaturesToAdd));
        for (int i=0; i<numFeaturesToAdd; i++) {
//            Feature iFeature = featuresToAdd.get(i);
//            SymMatrix network_i = featureLoader.load(iFeature);
            SymMatrix network_i = newFeatures[i];
            
            double networkSum = network_i.elementSum();
            KtKNew.set(i + oldSize, 0, networkSum);
            KtKNew.set(0, i + oldSize, networkSum);
            
            for (int j=0; j<numFeaturesToAdd; j++) {
                if (progress.isCanceled()) {
                    throw new CancellationException();
                }
                
                Feature jFeature = featuresToAdd.get(j);
                SymMatrix network_j = featureLoader.load(jFeature);        
                
                double prodSum = network_i.elementMultiplySum(network_j);
                KtKNew.set(i + oldSize, j + oldSize, prodSum);
                KtKNew.set(j + oldSize, i + oldSize, prodSum);
            }
        }            
        
        return KtKNew;
    }

    public DenseMatrix updateKtT(DenseMatrix Ktt, FeatureList featureList, FeatureList featuresToAdd, CoAnnotationSet annoSet, ProgressReporter reporter) throws ApplicationException {
        checkFeatureList(featureList, true);
        checkFeatureList(featuresToAdd, false);
        
        DatasetInfo info = cache.getDatasetInfo(organismId);     
        int numberOfGenes = info.getNumGenes();

        SymMatrix CoAnnotationMatrix = annoSet.GetCoAnnotationMatrix();
        DenseVector BHalf = annoSet.GetBHalf();
        double constant = annoSet.GetConstant();

        // allocate new KtT, copy over data
        final int oldSize = featureList.size(); // == KtK.numRows() == KtK.numCols()
        final int numFeaturesToAdd = featuresToAdd.size();
        final int newSize = oldSize + numFeaturesToAdd;
        
        DenseMatrix KttNew = new DenseMatrix(newSize, 1);
        for (int i=0; i<oldSize; i++) {
            KttNew.set(i, 0, Ktt.get(i,0));
        }
        
        FeatureLoader featureLoader = new FeatureLoader(cache, namespace, organismId);

        // compute new elements
        for (int i=0; i<numFeaturesToAdd; i++) {
            if (progress.isCanceled()) {
                throw new CancellationException();
            }
            
            Feature iFeature = featuresToAdd.get(i);
            SymMatrix network_i = featureLoader.load(iFeature);
            
            double val = computeKttElement(numberOfGenes, network_i, CoAnnotationMatrix, BHalf, constant);
            KttNew.set(i + oldSize, 0, val);            
        }

        return KttNew;
    }
    
    /*
     *  we're making assumptions about position of bias, verify.
     *  anything else we want to test?
     */
    public static void checkFeatureList(FeatureList featureList, boolean hasBias) throws ApplicationException {
        
        if (hasBias) {
            if (featureList.get(0).getType() != NetworkType.BIAS) {
                throw new ApplicationException("must include bias in first row/col");
            }
        }
        else {
            if (featureList.get(0).getType() == NetworkType.BIAS) {
                throw new ApplicationException("must not include bias in first row/col");
            }           
        }
    }
}
