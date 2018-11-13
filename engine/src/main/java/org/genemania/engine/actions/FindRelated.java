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

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Vector;

import org.apache.log4j.Logger;
import org.genemania.dto.AttributeDto;
import org.genemania.dto.InteractionDto;
import org.genemania.dto.NetworkDto;
import org.genemania.dto.NodeDto;
import org.genemania.dto.RelatedGenesEngineRequestDto;
import org.genemania.dto.RelatedGenesEngineResponseDto;
import org.genemania.engine.Constants;
import org.genemania.engine.Constants.NetworkType;
import org.genemania.engine.Constants.ScoringMethod;
import org.genemania.engine.cache.DataCache;
import org.genemania.engine.core.MatrixUtils;
import org.genemania.engine.core.data.AttributeData;
import org.genemania.engine.core.data.AttributeGroups;
import org.genemania.engine.core.data.Data;
import org.genemania.engine.core.data.DataSupport;
import org.genemania.engine.core.data.Network;
import org.genemania.engine.core.data.NetworkIds;
import org.genemania.engine.core.data.NodeIds;
import org.genemania.engine.core.integration.Feature;
import org.genemania.engine.core.integration.FeatureWeightMap;
import org.genemania.engine.core.mania.CoreMania;
import org.genemania.engine.core.utils.Logging;
import org.genemania.engine.exception.CancellationException;
import org.genemania.engine.labels.LabelVectorGenerator;
import org.genemania.engine.matricks.Matrix;
import org.genemania.engine.matricks.SymMatrix;
import org.genemania.exception.ApplicationException;
import org.genemania.type.CombiningMethod;

/**
 * process a find-related genes request. implemented for production mode,
 * for test validation mode need to add a way to control the unlabelled-value,
 * although some validation code directly calls into coremania with complete
 * label vector's and doesn't need this ...
 */
public class FindRelated {

    private static Logger logger = Logger.getLogger(FindRelated.class);
    private DataCache cache;
    private RelatedGenesEngineRequestDto request;
    private int numRequestNetworks;
    private int numRequestAttributeGroups;
    private boolean hasUserNetworks;
    private boolean hasUserAttributes;
    static final double posLabelValue = 1.0d;
    static final double negLabelValue = -1.0d;
    static final double unLabeledValueProduction = -1.0d;
    static final double unLabeledValueValidation = 0.0d;

    private long requestStartTimeMillis;
    private long requestEndTimeMillis;
    
    public FindRelated(DataCache cache, RelatedGenesEngineRequestDto request) {
        this.cache = cache;
        this.request = request;
    }

    /*
     * main request processing logic
     */
    public RelatedGenesEngineResponseDto process() throws ApplicationException {
        try {
            requestStartTimeMillis = System.currentTimeMillis();

            logStart();
            checkQuery();
            logQuery();
            
            ArrayList<Long> negativeNodes = new ArrayList<Long>();

            Vector labels = LabelVectorGenerator.createLabelsFromIds(cache.getNodeIds(request.getOrganismId()),
                    request.getPositiveNodes(), negativeNodes, posLabelValue, negLabelValue, unLabeledValueProduction);

            String goCategory = null;
            
            // crunch the numbers
            org.genemania.engine.Constants.CombiningMethod combiningMethod = Constants.convertCombiningMethod(request.getCombiningMethod(), request.getPositiveNodes().size());
            org.genemania.engine.Constants.ScoringMethod scoringMethod = Constants.convertScoringMethod(request.getScoringMethod());

            Collection<Collection<Long>> idList = request.getInteractionNetworks();

            CoreMania coreMania = new CoreMania(cache, request.getProgressReporter());
            coreMania.compute(safeGetNamespace(), request.getOrganismId(), labels, combiningMethod, idList, request.getAttributeGroups(), request.getAttributesLimit(), goCategory, "average");
            SymMatrix partiallyCombinedKernel = coreMania.getPartiallyCombinedKernel();
            FeatureWeightMap featureWeights = coreMania.getFeatureWeights();
            Vector discriminant = coreMania.getDiscriminant();
            Vector score = convertScore(scoringMethod, discriminant, partiallyCombinedKernel, labels, posLabelValue, negLabelValue);
            
            double scoreThreshold = selectScoreThreshold(scoringMethod);
            RelatedGenesEngineResponseDto response = prepareResponse(score, discriminant,
                    featureWeights, partiallyCombinedKernel, scoreThreshold, scoringMethod, Constants.convertCombiningMethod(combiningMethod));
            
            requestEndTimeMillis = System.currentTimeMillis();

            logEnd();

            return response;
        }
        catch (CancellationException e) {
            logger.info("request was cancelled");
            return null;
        }
    }

