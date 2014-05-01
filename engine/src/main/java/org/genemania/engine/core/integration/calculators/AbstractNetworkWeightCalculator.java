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

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.Vector;
import org.apache.log4j.Logger;
import org.genemania.engine.Constants;
import org.genemania.engine.Constants.DataFileNames;
import org.genemania.engine.Constants.NetworkType;
import org.genemania.engine.cache.DataCache;
import org.genemania.engine.core.data.DataSupport;
import org.genemania.engine.core.data.DatasetInfo;
import org.genemania.engine.core.data.Data;
import org.genemania.engine.core.data.KtK;
import org.genemania.engine.core.data.KtT;
import org.genemania.engine.core.integration.Feature;
import org.genemania.engine.core.integration.FeatureList;
import org.genemania.engine.core.integration.INetworkWeightCalculator;
import org.genemania.engine.core.integration.FeatureWeightMap;
import org.genemania.engine.core.integration.attribute.IAttributeScorer;
import org.genemania.engine.core.integration.attribute.IAttributeSelector;
import org.genemania.engine.core.integration.attribute.TopXOverallSelector;
import org.genemania.engine.core.utils.ObjectSelector;
import org.genemania.engine.matricks.SymMatrix;
import org.genemania.exception.ApplicationException;
import org.genemania.util.ProgressReporter;

/**
 * common implementation code for network weight calculators
 *
 * TODO: keep a CombinedObject ref here instead of the separated weight map and matrix?
 * how to ensure we can write to it for computing label prop without having to copy (eg
 * version from cache we shouldn't change ...
 */
public abstract class AbstractNetworkWeightCalculator implements INetworkWeightCalculator {

    private static Logger logger = Logger.getLogger(AbstractNetworkWeightCalculator.class);
    FeatureWeightMap weights = new FeatureWeightMap();
    Map<Integer, Long> IndexToNetworkIdMap = new HashMap<Integer, Long>();
    SymMatrix combinedMatrix = null;
    String namespace;
    DataCache cache;
    Collection<Collection<Long>> networkIds;
    Collection<Long> attributeGroupIds;
    long organismId;
    Vector label;
    int attributesLimit;
    ProgressReporter progress;

    public AbstractNetworkWeightCalculator(String namespace, 
            DataCache cache, Collection<Collection<Long>> networkIds, Collection<Long> attributeGroupIds,
            long organismId, Vector label, int attributesLimit, ProgressReporter progress) throws ApplicationException {
        this.namespace = namespace;
        this.cache = cache;
        this.networkIds = networkIds;
        this.attributeGroupIds = attributeGroupIds;
        this.organismId = organismId;
        this.label = label;
        this.attributesLimit = attributesLimit;
        this.progress = progress;
    }

    public FeatureWeightMap getWeights() {
        return weights;
    }

    public SymMatrix getCombinedMatrix() {
        return combinedMatrix;
    }

    /*
     * to support caching of combined networks, should be
     * overriden to support caching.
     *
     * return a key that captures the parameters necessary
     * to reproduce a computation. e.g. for some combining methods
     * this would be network ids and go branch, for others it would
     * be network ids ant +ve gene ids.
     *
     * equivalent queries should give the same key, even if in the query
     * the order of network ids is shuffled etc.
     */
    public String getParameterKey() throws ApplicationException {
        throw new ApplicationException("not cacheable");
    }

    /*
     * indirection for retrieving user or core KtK. TODO: move this directly into the cache layer?
     *
     * note we take a copy of KtK since we need to scale it depending on the go branch. want to leave cached version untouched.
     * wait, surely we can optimize this out? TODO 
     * 
     * basic ktk, no scaling applied
     */
    DenseMatrix getKtK(boolean hasUserNetworks) throws ApplicationException {
        DenseMatrix KtK;
        if (!hasUserNetworks) {
            KtK KtKObject = cache.getKtK(Data.CORE, organismId, DataFileNames.KtK_BASIC.getCode());
            KtK = KtKObject.getData().copy();
        }
        else {
            KtK KtKObject = cache.getKtK(namespace, organismId, DataFileNames.KtK_BASIC.getCode());
            KtK = KtKObject.getData().copy();
        }
        return KtK;
    }
    
    // apply branch specific scaling
    void scaleKtK(DenseMatrix KtK, String goBranch) throws ApplicationException {
        DatasetInfo info = cache.getDatasetInfo(organismId);

        int indexForBranch = Constants.getIndexForGoBranch(goBranch);
        int numCategories = info.getNumCategories()[indexForBranch];
        KtK.scale(numCategories);
    }
    
    /*
     * indirection for retrieving user or core KtT
     */
    DenseMatrix getKtT(String goBranch, boolean hasUserNetworks) throws ApplicationException {
        DenseMatrix KtT;
        if (!hasUserNetworks) {
            KtT KtTObject = cache.getKtT(Data.CORE, organismId, goBranch);
            KtT = KtTObject.getData();
        }
        else {
            KtT KtTObject = cache.getKtT(namespace, organismId, goBranch);
            KtT = KtTObject.getData();
        }

        return KtT;
    }

    boolean queryHasUserNetworks() {
        return DataSupport.queryHasUserNetworks(networkIds);
    }
    
