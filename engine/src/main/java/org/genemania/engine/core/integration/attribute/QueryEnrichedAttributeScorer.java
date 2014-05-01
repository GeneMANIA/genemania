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
package org.genemania.engine.core.integration.attribute;

import java.util.ArrayList;
import java.util.Collection;

import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Vector;

import org.apache.log4j.Logger;
import org.genemania.engine.Constants.NetworkType;
import org.genemania.engine.actions.ComputeEnrichment;
import org.genemania.engine.cache.DataCache;
import org.genemania.engine.core.MatrixUtils;
import org.genemania.engine.core.data.AttributeData;
import org.genemania.engine.core.data.AttributeGroups;
import org.genemania.engine.core.data.Data;
import org.genemania.engine.core.data.NodeIds;
import org.genemania.engine.core.integration.Feature;
import org.genemania.engine.core.integration.FeatureList;
import org.genemania.engine.core.utils.ObjectSelector;
import org.genemania.engine.matricks.Matrix;
import org.genemania.exception.ApplicationException;

/*
 * given an attribute group id and list of genes,
 * return attributes enriched for those genes of
 * some max size.
 * 
 * Since there may be too many attributes to be able to
 * compute an optimal weighting by GeneMANIA's network
 * integration, this provides a simple way to narrow
 * down the computation.
 * 
 * to select we require at least a few genes in the query list 
 * have the attribute, and then select up to the max requested
 * # of attributes by an enrichment test.
 * 
 * TODO: make interruptible via progress reporter?
 */ 
public class QueryEnrichedAttributeScorer implements IAttributeScorer {
    private static Logger logger = Logger.getLogger(QueryEnrichedAttributeScorer.class);

    // an attribute with 1 gene isn't a network, we can't use it. these
    // should be excluded at data-load time, but filter here to be safe
    private static int MIN_NUM_TOTAL_GENES_PER_ATTRIBUTE = 2;
    
    DataCache cache;
    Vector labels;
    int minQueryGenesPerAttribute;
    
    public QueryEnrichedAttributeScorer(DataCache cache, Vector labels, int minQueryGenesPerAttribute) {
        super();
        this.cache = cache;
        this.labels = labels;
        this.minQueryGenesPerAttribute = minQueryGenesPerAttribute;
    }

    void logAttributeCounts(String namespace, long organismId, long groupId) throws ApplicationException {
        AttributeData attributeSet = cache.getAttributeData(namespace, organismId, groupId);    
        Matrix data = attributeSet.getData();
        org.genemania.engine.matricks.Vector colSums = data.columnSums();
        for (int i=0; i<colSums.getSize(); i++) {
            if (colSums.get(i)>0) {
                logger.debug(String.format("attribute %d has col sum %f", i, colSums.get(i)));
            }
        }  
    }
    
    /*
     * note this is allowed to throw away some attributes that we don't want to consider at all,
     * ie that have less than some threshold of genes in the query list
     * 
     * (non-Javadoc)
     * @see org.genemania.engine.core.integration.attribute.IAttributeRanker#rankAttributes(long, long)
     */
    @Override
    public ObjectSelector<Feature> scoreAttributes(String namespace, long organismId, long attributeGroupId) throws ApplicationException {
        
        AttributeData attributeData = cache.getAttributeData(namespace, organismId, attributeGroupId);
        DenseVector pvals = new DenseVector(attributeData.getData().numCols());
        
        DenseVector selection = computeSelectionMask(organismId, labels);

        Matrix annotations = attributeData.getData();
        
        int numCategories = annotations.numCols();
        int numGenes = annotations.numRows();
        
        DenseVector backgroundCounts = new DenseVector(numCategories);
        annotations.columnSums(backgroundCounts.getData());
        
        DenseVector sampleCounts = new DenseVector(numCategories);
        annotations.transMult(selection.getData(), sampleCounts.getData());        
        
        /* test logging
        int n = 0;
        for (int i=0; i<sampleCounts.getData().length; i++) {
            if (sampleCounts.get(i) > 0) {
                System.out.println(String.format("i: %d, sample(i): %f, background(i): %f", i, sampleCounts.get(i), backgroundCounts.get(i)));
                n += 1;
            }
        }
        logger.debug("number of categories with at least 1 annotation to +ve set: " + n);
        
        n = 0;
        for (int i=0; i<sampleCounts.getData().length; i++) {
            if (sampleCounts.get(i) > 1) {
                n += 1;
            }
        }
        logger.debug("number of categories with at least 2 annotation to +ve set: " + n);
        
        n = 0;
        for (int i=0; i<sampleCounts.getData().length; i++) {
            if (sampleCounts.get(i) > 2) {
                n += 1;
            }
        }
        logger.debug("number of categories with at least 3 annotation to +ve set: " + n);
        */
        
        computePVals(annotations, numCategories, numGenes,  backgroundCounts, sampleCounts, selection, pvals);
                
        ObjectSelector<Feature> list = buildList(namespace, organismId, attributeGroupId, pvals, sampleCounts, backgroundCounts);
        return list;
    }
    

    
    // TODO: some refactoring with common code in ComputeEnrichment
    private ObjectSelector<Feature> buildList(String namespace, long organismId, long attributeGroupId,
            DenseVector pvals, DenseVector sampleCounts, DenseVector backgroundCounts) throws ApplicationException {
        
        ObjectSelector<Feature> list = new ObjectSelector<Feature>();

        AttributeGroups groups = cache.getAttributeGroups(namespace, organismId);
        ArrayList<Long> attributeIds = groups.getAttributeGroups().get(attributeGroupId);
        for (int i=0; i<attributeIds.size(); i++) {   
            long attributeId = attributeIds.get(i);
            double sampleCount = sampleCounts.get(i);
            double backgroundCount = backgroundCounts.get(i);
            double pval = pvals.get(i);
            
            if (pval < (1d-1.0e-5)   // ignore features with pvals that are just about 1
                && sampleCount >= minQueryGenesPerAttribute   // ignore features with too few genes in query
                && backgroundCount > MIN_NUM_TOTAL_GENES_PER_ATTRIBUTE) { // ignore features that aren't a network

                Feature feature = new Feature(NetworkType.ATTRIBUTE_VECTOR, attributeGroupId, attributeId);
                list.add(feature, pvals.get(i));
            }
        }
       
        logger.debug(String.format("ranked %d attributes by enrichment based on query list", list.size()));
        return list;
    }

