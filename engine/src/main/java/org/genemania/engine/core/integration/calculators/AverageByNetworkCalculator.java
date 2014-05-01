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
import java.util.HashMap;
import java.util.Map;
import no.uib.cipr.matrix.Vector;
import org.genemania.engine.Constants;
import org.genemania.engine.Constants.NetworkType;
import org.genemania.engine.cache.DataCache;
import org.genemania.engine.core.integration.CombineNetworksOnly;
import org.genemania.engine.core.integration.Feature;
import org.genemania.engine.core.integration.FeatureList;
import org.genemania.engine.core.integration.FeatureWeightMap;
import org.genemania.engine.core.integration.QueryEnrichedAttributeSelector;
import org.genemania.engine.core.integration.attribute.QueryEnrichedAttributeScorer;
import org.genemania.exception.ApplicationException;
import org.genemania.util.ProgressReporter;

/**
 * compute the plain old average network weighting
 */
public class AverageByNetworkCalculator extends AbstractNetworkWeightCalculator {

    private static int MIN_QUERY_GENES_PER_ATTRIBUTE = 1;
    
    public AverageByNetworkCalculator(String namespace, DataCache cache, Collection<Collection<Long>> networkIds, 
    		Collection<Long> attributeGroupIds, long organismId, Vector label, int attributesLimit, ProgressReporter progress) 
    				throws ApplicationException {
        super(namespace, cache, networkIds, attributeGroupIds, organismId, label, attributesLimit, progress);
    }

    public void process() throws ApplicationException {
        int i = 0;
        for (Collection<Long> group: networkIds) {
            for (long id: group) {
                IndexToNetworkIdMap.put(i, id);
                i++;
            }
        }
        progress.setStatus(Constants.PROGRESS_WEIGHTING_MESSAGE);
        progress.setProgress(Constants.PROGRESS_WEIGHTING);
        
        QueryEnrichedAttributeScorer attributeScorer = new QueryEnrichedAttributeScorer(cache, label, MIN_QUERY_GENES_PER_ATTRIBUTE);        
        FeatureList featureList = buildFeatureList(attributeScorer, true);        

        weights = average(featureList);

        progress.setStatus(Constants.PROGRESS_COMBINING_MESSAGE);
        progress.setProgress(Constants.PROGRESS_COMBINING);
        combinedMatrix = CombineNetworksOnly.combine(weights, namespace, organismId, cache, progress);
    }

    /*
     * average weight for all features (networks, attributes). If the
     * given feature list includes a bias, as when called as fallback
     * from another weighting method, exclude the bias.
     */
    protected static FeatureWeightMap average(FeatureList features) {
    	FeatureWeightMap weightMap = new FeatureWeightMap();
    	
    	int n = features.size();
    	if (features.hasBias()) {
    	    n = n - 1;
    	}
    	
    	double weight = 1.0d / n;
    	
    	for (Feature feature: features) {
    	    if (feature.getType() != NetworkType.BIAS) {
    	        weightMap.put(feature, weight);
    	    }
    	}
    	
    	return weightMap;
    }
    
    // TODO: remove when finished refactoring/cleanup
    protected static Map<Long, Double> averageNetworksOnly(Map<Integer, Long> IndexToNetworkMap) {

        Double weight = 1.0d / IndexToNetworkMap.size();

        Map<Long, Double> matrixToWeightMap = new HashMap<Long, Double>();
        for (Integer i: IndexToNetworkMap.keySet()) {
            matrixToWeightMap.put(IndexToNetworkMap.get(i), weight);
        }

        return matrixToWeightMap;
    }

    public static final String PARAM_KEY_FORMAT = "%s-%s"; // method-networks

    /*
     * the method and sorted list of network ids uniquely defines the weight
     * calculation. if attributes are present, don't cache since we pre-select
     * attributes by query genes.
     */
    @Override
    public String getParameterKey() throws ApplicationException {
        if (this.attributeGroupIds != null && this.attributeGroupIds.size()>0) {
            throw new ApplicationException("not cacheable");
        }
        String networks = formattedNetworkList(this.networkIds);
        return String.format(PARAM_KEY_FORMAT, Constants.CombiningMethod.AVERAGE, networks);
    }    
}
