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

package org.genemania.engine;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;

import org.genemania.dto.InteractionDto;
import org.genemania.dto.NetworkDto;
import org.genemania.dto.RelatedGenesEngineRequestDto;
import org.genemania.dto.RelatedGenesEngineResponseDto;
import org.genemania.dto.UploadNetworkEngineRequestDto;
import org.genemania.dto.UploadNetworkEngineResponseDto;
import org.genemania.type.CombiningMethod;
import org.genemania.type.ScoringMethod;
import org.genemania.engine.actions.support.UserNetworkProcessor;
import org.genemania.engine.cache.NetworkMemCache;
import org.genemania.engine.cache.RandomDataCacheBuilder;
import org.genemania.engine.converter.sym.CacheNetworkSymMatrixProvider;
import org.genemania.engine.converter.sym.INetworkSymMatrixProvider;
import org.genemania.type.DataLayout;
import org.genemania.type.NetworkProcessingMethod;
import org.genemania.util.NullProgressReporter;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * testing addition of user-networks to core dataset,
 * used with the simultaneous (annotation based) combining methods
 */
public class UserNetworkProcessorSimultCombing2Test {

    RandomDataCacheBuilder randomCacheBuilder = new RandomDataCacheBuilder(2112);
    long[] networkIds;
    // params for test organism
    static int org1Id = 1;
    static int org1numGenes = 50;
    static int org1numNetworks = 10;
    static double org1networkSparsity = .5;
    static int numCategories = 20;
    static double org1AnnotationSparsity = .5;

    public UserNetworkProcessorSimultCombing2Test() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        randomCacheBuilder.setUp();
        // random organism 1
        networkIds = randomCacheBuilder.addOrganism(org1Id, org1numGenes, org1numNetworks,
                org1networkSparsity, numCategories, org1AnnotationSparsity);

