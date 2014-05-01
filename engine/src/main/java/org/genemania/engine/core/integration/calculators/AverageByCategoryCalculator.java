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
import no.uib.cipr.matrix.Vector;
import org.genemania.engine.Constants;
import org.genemania.engine.cache.DataCache;
import org.genemania.engine.core.integration.CombineNetworksOnly;
import org.genemania.engine.core.integration.Feature;
import org.genemania.engine.core.integration.FeatureList;
import org.genemania.engine.core.integration.FeatureWeightMap;
import org.genemania.engine.core.integration.attribute.QueryEnrichedAttributeScorer;
import org.genemania.exception.ApplicationException;
import org.genemania.util.ProgressReporter;

/**
 * compute average by category, first each category gets an equal weight, then
 * all the networks within the category split that weight up equally
 */
public class AverageByCategoryCalculator extends
        AbstractNetworkWeightCalculator {

    private static int MIN_QUERY_GENES_PER_ATTRIBUTE = 1;
    
    public AverageByCategoryCalculator(String namespace, DataCache cache,
            Collection<Collection<Long>> networkIds,
            Collection<Long> attributeGroupIds, long organismId, Vector label,
            int attributesLimit, ProgressReporter progress)
            throws ApplicationException {
        super(namespace, cache, networkIds, attributeGroupIds, organismId,
                label, attributesLimit, progress);
    }

    /*
     * TODO: the code in AverageCategory can be moved here, and that class
     * removed
     */
    public void process() throws ApplicationException {
        progress.setStatus(Constants.PROGRESS_WEIGHTING_MESSAGE);
        progress.setProgress(Constants.PROGRESS_WEIGHTING);

        QueryEnrichedAttributeScorer attributeScorer = new QueryEnrichedAttributeScorer(cache, label, MIN_QUERY_GENES_PER_ATTRIBUTE);        
        FeatureList featureList = buildFeatureList(attributeScorer, true);        

        weights = averageCategory(featureList);

        progress.setStatus(Constants.PROGRESS_COMBINING_MESSAGE);
        progress.setProgress(Constants.PROGRESS_COMBINING);
        combinedMatrix = CombineNetworksOnly.combine(weights, namespace,
                organismId, cache, progress);
    }

    protected static FeatureWeightMap averageCategory(FeatureList features)
            throws ApplicationException {
        FeatureWeightMap weightMap = new FeatureWeightMap();
        Collection<FeatureList> groups = features.group();

        int numGroups = groups.size();
        for (FeatureList featureList : groups) {
            int numFeatures = featureList.size();
            double weight = 1d / ((double) numGroups * (double) numFeatures);
            for (Feature feature : featureList) {
                weightMap.put(feature, weight);
            }
        }

        return weightMap;
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
        return String.format(PARAM_KEY_FORMAT,
                Constants.CombiningMethod.AVERAGE_CATEGORY, networks);
    }
}
