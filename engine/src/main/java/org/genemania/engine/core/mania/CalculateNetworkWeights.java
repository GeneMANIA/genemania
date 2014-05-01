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

/**
 * Class for weight selection method
 * 
 * Produces a <NetworkId, weight> map and a combined Matrix based on 
 * the weights produced.
 * 
 * Method of calculation depends on Constants.CombiningMethod
 * 
 * 
 */
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import no.uib.cipr.matrix.Vector;

import org.apache.log4j.Logger;
import org.genemania.engine.Constants.CombiningMethod;
import org.genemania.engine.cache.DataCache;
import org.genemania.engine.core.data.CombinedNetwork;
import org.genemania.engine.core.integration.FeatureWeightMap;
import org.genemania.engine.core.integration.INetworkWeightCalculator;
import org.genemania.engine.core.integration.NetworkWeightCalculatorFactory;
import org.genemania.engine.core.integration.calculators.AbstractNetworkWeightCalculator;
import org.genemania.engine.matricks.SymMatrix;
import org.genemania.exception.ApplicationException;
import org.genemania.util.ProgressReporter;

/*
 * TODO: may be able to get rid of this entire class, using the code currently
 * in process() directly in CoreMania.
 */
public class CalculateNetworkWeights {

    private static Logger logger = Logger.getLogger(CalculateNetworkWeights.class);
    private FeatureWeightMap weights = new FeatureWeightMap();
    private Map<Integer, Long> IndexToNetworkIdMap = new HashMap<Integer, Long>();
    private SymMatrix combinedMatrix = null;

    private String namespace;
    private DataCache cache;
    private Collection<Collection<Long>> networkIds;
    private Collection<Long> attributeGroupIds;
    private long organismId;
    private Vector label;
    private int attributesLimit;
    private CombiningMethod method;
    private ProgressReporter progress;

    public CalculateNetworkWeights(String namespace, DataCache cache, Collection<Collection<Long>> networkIds, Collection<Long> attributeGroupIds,
            long organismId, Vector label, int attributesLimit, CombiningMethod method, ProgressReporter progress) throws ApplicationException {
        this.namespace = namespace;
        this.cache = cache;
        this.networkIds = networkIds;
        this.attributeGroupIds = attributeGroupIds;
        this.organismId = organismId;
        this.label = label;
        this.method = method;
        this.attributesLimit = attributesLimit;
        this.progress = progress;        
    }

    public void process() throws ApplicationException {
        INetworkWeightCalculator calculator = NetworkWeightCalculatorFactory.getCalculator(namespace, cache, networkIds, attributeGroupIds, organismId, label, attributesLimit, method, progress);

        try {
            getFromCache(organismId, calculator);
            return;
        }
        catch (ApplicationException e) {
            // either wasn't cacheable, or wasn't in the cache
            recompute(calculator);
        }
    }

    private void getFromCache(long organismId, INetworkWeightCalculator calculator) throws ApplicationException {
        String key = calculator.getParameterKey();
        String hash = AbstractNetworkWeightCalculator.hashString(key);

        CombinedNetwork combined = cache.getCombinedNetwork(namespace, organismId, hash);
        logger.debug(String.format("found pre-combined network in cache for namespace %s organism %s with hash %s for key %s", namespace, organismId, hash, key));
        weights = combined.getFeatureWeightMap();
        combinedMatrix = combined.getData();
    }
    
    /*
     * compute the weights directly
     */
    private void recompute(INetworkWeightCalculator calculator) throws ApplicationException {
        calculator.process();
        weights = calculator.getWeights();
        combinedMatrix = calculator.getCombinedMatrix();
    }

    public FeatureWeightMap getWeights() {
        return weights;
    }

    public SymMatrix getCombinedMatrix() {
        return combinedMatrix;
    }

    public Map<Integer, Long> getIndexToNetworkIdMap() {
        return IndexToNetworkIdMap;
    }
}
