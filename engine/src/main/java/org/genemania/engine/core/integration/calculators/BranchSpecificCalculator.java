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

package org.genemania.engine.core.integration.calculators;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.Vector;
import org.apache.log4j.Logger;
import org.genemania.engine.Constants;
import org.genemania.engine.Constants.CombiningMethod;
import org.genemania.engine.Constants.NetworkType;
import org.genemania.engine.cache.DataCache;
import org.genemania.engine.core.MatrixUtils;
import org.genemania.engine.core.data.CoAnnotationSet;
import org.genemania.engine.core.data.KtKFeatures;
import org.genemania.engine.core.integration.CombineNetworksOnly;
import org.genemania.engine.core.integration.Feature;
import org.genemania.engine.core.integration.FeatureList;
import org.genemania.engine.core.integration.Solver;
import org.genemania.engine.core.integration.attribute.QueryEnrichedAttributeScorer;
import org.genemania.engine.core.integration.gram.BasicGramBuilder;
import org.genemania.engine.core.integration.gram.GramEditor;
import org.genemania.engine.exception.WeightingFailedException;
import org.genemania.exception.ApplicationException;
import org.genemania.util.ProgressReporter;

/**
 * compute branch specific go annotation based weighting, for one of
 * a given branch: BP, MF, CC
 */
public class BranchSpecificCalculator extends AbstractNetworkWeightCalculator {

    private static Logger logger = Logger.getLogger(BranchSpecificCalculator.class);
    CombiningMethod method;
    
    private static int MIN_QUERY_GENES_PER_ATTRIBUTE = 1;

    public BranchSpecificCalculator(String namespace, DataCache cache, Collection<Collection<Long>> networkIds, 
    		Collection<Long> attributeGroupIds, long organismId, Vector label, int attributesLimit, CombiningMethod method, ProgressReporter progress) throws ApplicationException {
        super(namespace, cache, networkIds, attributeGroupIds, organismId, label, attributesLimit, progress);
        this.method = method;
    }

    public void process() throws ApplicationException {
        progress.setStatus(Constants.PROGRESS_WEIGHTING_MESSAGE);
        progress.setProgress(Constants.PROGRESS_WEIGHTING);

        boolean hasUserNetworks = queryHasUserNetworks();
        computeNewResult(hasUserNetworks);        
    }
    
    /*
     * perform branch specific computation
     */
    void computeNewResult(boolean hasUserNetworks) throws ApplicationException {
        DenseMatrix KtK = (DenseMatrix) getKtK(hasUserNetworks);
        DenseMatrix KtT = (DenseMatrix) getKtT(method.toString(), hasUserNetworks);

        // this is the list used in the precomputed ktk/ktt
        KtKFeatures ktkFeatures = cache.getKtKFeatures(namespace, organismId);
        FeatureList KtKFeatureList = ktkFeatures.getFeatures();

        QueryEnrichedAttributeScorer attributeScorer = new QueryEnrichedAttributeScorer(cache, label, MIN_QUERY_GENES_PER_ATTRIBUTE);        
        FeatureList featureList = buildFeatureList(attributeScorer, false);        
        featureList.addBias();
       

        // so we have a precomputed KtK/KtT, which likely contains more
        // features than we need, but possibly doesn't contain all the ones
        // we do (if we didn't cache attribute correlated). proceed as
        // follows: throw away rows/columns we don't need, then from
        // what's left update with any additional features
        
        FeatureList haveThem = intersect(KtKFeatureList, featureList);
        FeatureList needThem = setdiff(KtKFeatureList, featureList);
        
        // this should always be true, but i feel the need to check
        if (haveThem.get(0).getType() != NetworkType.BIAS) { 
            throw new ApplicationException("internal error: bias must be first column");
        }
        
        // notice that after this removal, the resulting KtK/KtT have features specified by haveThem not KtKFeatureList
        KtK = GramEditor.RemoveNetworkKtK(KtK, KtKFeatureList, haveThem);
        KtT = GramEditor.RemoveNetworkKtT(KtT, KtKFeatureList, haveThem);
        
        if (needThem.size() > 0) {
            logger.debug(String.format("need to update gram for %d features", needThem.size()));
            BasicGramBuilder builder = new BasicGramBuilder(cache, namespace, organismId, progress);
            KtK = builder.updateBasicKtK(KtK, haveThem, needThem, progress);
            CoAnnotationSet annoSet = cache.getCoAnnotationSet(organismId, method.toString());
            KtT = builder.updateKtT(KtT, haveThem, needThem, annoSet, progress);
            haveThem.addAll(needThem);
        }
        
        scaleKtK(KtK, method.toString());
        
        try {
            weights = Solver.solve(KtK, MatrixUtils.extractColumnToVector(KtT, 0), haveThem, progress);
        }
        catch (WeightingFailedException e) {
            logger.error("weighting calculation failed, falling back to average: " + e.getMessage());
            weights = AverageByNetworkCalculator.average(haveThem);
        }
        progress.setStatus(Constants.PROGRESS_COMBINING_MESSAGE);
        progress.setProgress(Constants.PROGRESS_COMBINING);
        combinedMatrix = CombineNetworksOnly.combine(weights, namespace, organismId, cache, progress);
    }
    
    /*
     * list of features in both A and B
     */
    private FeatureList intersect(FeatureList A, FeatureList B) {
        HashSet<Feature> sA = new HashSet<Feature>();
        sA.addAll(A);
        HashSet<Feature> sB = new HashSet<Feature>();
        sB.addAll(B);
        sA.retainAll(sB);
        
        FeatureList features = new FeatureList();
        features.addAll(sA);
        Collections.sort(features);
        return features;
    }
    
    /*
     * return features in B but no in A
     */
    private FeatureList setdiff(FeatureList A, FeatureList B) {
        HashSet<Feature> sB = new HashSet<Feature>();
        sB.addAll(B);
        sB.removeAll(intersect(A, B));
        
        FeatureList features = new FeatureList();
        features.addAll(sB);
        Collections.sort(features);
        return features;      
    }
    
    public static final String PARAM_KEY_FORMAT = "%s-%s"; // method-networks

    /*
     * if there are no attributes in the query, then the weight calc is
     * independent of the genes list and can be precomputed. the key
     * for the network is the go-branch (encoded in method) and the list
     * of networks.
     * 
     * if attributes are included then the precise subset of attributes
     * we consider depends on the gene list and a user-selectable threshold.
     * in this case we don't cache.
     * 
     */
    @Override
    public String getParameterKey() throws ApplicationException {
        if (this.attributeGroupIds != null && this.attributeGroupIds.size()>0) {
            throw new ApplicationException("not cacheable");
        }
        String networks = formattedNetworkList(this.networkIds);
        return String.format(PARAM_KEY_FORMAT, method.toString(), networks);
    }
}
