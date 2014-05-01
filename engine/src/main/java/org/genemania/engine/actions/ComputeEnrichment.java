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


package org.genemania.engine.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import no.uib.cipr.matrix.DenseVector;
import org.apache.commons.math.special.Gamma;
import org.apache.log4j.Logger;
import org.genemania.dto.EnrichmentEngineRequestDto;
import org.genemania.dto.EnrichmentEngineResponseDto;
import org.genemania.dto.OntologyCategoryDto;
import org.genemania.engine.cache.DataCache;
import org.genemania.engine.core.MatrixUtils;
import org.genemania.engine.core.data.CategoryIds;
import org.genemania.engine.core.data.Data;
import org.genemania.engine.core.data.DatasetInfo;
import org.genemania.engine.core.data.GoAnnotations;
import org.genemania.engine.core.data.GoIds;
import org.genemania.engine.core.data.NodeDegrees;
import org.genemania.engine.core.data.NodeIds;
import org.genemania.engine.core.utils.Logging;
import org.genemania.engine.exception.CancellationException;
import org.genemania.engine.matricks.Matrix;
import org.genemania.exception.ApplicationException;

/**
 * 
 */
public class ComputeEnrichment {
    private static Logger logger = Logger.getLogger(ComputeEnrichment.class);
    private DataCache cache;
    private EnrichmentEngineRequestDto request;

    private long requestStartTimeMillis;
    private long requestEndTimeMillis;

    public ComputeEnrichment(DataCache cache, EnrichmentEngineRequestDto request) {
        this.cache = cache;
        this.request = request;
    }

    /*
     * main request processing logic
     */
    public EnrichmentEngineResponseDto process() throws ApplicationException {
        try {
            requestStartTimeMillis = System.currentTimeMillis();

            logStart();
            checkQuery();
            logQuery();

            EnrichmentEngineResponseDto response = computeEnrichment();
            
            requestEndTimeMillis = System.currentTimeMillis();
            logEnd();

            return response;
        }
        catch (CancellationException e) {
            logger.info("request was cancelled");
            return null;
        }
    }

    EnrichmentEngineResponseDto computeEnrichment() throws CancellationException, ApplicationException {

        // load in annotations based on requested ontology, TODO: this needs to be updated to support ontology ids (convert to strings temporarily)
        
        GoAnnotations goAnnos = cache.getGoAnnotations(request.getOrganismId(), "" + request.getOntologyId());
        GoIds goIds = cache.getGoIds(request.getOrganismId(), "" + request.getOntologyId());
        CategoryIds catIds = cache.getCategoryIds(request.getOrganismId(), request.getOntologyId());
        DatasetInfo datasetInfo = cache.getDatasetInfo(request.getOrganismId());

        // TODO: for now we get the degrees from the core dataset, but we should extend this
        // to compute degrees for user datasets and use that instead, so if a user uploads
        // a network containing a gene for which we have no interactions (but do have annotation
        // data from GO), then that gene would get included in the enrichment calculation
        NodeDegrees nodeDegrees = cache.getNodeDegrees(Data.CORE, request.getOrganismId());
        
        int numCategories = goAnnos.getData().numCols();
        int numGenes = goAnnos.getData().numRows();  // note: this count includes genes without any interactions

        logger.debug(String.format("num genes: %d, num categories: %d", numGenes, numCategories));

        // compute annotations in background for all categories, using
        // node degrees to determine what genes are in the background distribution
        DenseVector backgroundMask = makeGeneMaskVector(nodeDegrees.getDegrees().getData(), numGenes);
        DenseVector backgroundCounts = new DenseVector(numCategories);

        Matrix annoData = goAnnos.getData();
        annoData.transMult(backgroundMask.getData(), backgroundCounts.getData());

        // for sample, we are only interested in genes in the sample which are in our background
        NodeIds nodeIds = cache.getNodeIds(request.getOrganismId());
        DenseVector sampleMask = makeGeneMaskVector(nodeIds, numGenes);
        dotMult(sampleMask, backgroundMask);
        
        DenseVector sampleCounts = new DenseVector(numCategories);
        annoData.transMult(sampleMask.getData(), sampleCounts.getData());

        // only count number of genes in sample that are in the background
        //long n = request.getNodes().size();
        long n = Math.round(MatrixUtils.sum(sampleMask));

        // TODO: this N won't be right if user networks to contribute information on genes with degree 0 in our core data set
        int N = datasetInfo.getNumInteractingGenes(); 

        logger.debug(String.format("enrichment sample size: %d, background size: %d", n, N));
        
        DenseVector pvals = new DenseVector(numCategories);

        for (int category = 0; category < numCategories; category++) {

            if (request.getProgressReporter().isCanceled()) {
                throw new CancellationException();
            }

            long x = Math.round(sampleCounts.get(category));
            long k = Math.round(backgroundCounts.get(category));
            double pval = computeCumulHyperGeo(x, N, n, k);

            pvals.set(category, pval);
        }

        DenseVector qvals = computeFDRqval(N, pvals);

        boolean [] shouldReturn = selectCategoriesToReturn(qvals, request.getqValueThreshold(), request.getMinCategories());

        // build up return data structure
        Map<String, OntologyCategoryDto> categoryIdToVO = new HashMap<String, OntologyCategoryDto>();
        Collection<OntologyCategoryDto> enrichedCategories = new ArrayList<OntologyCategoryDto>();
        
        for (int category = 0; category < numCategories; category++) {

            if (shouldReturn[category]) {
                String categoryName = goIds.getIdForIndex(category);
                long catId = catIds.getIdForIndex(category);

                OntologyCategoryDto categoryVO = new OntologyCategoryDto();
                categoryVO.setId(catId);
                categoryVO.setpValue(pvals.get(category));
                categoryVO.setqValue(qvals.get(category));
                categoryVO.setNumAnnotatedInSample((int) Math.round(sampleCounts.get(category)));
                categoryVO.setNumAnnotatedInTotal((int) Math.round(backgroundCounts.get(category)));

                enrichedCategories.add(categoryVO);
                categoryIdToVO.put(categoryName, categoryVO);
            }

        }

        Map<Long, Collection<OntologyCategoryDto>> annotations = makeAnnotationsMap(goAnnos, goIds, nodeIds, categoryIdToVO);

        EnrichmentEngineResponseDto response = new EnrichmentEngineResponseDto();
        response.setEnrichedCategories(enrichedCategories);
        response.setAnnotations(annotations);
        
        logger.debug(String.format("returning %d categories", enrichedCategories.size()));

        return response;
    }