    /*
     * set attributes in response object
     */
    private void encodeAttributes(RelatedGenesEngineResponseDto response, int[] indicesForTopScores, FeatureWeightMap featureWeights) throws ApplicationException {  
        
        if (request.getAttributeGroups() == null || request.getAttributeGroups().size() == 0) {
            setEmptyAttributeResponse(response);
            return;
        }
        
        Map<Long, AttributeDto> allAttributeDtos = makeAllAttributeDtos(response, featureWeights);
        addAttributesForSelectedNodes(response, allAttributeDtos, featureWeights);
    }
    
    /* 
     * pass back empty for attribute-less requests
     * 
     */
    private void setEmptyAttributeResponse(RelatedGenesEngineResponseDto response) {
        Map<Long, Collection<AttributeDto>> nodeToAttributes = new HashMap<Long, Collection<AttributeDto>>();
        response.setNodeToAttributes(nodeToAttributes);
    }
    
    /*
     * update response with map from node-ids to attributes, for just those nodes included
     * in response. 
     * 
     * assumes that the collection of selected nodes has already been constructed and populated
     * in the response object.
     */
    private void addAttributesForSelectedNodes(RelatedGenesEngineResponseDto response,  Map<Long, AttributeDto> allAttributeDtos, FeatureWeightMap features) throws ApplicationException {
        NodeIds nodeIds = cache.getNodeIds(request.getOrganismId());
        AttributeGroups attributeGroups = cache.getAttributeGroups(safeGetNamespace(), request.getOrganismId());        
        Map<Long, Collection<AttributeDto>> nodeToAttributes = new HashMap<Long, Collection<AttributeDto>>();
        
        for (Feature feature: features.keySet()) {
            
            if (feature.getType() != NetworkType.ATTRIBUTE_VECTOR) {
                continue;
            }

            if (request.getProgressReporter().isCanceled()) {
                throw new CancellationException();
            }
            
            long groupId = feature.getGroupId();
            long attributeId = feature.getId();
            AttributeDto attributeDto = allAttributeDtos.get(attributeId);

            AttributeData attributeSet = cache.getAttributeData(safeGetNamespace(), request.getOrganismId(), groupId);
            int attributeIndex = attributeGroups.getIndexForAttributeId(feature.getGroupId(), feature.getId());
            
            Matrix data = attributeSet.getData();
            for (NodeDto node: response.getNodes()) {
                int nodeIndex = nodeIds.getIndexForId(node.getId());
                if (data.get(nodeIndex, attributeIndex) != 0d) {
                    updateNodeAttributes(node.getId(), nodeToAttributes, attributeDto);
                }
            }
        }    

        response.setNodeToAttributes(nodeToAttributes);
    }
    
    /*
     * tiny helper to build up map from node-id's to collections of attribute 
     * dto's.
     */
    private void updateNodeAttributes(long nodeId,
            Map<Long, Collection<AttributeDto>> nodeToAttribute,
            AttributeDto attributeDto) {
  
        Collection<AttributeDto> attributes = nodeToAttribute.get(nodeId);
        if (attributes == null) {
            attributes = new HashSet<AttributeDto>();
            nodeToAttribute.put(nodeId, attributes);
        }
        
        attributes.add(attributeDto);
    }

