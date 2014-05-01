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

package org.genemania.engine.core.mania;

import java.util.ArrayList;
import java.util.Collection;
import no.uib.cipr.matrix.Vector;
import org.apache.log4j.Logger;
import org.genemania.engine.Constants.CombiningMethod;
import org.genemania.engine.Constants.NetworkType;
import org.genemania.engine.cache.DataCache;
import org.genemania.engine.config.Config;
import org.genemania.engine.core.data.Data;
import org.genemania.engine.core.integration.CombinedKernelBuilder;
import org.genemania.engine.core.integration.Feature;
import org.genemania.engine.core.integration.FeatureWeightMap;
import org.genemania.engine.core.propagation.PropagateLabels;
import org.genemania.engine.matricks.SymMatrix;
import org.genemania.exception.ApplicationException;
import org.genemania.util.NullProgressReporter;
import org.genemania.util.ProgressReporter;

/*
 * Apply the genemania algorithm with the given params.
 * Data must be in cache files. No input/output conversion
 * or filtering is performed here, minimal dependencies.
 *
 * To use, create an instance, call compute(), then call
 * getters to get the outputs.
 *
 * Alternatively, to compute just the weights and combined kernel,
 * call computeWeights(). The computation of the discriminant using
 * this set of weights can then be done using computeDiscriminant().
 * This may be useful if eg using the same weight matrix for
 * multiple discriminant calculation (eg for equal weighting).
 *
 */
public class CoreMania {

    private static Logger logger = Logger.getLogger(CoreMania.class);
    private DataCache cache;
    private Vector discriminant;
    private SymMatrix partiallyCombinedKernel;
    private SymMatrix combinedKernel;
    private FeatureWeightMap featureWeights;
    private ProgressReporter progress;

    public CoreMania(DataCache cache, ProgressReporter progress) {
        this.cache = cache;
        this.progress = progress;
    }

    public CoreMania(DataCache cache) {
        this(cache, NullProgressReporter.instance());
    }

    /*
     * general use method that performs both network combination and
     * label propagation for the given query parameters.
     */
    public void compute(String namespace, long organismId, Vector labels, CombiningMethod combiningMethod,
            Collection<Collection<Long>> networkIds, Collection<Long> attributeGroupIds, int attributesLimit, String goCategory, String biasingMethod) throws ApplicationException {

        long t1 = System.nanoTime();

        // network combination
        computeWeights(namespace, organismId, labels, combiningMethod, networkIds, attributeGroupIds, attributesLimit);

        // label propagation
        computeDiscriminant(Data.CORE, organismId, labels, goCategory, biasingMethod);

        long t2 = System.nanoTime();
        logger.info("total time for compute: " + (t2-t1));
    }
    
    /*
     * support old api, no attributes
     */
    public void compute(String namespace, long organismId, Vector labels, CombiningMethod combiningMethod,
            Collection<Collection<Long>> networkIds, String goCategory, String biasingMethod) throws ApplicationException {

            Collection<Long> emptyAttributeGroupList = new ArrayList<Long>();
            compute(namespace, organismId, labels, combiningMethod, networkIds, 
                    emptyAttributeGroupList, 0, goCategory, biasingMethod);
    }
    
    /*
     * Compute network weighting for the given networks and combining method.
     * A map of the weights and the resulting combined kernel are stored in
     * member fields.
     */
    public void computeWeights(String namespace, long organismId, Vector labels, CombiningMethod combiningMethod, 
            Collection<Collection<Long>> networkIds, Collection<Long> attributeGroupIds, int attributesLimit) throws ApplicationException {
        logger.info("computing weights");

        long t1 = System.nanoTime();
        CalculateNetworkWeights calculator = new CalculateNetworkWeights(namespace, cache, networkIds, attributeGroupIds,
                organismId, labels, attributesLimit, combiningMethod, progress);
        calculator.process();

        SymMatrix combinedKernel = calculator.getCombinedMatrix();
        FeatureWeightMap featureWeights = calculator.getWeights();
        
        logger.debug("# weights: " + featureWeights.size());
        int numSparse = 0;
        int numAttribute = 0;
        for (Feature feature: featureWeights.keySet()) {
            if (feature.getType() == NetworkType.SPARSE_MATRIX) {
                numSparse += 1;
            }
            else if (feature.getType() == NetworkType.ATTRIBUTE_VECTOR) {
                numAttribute += 1;
            }
            else {
                throw new ApplicationException("unexpected feature type");
            }
        }
        logger.debug(String.format("# sparse: %d, # attribute %d", numSparse, numAttribute));
        

        /*
         * this used to be supported optionally, via 
         * Normalization.normalizeNetwork(combinedKernel),
         * but now with our partially materialized combined network
         * with optimized attributes, normalization will break. 
         * Haven't used it for ages, so its probably safe to remove
         * support, just throw exception in case some caller enables
         * it in the config.
         */
        if (Config.instance().isCombinedNetworkRenormalizationEnabled()) {
            throw new ApplicationException("renormalization of combined network not supported");
        }

        this.partiallyCombinedKernel = combinedKernel;
        this.featureWeights = featureWeights;

        long t2 = System.nanoTime();
        logger.info("time for computeWeights: " + (t2-t1));
    }

    /*
     * Compute the discriminant scores given the vector of labels, and using the
     * combined kernel that was computed from the last call to computeWeights().
     *
     * The resulting discriminant score vector is written is stored in a member field.
     */
    public void computeDiscriminant(String namespace, long organismId, Vector labels, String goCategory, String biasingMethod) throws ApplicationException {
        logger.info("computing scores");

        long t1 = System.nanoTime();

        Vector discriminant = null;
        if (biasingMethod.equalsIgnoreCase("hierarchy")) {
            logger.info("using GO hierarchy label bias method");
            throw new ApplicationException("hierarchical biasing not implemented");
        }
        else if (biasingMethod.equalsIgnoreCase("average")) {            
            logger.info("using average label bias method");
            discriminant = PropagateLabels.process(
                    getCombinedKernel(organismId, namespace), labels, progress);
        }
        else {
            throw new ApplicationException("illegal biasing method name");
        }

        this.discriminant = discriminant;

        long t2 = System.nanoTime();
        logger.info("time for computeDiscriminant: " + (t2-t1));

    }

    /**
     * @return the discriminant
     */
    public Vector getDiscriminant() {
        return discriminant;
    }

    /**
     * @return the part of the combined network computed by
     * adding up weighted interaction networks. doesn't include
     * attributes.
     */
    public SymMatrix getPartiallyCombinedKernel() {
        return partiallyCombinedKernel;
    }

    /**
     * 
     * having to pass in the organism/namespace here is a bit ugly, should
     * consider moving to object members initialized in constructor
     * 
     * @return the combinedKernel
     * @throws ApplicationException 
     */
    public SymMatrix getCombinedKernel(long organismId, String namespace) throws ApplicationException {
        if (combinedKernel == null) {
            
            // can't even build one, return null
            if (partiallyCombinedKernel == null && featureWeights == null) {
                return null;
            }
            
            CombinedKernelBuilder builder = new CombinedKernelBuilder(cache);
            combinedKernel = builder.build(organismId, namespace, partiallyCombinedKernel, featureWeights);
        }
        
        return combinedKernel;
    }
    
    /**
     * @return the matrixWeights
     */
    public FeatureWeightMap getFeatureWeights() {
        return featureWeights;
    }


}