    /*
     * return mask array set to true for values we should return.
     * we could save a round of sorting if we integrated this functionality
     * with the computation of the qvals. but it seems tidy to keep these
     * separate
     */
    protected static boolean [] selectCategoriesToReturn(DenseVector qvals, double threshold, int minToReturn) {

        boolean [] shouldReturn = new boolean[qvals.size()];
        
        // ranks
        DenseVector ranks = qvals.copy();
        MatrixUtils.rank(ranks);

        // build ordered list
        DenseVector ordered = new DenseVector(qvals.size());
        int [] unrank = new int[qvals.size()];
        for (int i=0; i<ranks.size(); i++) {
            int p = (int) Math.round(ranks.get(i))-1;
            ordered.set(p, qvals.get(i));
            unrank[p] = i;
        }

        int numSelected = 0;
        int i = 0;

        // pick of all the elements meeting the threshold
        while (i < ranks.size()) {
            if (ordered.get(i) > threshold) {
                break;
            }

            shouldReturn[unrank[i]] = true;
            numSelected += 1;
            i += 1;
        }

        // add in more to meet minToReturn if necessary
        while (numSelected < minToReturn && i < ranks.size()) {
            
            // the multiple testing adjustment can actually scale some
            // pvals to > 1, stop when we get to those
            if (ordered.get(i) > 1) {
                break;
            }

            shouldReturn[unrank[i]] = true;
            numSelected += 1;
            i += 1;
        }

        return shouldReturn;
    }

    /*
     * for each node id in the request, construct collection of corresponding categories to
     * which it is annotated
     */
    private Map<Long, Collection<OntologyCategoryDto>> makeAnnotationsMap(GoAnnotations goAnnos, GoIds goIds, NodeIds nodeIds,
            Map<String, OntologyCategoryDto> categoryIdToVO) throws ApplicationException {

        Map<Long, Collection<OntologyCategoryDto>> annotations = new HashMap<Long, Collection<OntologyCategoryDto>>();
        int numCategories = goAnnos.getData().numCols();
        Matrix annoData = goAnnos.getData();
        for (Long nodeId: request.getNodes()) {
            Collection<OntologyCategoryDto> categories = new ArrayList<OntologyCategoryDto>();

            int index = nodeIds.getIndexForId(nodeId);

            // this loop doesn't take advantage of the sparsity of the goAnnos matrix,
            // it tests each category
            for (int category=0; category<numCategories; category++) {
                double x = annoData.get(index, category);
                long longx = Math.round(x);
                if (longx == 1) {
                    String categoryName = goIds.getIdForIndex(category);
                    OntologyCategoryDto categoryVO = categoryIdToVO.get(categoryName);

                    // only the *enriched* categories are in the map
                    if (categoryVO != null) {
                        categories.add(categoryVO);
                    }
                }
            }
                          
            annotations.put(nodeId, categories);
        }

         return annotations;
    }