    /*
     * given a set of feature weights, build map from attribute id
     * to corresponding dto, and update response with attribute collection. 
     * note that its possible that some selected attributes may not be
     * associated with any of the nodes included in the response (since we are 
     * just selecting a subset of the nodes by score), however we return all
     * the attribute scores so the weights make sense (add to unity).
     * 
     * the returned map is attributeId -> attributeDto (which include's weight)
     * 
     * the given response object has its attributes field set to a collection
     * of attributeDto's.
     */
    private Map<Long, AttributeDto> makeAllAttributeDtos(RelatedGenesEngineResponseDto response, FeatureWeightMap featureWeights) throws ApplicationException {
        
        Map<Long, AttributeDto> allMap = new HashMap<Long, AttributeDto>();
        Collection<AttributeDto> all = new ArrayList<AttributeDto>();

        for (Feature feature: featureWeights.keySet()) {
            if (feature.getType() != NetworkType.ATTRIBUTE_VECTOR || featureWeights.get(feature) <= 0d) {
                continue;
            }
            
            if (request.getProgressReporter().isCanceled()) {
                throw new CancellationException();
            }
            
            AttributeDto attributeDto = new AttributeDto();
            attributeDto.setId(feature.getId());
            attributeDto.setGroupId(feature.getGroupId());
            attributeDto.setWeight(featureWeights.get(feature));
            allMap.put(attributeDto.getId(), attributeDto);
            all.add(attributeDto);            
        }
                
        response.setAttributes(all);
        return allMap;
    }
    
    private double selectScoreThreshold(ScoringMethod scoringMethod) {
        
        if (scoringMethod == ScoringMethod.ZSCORE) {
            return Double.NEGATIVE_INFINITY;
        }
        else {
            return Constants.DISCRIMINANT_THRESHOLD;
        }
    }
    
    private Vector convertScore(ScoringMethod scoringMethod, Vector discriminant, SymMatrix combinedKernel, Vector labels, double posLabelValue, double negLabelValue) throws ApplicationException {
        // apply scoring method conversions
        Vector score;
        if (scoringMethod == ScoringMethod.DISCRIMINANT) {
            discriminant.set(MatrixUtils.rescale(discriminant));
            score = discriminant;
        }
        else if (scoringMethod == ScoringMethod.CONTEXT) {
            throw new ApplicationException("context score no longer supported");
//            score = PropagateLabels.computeContextScore(combinedKernel, discriminant);
//            discriminant.set(PropagateLabels.rescale(discriminant));
//            score = PropagateLabels.rescale(score);
        }
        else if (scoringMethod == ScoringMethod.ZSCORE) {
            score = computeZScore(discriminant, combinedKernel, labels, posLabelValue, negLabelValue);
        }
        else {
            throw new ApplicationException("Unexpected scoring method: " + scoringMethod);
        }
        return score;
    }
    
    /*
     * compute z-score from the log of the discriminant's excluding those pinned to -1 (neg label value)
     * 
     * this is a quick implementation, TODO:
     *  - create a separate package for scoring functions
     *  - implement z=score, context-score in that package
     *  - move some methods to matrix-utils package
     *  - implement unit tests
     * 
     */
    private Vector computeZScore(Vector discriminant, SymMatrix combinedKernel, Vector labels, double posLabelValue, double negLabelValue) throws ApplicationException {
        
        logger.debug("computing z-score");
        
        // compute node degree of combined network
        DenseVector degrees = new DenseVector(discriminant.size());
        combinedKernel.columnSums(degrees.getData());
           
        // our stats functions need matrices, copy discriminant scores over, only for
        // nodes with +ve degree, other nodes get NaN which is ignored in our stats computations
        DenseMatrix score = new DenseMatrix(discriminant.size(), 1); 
        int n = 0;
        for (int i=0; i<discriminant.size(); i++) {
            if (degrees.get(i) > 0d) {
                score.set(i, 0, discriminant.get(i));
                n += 1;
            }
            else {
                score.set(i, 0, Double.NaN);
            }
        }
        
        logger.debug("# of nodes with +ve degree in combined network: " + n);
                        
        // clear the scores of the query nodes by setting to NaN
        for (int i=0; i<labels.size(); i++) {
            if (labels.get(i) == posLabelValue) { // probably would be safer to do abs(x-y) < tol ...
                logger.debug("clearing modes with postive label value for " + i);
                score.set(i, 0, Double.NaN);
            }
        }
                
//        dumpNumbers(cache.getCacheDir() + File.separator + "dump.txt", discriminant, labels, degrees.getDegrees());
        
        Vector counts = MatrixUtils.columnCountsIgnoreMissingData(score);
 
        Vector zscores = null;
        
        // if no nodes connected to query nodes, we can't compute a z-score. set all scores to -inf, except query nodes which we set to 1
        if (counts.get(0) == 0) {
            logger.info("no nodes connected to query nodes, special casing z-scores");
            zscores = discriminant.copy(); // so i don't have to worry about its concrete type, will be same as discrminant
            seteq(zscores, Double.NEGATIVE_INFINITY);
            setmatches(posLabelValue, labels, 1d, zscores);            
        }
        else {
            Vector means = MatrixUtils.columnMeanIgnoreMissingData(score, counts);        
            Vector stdevs = MatrixUtils.columnVarianceIgnoreMissingData(score, means);
            MatrixUtils.sqrt(stdevs);
                                
            logger.debug("count, mean, stdev: " + counts.get(0) + ", " + means.get(0) + ", " + stdevs.get(0));     
            
            // note by applying the z-score to the entire discriminant vector,
            // we are also scaling
            // the query nodes, which we didn't use to compute the mean/stdev
            zscores = discriminant.copy();

            MatrixUtils.add(zscores, -means.get(0));
            zscores.scale(1d / (stdevs.get(0) + 0.01));

            // log max for testing
            logger.debug("max of z-scores: " + MatrixUtils.max(zscores));
        }
        
        return zscores;
    }
    
