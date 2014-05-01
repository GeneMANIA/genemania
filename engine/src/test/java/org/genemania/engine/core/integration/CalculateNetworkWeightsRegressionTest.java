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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;


import no.uib.cipr.matrix.DenseVector;
import org.genemania.engine.Constants.CombiningMethod;
import org.genemania.engine.cache.NetworkMemCache;
import org.genemania.engine.cache.RandomDataCacheBuilder;
import org.genemania.engine.cache.RandomDataCacheConfig;
import org.genemania.engine.config.Config;
import org.genemania.engine.core.data.Data;
import org.genemania.engine.core.data.NetworkIds;
import org.genemania.engine.matricks.SymMatrix;
import org.genemania.engine.core.integration.calculators.AbstractNetworkWeightCalculator;
import org.genemania.engine.core.mania.CalculateNetworkWeights;
import org.genemania.exception.ApplicationException;
import org.genemania.util.NullProgressReporter;
import static junit.framework.Assert.*;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class CalculateNetworkWeightsRegressionTest {

    RandomDataCacheBuilder cacheBuilder;
    RandomDataCacheConfig config = RandomDataCacheConfig.getStandardConfig2();

    public CalculateNetworkWeightsRegressionTest(String configFile) throws Exception {
        Config.reload(configFile);
    }

    @Parameters
    public static Collection<Object[]> configure() {
        Object[][] data = {{"mtjconfig.properties"}, {"symconfig.properties"}, {"floatsymconfig.properties"}};
        return Arrays.asList(data);
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        Config.reload();
    }

    @Before
    public void setUp() throws Exception {

        cacheBuilder = new RandomDataCacheBuilder(config.getSeed());
        cacheBuilder.setUp();

        cacheBuilder.addOrganism(config);

        // clear mem cache since its a singleton process wide, don't want stuff left over from other tests
        NetworkMemCache.instance().clear();
    }

    @After
    public void tearDown() {
        cacheBuilder.tearDown();
    }


    /*
     * regression test on average combining
     */
    @Test
    public void testRegressionAverageCombining() throws Exception {

        CalculateNetworkWeights weightCalc = regressionHelper(CombiningMethod.AVERAGE, .5);
        Map<Long, Double> weights = AbstractNetworkWeightCalculator.convertToWeightMap(weightCalc.getWeights()); // TODO: remove compat hack
        System.out.println("weights:" + weights);

        assertNotNull(weights);
        assertEquals(10, weights.size());

        for (Double weight: weights.values()) {
            assertEquals(1.0 / config.getOrg1numNetworks(), weight, .000001d);
        }

        SymMatrix combinedNetwork = weightCalc.getCombinedMatrix();
        assertNotNull(combinedNetwork);

        double sum = combinedNetwork.elementSum();
        System.out.println("combined elem sum: " + sum);
        assertEquals(49.5620665003467d, sum, .000001d);

    }

    @Test
    public void testRegressionAverageCombiningNoNamespace() throws Exception {

        CalculateNetworkWeights weightCalc = regressionHelper(CombiningMethod.AVERAGE, .5, Data.CORE);
        Map<Long, Double> weights = AbstractNetworkWeightCalculator.convertToWeightMap(weightCalc.getWeights()); // TODO: remove compat hack
        System.out.println("weights:" + weights);

        assertNotNull(weights);
        assertEquals(10, weights.size());

        for (Double weight: weights.values()) {
            assertEquals(1.0 / config.getOrg1numNetworks(), weight, .000001d);
        }

        SymMatrix combinedNetwork = weightCalc.getCombinedMatrix();
        assertNotNull(combinedNetwork);

        double sum = combinedNetwork.elementSum();
        System.out.println("combined elem sum: " + sum);
        assertEquals(49.5620665003467d, sum, .000001d);

    }

    /*
     * regresion test average category combining
     */
    @Test
    public void testRegressionAverageCategoryCombining() throws Exception {

        CalculateNetworkWeights weightCalc = regressionHelper(CombiningMethod.AVERAGE_CATEGORY, .5);
        Map<Long, Double> weights = AbstractNetworkWeightCalculator.convertToWeightMap(weightCalc.getWeights()); // TODO: remove compat hack;
        System.out.println("weights:" + weights);

        assertNotNull(weights);
        assertEquals(10, weights.size());

        SymMatrix combinedNetwork = weightCalc.getCombinedMatrix();
        assertNotNull(combinedNetwork);

        double sum = combinedNetwork.elementSum();
        System.out.println("combined elem sum: " + sum);
        assertEquals(49.573414660816375d, sum, .000001d);
    }

    /*
     * regresion test automatic combining
     */
    @Test
    public void testRegressionAutomaticCombining() throws Exception {

        CalculateNetworkWeights weightCalc = regressionHelper(CombiningMethod.AUTOMATIC, .5);
        Map<Long, Double> weights = AbstractNetworkWeightCalculator.convertToWeightMap(weightCalc.getWeights()); // TODO: remove compat hack
        System.out.println("weights:" + weights);

        assertNotNull(weights);
        assertEquals(2, weights.size());
        assertEquals(1.6267293625545087d, weights.get(10001L), .00001d);
        assertEquals(0.40938456631174364d, weights.get(10002L), .00001d);
        SymMatrix combinedNetwork = weightCalc.getCombinedMatrix();
        assertNotNull(combinedNetwork);

        double sum = combinedNetwork.elementSum();
        System.out.println("combined elem sum: " + sum);
        assertEquals(100.91567860244282d, sum, .00001d); // TODO: float vs double precision internal storage shows up in
                                                         // tests like these, we can paramterize the difference constant
                                                         // as well

    }

    /*
     * regression test for simultaneous weights for BP
     */
    @Test
    public void testRegressionBPCombining() throws Exception {

        CalculateNetworkWeights weightCalc = regressionHelper(CombiningMethod.BP, .5);
        Map<Long, Double> weights = AbstractNetworkWeightCalculator.convertToWeightMap(weightCalc.getWeights()); // TODO: remove compat hack
        System.out.println("weights:" + weights);

        assertNotNull(weights);
        assertEquals(10, weights.size());

        // TODO: update to test rest of weights
        assertEquals(0.0884385703377288d, weights.get(10001L), .00001d);
        assertEquals(0.07097750037733296d, weights.get(10002L), .00001d);
        SymMatrix combinedNetwork = weightCalc.getCombinedMatrix();
        assertNotNull(combinedNetwork);

        double sum = combinedNetwork.elementSum();
        System.out.println("combined elem sum: " + sum);
        assertEquals(68.11348451122349d, sum, .000001d);
    }

    @Test
    public void testRegressionBPCombiningNoNamespace() throws Exception {

        CalculateNetworkWeights weightCalc = regressionHelper(CombiningMethod.BP, .5, Data.CORE);
        Map<Long, Double> weights = AbstractNetworkWeightCalculator.convertToWeightMap(weightCalc.getWeights()); // TODO: remove compat hack
        System.out.println("weights:" + weights);

        assertNotNull(weights);
        assertEquals(10, weights.size());

        // TODO: update to test rest of weights
        assertEquals(0.0884385703377288d, weights.get(10001L), .00001d);
        assertEquals(0.07097750037733296d, weights.get(10002L), .00001d);
        SymMatrix combinedNetwork = weightCalc.getCombinedMatrix();
        assertNotNull(combinedNetwork);

        double sum = combinedNetwork.elementSum();
        System.out.println("combined elem sum: " + sum);
        assertEquals(68.11348451122349d, sum, .000001d);
    }
    
    /*
     * regression test for simultaneous weights for MF
     */
    @Test
    public void testRegressionMFCombining() throws Exception {

        CalculateNetworkWeights weightCalc = regressionHelper(CombiningMethod.MF, .5);
        Map<Long, Double> weights = AbstractNetworkWeightCalculator.convertToWeightMap(weightCalc.getWeights()); // TODO: remove compat hack
        System.out.println("weights:" + weights);

        assertNotNull(weights);
        assertEquals(10, weights.size());

        // TODO: update to test rest of weights
        assertEquals(1.9555421692581575E-5d, weights.get(10001L), .00001d);
        assertEquals(0.09956190006435842d, weights.get(10002L), .00001d);
        SymMatrix combinedNetwork = weightCalc.getCombinedMatrix();
        assertNotNull(combinedNetwork);

        double sum = combinedNetwork.elementSum();
        System.out.println("combined elem sum: " + sum);
        assertEquals(72.17211599490032d, sum, .000001d);
    }

    /*
     * regression test for simultaneous weights for CC
     */
    @Test
    public void testRegressionCCCombining() throws Exception {

        CalculateNetworkWeights weightCalc = regressionHelper(CombiningMethod.CC, .5);
        Map<Long, Double> weights = AbstractNetworkWeightCalculator.convertToWeightMap(weightCalc.getWeights()); // TODO: remove compat hack;
        System.out.println("weights:" + weights);

        assertNotNull(weights);
        assertEquals(10, weights.size());

        // TODO: update to test rest of weights
        assertEquals(0.10769312106033117d, weights.get(10001L), .00001d);
        assertEquals(0.10989575079556282d, weights.get(10002L), .00001d);
        SymMatrix combinedNetwork = weightCalc.getCombinedMatrix();
        assertNotNull(combinedNetwork);

        double sum = combinedNetwork.elementSum();
        System.out.println("combined elem sum: " + sum);
        assertEquals(62.58719774390421d, sum, .000001d);
    }

    /*
     * execute given combining method on test networks from our random test data
     */
    public CalculateNetworkWeights  regressionHelper(CombiningMethod method, double percentPositives, String namespace) throws ApplicationException {
        NetworkMemCache.instance().clear();
        
        Collection<Collection<Long>> queryNetworkIds = new ArrayList<Collection<Long>>();
        Collection<Long> queryAttributeGroupIds = new ArrayList<Long>();
        int organismId = 1;
        DenseVector label = new DenseVector(config.getOrg1numGenes());

        // initialize labels to -1
        for (int i = 0; i < label.size(); i++) {
            label.set(i, -1);
        }

        // and set a few to +1
        for (int i = 0; i < label.size()*percentPositives; i++) {
            label.set(i, 1);
        }

        //Map<Integer, Integer> networkMap = cacheBuilder.getCache().getColumnId(org1Id);
        NetworkIds networkIds = cacheBuilder.getCache().getNetworkIds(namespace, organismId);

        // put network ids into two groups
        ArrayList<Long> temp = new ArrayList<Long>();
        for (int i = 0; i < networkIds.getNetworkIds().length; i++) {
            temp.add(networkIds.getNetworkIds()[i]);
        }
        Collections.sort(temp);
        int group1Size = temp.size() / 5;
        int group2Size = temp.size() - group1Size;

        //System.out.println(temp);
        ArrayList<Long> group = new ArrayList<Long>();

        Iterator<Long> networkIdIter = temp.iterator();
        for (int n = 0; n < group1Size; n++) {
            group.add(networkIdIter.next());
        }
        Collections.shuffle(group);
        queryNetworkIds.add(group);

        group = new ArrayList<Long>();

        for (int n = 0; n < group2Size; n++) {
            group.add(networkIdIter.next());
        }
        Collections.shuffle(group);
        queryNetworkIds.add(group);

        //System.out.println(networkIds);

//        CalculateNetworkWeights weightCalc = new CalculateNetworkWeights(namespace, cacheBuilder.getCache(), queryNetworkIds, organismId, label, method, NullProgressReporter.instance());
        CalculateNetworkWeights  weightCalc =  new CalculateNetworkWeights(namespace, cacheBuilder.getCache(),
                queryNetworkIds, queryAttributeGroupIds, organismId, label, Config.instance().getAttributeEnrichmentMaxSize(), method, NullProgressReporter.instance());

        weightCalc.process();

        return weightCalc;
    }

    /*
     * use namespace "user1" for the query
     */
    public CalculateNetworkWeights  regressionHelper(CombiningMethod method, double percentPositives) throws ApplicationException {
        return regressionHelper(method, percentPositives, Data.CORE);
    }

}
