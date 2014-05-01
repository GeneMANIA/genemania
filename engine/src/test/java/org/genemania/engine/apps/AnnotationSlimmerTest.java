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
import org.genemania.engine.Constants;
import org.genemania.engine.cache.RandomDataCacheBuilder;
import org.genemania.engine.core.data.GoIds;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 */
public class AnnotationSlimmerTest {

    static RandomDataCacheBuilder cacheBuilder;
    // params for test organism
    static int org1Id = 1;
    static int org1numGenes = 50;
    static int org1numNetworks = 10;
    static double org1networkSparsity = .5;
    static int numCategories = 20;
    static double org1AnnotationSparsity = .5;
    static long [] org1NetworkIds;

    public AnnotationSlimmerTest() {
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
     * first numCatsFromBranch from each of the main go banches
     */
    public void buildTestCategoryFile(String filename, int numCatsFromBranch) throws Exception {

        FileWriter file = new FileWriter(filename);

        for (String branch: Constants.goBranches) {
            GoIds goIds = cacheBuilder.getCache().getGoIds(org1Id, branch);
            for (int i=0; i<numCatsFromBranch; i++) {
                String id = goIds.getGoIds()[i];
                String desc = String.format("description for %s", id);
                file.write(String.format("%s\t%s\n", id, desc));
            }
        }

        file.close();
    }

    @Test
    @Ignore // TODO: broken after domain object support implemented, update
    public void testProcess() throws Exception {
        String TEST_FILENAME = cacheBuilder.getCacheDir() + File.separator + "test_categories.txt";
        buildTestCategoryFile(TEST_FILENAME, 5);
        
        AnnotationSlimmer slimmer = new AnnotationSlimmer();
        slimmer.setOrgId(org1Id);
        slimmer.setCacheDir(cacheBuilder.getCacheDir());
        slimmer.setPondir(cacheBuilder.getCacheDir());
        slimmer.setCache(cacheBuilder.getCache());
//        slimmer.setOntologyName("TEST_SLIM");
        
        slimmer.process();

        assertTrue("one day, we'll have real tests here!", true);
    }
}