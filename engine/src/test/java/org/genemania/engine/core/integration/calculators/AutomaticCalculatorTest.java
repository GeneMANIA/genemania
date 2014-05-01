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

import org.genemania.engine.core.data.Data;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import no.uib.cipr.matrix.DenseVector;
import org.genemania.engine.cache.NetworkMemCache;
import org.genemania.engine.cache.RandomDataCacheBuilder;
import org.genemania.engine.config.Config;
import org.genemania.engine.core.integration.FeatureWeightMap;
import org.genemania.engine.core.integration.INetworkWeightCalculator;
import org.genemania.exception.ApplicationException;
import org.genemania.util.NullProgressReporter;
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
public class AutomaticCalculatorTest {

    static RandomDataCacheBuilder cacheBuilder;
    // params for test organism
    static int org1Id = 1;
    static int org1numGenes = 50;
    static int org1numNetworks = 10;
    static double org1networkSparsity = .5;
    static int numCategories = 20;
    static double org1AnnotationSparsity = .5;

    static long[] org1networkIds;

    public AutomaticCalculatorTest(String configFile) throws ApplicationException {
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
        cacheBuilder = new RandomDataCacheBuilder(7132);
        cacheBuilder.setUp();

        // random organism 1
        org1networkIds = cacheBuilder.addOrganism(org1Id, org1numGenes, org1numNetworks,
                org1networkSparsity, numCategories, org1AnnotationSparsity);

        // clear mem cache since its a singleton process wide, don't want stuff left over from other tests
        NetworkMemCache.instance().clear();
    }

    @After
    public void tearDown() {
        cacheBuilder.tearDown();
    }

    /**
     * Test of process method, of class AverageByNetworkCalculator.
     */
    @Test
    public void testProcess() throws Exception {
        System.out.println("process");

        Collection<Collection<Long>> groups = new ArrayList<Collection<Long>>();
        Collection<Long> attributeGroupIds = new ArrayList<Long>();
        Collection<Long> group = new ArrayList<Long>();
        for (int i=0; i<org1networkIds.length; i++) {
            group.add(org1networkIds[i]);
        }
        groups.add(group);

        no.uib.cipr.matrix.Vector labels = new DenseVector(org1numGenes);
        for (int i = 0; i < labels.size()/2; i++) {
            if (i < 20) {
                labels.set(i, 1.0);
            }
            else {
                labels.set(i, -1.0);
            }
        }
        INetworkWeightCalculator calculator = new AutomaticCalculator(Data.CORE, cacheBuilder.getCache(), 
        		groups, attributeGroupIds, org1Id, labels, Config.instance().getAttributeEnrichmentMaxSize(), NullProgressReporter.instance());
        calculator.process();

        FeatureWeightMap weights = calculator.getWeights();
        assertNotNull(weights);
//        assertEquals(org1networkIds.length, weights.size());

        // TODO: test weight values
    }
}