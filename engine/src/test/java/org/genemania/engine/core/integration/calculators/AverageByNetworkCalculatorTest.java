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

import org.genemania.engine.config.Config;
import org.genemania.engine.core.data.Data;
import org.genemania.engine.core.integration.calculators.AverageByNetworkCalculator;
import java.lang.Integer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.genemania.engine.cache.NetworkMemCache;
import org.genemania.engine.cache.RandomDataCacheBuilder;
import org.genemania.engine.cache.RandomDataCacheConfig;
import org.genemania.engine.core.integration.INetworkWeightCalculator;
import org.genemania.util.NullProgressReporter;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 */
public class AverageByNetworkCalculatorTest {

    static RandomDataCacheBuilder cacheBuilder = new RandomDataCacheBuilder(7132);
    static RandomDataCacheConfig config = RandomDataCacheConfig.getStandardConfig2();

    @BeforeClass
    public static void setUpClass() throws Exception {
        cacheBuilder = new RandomDataCacheBuilder(config.getSeed());
        cacheBuilder.setUp();

        cacheBuilder.addOrganism(config);

        // clear mem cache since its a singleton process wide, don't want stuff left over from other tests
        NetworkMemCache.instance().clear();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        cacheBuilder.tearDown();

    }

    public AverageByNetworkCalculatorTest() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of process method, of class AverageByNetworkCalculator.
     */
    @Test
    public void testProcess() throws Exception {
        System.out.println("process");

        Collection<Collection<Long>> groups = new ArrayList<Collection<Long>>();
        Collection<Long> group = new ArrayList<Long>();
        for (int i=0; i<config.getOrg1NetworkIds().length; i++) {
            group.add(config.getOrg1NetworkIds()[i]);
        }
        groups.add(group);
        
        Collection<Long> attributeGroups = new ArrayList<Long>();
        
        INetworkWeightCalculator calculator = new AverageByNetworkCalculator(Data.CORE, cacheBuilder.getCache(), 
        		groups, attributeGroups, config.getOrg1Id(), null, Config.instance().getAttributeEnrichmentMaxSize(), NullProgressReporter.instance());
        calculator.process();

        Map<Long, Double> weights = AbstractNetworkWeightCalculator.convertToWeightMap(calculator.getWeights()); // TODO: remove conversion compat kludge 
        assertNotNull(weights);
        assertEquals(config.getOrg1NetworkIds().length, weights.size());
        for (Double weight: weights.values()) {
            assertEquals(1d/config.getOrg1NetworkIds().length, weight, .000001d);
        }
    }

    /**
     * Test of average method, of class AverageByNetworkCalculator.
     */
    @Test
    public void testAverage() {
        System.out.println("average");
        Map<Integer, Long> IndexToNetworkMap = new HashMap<Integer, Long>();

        for (int i=0; i<config.getOrg1NetworkIds().length; i++) {
            IndexToNetworkMap.put(i, config.getOrg1NetworkIds()[i]);
        }

        Map<Long, Double> result = AverageByNetworkCalculator.averageNetworksOnly(IndexToNetworkMap);
        assertNotNull(result);
        assertEquals(config.getOrg1NetworkIds().length, result.size());
        for (Double weight: result.values()) {
            assertEquals(1d/config.getOrg1NetworkIds().length, weight, .000001d);
        }
    }

}