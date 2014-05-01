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

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.genemania.domain.Gene;
import org.genemania.domain.Node;
import org.genemania.domain.Organism;
import org.genemania.engine.cache.RandomDataCacheBuilder;
import org.genemania.engine.mediators.DataCacheGeneMediator;
import org.genemania.engine.mediators.DataCacheNetworkMediator;
import org.genemania.engine.mediators.DataCacheNodeMediator;
import org.genemania.engine.mediators.DataCacheOrganismMediator;
import org.genemania.exception.ApplicationException;
import org.genemania.mediator.GeneMediator;
import org.genemania.mediator.NodeMediator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 */
public class VectorCrossValidatorTest {

    static RandomDataCacheBuilder cacheBuilder;
    // params for test organism
    static int org1Id = 1;
    static int org1numGenes = 50;
    static int org1numNetworks = 10;
    static double org1networkSparsity = .5;
    static int numCategories = 20;
    static double org1AnnotationSparsity = .5;
    static long [] org1NetworkIds;

    public VectorCrossValidatorTest() {
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

    /*
     * random gene lists
     */
    public void makeQueryFile(String filename, Organism organism, int numRecs, int queryLen, long seed, NodeMediator nodeMediator, GeneMediator geneMediator) throws Exception {
        FileWriter writer = new FileWriter(filename);

        long [] nodeIds = cacheBuilder.getCache().getNodeIds(organism.getId()).getNodeIds();
        int numNodes = nodeIds.length;

        List<Integer> indices = new ArrayList<Integer>();
        for (int i=0; i<numNodes; i++) {
            indices.add(i);
        }        

        Random random = new Random(seed);
        for (int i=0; i<numRecs; i++) {
            Collections.shuffle(indices, random);

            StringBuilder builder = new StringBuilder("query"+i);
            builder.append("\t+");
            for (int j=0; j<queryLen; j++) {

                int nodeIndex = indices.get(j);
                long nodeId = nodeIds[nodeIndex];

                Node node = nodeMediator.getNode(nodeId, organism.getId());
                if (node == null) {
                    throw new ApplicationException("failed to look up gene for node id '" + nodeId);
                }

                Gene gene = (Gene) node.getGenes().iterator().next();

                builder.append("\t");
                builder.append(gene.getSymbol());
            }
            builder.append("\n");
            writer.write(builder.toString());
        }

        writer.close();
    }

    /*
     * generate a random query file based on the data cache, and
     * run cross-validation
     */
    @Test
    public void testValidator() throws Exception {
        String filename = "test_query_file.txt";
        int numFolds = 3;
        int numQueries = 3;
        int seed = 2010; // for randomizing the query genes
        int numGenesPerQuery = org1numGenes/3;

        VectorCrossValidator vcv = new VectorCrossValidator();

        vcv.setGeneMediator(new DataCacheGeneMediator(cacheBuilder.getCache()));
        vcv.setNetworkMediator(new DataCacheNetworkMediator(cacheBuilder.getCache()));
        vcv.setOrganismMediator(new DataCacheOrganismMediator(cacheBuilder.getCache()));

        vcv.setOrganismId(org1Id);
        vcv.setNetworkIdsList(String.format("%d,%d,%d", org1NetworkIds[0], org1NetworkIds[1], org1NetworkIds[2]));

        Organism organism = vcv.getOrganismMediator().getOrganism(org1Id);
        NodeMediator nodeMediator = new DataCacheNodeMediator(cacheBuilder.getCache(), organism);

        // construct a query file based on cached data
        filename = cacheBuilder.getCacheDir() + File.separator + filename;
        makeQueryFile(filename, organism, numQueries, numGenesPerQuery, seed, nodeMediator, vcv.getGeneMediator());
        vcv.setQueryFileName(filename);

        vcv.setCacheDir(cacheBuilder.getCacheDir());
        vcv.setCombiningMethodName("AUTOMATIC");
        vcv.setNumFolds(numFolds);

        // run the x-val 
        vcv.initValidation();
        vcv.crossValidate();

        // what useful things could we test for regression?
        assertEquals("execute the expected number of findRelated() calls", numFolds*numQueries, vcv.getQueryCounter());
    }
}