    /*
     * set values less than threshold to new given val. maybe move to matrixutils ...
     */
    private static void setlt(Vector v, final double thresh, final double newval) {
        int n = v.size();
        for (int i=0; i<n; i++) {
            if (v.get(i) < thresh) {
                v.set(i, newval);
            }
        }
    }
    
    private static void setge(Vector v, final double thresh, final double newval) {
        int n = v.size();
        for (int i=0; i<n; i++) {
            if (v.get(i) >= thresh) {
                v.set(i, newval);
            }
        }
    }
    
    private static void seteq(Vector v, final double newval) {
        int n = v.size();
        for (int i=0; i<n; i++) {
            v.set(i, newval);
        }
    }
    
    /* 
     * set newhaystack[i] = newneedle for all i where haystack[i] == needle
     */
    private static void setmatches(final double needle, Vector haystack, final double newneedle, Vector newhaystack) {
        int n = haystack.size();
        for (int i=0; i<n; i++) {
            if (haystack.get(i) == needle) {
                newhaystack.set(i, newneedle);
            }
        }
    }
    
    /*
     * build a response object containing nodes/interactions, network weights,
     * and attributes
     */
    protected RelatedGenesEngineResponseDto prepareResponse(Vector score, Vector discriminant,
            FeatureWeightMap featureWeights, SymMatrix combinedKernel, double scoreThreshold, 
            ScoringMethod scoringMethod, CombiningMethod combiningMethod) throws ApplicationException {

        logPreparingOutputs();
        RelatedGenesEngineResponseDto response = new RelatedGenesEngineResponseDto();

        // we're interested in only the top scoring nodes as specified in the query parameters,
        // but the query nodes (+ve's) must always be included in the response
        NodeIds nodeIds = cache.getNodeIds(request.getOrganismId());
        List<Integer> indicesForPositiveNodes = nodeIds.getIndicesForIds(request.getPositiveNodes());

        // the selection of top nodes does not necessarily have to be by the reported score 
        // (weird huh?). For example, for context score we select top nodes by discriminant, but
        // for each of these selected nodes we return the context score. 
        // but for most scoring method (z-score, discriminant) we select and report using 
        // the same scoring system. special case here.
        int [] indicesForTopScores;
        if (scoringMethod == ScoringMethod.CONTEXT) {
            indicesForTopScores = MatrixUtils.getIndicesForTopScores(discriminant, indicesForPositiveNodes, request.getLimitResults(), scoreThreshold);
        }
        else {
            indicesForTopScores = MatrixUtils.getIndicesForTopScores(score, indicesForPositiveNodes, request.getLimitResults(), scoreThreshold);            
        }
        
        logger.debug(String.format("number of nodes available for return: %d", indicesForTopScores.length));

        if (request.getProgressReporter().isCanceled()) {
            throw new CancellationException();
        }

        // source interactions connecting the selected nodes. we don't apply the computed network weights
        // so just the raw interaction weights from each network
        logger.debug("extracting source interactions");
        getSourceInteractions(response, indicesForTopScores, score, featureWeights);
  
        // while attribute are treated in a manner making them equivalent to networks, users
        // consider them to be separate entities, and we report them in a separate structure.
        logger.debug("extracting attributes");
        encodeAttributes(response, indicesForTopScores, featureWeights);
        
        
        // for e.g. auto-select combining, we chose an specific combining method based
        // on a heuristic. return the actually selected method to the user in
        // case they're interested.
        response.setCombiningMethodApplied(combiningMethod);
  
        return response;
    }
    
