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

import org.apache.log4j.Logger;
import org.genemania.engine.Constants;
import org.genemania.engine.cache.DataCache;
import org.genemania.engine.core.integration.CombineNetworksOnly;
import org.genemania.engine.core.integration.FeatureList;
import org.genemania.engine.core.integration.attribute.QueryEnrichedAttributeScorer;
import org.genemania.engine.core.integration.gram.AutomaticGramBuilder;
import org.genemania.engine.exception.WeightingFailedException;
import org.genemania.exception.ApplicationException;
import org.genemania.util.ProgressReporter;

public class AutomaticCalculator extends
        AbstractNetworkWeightCalculator {
    private static Logger logger = Logger.getLogger(AutomaticCalculator.class);

    private static int MIN_QUERY_GENES_PER_ATTRIBUTE = 2;
    
    public AutomaticCalculator(String namespace, DataCache cache, Collection<Collection<Long>> networkIds, 
            Collection<Long> attributeGroupIds, long organismId, Vector label, int attributesLimit, ProgressReporter progress) throws ApplicationException {
        super(namespace, cache, networkIds, attributeGroupIds, organismId, label, attributesLimit, progress);  
    }
    
    @Override
    public void process() throws ApplicationException {
    	
        progress.setStatus(Constants.PROGRESS_WEIGHTING_MESSAGE);
        progress.setProgress(Constants.PROGRESS_WEIGHTING);
        
        QueryEnrichedAttributeScorer attributeScorer = new QueryEnrichedAttributeScorer(cache, label, MIN_QUERY_GENES_PER_ATTRIBUTE);        
        FeatureList featureList = buildFeatureList(attributeScorer, false);        
        
        try {
            AutomaticGramBuilder builder = new AutomaticGramBuilder(cache, namespace, organismId, featureList, label, progress);
            weights = builder.build(progress);
        }
        catch (WeightingFailedException e) {
            // TODO: add attributes to average calculator
            logger.error("weighting calculation failed, falling back to average: " + e.getMessage());
            weights = AverageByNetworkCalculator.average(featureList);
        }
  
        progress.setStatus(Constants.PROGRESS_COMBINING_MESSAGE);
        progress.setProgress(Constants.PROGRESS_COMBINING);
        combinedMatrix = CombineNetworksOnly.combine(weights, namespace, organismId, cache, progress);
    }

}