    /*
     * N = population size
     * k = # true in population
     * n = sample size
     * x = # true in sample
     *
     * compute h(x; N, n, k) = C(k, x) * C(N-k, n-x) / C(N, n)
     */
    public static double computeHyperGeo(double x, double N, double n, double k) {
        double h = Gamma.logGamma(k+1) - Gamma.logGamma(k-x+1) - Gamma.logGamma(x+1)
                   + Gamma.logGamma(N-k+1) - Gamma.logGamma(N-k-n+x+1) - Gamma.logGamma(n-x+1)
                   - Gamma.logGamma(N+1) + Gamma.logGamma(N-n+1) + Gamma.logGamma(n+1);
        
        return Math.exp(h);
    }


    /*
     * TODO: special case this for large x, so compute the sum the other way to reduce terms
     *
     * p(X > x; N, n, k)
     */
    public static double computeCumulHyperGeo(double x, double N, double n, double k) {
        double p = 0;
        double upperBound = Math.min(n, k);
        for (double i=x; i<=upperBound; i++) {
            p += computeHyperGeo(i, N, n, k);
        }

        return p;
    }

    public static DenseVector computeFDRqval(int N, DenseVector pvals) {

        // compute ranks of pvals
        DenseVector ranks = pvals.copy();
        MatrixUtils.rank(ranks);

        // build ordered pval for simple looping
        DenseVector ordered = new DenseVector(pvals.size());
        for (int i=0; i<ranks.size(); i++) {
            int p = (int) Math.round(ranks.get(i))-1;
            ordered.set(p, pvals.get(i));
        }

        // loop over ordered pval in descending order, computing qval
        DenseVector qval = new DenseVector(pvals.size());
        double minSoFar = Double.MAX_VALUE;
        for (int i=ranks.size()-1; i>=0; i--) {
            double p = ordered.get(i);
            int m = i+1;
            minSoFar = Math.min(minSoFar, N*p/m);
            qval.set(i, minSoFar);            
        }

        // reorder qval so it matches the original pval
        DenseVector unOrderedqval = new DenseVector(pvals.size());
        for (int i=0; i<unOrderedqval.size(); i++) {
            int p = (int) Math.round(ranks.get(i))-1;
            unOrderedqval.set(i, qval.get(p));
        }
        

        return unOrderedqval;
    }

    /* move to helper class
     * 
     */
    void setTo(DenseVector v, double val) {
        for (int i=0; i< v.size(); i++) {
            v.set(i, val);
        }
    }

    /*
     * create a vector containing 1's if the corresponding node id is in
     * the request's list, otherwise 0.
     */
    DenseVector makeGeneMaskVector(NodeIds nodeIds, int numGenes) throws ApplicationException {

        DenseVector mask = new DenseVector(numGenes);

        for (long nodeId: request.getNodes()) {
            int index = nodeIds.getIndexForId(nodeId);
            mask.set(index, 1);
        }

        return mask;
    }

    /*
     * create a vector contains 1's if the corresponding node has degree > 0,
     * otherwise 0
     */
    DenseVector makeGeneMaskVector(double [] degree, int numGenes) throws ApplicationException {

        // shouldn't happen, but ...
        if (degree.length != numGenes) {
            throw new ApplicationException("inconsistent data");
        }
        
        DenseVector mask = new DenseVector(numGenes);

        for (int index = 0; index < degree.length; index++) {
            if (degree[index] > 0) {
                mask.set(index, 1);
            }
        }
        
        return mask;
    }

    /*
     * todo: put in utils class?
     * 
     * x = x .* y
     */
    void dotMult(DenseVector x, DenseVector y) {
        for (int i=0; i<x.size(); i++) {
            x.set(i, x.get(i) * y.get(i));
        }
    }
    
    void logStart() {

    }

    void logEnd() {
        logger.info("completed processing request, duration = " + Logging.duration(requestStartTimeMillis, requestEndTimeMillis));
    }

    void logQuery() {
        logger.info(String.format("request for enrichment for organism %s with a set of %d genes using ontology %s qval threshold %f for a minimum of %d categories",
                request.getOrganismId(), request.getNodes().size(), request.getOntologyId(), request.getqValueThreshold(), request.getMinCategories()));

        logger.debug(String.format("sample nodes: " + request.getNodes()));
    }

    void checkQuery() throws ApplicationException {
        // TODO: check for duplicate node ids, invalid node ids,
        // invalid ontology

        if (request == null) {
            throw new ApplicationException("request object was null");
        }

        if (request.getNodes() == null) {
            throw new ApplicationException("list of sample nodes was null");
        }

        if (request.getNodes().size() == 0) {
            throw new ApplicationException("list of sample nodes was empty");
        }

        if (request.getProgressReporter() == null) {
            throw new ApplicationException("ProgressReporter was null");
        }      
    }
}