    /**
     * return a collection of interaction objects from the network.
     * don't include the symmetric interactions (assume the matrix is
     * symmetric with 0 diagonal and convert only lower triangle)
     *
     * @param network
     * @return
     */
    public Collection<InteractionDto> matrixToInteractions(SymMatrix network, int[] indicesForTopScores, HashMap<Long, NodeDto> nodeVOs) throws ApplicationException {

        ArrayList<InteractionDto> interactions = new ArrayList<InteractionDto>();


        NodeIds nodeIds = cache.getNodeIds(request.getOrganismId());

        for (int i = 0; i < indicesForTopScores.length; i++) {
            for (int j = 0; j < i; j++) {
                int idx = indicesForTopScores[i];
                int jdx = indicesForTopScores[j];
                long from = nodeIds.getIdForIndex(idx);
                long to = nodeIds.getIdForIndex(jdx);
                double weight = network.get(idx, jdx);

                // we'll be getting 0 from the sparse matrix for
                // interactions that are not present, filter out
                if (weight == 0d) {
                    continue;
                }

                NodeDto fromNodeVO = nodeVOs.get(from);
                NodeDto toNodeVO = nodeVOs.get(to);

                if (fromNodeVO == null || toNodeVO == null) {
                    throw new ApplicationException("mapping error");
                }

                InteractionDto interaction = new InteractionDto();
                interaction.setNodeVO1(fromNodeVO);
                interaction.setNodeVO2(toNodeVO);
                interaction.setWeight(weight);
                interactions.add(interaction);
            }
        }

        return interactions;

    }

    /*
     * update response with list of network dto's, and node dto's
     */
    public void getSourceInteractions(RelatedGenesEngineResponseDto response, int[] indicesForTopScores, Vector scores,
            FeatureWeightMap featureWeights) throws ApplicationException {

        List<NetworkDto> sourceNetworks = new ArrayList<NetworkDto>();

        // build up NodeVO's which we'll use in our interaction graph
        HashMap<Long, NodeDto> nodeVOs = new HashMap<Long, NodeDto>();
        for (int i = 0; i < indicesForTopScores.length; i++) {
            NodeDto nodeVO = new NodeDto();

            NodeIds nodeIds = cache.getNodeIds(request.getOrganismId());
            long nodeId = nodeIds.getIdForIndex(indicesForTopScores[i]);

            double score = scores.get(indicesForTopScores[i]);

            nodeVO.setId(nodeId);
            nodeVO.setScore(score);
            nodeVOs.put(nodeId, nodeVO);
        }

        for (Feature feature: featureWeights.keySet()) {
            if (feature.getType() != NetworkType.SPARSE_MATRIX) {
                continue;
            }

            if (request.getProgressReporter().isCanceled()) {
                throw new CancellationException();
            }

            Double weight = featureWeights.get(feature);
            long networkId = feature.getId();
            
            // debug logging for network weights
            if (weight == null) {
                // network wasn't in the query, don't log anything
            }
            else if (weight.doubleValue() == 0) {
                logger.debug(String.format("network %s has zero weight, excluding from results", networkId));
            }

            if (weight == null || weight.doubleValue() == 0) {
                continue;
            }

            NetworkDto sourceNetwork = new NetworkDto();
            sourceNetwork.setWeight(weight);
            sourceNetwork.setId(networkId);

            Network network = cache.getNetwork(safeGetNamespace(), request.getOrganismId(), networkId);
            Collection<InteractionDto> sourceInteractions = matrixToInteractions(network.getData(), indicesForTopScores, nodeVOs);
            sourceNetwork.setInteractions(sourceInteractions);

            //logger.debug(String.format("network %s has a weight of %s and contains %s interactions", networkId, weight, sourceInteractions.size()));
            //SimpleModelConverter.logInteractions(networkId, sourceInteractions);

            sourceNetworks.add(sourceNetwork);
        }

        response.setNetworks(sourceNetworks);
        
        // add in the list of nodes separately in the result, nodes with attributes but not connected to networks
        // won't be present in the graph otherwise.
        ArrayList<NodeDto> nodes = new ArrayList<NodeDto>();
        nodes.addAll(nodeVOs.values());
        response.setNodes(nodes);
    }

