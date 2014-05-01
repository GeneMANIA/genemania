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


package org.genemania.engine.apps;

import no.uib.cipr.matrix.DenseVector;
import org.genemania.domain.Organism;
import org.genemania.engine.cache.RandomDataCacheBuilder;
import org.genemania.engine.core.data.Data;
import org.genemania.engine.core.data.NodeDegrees;
import org.genemania.engine.mediators.DataCacheGeneMediator;
import org.genemania.engine.mediators.DataCacheNetworkMediator;
import org.genemania.engine.mediators.DataCacheOrganismMediator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 */
public class NodeDegreeComputerTest {

    static RandomDataCacheBuilder cacheBuilder;
    // params for test organism
    static int org1Id = 1;
    static int org1numGenes = 50;
    static int org1numNetworks = 10;
    static double org1networkSparsity = .5;
    static int numCategories = 20;
    static double org1AnnotationSparsity = .5;
    static long [] org1NetworkIds;

    public NodeDegreeComputerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        cacheBuilder = new RandomDataCacheBuilder(7132);
        cacheBuilder.setUp();

        // random organism 1
        org1NetworkIds = cacheBuilder.addOrganism(org1Id, org1numGenes, org1numNetworks,
                org1networkSparsity, numCategories, org1AnnotationSparsity);
    }

    @After
    public void tearDown() {
        cacheBuilder.tearDown();
    }


    /**
     * construct an instance of the class, connect to test mediators and
     * execute
     */
    @Test
    public void testNodeDegreeComputer() throws Exception {

        // plumbing
        NodeDegreeComputer ndc = new NodeDegreeComputer();

        ndc.setGeneMediator(new DataCacheGeneMediator(cacheBuilder.getCache()));
        ndc.setNetworkMediator(new DataCacheNetworkMediator(cacheBuilder.getCache()));
        ndc.setOrganismMediator(new DataCacheOrganismMediator(cacheBuilder.getCache()));

        ndc.setOrgId(org1Id);
        Organism organism = ndc.getOrganismMediator().getOrganism(org1Id);

        ndc.setCacheDir(cacheBuilder.getCacheDir());
        ndc.setCache(cacheBuilder.getCache());

        // execution
        ndc.processOrganism(organism);

        // what useful things could we test for regression?
        NodeDegrees nodeDegrees = cacheBuilder.getCache().getNodeDegrees(Data.CORE, org1Id);
        assertNotNull(nodeDegrees);

        DenseVector degrees = nodeDegrees.getDegrees();
        assertNotNull(degrees);
        assertEquals(50, ndc.getNumConnectedGenes());
        
    }

}