        NetworkMemCache.instance().clear();
    }

    @After
    public void tearDown() {
        randomCacheBuilder.tearDown();
    }

    /**
     * add a user-network to data set, then compute
     * a result using BP combining
     */
    @Test
    public void testBPCombiningWithUserNetwork() throws Exception {
        RelatedGenesEngineResponseDto relatedResponse = regressionHelper(CombiningMethod.BP);
        assertNotNull(relatedResponse);
        assertNotNull(relatedResponse.getNetworks());
        assertFalse(relatedResponse.getNetworks().size() == 0);
    }

    @Test
    public void testMFCombiningWithUserNetwork() throws Exception {
        RelatedGenesEngineResponseDto relatedResponse = regressionHelper(CombiningMethod.MF);
        assertNotNull(relatedResponse);
        assertNotNull(relatedResponse.getNetworks());
        assertFalse(relatedResponse.getNetworks().size() == 0);
    }

    @Test
    public void testCCCombiningWithUserNetwork() throws Exception {
        RelatedGenesEngineResponseDto relatedResponse = regressionHelper(CombiningMethod.CC);
        assertNotNull(relatedResponse);
        assertNotNull(relatedResponse.getNetworks());
        assertFalse(relatedResponse.getNetworks().size() == 0);
    }

    public void dumpNetworks(RelatedGenesEngineResponseDto relatedResponse) {

        System.out.println("num networks in result: " + relatedResponse.getNetworks().size());
        for (NetworkDto n: relatedResponse.getNetworks()) {
            System.out.println("  network id: " + n.getId() + " weight: " + n.getWeight() + " num interactions: " + n.getInteractions().size());
            for (InteractionDto interaction: n.getInteractions()) {
                /*
                System.out.println(String.format("   inter: node %s %s, node %s %s, %s",
                interaction.getNodeVO1().getId(),
                interaction.getNodeVO1().getScore(),
                interaction.getNodeVO2().getId(),
                interaction.getNodeVO2().getScore(),
                interaction.getWeight()
                ));
                 */
            }
        }
    }

    /*
     * helper to add a small user network to the test data set, then
     * call a related genes request with given combining method, and
     * other params set to std values
     */
    public RelatedGenesEngineResponseDto regressionHelper(CombiningMethod method) throws Exception {
        int orgId = 1;
        int userNetworkId = -1;
        String userName = "user1";

        // add a user network

//        INetworkSymMatrixProvider provider = new CacheNetworkSymMatrixProvider(userName, orgId, randomCacheBuilder.getCache());
        UserNetworkProcessor instance = new UserNetworkProcessor(randomCacheBuilder.getCache(), randomCacheBuilder.getCacheDir());

        UploadNetworkEngineRequestDto uploadRequest = new UploadNetworkEngineRequestDto();
        uploadRequest.setLayout(DataLayout.PROFILE);
        uploadRequest.setMethod(NetworkProcessingMethod.PEARSON);
        uploadRequest.setNamespace(userName);
        uploadRequest.setProgressReporter(NullProgressReporter.instance());
        uploadRequest.setOrganismId(orgId);
        uploadRequest.setNetworkId(userNetworkId);
        uploadRequest.setSparsification(50);

        String data = "id\tf1\tf2\tf3\n10000\t1.5\t3.0\t4.0\n10001\t2.0\t2.2\t2.1\n";
        uploadRequest.setData(new StringReader(data));

        UploadNetworkEngineResponseDto uploadResponse = instance.process(uploadRequest);
        assertNotNull(uploadResponse);

        // now send in a request for related genes, involving all the old networks plus
        // the new one

        IMania mania = new Mania2(randomCacheBuilder.getCache());

        // create request for organism 1
        RelatedGenesEngineRequestDto relatedRequest = new RelatedGenesEngineRequestDto();
        relatedRequest.setNamespace(userName);
        relatedRequest.setOrganismId(orgId);
        relatedRequest.setCombiningMethod(method);
        relatedRequest.setScoringMethod(ScoringMethod.DISCRIMINANT);

        long[] nodeIds = randomCacheBuilder.getCache().getNodeIds(orgId).getNodeIds();

        // first 10 nodes as +ve
        ArrayList<Long> posNodes = new ArrayList<Long>();
        for (int i = 0; i < 10; i++) {
            posNodes.add(nodeIds[i]);
        }
        relatedRequest.setPositiveNodes(posNodes);

        // all networks
        Collection<Collection<Long>> requestNetworks = new ArrayList<Collection<Long>>();

        ArrayList<Long> group = new ArrayList<Long>();
        for (int i = 0; i < networkIds.length; i++) {
            group.add((long) networkIds[i]);
        }

        group.add((long) userNetworkId);

        requestNetworks.add(group);
        relatedRequest.setInteractionNetworks(requestNetworks);
        relatedRequest.setLimitResults(10);
        relatedRequest.setProgressReporter(NullProgressReporter.instance());

        // compute result
        RelatedGenesEngineResponseDto relatedResponse = mania.findRelated(relatedRequest);
        return relatedResponse;
    }
    /**
     * add a user-network to data set, then compute
     * a result using BP combining from the core networks
     * *not* including the user network that was just uploaded
     */
    @Test
    public void testBPCombiningWithoutUserNetwork() throws Exception {
        System.out.println("averageCombiningWithUserNetworkChaChaCha");
        int orgId = 1;
        int userNetworkId = -1;
        String userName = "user1";

        // add a user network

//        INetworkSymMatrixProvider provider = new CacheNetworkSymMatrixProvider(userName, orgId, randomCacheBuilder.getCache());
        UserNetworkProcessor instance = new UserNetworkProcessor(randomCacheBuilder.getCache(), randomCacheBuilder.getCacheDir());

        UploadNetworkEngineRequestDto uploadRequest = new UploadNetworkEngineRequestDto();
        uploadRequest.setLayout(DataLayout.PROFILE);
        uploadRequest.setMethod(NetworkProcessingMethod.PEARSON);
        uploadRequest.setNamespace(userName);
        uploadRequest.setProgressReporter(NullProgressReporter.instance());
        uploadRequest.setOrganismId(orgId);
        uploadRequest.setNetworkId(userNetworkId);
        uploadRequest.setSparsification(50);

        String data = "id\tf1\tf2\tf3\n10000\t1.5\t3.0\t4.0\n10001\t2.0\t2.2\t2.1\n";
        uploadRequest.setData(new StringReader(data));

        UploadNetworkEngineResponseDto uploadResponse = instance.process(uploadRequest);
        assertNotNull(uploadResponse);

        // now send in a request for related genes, involving all the old networks plus
        // the new one

        IMania mania = new Mania2(randomCacheBuilder.getCache());

        // create request for organism 1
        RelatedGenesEngineRequestDto relatedRequest = new RelatedGenesEngineRequestDto();
        relatedRequest.setNamespace(userName);
        relatedRequest.setOrganismId(orgId);
        relatedRequest.setCombiningMethod(CombiningMethod.BP);
        relatedRequest.setScoringMethod(ScoringMethod.DISCRIMINANT);

        long[] nodeIds = randomCacheBuilder.getCache().getNodeIds(orgId).getNodeIds();

        // first 10 nodes as +ve
        ArrayList<Long> posNodes = new ArrayList<Long>();
        for (int i = 0; i < 10; i++) {
            posNodes.add((long) nodeIds[i]);
        }
        relatedRequest.setPositiveNodes(posNodes);

        // all networks less one
        Collection<Collection<Long>> requestNetworks = new ArrayList<Collection<Long>>();

        ArrayList<Long> group = new ArrayList<Long>();
        for (int i = 0; i < networkIds.length; i++) {
            group.add(networkIds[i]);
        }

        //group.add((long) userNetworkId);

        requestNetworks.add(group);
        relatedRequest.setInteractionNetworks(requestNetworks);
        relatedRequest.setLimitResults(10);
        relatedRequest.setProgressReporter(NullProgressReporter.instance());

        // compute result
        RelatedGenesEngineResponseDto relatedResponse = mania.findRelated(relatedRequest);
        assertNotNull(relatedResponse);
        assertNotNull(relatedResponse.getNetworks());
        assertFalse(relatedResponse.getNetworks().size() == 0);

        System.out.println("num networks in result: " + relatedResponse.getNetworks().size());
        for (NetworkDto n: relatedResponse.getNetworks()) {
            System.out.println("  network id: " + n.getId() + " weight: " + n.getWeight() + " num interactions: " + n.getInteractions().size());
            for (InteractionDto interaction: n.getInteractions()) {
                /*
                System.out.println(String.format("   inter: node %s %s, node %s %s, %s",
                interaction.getNodeVO1().getId(),
                interaction.getNodeVO1().getScore(),
                interaction.getNodeVO2().getId(),
                interaction.getNodeVO2().getScore(),
                interaction.getWeight()
                ));
                 */
            }
        }

        // TODO: send the same request through again, but this time using the old mania api

    }
}