    /*
     * compute md5sum of a string. this actually return error strings if the encoding fails,
     * but the encoding and digest alg we are using is standard in the jdk so this
     * shouldn't happen.
     */
    public static String hashString(String target) {

        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.reset();
            digest.update(target.getBytes("iso-8859-1"), 0, target.length());

            byte[] md5sum = digest.digest();
            BigInteger bigInt = new BigInteger(1, md5sum);
            String hex = bigInt.toString(16);
            return hex;
        }
        catch (UnsupportedEncodingException e) {
            return "ENCODING_FAILURE";
        }
        catch (NoSuchAlgorithmException e) {
            return "DIGEST_FAILURE";
        }
    }

    /*
     * apply a lexical ordering to the list of group ids, returning
     * a string of the form:
     *   "(1,3),(2),(4,5,6)"
     *
     */
    public static String formattedNetworkList(Collection<Collection<Long>> inputNetworks) {

        if (inputNetworks == null || inputNetworks.size() == 0) {
            return "NO_NETWORKS"; // this must be an error no?
        }

        int numGroups = inputNetworks.size();
        String [] groupLists = new String[numGroups];

        int g = 0;
        for (Collection<Long> group: inputNetworks) {

            ArrayList<Long> sortedGroup = new ArrayList<Long>();
            sortedGroup.addAll(group);
            Collections.sort(sortedGroup);

            StringBuilder builder = new StringBuilder("(");
            for (Long id: sortedGroup) {
                builder.append("" + id + ",");
            }
            builder.setCharAt(builder.length()-1, ')');

            groupLists[g] = builder.toString();
            g++;

        }

        Arrays.sort(groupLists);

        StringBuilder builder = new StringBuilder(groupLists[0]);
        for (int i=1; i<numGroups; i++) {
            builder.append("," + groupLists[i]);
        }

        return builder.toString();
    }
    
    /*
     * TODO: should we include the max # attributes/group used to build the network,
     * as part of its key? if so (and we probably should), then we need to consider
     * that the network may be the same for various different (e.g. large) values of
     * the max, and we might also want to parameterize by the attribute selection method.
     * hmmm ... simplest thing first
     */
    public static String formattedAttributeGroupList(Collection<Long> attributeGroupIds) {
        ArrayList<Long> list = new ArrayList<Long>();
        list.addAll(attributeGroupIds);
        Collections.sort(list);
        
        StringBuilder builder = new StringBuilder("(");
        for (Long id: list) {
            builder.append("" + id + ",");
        }
        builder.setCharAt(builder.length()-1, ')');
        
        return builder.toString();
    }

    /*
     * construct a featureList from network & attribute group settings. 
     * for attributes we only select a subset of those available to
     * keep computational costs reasonable, as specified by attributesLimit.
     * 
     * note that attribute selection may make combining methods dependent 
     * on the gene list (where they weren't otherwise).
     */
    public FeatureList buildFeatureList(IAttributeScorer scorer, boolean enumerateGroups) throws ApplicationException {
        FeatureList features = buildFeatureListForSelectedNetworks(enumerateGroups);

        // all attributes for specified attribute groups
        features.addAll(buildFeatureListForAttributes(scorer));

        return features;
    }
    
    
    public FeatureList buildFeatureListForAttributes(IAttributeScorer scorer) throws ApplicationException {
        
        IAttributeSelector selector = getAttributeSelector();
        ObjectSelector<Feature> bigList = new ObjectSelector<Feature>();
        
        // all attributes for specified attribute groups
        for (long attributeGroupId: attributeGroupIds) {
            ObjectSelector<Feature> list = scorer.scoreAttributes(namespace, organismId, attributeGroupId);
            list = selector.selectGroupAttributes(list);
            bigList.add(list);
        }
        
        bigList = selector.selectOverallAttributes(bigList);
        
        FeatureList features = new FeatureList();
        features.addAll(bigList.getElements());
        return features;               
    }

    /*
     * TODO: make choice of selector class configurable
     */
    private org.genemania.engine.core.integration.attribute.IAttributeSelector getAttributeSelector() {
      //  return new TopXByGroupSelector(attributesLimit, Config.instance().getAttributeEnrichmentMaxSize());
        
        int leveledLimit = Math.round(1.5f*attributesLimit); // 50% more, TODO: another thing to make configurable
        return new TopXOverallSelector(attributesLimit, leveledLimit);
    }

    public FeatureList buildFeatureListForSelectedNetworks(boolean enumerateGroups) throws ApplicationException {
        FeatureList features = new FeatureList();
        
        // specified networks. we don't get passed real network group ids, 
        // synthesizing via counter is good enough for us here
        int fakeGroupId = 1;
        for (Collection<Long> group: networkIds) {
            for (long networkId: group) {
                if (enumerateGroups) {
                    features.add(new Feature(NetworkType.SPARSE_MATRIX, fakeGroupId, networkId));
                }
                else {
                    features.add(new Feature(NetworkType.SPARSE_MATRIX, Feature.FAKE_SPARSE_NETWORK_GROUP_ID, networkId));
                }
            }
            fakeGroupId += 1;
        }
        return features;
    }
    
    /*
     * temp compatibility, convert an old weight map describing networks only (not attributes) 
     * into a featuremap
     */
    public static FeatureWeightMap convertToFeatureWeightMap(Map<Long, Double> networkWeights) {
        FeatureWeightMap weightMap = new FeatureWeightMap();
        
        for (Entry<Long, Double> entry: networkWeights.entrySet()) {
            weightMap.put(new Feature(NetworkType.SPARSE_MATRIX, 0, entry.getKey()), entry.getValue());
        }
        
        return weightMap;        
    }
    
    public static Map<Long, Double> convertToWeightMap(FeatureWeightMap featureWeights) throws ApplicationException {
        Map<Long, Double> map = new HashMap<Long, Double>();
        
        for (Feature feature: featureWeights.keySet()) {
            if (feature.getType() != NetworkType.SPARSE_MATRIX) {
                throw new ApplicationException("can't convert weight map"); // shouldn't ever get here
            }
            map.put(feature.getId(), featureWeights.get(feature));
        }
        
        return map;
        
    }
}