    /*
     * featurelist based on given group, ordered consistently with attributes in 
     * the group
     */
    public static FeatureList buildFeatureList(AttributeGroups attributeGroups) {
        FeatureList features = new FeatureList();
        for (long groupId: attributeGroups.getAttributeGroups().keySet()) {
            ArrayList<Long> attributeIds = attributeGroups.getAttributeGroups().get(groupId);
            for (long attributeId: attributeIds) {            
                Feature feature = new Feature(NetworkType.ATTRIBUTE_VECTOR, groupId, attributeId);
                features.add(feature);
            }
        }
        
        return features;
    }
    
    /*
     * the given label vector contains +ve, -ve, and unlabelled values. we want a vector with 1's for 
     * the +ve's, and 0's for everything else.
     *  
     * this is evil, we're filtering the labels vector, but assuming some knowledge about it.
     * we should revise the api to be given the list of +ve node ids, and build the selection vector
     * up from this using the second version of computeSelectionMask() below. TODO
     */
    DenseVector computeSelectionMask(long organismId, Vector labels) throws ApplicationException {
        NodeIds nodeIds = cache.getNodeIds(organismId);
        DenseVector selection = new DenseVector(nodeIds.getNodeIds().length);
        
        for (int i = 0; i<labels.size(); i++) {
            if (labels.get(i) == 1d) { // also, evil hardcoding of the +'ve value in the label vector assuming its 1 ... TODO fix me!
                selection.set(i, 1);
            }
        }
        
        return selection;
    }
   
    // see comment above
    DenseVector computeSelectionMask(long organismId, Collection<Long> nodes) throws ApplicationException {
        NodeIds nodeIds = cache.getNodeIds(organismId);
        DenseVector selection = new DenseVector(nodeIds.getNodeIds().length);
        
        for (long id: nodes) {
            int index = nodeIds.getIndexForId(id);
            selection.set(index, 1);
        }
        
        return selection;
    }
    
    /*
     * sort of a generic enrichment computation function. doesn't mask for interacting background
     * just uses full. since we're not reporting p-values but just selecting genes, should be ok.
     */
    void computePVals(Matrix annotations, int numCategories, int numGenes, DenseVector backgroundCounts, 
            DenseVector sampleCounts, DenseVector selection, DenseVector pvals) {
        // we must have numGenes == selection.size() == pvals.size() == annotations.numRows()
        
        int N = numGenes ; // population size, more properly we could mask to genes with interactions
        int n = MatrixUtils.countMatches(selection, 1); // sample size
        
        for (int category=0; category<numCategories; category++) {
            
            long x = Math.round(sampleCounts.get(category));      // # true in sample
            long k = Math.round(backgroundCounts.get(category));  // # true in population
           
            double pval = ComputeEnrichment.computeCumulHyperGeo(x, N, n, k);            
            pvals.set(category, pval);
        }
    }
    
}
