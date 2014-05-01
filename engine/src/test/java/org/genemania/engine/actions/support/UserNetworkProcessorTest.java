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


package org.genemania.engine.actions.support;

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
import org.genemania.engine.IMania;
import org.genemania.engine.Mania2;
import org.genemania.engine.actions.support.UserNetworkProcessor;
import org.genemania.engine.cache.NetworkMemCache;
import org.genemania.engine.cache.RandomDataCacheBuilder;
import org.genemania.engine.cache.RandomDataCacheConfig;
import org.genemania.engine.config.Config;
import org.genemania.engine.matricks.SymMatrix;
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
 * testing addition of user-networks to core dataset
 */
public class UserNetworkProcessorTest {

    RandomDataCacheBuilder randomCacheBuilder;
    RandomDataCacheConfig config = RandomDataCacheConfig.getStandardConfig2();
    
    public UserNetworkProcessorTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        randomCacheBuilder = new RandomDataCacheBuilder(config.getSeed());
        randomCacheBuilder.setUp();
        randomCacheBuilder.addOrganism(config);
        
        NetworkMemCache.instance().clear();
    }

    @After
    public void tearDown() {
        randomCacheBuilder.tearDown();
    }

    /**
     * Test of process method, of class UserNetworkProcessor.
     */
    @Test
    public void testProcessProfile() throws Exception {
        System.out.println("processProfile");
        String user = "user1";

        UserNetworkProcessor instance = new UserNetworkProcessor(randomCacheBuilder.getCache(), randomCacheBuilder.getCacheDir());

        // populate a request
        UploadNetworkEngineRequestDto request = new UploadNetworkEngineRequestDto();
        request.setLayout(DataLayout.PROFILE);
        request.setMethod(NetworkProcessingMethod.PEARSON);
        request.setNamespace(user);
        request.setProgressReporter(NullProgressReporter.instance());
        request.setOrganismId(1);
        request.setNetworkId(-1);
        request.setSparsification(50);

        String data = "id\tf1\tf2\tf3\n10000\t1.5\t3.0\t4.0\n10001\t2.0\t2.2\t2.1\n";
        request.setData(new StringReader(data));

        // process load request
        UploadNetworkEngineResponseDto result = instance.process(request);

        assertNotNull(result);

        // should now be able to fetch the new network directly out of teh cache
        SymMatrix m = randomCacheBuilder.getCache().getNetwork(user, 1, -1).getData();
        assertNotNull(result);
        System.out.println("m: " + m);
    }

    @Test
    public void testProcessSparseProfile() throws Exception {
        System.out.println("processSparseProfile");
        String user = "user1";

//        INetworkSymMatrixProvider provider = new CacheNetworkSymMatrixProvider(user, 1, randomCacheBuilder.getCache());
        UserNetworkProcessor instance = new UserNetworkProcessor(randomCacheBuilder.getCache(), randomCacheBuilder.getCacheDir());

        // populate a request
        UploadNetworkEngineRequestDto request = new UploadNetworkEngineRequestDto();
        request.setLayout(DataLayout.SPARSE_PROFILE);
        request.setMethod(NetworkProcessingMethod.LOG_FREQUENCY);
        request.setNamespace(user);
        request.setProgressReporter(NullProgressReporter.instance());
        request.setOrganismId(1);
        request.setNetworkId(-1);
        request.setSparsification(50);
        
        // TODO: there is test network upload data in ProfileToNetworkDriverTest, should consolidate 
        // that somewhere and share, to avoid having to update in two places.
        String data = 
            "10000\t1\t5\n" +
            "10001\t3\t4\n" +
            "10002\t2\t4\n" +
            "10003\t0\t3\t5\n" +
            "10004\t0\t1\t2\n" +
            "10005\t6\n" +
            "10006\t7\n" +
            "10007\t8\n" +
            "10008\t9\n" +
            "10009\t10\n";
        
        request.setData(new StringReader(data));

        // process load request
        UploadNetworkEngineResponseDto result = instance.process(request);

        assertNotNull(result);
        assertEquals(6, result.getNumInteractions()); // remember, this doesn't count symmetric interactions

        // should now be able to fetch the new network directly out of the cache
        SymMatrix m = randomCacheBuilder.getCache().getNetwork(user, 1, -1).getData();
        assertNotNull(result);
        System.out.println("m: " + m);
    }

    @Test
    public void testProcessBinaryToDirectNetwork() throws Exception {
        System.out.println("processBinaryToDirectNetwork");
        String user = "user1";

//        INetworkSymMatrixProvider provider = new CacheNetworkSymMatrixProvider(user, 1, randomCacheBuilder.getCache());
        UserNetworkProcessor instance = new UserNetworkProcessor(randomCacheBuilder.getCache(), randomCacheBuilder.getCacheDir());

        // populate a request
        UploadNetworkEngineRequestDto request = new UploadNetworkEngineRequestDto();
        request.setLayout(DataLayout.BINARY_NETWORK);
        request.setMethod(NetworkProcessingMethod.DIRECT);
        request.setNamespace(user);
        request.setProgressReporter(NullProgressReporter.instance());
        request.setOrganismId(1);
        request.setNetworkId(-1);

        String data = 	"10000\t10002\n" +
			"10001\t10003\n" +
			"10002\t10000\n" +
			"10004\t10007\n" +
			"10003\t10008\n";
        
        request.setData(new StringReader(data));

        // process load request
        UploadNetworkEngineResponseDto result = instance.process(request);

        assertNotNull(result);
        assertEquals(4, result.getNumInteractions());

        // should now be able to fetch the new network directly out of teh cache
        SymMatrix m = randomCacheBuilder.getCache().getNetwork(user, 1, -1).getData();
        assertNotNull(result);
        System.out.println("m: " + m);
    }

    @Test
    public void testProcessBinaryToSharedNeighbor() throws Exception {
        System.out.println("processBinaryToSharedNeighbor");
        String user = "user1";

//        INetworkSymMatrixProvider provider = new CacheNetworkSymMatrixProvider(user, 1, randomCacheBuilder.getCache());
        UserNetworkProcessor instance = new UserNetworkProcessor(randomCacheBuilder.getCache(), randomCacheBuilder.getCacheDir());

        // populate a request
        UploadNetworkEngineRequestDto request = new UploadNetworkEngineRequestDto();
        request.setLayout(DataLayout.BINARY_NETWORK);
        request.setMethod(NetworkProcessingMethod.LOG_FREQUENCY);
        request.setNamespace(user);
        request.setProgressReporter(NullProgressReporter.instance());
        request.setOrganismId(1);
        request.setNetworkId(-1);
        request.setSparsification(50);
        
        String data =   "10000\t10002\n" +
            "10001\t10003\n" +
            "10002\t10000\n" +
            "10004\t10007\n" +
            "10003\t10008\n";
        
        request.setData(new StringReader(data));

        // process load request
        UploadNetworkEngineResponseDto result = instance.process(request);

        assertNotNull(result);
        assertEquals(1, result.getNumInteractions());

        // should now be able to fetch the new network directly out of teh cache
        SymMatrix m = randomCacheBuilder.getCache().getNetwork(user, 1, -1).getData();
        assertNotNull(result);
        System.out.println("m: " + m);
    }

    @Test
    public void testProcessNetwork() throws Exception {
        System.out.println("processNetwork");
        String user = "user1";

//        INetworkSymMatrixProvider provider = new CacheNetworkSymMatrixProvider(user, 1, randomCacheBuilder.getCache());
        UserNetworkProcessor instance = new UserNetworkProcessor(randomCacheBuilder.getCache(), randomCacheBuilder.getCacheDir());

        // populate a request
        UploadNetworkEngineRequestDto request = new UploadNetworkEngineRequestDto();
        request.setLayout(DataLayout.WEIGHTED_NETWORK);
        request.setMethod(NetworkProcessingMethod.DIRECT);
        request.setNamespace(user);
        request.setProgressReporter(NullProgressReporter.instance());
        request.setOrganismId(1);
        request.setNetworkId(-2);
        request.setSparsification(50);

        String data = "10000\t10001\t1.0\n10000\t10002\t0.8\n";
        request.setData(new StringReader(data));

        // process load request
        UploadNetworkEngineResponseDto result = instance.process(request);

        assertNotNull(result);
        assertEquals(2, result.getNumInteractions());

        // should now be able to fetch the new network directly out of teh cache
        SymMatrix m = randomCacheBuilder.getCache().getNetwork(user, 1, -2).getData();
        assertNotNull(result);
        System.out.println("m: " + m);
    }

    @Test
    public void testProcessNetworkDuplicates() throws Exception {
        System.out.println("processNetwork");
        String user = "user1";

//        INetworkSymMatrixProvider provider = new CacheNetworkSymMatrixProvider(user, 1, randomCacheBuilder.getCache());
        UserNetworkProcessor instance = new UserNetworkProcessor(randomCacheBuilder.getCache(), randomCacheBuilder.getCacheDir());

        // populate a request
        UploadNetworkEngineRequestDto request = new UploadNetworkEngineRequestDto();
        request.setLayout(DataLayout.WEIGHTED_NETWORK);
        request.setMethod(NetworkProcessingMethod.DIRECT);
        request.setNamespace(user);
        request.setProgressReporter(NullProgressReporter.instance());
        request.setOrganismId(1);
        request.setNetworkId(-2);
        request.setSparsification(50);

        // input data contains a symmetric interaction for one of the input points
        String data = "10000\t10001\t1.0\n10000\t10002\t0.8\n10001\t10000\t1.0\n";
        request.setData(new StringReader(data));

        // process load request
        UploadNetworkEngineResponseDto result = instance.process(request);

        assertNotNull(result);
        assertEquals(2, result.getNumInteractions());

        // should now be able to fetch the new network directly out of the cache
        SymMatrix m = randomCacheBuilder.getCache().getNetwork(user, 1, -2).getData();
        assertNotNull(result);
        System.out.println("m: " + m);
    }

    @Test
    public void testProcessNetworkNoNewlineAtEOF() throws Exception {
        System.out.println("processNetwork");
        String user = "user1";

//        INetworkSymMatrixProvider provider = new CacheNetworkSymMatrixProvider(user, 1, randomCacheBuilder.getCache());
        UserNetworkProcessor instance = new UserNetworkProcessor(randomCacheBuilder.getCache(), randomCacheBuilder.getCacheDir());

        // populate a request
        UploadNetworkEngineRequestDto request = new UploadNetworkEngineRequestDto();
        request.setLayout(DataLayout.WEIGHTED_NETWORK);
        request.setMethod(NetworkProcessingMethod.DIRECT);
        request.setNamespace(user);
        request.setProgressReporter(NullProgressReporter.instance());
        request.setOrganismId(1);
        request.setNetworkId(-2);
        request.setSparsification(50);

        // no newline at end of file, should still get both interactions
        String data = "10000\t10001\t1.0\n10000\t10002\t0.8";
        request.setData(new StringReader(data));

        // process load request
        UploadNetworkEngineResponseDto result = instance.process(request);

        assertNotNull(result);
        assertEquals(2, result.getNumInteractions());

        // should now be able to fetch the new network directly out of the cache
        SymMatrix m = randomCacheBuilder.getCache().getNetwork(user, 1, -2).getData();
        assertNotNull(result);
        System.out.println("m: " + m);
    }
    
    /**
     * Test of convertNetwork method, of class UserNetworkProcessor.
     */
    @Test
    public void testConvertNetwork() throws Exception {
        System.out.println("convertNetwork");
        
//        INetworkSymMatrixProvider provider = new CacheNetworkSymMatrixProvider(1, randomCacheBuilder.getCache());
        UserNetworkProcessor instance = new UserNetworkProcessor(randomCacheBuilder.getCache(), randomCacheBuilder.getCacheDir());

        // populate a request
        UploadNetworkEngineRequestDto request = new UploadNetworkEngineRequestDto();
        request.setLayout(DataLayout.PROFILE);
        request.setMethod(NetworkProcessingMethod.PEARSON);
        request.setNamespace("user1");
        request.setProgressReporter(NullProgressReporter.instance());
        request.setOrganismId(1);
        request.setNetworkId(-1);
        request.setSparsification(50);
        
        String data = "id\tf1\tf2\tf3\n10000\t1.5\t3.0\t4.0\n10001\t2.0\t2.2\t2.1\n";
        request.setData(new StringReader(data));

        // process & validate
        SymMatrix result = instance.convertNetwork(request);
        assertNotNull(result);

    }

    /**
     * Test of computeStats method, of class UserNetworkProcessor.
     */
    @Test
    public void testComputeStats() {
        System.out.println("computeStats");

        SymMatrix network = Config.instance().getMatrixFactory().symSparseMatrix(5);
        System.out.println("type of SymMatrix class: " + network.getClass().getName());
        network.set(1, 2, 1.5);
        //network.set(2, 1, 1.5);
        network.set(2, 3, .8);
        //network.set(3, 2, .8);
        
        UploadNetworkEngineResponseDto result = UserNetworkProcessor.computeStats(network);
        assertNotNull(result);
        assertEquals( 2, result.getNumInteractions());
        assertEquals( .8, result.getMinValue(), 10e-8);
        assertEquals(1.5, result.getMaxValue(), 10e-8);
    }

    /**
     * add a user-network to data set, then compute
     * a result using average combining
     */
    @Test
    public void testAverageCombiningWithUserNetwork() throws Exception {
        System.out.println("averageCombiningWithUserNetworkChaChaCha");
        long orgId = 1;
        long userNetworkId = -1;
        String userName = "user1";
        
        // add a user network
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
        relatedRequest.setCombiningMethod(CombiningMethod.AVERAGE);
        relatedRequest.setScoringMethod(ScoringMethod.DISCRIMINANT);

        long [] nodeIds = randomCacheBuilder.getCache().getNodeIds(orgId).getNodeIds();
        
        // first 10 nodes as +ve
        ArrayList<Long> posNodes = new ArrayList<Long>();
        for (int i = 0; i < 10; i++) {
            posNodes.add(nodeIds[i]);
        }
        relatedRequest.setPositiveNodes(posNodes);

        // all networks
        Collection<Collection<Long>> requestNetworks = new ArrayList<Collection<Long>>();

        ArrayList<Long> group = new ArrayList<Long>();
        for (int i = 0; i < config.getOrg1NetworkIds().length; i++) {
            group.add(config.getOrg1NetworkIds()[i]);
        }

        group.add((long) userNetworkId);
        
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
    }
    
    /**
     * add a user-network to data set, then compute
     * a result using auto combining, including 
     * core attributes in the request parameters
     */
    @Test
    public void testAutoCombiningWithUserNetworkAndAttributes() throws Exception {
        long orgId = 1;
        long userNetworkId = -1;
        String userName = "user1";
        
        // add a user network
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
        relatedRequest.setCombiningMethod(CombiningMethod.AVERAGE);
        relatedRequest.setScoringMethod(ScoringMethod.DISCRIMINANT);

        long [] nodeIds = randomCacheBuilder.getCache().getNodeIds(orgId).getNodeIds();
        
        // first 10 nodes as +ve
        ArrayList<Long> posNodes = new ArrayList<Long>();
        for (int i = 0; i < 10; i++) {
            posNodes.add(nodeIds[i]);
        }
        relatedRequest.setPositiveNodes(posNodes);

        // all networks
        Collection<Collection<Long>> requestNetworks = new ArrayList<Collection<Long>>();

        ArrayList<Long> group = new ArrayList<Long>();
        for (int i = 0; i < config.getOrg1NetworkIds().length; i++) {
            group.add(config.getOrg1NetworkIds()[i]);
        }

        group.add((long) userNetworkId);
        
        requestNetworks.add(group);
        relatedRequest.setInteractionNetworks(requestNetworks);
        relatedRequest.setLimitResults(10);
        
        // add attributes
        ArrayList<Long> attributeGroups = new ArrayList<Long>();
        for (long groupId: config.getAttributeGroupId()) {
            attributeGroups.add(groupId);
        }
        relatedRequest.setAttributeGroups(attributeGroups);
        relatedRequest.setAttributesLimit(10);
        
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
    }    
}