    /*
     * write out some query params for later trouble shooting ...
     */
    private void logQuery() {
        logger.info(String.format("findRelated query using combining method %s for organism %d contains %d nodes, %d network groups, %d networks, %d attribute groups, and requests a maximum of %d related nodes using a maximum of %d attributes per group",
                request.getCombiningMethod(), request.getOrganismId(), request.getPositiveNodes().size(), 
                request.getInteractionNetworks().size(), numRequestNetworks, numRequestAttributeGroups, request.getLimitResults(), request.getAttributesLimit()));
    }

    private void logStart() {
        logger.info("processing findRelated() request");
        request.getProgressReporter().setMaximumProgress(Constants.PROGRESS_COMPLETE);
        request.getProgressReporter().setStatus(Constants.PROGRESS_START_MESSAGE);
        request.getProgressReporter().setProgress(Constants.PROGRESS_START);
    }

    private void logPreparingOutputs() {
        logger.info("preparing outputs for findRelated() request");
        request.getProgressReporter().setDescription(Constants.PROGRESS_OUTPUTS_MESSAGE);
        request.getProgressReporter().setProgress(Constants.PROGRESS_OUTPUTS);
    }

    private void logEnd() {
        logger.info("completed processing request, duration = " + Logging.duration(requestStartTimeMillis, requestEndTimeMillis));
        request.getProgressReporter().setStatus(Constants.PROGRESS_COMPLETE_MESSAGE);
        request.getProgressReporter().setProgress(Constants.PROGRESS_COMPLETE);
    }

    private void logNodeScores(int[] indicesForTopScores, Vector discriminant) throws ApplicationException {
        if (logger.isDebugEnabled()) {
            NodeIds nodeIds = cache.getNodeIds(request.getOrganismId());

            for (int i = 0; i < indicesForTopScores.length; i++) {
                long nodeId = nodeIds.getIdForIndex(indicesForTopScores[i]);
                double nodeScore = discriminant.get(indicesForTopScores[i]);
                logger.debug(String.format("Node %d as a score of %f", nodeId, nodeScore));
            }
        }
    }

    /*
     * validate the query params, throw exception if a problem is found,
     * otherwise return silently
     */
    public void checkQuery() throws ApplicationException {

        if (request.getPositiveNodes() == null || request.getPositiveNodes().size() == 0) {
            throw new ApplicationException("No query nodes given");
        }

        if (request.getInteractionNetworks() == null || request.getInteractionNetworks().size() == 0) {
            if (request.getAttributeGroups() == null || request.getAttributeGroups().size() == 0) {
                throw new ApplicationException("No query networks or attributes given");
            }
        }

        checkNodes(request.getOrganismId(), request.getPositiveNodes());

        hasUserNetworks = DataSupport.queryHasUserNetworks(request.getInteractionNetworks());
        hasUserAttributes = DataSupport.queryHasUserAttributes(request.getAttributeGroups());
        
        numRequestNetworks = checkNetworks(safeGetNamespace(), request.getOrganismId(), request.getInteractionNetworks());
        
        // fixup for old requests that get passed through with
        // a  null attribute group set instead of an empty list
        if (request.getAttributeGroups() == null) {
        	request.setAttributeGroups(new ArrayList<Long>());
        }
        numRequestAttributeGroups = checkAttributeGroups(safeGetNamespace(), request.getOrganismId(), request.getAttributeGroups());
    }

    /*
     * query validation helper, error if node id in query is repeated
     * or does not exist in the dataset
     */
    protected void checkNodes(long organismId, Collection<Long> nodes) throws ApplicationException {

        if (nodes.size() == 0) {
            throw new ApplicationException("the list of nodes in the request is empty");
        }
     
        HashSet<Long> uniqueNodeIds = new HashSet<Long>();

        NodeIds nodeIds = cache.getNodeIds(organismId);

        for (Long nodeId: nodes) {
            if (uniqueNodeIds.contains(nodeId)) {
                throw new ApplicationException(String.format("the node id %d was passed multiple times in request", nodeId));
            }

            long n = nodeId.longValue();
            try {
                nodeIds.getIndexForId(n);
            }
            catch (ApplicationException e) {
                throw new ApplicationException(String.format("node id %d is not valid for organism id %d", nodeId, organismId));
            }
        }
    }

