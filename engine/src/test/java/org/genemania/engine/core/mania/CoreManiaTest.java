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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Vector;
import org.genemania.engine.config.Config;
import org.genemania.engine.Constants;
import org.genemania.engine.Utils;
import org.genemania.engine.cache.NetworkMemCache;
import org.genemania.engine.cache.RandomDataCacheBuilder;
import org.genemania.engine.matricks.MatricksException;
//import org.genemania.engine.matricks.Vector;
import org.genemania.engine.core.data.Data;
import org.genemania.engine.core.integration.calculators.AbstractNetworkWeightCalculator;
import org.genemania.exception.ApplicationException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import static org.junit.Assert.*;

/**
 *
 */
@RunWith(Parameterized.class)
public class CoreManiaTest {

    public CoreManiaTest(String configFile) throws ApplicationException {
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
    public void setUp() {
        NetworkMemCache.instance().clear();
    }

    @After
    public void tearDown() {
    }

    /*
     * helper. would be better to put this in a data file, no?
     */
    public Vector getExpectedResult() throws Exception {
        Vector expectedResult;


        try {
            expectedResult = new DenseVector(50);

            expectedResult.set(0, 4.043559659004e-01);
            expectedResult.set(1, 3.994898249630e-01);
            expectedResult.set(2, 4.030673923299e-01);
            expectedResult.set(3, 4.180895829556e-01);
            expectedResult.set(4, 3.803373796498e-01);
            expectedResult.set(5, 3.908877824877e-01);
            expectedResult.set(6, 3.836518887937e-01);
            expectedResult.set(7, 4.430004523962e-01);
            expectedResult.set(8, 4.122634391558e-01);
            expectedResult.set(9, 4.054013481995e-01);
            expectedResult.set(10, 3.983191261474e-01);
            expectedResult.set(11, 3.778052571113e-01);
            expectedResult.set(12, 4.069554762139e-01);
            expectedResult.set(13, 4.012886826674e-01);
            expectedResult.set(14, 4.117302854601e-01);
            expectedResult.set(15, 3.917287219613e-01);
            expectedResult.set(16, 3.625079184793e-01);
            expectedResult.set(17, 3.694247321766e-01);
            expectedResult.set(18, 3.833077403845e-01);
            expectedResult.set(19, 4.266432970633e-01);
            expectedResult.set(20, -5.855638358007e-01);
            expectedResult.set(21, -6.195846895557e-01);
            expectedResult.set(22, -5.999148545508e-01);
            expectedResult.set(23, -6.148940850024e-01);
            expectedResult.set(24, -5.935011083825e-01);
            expectedResult.set(25, -6.002136795938e-01);
            expectedResult.set(26, -5.968701480829e-01);
            expectedResult.set(27, -5.723905719060e-01);
            expectedResult.set(28, -5.692925216438e-01);
            expectedResult.set(29, -5.882700683856e-01);
            expectedResult.set(30, -5.908875513821e-01);
            expectedResult.set(31, -6.048254070056e-01);
            expectedResult.set(32, -5.802148138478e-01);
            expectedResult.set(33, -5.821195763140e-01);
            expectedResult.set(34, -5.862784315530e-01);
            expectedResult.set(35, -5.978939511022e-01);
            expectedResult.set(36, -5.736862421878e-01);
            expectedResult.set(37, -5.982761140601e-01);
            expectedResult.set(38, -6.186433503705e-01);
            expectedResult.set(39, -6.141065021995e-01);
            expectedResult.set(40, -5.923207460372e-01);
            expectedResult.set(41, -6.217098643465e-01);
            expectedResult.set(42, -6.161612923949e-01);
            expectedResult.set(43, -5.987928614531e-01);
            expectedResult.set(44, -6.253772675203e-01);
            expectedResult.set(45, -6.495297204068e-01);
            expectedResult.set(46, -5.524301206004e-01);
            expectedResult.set(47, -5.561753883564e-01);
            expectedResult.set(48, -5.959933223151e-01);
            expectedResult.set(49, -6.743382081390e-01);
        }
        catch (MatricksException e) {
            throw new RuntimeException("failed to init test data: " + e);
        }

        return expectedResult;
    }
    
    @Test
    public void testComputeAverageCombining() throws Exception {

        Vector expectedResult = getExpectedResult();

        // create some test data
        RandomDataCacheBuilder rcb = new RandomDataCacheBuilder(2112);
        rcb.setUp();
        long[] organism1NetworkIds = rcb.addOrganism(1, 50, 5, 0.5);
        long[] organism2NetworkIds = rcb.addOrganism(2, 20, 3, 0.5);

        //int[] organism1NodeIds = rcb.getCache().getMapping(1);
        //int[] organism2NodeIds = rcb.getCache().getMapping(2);
        long[] organism1NodeIds = rcb.getCache().getNodeIds(1).getNodeIds();
        long[] organism2NodeIds = rcb.getCache().getNodeIds(2).getNodeIds();

        // sanity check on test data
        assertEquals(5, organism1NetworkIds.length);
        assertEquals(3, organism2NetworkIds.length);
        assertEquals(50, organism1NodeIds.length);
        assertEquals(20, organism2NodeIds.length);

        // setup mania instance ... only data dependency is on the cache
        CoreMania coreMania = new CoreMania(rcb.getCache());

        // set first 20 labels to 1.0, the rest to -1.0;
        Vector labels = new DenseVector(50);
        for (int i = 0; i < 50; i++) {
            if (i < 20) {
                labels.set(i, 1.0);
            }
            else {
                labels.set(i, -1.0);
            }
        }

        // all the networks in one group
        Collection<Collection<Long>> networkIds = new ArrayList<Collection<Long>>();
        Collection<Long> group1 = new ArrayList<Long>();
        for (int i = 0; i < organism1NetworkIds.length; i++) {
            group1.add(organism1NetworkIds[i]);
        }
        networkIds.add(group1);
        Collection<Long> attributeGroupIds = new ArrayList<Long>();

        // compute & check
        coreMania.compute(Data.CORE, 1, labels, Constants.CombiningMethod.AVERAGE, networkIds, null, "average");

        assertNotNull(coreMania.getCombinedKernel(1, Data.CORE));
//        assertEquals(0d, coreMania.getCombinedKernel().elementSum(), 1e-5);

        assertNotNull(coreMania.getFeatureWeights());
        assertFalse(coreMania.getFeatureWeights().size() == 0);
        assertNotNull(coreMania.getDiscriminant());

        // all the network weights should be 1/N
        Map<Long, Double> matrixWeights = AbstractNetworkWeightCalculator.convertToWeightMap(coreMania.getFeatureWeights());
        for (Long m: matrixWeights.keySet()) { // TODO: remove compat hack

            double weight = matrixWeights.get(m);
            assertEquals(1d / organism1NetworkIds.length, weight, .00001);
        }

        // discriminant scores regression test
        Utils.elementWiseCompare(expectedResult, coreMania.getDiscriminant(), 1e-7);
    }
    /*
     * test case for computing the network combination
     * and discriminant with separate api calls.
     */
    public void testSplitComputation() throws Exception {

        Vector expectedResult = getExpectedResult();

        // create some test data
        RandomDataCacheBuilder rcb = new RandomDataCacheBuilder(2112);
        rcb.setUp();
        long[] organism1NetworkIds = rcb.addOrganism(1, 50, 5, 0.5);
        long[] organism2NetworkIds = rcb.addOrganism(2, 20, 3, 0.5);

        long[] organism1NodeIds = rcb.getCache().getNodeIds(1).getNodeIds();
        long[] organism2NodeIds = rcb.getCache().getNodeIds(2).getNodeIds();

        // sanity check on test data
        assertEquals(5, organism1NetworkIds.length);
        assertEquals(3, organism2NetworkIds.length);
        assertEquals(50, organism1NodeIds.length);
        assertEquals(20, organism2NodeIds.length);

        // setup mania instance ... only data dependency is on the cache
        CoreMania coreMania = new CoreMania(rcb.getCache());

        // set first 20 labels to 1.0, the rest to -1.0;
        Vector labels = new DenseVector(50);
        for (int i=0; i<50; i++) {
            if (i < 20) {
                labels.set(i, 1.0);
            }
            else {
                labels.set(i, -1.0);
            }
        }

        // all the networks in one group
        Collection<Collection<Long>> networkIds = new ArrayList<Collection<Long>>();
        Collection<Long> attributeGroupIds = new ArrayList<Long>();
        Collection<Long> group1 = new ArrayList<Long>();
        for (int i=0; i<organism1NetworkIds.length; i++) {
            group1.add(organism1NetworkIds[i]);
        }
        networkIds.add(group1);

        // compute & check
        coreMania.computeWeights(Data.CORE, 1, labels, Constants.CombiningMethod.AVERAGE, networkIds, attributeGroupIds, Config.instance().getAttributeEnrichmentMaxSize());

        assertNotNull(coreMania.getCombinedKernel(1, Data.CORE));
        assertNotNull(coreMania.getFeatureWeights());
        assertFalse(coreMania.getFeatureWeights().size() == 0);

        // all the network weights should be 1/N
        Map<Long, Double> matrixWeights = AbstractNetworkWeightCalculator.convertToWeightMap(coreMania.getFeatureWeights());
        for (Long m: matrixWeights.keySet()) { // TODO: remove compat hack

            double weight = matrixWeights.get(m);
            assertEquals(1d/organism1NetworkIds.length, weight, .00001);
        }

        coreMania.computeDiscriminant(Data.CORE, 1, labels, null, "average");
        assertNotNull(coreMania.getDiscriminant());

        // discriminant scores regression test
        Utils.elementWiseCompare(expectedResult, coreMania.getDiscriminant(), 1e-10);

    }

    public void testMultipleCalculationsWithSameWeights() throws Exception {

        Vector expectedResult = getExpectedResult();

        // create some test data
        RandomDataCacheBuilder rcb = new RandomDataCacheBuilder(2112);
        rcb.setUp();
        long[] organism1NetworkIds = rcb.addOrganism(1, 50, 5, 0.5);
        long[] organism2NetworkIds = rcb.addOrganism(2, 20, 3, 0.5);

        long[] organism1NodeIds = rcb.getCache().getNodeIds(1).getNodeIds();
        long[] organism2NodeIds = rcb.getCache().getNodeIds(2).getNodeIds();

        // sanity check on test data
        assertEquals(5, organism1NetworkIds.length);
        assertEquals(3, organism2NetworkIds.length);
        assertEquals(50, organism1NodeIds.length);
        assertEquals(20, organism2NodeIds.length);

        // setup mania instance ... only data dependency is on the cache
        CoreMania coreMania = new CoreMania(rcb.getCache());

        // set first 20 labels to 1.0, the rest to -1.0;
        Vector labels = new DenseVector(50);
        for (int i=0; i<50; i++) {
            if (i < 20) {
                labels.set(i, 1.0);
            }
            else {
                labels.set(i, -1.0);
            }
        }

        // all the networks in one group
        Collection<Collection<Long>> networkIds = new ArrayList<Collection<Long>>();
        Collection<Long> attributeGroupIds = new ArrayList<Long>();
        Collection<Long> group1 = new ArrayList<Long>();
        for (int i=0; i<organism1NetworkIds.length; i++) {
            group1.add(organism1NetworkIds[i]);
        }
        networkIds.add(group1);

        // compute & check
        coreMania.computeWeights(Data.CORE, 1, labels, Constants.CombiningMethod.AVERAGE, networkIds, attributeGroupIds, Config.instance().getAttributeEnrichmentMaxSize());
        assertNotNull(coreMania.getCombinedKernel(1, Data.CORE));
        assertNotNull(coreMania.getFeatureWeights());
        assertFalse(coreMania.getFeatureWeights().size() == 0);

        // all the network weights should be 1/N
        Map<Long, Double> matrixWeights = AbstractNetworkWeightCalculator.convertToWeightMap(coreMania.getFeatureWeights());        
        for (Long m: matrixWeights.keySet()) { // TODO: remove compat hack
            double weight = matrixWeights.get(m);
            assertEquals(1d/organism1NetworkIds.length, weight, .00001);
        }

        coreMania.computeDiscriminant(Data.CORE, 1, labels, null, "average");
        assertNotNull(coreMania.getDiscriminant());

        // discriminant scores regression test
        Utils.elementWiseCompare(expectedResult, coreMania.getDiscriminant(), 1e-10);

        // another set of labels, first 30 set to 1
        Vector labels2 = new DenseVector(50);
        for (int i=0; i<50; i++) {
            if (i < 30) {
                labels2.set(i, 1.0);
            }
            else {
                labels2.set(i, -1.0);
            }
        }

        // run the second query with original combined networks, just regression test
        // first few elements
        coreMania.computeDiscriminant(Data.CORE, 1, labels2, null, "average");
        assertNotNull(coreMania.getDiscriminant());
        System.out.println(coreMania.getDiscriminant());
        assertEquals(5.948889395253e-01, coreMania.getDiscriminant().get(0), 1e-10);
        assertEquals(5.992815340716e-01, coreMania.getDiscriminant().get(1), 1e-10);
        assertEquals(6.017207119987e-01, coreMania.getDiscriminant().get(2), 1e-10);
        assertEquals(5.800145918994e-01, coreMania.getDiscriminant().get(3), 1e-10);
        assertEquals(5.881483019749e-01, coreMania.getDiscriminant().get(4), 1e-10);


        // first query again, should give the original result
        coreMania.computeDiscriminant(Data.CORE, 1, labels, null, "average");
        assertNotNull(coreMania.getDiscriminant());
        Utils.elementWiseCompare(expectedResult, coreMania.getDiscriminant(), 1e-10);

    }
}
