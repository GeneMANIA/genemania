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
package org.genemania.engine.core.integration;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.genemania.engine.Constants.NetworkType;
import org.genemania.engine.cache.NetworkMemCache;
import org.genemania.engine.cache.RandomDataCacheBuilder;
import org.genemania.engine.cache.RandomDataCacheConfig;
import org.genemania.engine.config.Config;
import org.genemania.engine.core.data.AttributeGroups;
import org.genemania.engine.core.data.Data;
import org.genemania.engine.matricks.SymMatrix;
import org.genemania.exception.ApplicationException;
import org.genemania.util.NullProgressReporter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class CombineNetworksOnlyTest {
   
    RandomDataCacheBuilder cacheBuilder;
    RandomDataCacheConfig config = RandomDataCacheConfig.getStandardConfig2();
    
    public CombineNetworksOnlyTest(String configFile) throws Exception {
        Config.reload(configFile);
    }

    @Parameters
    public static Collection<Object[]> configure() {
        Object[][] data = { { "symconfig.properties" },
                { "floatsymconfig.properties" } };
        return Arrays.asList(data);
    }

    @Before
    public void setUp() throws Exception {
        cacheBuilder = new RandomDataCacheBuilder(config.getSeed());
        cacheBuilder.setUp();

        cacheBuilder.addOrganism(config);

        // clear mem cache since its a singleton process wide, don't want stuff
        // left over from other tests
        NetworkMemCache.instance().clear();
    }

    @After
    public void tearDown() throws Exception {
        cacheBuilder.tearDown();
    }

    @Test
    public void testCombineFeatures() throws ApplicationException {
        SymMatrix result = CombineNetworksOnly.combine(getFeatureWeightMap(getFeatures()), Data.CORE, 
                config.getOrg1Id(), 
                cacheBuilder.getCache(), NullProgressReporter.instance());
        assertNotNull(result);
    }

    /*
     * set all weights to 1
     */
    FeatureWeightMap getFeatureWeightMap(FeatureList features) {
        
        FeatureWeightMap weights = new FeatureWeightMap();
        for (Feature feature: features) {
            weights.put(feature, 1d);
        }
        
        return weights;
    }
    /*
     * feature vector with all network ids and feature ids
     * 
     * this boilerplate shared with AutomaticGramBuilderTest, find a way
     * to play nice and share
     */
    FeatureList getFeatures() throws ApplicationException {
        FeatureList features = new FeatureList();

        for (int i=0; i<config.getOrg1NetworkIds().length; i++) {
            Feature feature = new Feature(NetworkType.SPARSE_MATRIX, 0, config.getOrg1NetworkIds()[i]);
            features.add(feature);
        }

        AttributeGroups attributeGroups = cacheBuilder.getCache().getAttributeGroups(Data.CORE, config.getOrg1Id());

        for (long groupId: attributeGroups.getAttributeGroups().keySet()) {

            ArrayList<Long> attributeIds = attributeGroups.getAttributeGroups().get(groupId);
            for (long attributeId: attributeIds) {            
                Feature feature = new Feature(NetworkType.ATTRIBUTE_VECTOR, groupId, attributeId);
                features.add(feature);
            }
        }

        Collections.sort(features);
        return features;
    }
}