    /*
     * query validation helper, error if network id in query is repeated or
     * does not exist in the dataset
     */
    protected int checkNetworks(String namespace, long organismId, Collection<Collection<Long>> networks) throws ApplicationException {

        HashSet<Long> uniqueNetworkIds = new HashSet<Long>();

        NetworkIds networkIds = cache.getNetworkIds(namespace, organismId); // TODO: doesn't this fix up the problem of verifying user networks? add testcase

        for (Collection<Long> grouping: networks) {
            for (Long networkId: grouping) {
                if (uniqueNetworkIds.contains(networkId)) {
                    throw new ApplicationException(String.format("the network id %d was passed multiple times in request", networkId));
                }
                uniqueNetworkIds.add(networkId);

                long n = networkId.longValue();

                if (n > Integer.MAX_VALUE || n < Integer.MIN_VALUE) {
                    throw new ApplicationException(String.format("network ids must be in integer range, got id: %d", networkId));
                }

                if (n < 0) {
                    if (namespace == null) {
                        throw new ApplicationException(String.format("no namespace provided for user network %d organism %d", networkId, organismId));
                    }
                    else {
                        logger.warn("skipping validation check on user network: " + n);
                    }
                }

                try {
                    networkIds.getIndexForId(n);
                }
                catch (ApplicationException e) {
                    throw new ApplicationException(String.format("network id %d is not valid for organism id %d", networkId, organismId));
                }
            }
        }

        return uniqueNetworkIds.size();
    }

    /*
     * validate list of attribute groups in query
     */
    protected int checkAttributeGroups(String namespace, long organismId, Collection<Long> attributeGroups) throws ApplicationException {
        
        if (attributeGroups.size() == 0) {
            return 0;
        }
        
        HashSet<Long> uniqueIds = new HashSet<Long>(attributeGroups);
        if (attributeGroups.size() != uniqueIds.size()) {
            throw new ApplicationException("the list of attribute groups contains duplicates");
        }
        
        AttributeGroups ids = cache.getAttributeGroups(namespace, organismId);
        HashMap<Long, ArrayList<Long>> groupMap = ids.getAttributeGroups();
        
        for (long groupId: attributeGroups) {
            if (!groupMap.containsKey(groupId)) {
                throw new ApplicationException(String.format("organism %d in namspace '%s' does not contain the attribute group %d", organismId, namespace, groupId));
            }
        }
        
        return attributeGroups.size();
    }

//    Getter and setter for private fields 
    public void setRequestStartTimeMillis(long t) {
    	this.requestStartTimeMillis = t;
    }
    
    public long getRequestStartTimeMillis() {
    	return this.requestStartTimeMillis;
    }

    public void setRequestEndTimeMillis(long t) {
    	this.requestEndTimeMillis = t;
    }
    
    public long getRequestEndTimeMillis() {
    	return this.requestEndTimeMillis;
    }
    
    /*
     * external callers pass null for namespace instead of explicitly specifying
     * CORE namespace. instead of testing everywhere in engine, make namespace explicit
     * here.
     *
     */
    private String safeGetNamespace() {
        String namespace = request.getNamespace();
        if (namespace == null || namespace.equals("")) {
            return Data.CORE;
        }
        else if (!hasUserNetworks && !hasUserAttributes) { // no user-specific data? use core
            return Data.CORE;
        }
        else {
            return namespace;
        }
    }
        
    /*
     * helper to write discriminant values to file, for internal testing/data analysis
     */
    private void dumpNumbers(String fileName, Vector discriminant, Vector labels, Vector degrees) {
        try {
            logger.info("dumping to " + fileName);
            File file = new File(fileName);
            FileWriter writer = new FileWriter(file);

            int n = discriminant.size();

            String header = ("node\tdiscriminant\tlabels\tdegrees\n");
            writer.write(header);
            for (int i=0; i<n; i++) {
                String line = String.format("%d\t%.15e\t%.15e\t%.15e\n", i, discriminant.get(i), labels.get(i), degrees.get(i));
                writer.write(line);
            }

            writer.close();
        }
        catch (Exception e) {
            logger.warn("failed to dump data", e);

        }          
    }

    public static void logInteractions(long networkId, Collection<InteractionDto> interactions) {
        logger.debug("interactions for network " + networkId);
        for (InteractionDto i: interactions) {
            logger.debug(String.format("   %d %d %f", i.getNodeVO1().getId(), i.getNodeVO2().getId(), i.getWeight()));
        }
    }    
}
