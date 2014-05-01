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
import no.uib.cipr.matrix.Matrix;
import org.genemania.dto.ListNetworksEngineRequestDto;
import org.genemania.dto.ListNetworksEngineResponseDto;
import org.genemania.dto.RemoveNetworkEngineRequestDto;
import org.genemania.dto.RemoveNetworkEngineResponseDto;
import org.genemania.dto.UploadNetworkEngineRequestDto;
import org.genemania.dto.UploadNetworkEngineResponseDto;
import org.genemania.engine.actions.support.UserNetworkProcessor;
import org.genemania.engine.cache.NetworkMemCache;
import org.genemania.engine.cache.RandomDataCacheBuilder;
import org.genemania.engine.core.data.Data;
import org.genemania.engine.core.data.NetworkIds;
import org.genemania.engine.matricks.SymMatrix;
import org.genemania.exception.ApplicationException;
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
public class Mania2ListRemoveTest {

    RandomDataCacheBuilder randomCacheBuilder = new RandomDataCacheBuilder(2112);
    long [] networkIds;
    long [] networkIds2;
    
    public Mania2ListRemoveTest() {
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
        networkIds = randomCacheBuilder.addOrganism(1, 50, 5, .10, 3, .5);
        networkIds2 = randomCacheBuilder.addOrganism(2, 20, 5, .10, 3, .5);

        NetworkMemCache.instance().clear();
    }

    @After
    public void tearDown() {
        randomCacheBuilder.tearDown();
    }

    @Test (expected=org.genemania.exception.ApplicationException.class)
    public void testNoNamespace() throws Exception {
        String namespace = "user1";
        int organismId = 1;
        int networkId = -1;

        IMania mania = new Mania2(randomCacheBuilder.getCache());
        RemoveNetworkEngineRequestDto request = new RemoveNetworkEngineRequestDto();
        RemoveNetworkEngineResponseDto response = mania.removeUserNetworks(request);
    }

    /*
     * remove all networks for all organisms for user
     */
    @Test
    public void testRemoveEntireNamespace() throws Exception {
        String namespace = "user1";
        int organismId = 1;
        int networkId = -1;
        addUserNetworkHelper(namespace, organismId, networkId);

        // the network should now be there
        SymMatrix m = randomCacheBuilder.getCache().getNetwork(namespace, organismId, networkId).getData();
        assertNotNull(m);

        // delete it
        IMania mania = new Mania2(randomCacheBuilder.getCache());
        RemoveNetworkEngineRequestDto request = new RemoveNetworkEngineRequestDto();
        request.setNamespace(namespace);
        RemoveNetworkEngineResponseDto response = mania.removeUserNetworks(request);
        assertNotNull(response);

        // retrieving from cache should now fail
        boolean ok = false;
        try {
            SymMatrix m2 = randomCacheBuilder.getCache().getNetwork(namespace, organismId, networkId).getData();
        }
        catch (ApplicationException e) {
            ok = true;
        }
        assertTrue(ok);
    }

    @Test
    public void testRemoveCoreOrganism() throws Exception {
        String namespace = Data.CORE;
        int organismId = 1;
        int organism2Id = 2;

        // should be able to get an object for both organism 1 and 2
        NetworkIds nids1 = randomCacheBuilder.getCache().getNetworkIds(namespace, organismId);
        assertNotNull(nids1);

        NetworkIds nids2 = randomCacheBuilder.getCache().getNetworkIds(namespace, organism2Id);
        assertNotNull(nids2);

        // remove organism 1 namespace
        IMania mania = new Mania2(randomCacheBuilder.getCache());
        RemoveNetworkEngineRequestDto request = new RemoveNetworkEngineRequestDto();
        request.setNamespace(namespace);
        request.setOrganismId(organismId);

        RemoveNetworkEngineResponseDto response = mania.removeUserNetworks(request);
        assertNotNull(response);


        // try to fetch again from organsim1, should fail
        try {
            randomCacheBuilder.getCache().getNetworkIds(namespace, organismId);
            fail();
        }
        catch (ApplicationException e) {
            // good, we expect an exception here
        }

        // but organism2 should still be ok
        try {
            NetworkIds nids2try2 = randomCacheBuilder.getCache().getNetworkIds(namespace, organism2Id);
            assertNotNull(nids2try2);
        }
        catch (ApplicationException e) {
            fail("organism 2 should not have been affected");
        }
    }

    @Test
    public void testRemoveUserOrganism() throws Exception {
        String namespace = "user1";
        int organismId = 1;
        int networkId = -1;
        addUserNetworkHelper(namespace, organismId, networkId);

        IMania mania = new Mania2(randomCacheBuilder.getCache());
        RemoveNetworkEngineRequestDto request = new RemoveNetworkEngineRequestDto();
        request.setNamespace(namespace);
        request.setOrganismId(organismId);

        RemoveNetworkEngineResponseDto response = mania.removeUserNetworks(request);
        assertNotNull(response);

        // check the cache is clean
        try {
            randomCacheBuilder.getCache().getNetwork(namespace, organismId, networkId);
            fail();
        }
        catch (ApplicationException e) {
        }

        try {
            randomCacheBuilder.getCache().getNetworkIds(namespace, organismId);
            fail();
        }
        catch (ApplicationException e) {
        }

        try {
            randomCacheBuilder.getCache().getKtK(namespace, organismId, Constants.DataFileNames.KtK_BASIC.getCode());
            fail();
        }
        catch (ApplicationException e) {
        }
 

        for (int branch = 0; branch > Constants.goBranches.length; branch++) {
            try {
                randomCacheBuilder.getCache().getKtK(namespace, organismId, Constants.goBranches[branch]);
                fail();
            }
            catch (ApplicationException e) {
            }
            try {
                randomCacheBuilder.getCache().getKtT(namespace, organismId, Constants.goBranches[branch]);
                fail();
            }
            catch (ApplicationException e) {
            }
        }

        

    }



    @Test
    public void testRemoveUserNetwork() throws Exception {
        String namespace = "user1";
        int organismId = 1;
        int networkId = -1;

        Matrix before = randomCacheBuilder.getCache().getKtK(Data.CORE, organismId, Constants.DataFileNames.KtK_BASIC.getCode()).getData();

        addUserNetworkHelper(namespace, organismId, networkId);
        Matrix afterAdd = randomCacheBuilder.getCache().getKtK(namespace, organismId, Constants.DataFileNames.KtK_BASIC.getCode()).getData();

        IMania mania = new Mania2(randomCacheBuilder.getCache());

        // performing a list query should give the network
        ListNetworksEngineRequestDto listRequest = new ListNetworksEngineRequestDto();
        listRequest.setNamespace(namespace);
        listRequest.setOrganismId(organismId);
        ListNetworksEngineResponseDto listResponse = mania.listNetworks(listRequest);
        assertNotNull(listResponse);
        assertEquals(1, listResponse.getNetworkIds().size());
        assertEquals((long)networkId, (long)listResponse.getNetworkIds().iterator().next());

        RemoveNetworkEngineRequestDto request = new RemoveNetworkEngineRequestDto();
        request.setNamespace(namespace);
        request.setOrganismId(organismId);
        request.setNetworkId(networkId);
        
        RemoveNetworkEngineResponseDto response = mania.removeUserNetworks(request);
        assertNotNull(response);

        // retrieving from cache should give an error
        boolean ok = false;
        try {
            randomCacheBuilder.getCache().getNetwork(namespace, organismId, networkId).getData();
        }
        catch (ApplicationException e) {
            ok = true;
        }
        assertTrue(ok);

        // performing a list query should not yield the network
        listRequest = new ListNetworksEngineRequestDto();
        listRequest.setNamespace(namespace);
        listRequest.setOrganismId(organismId);
        listResponse = mania.listNetworks(listRequest);
        assertNotNull(listResponse);
        assertEquals(0, listResponse.getNetworkIds().size());

        Matrix after = randomCacheBuilder.getCache().getKtK(namespace, organismId, Constants.DataFileNames.KtK_BASIC.getCode()).getData();

        System.out.println("before: " + before);
        System.out.println("after add: " + afterAdd);
        System.out.println("after: " + after);
        Utils.elementWiseCompare(before, after, 10e-10d);

        // check network map cleanup
        NetworkIds coreNetworkIds = randomCacheBuilder.getCache().getNetworkIds(Data.CORE, organismId);
        NetworkIds userNetworkIds = randomCacheBuilder.getCache().getNetworkIds(namespace, organismId);
        assertEquals(coreNetworkIds.getNetworkIds().length, userNetworkIds.getNetworkIds().length);

        for (int coreIndex=0; coreIndex<coreNetworkIds.getNetworkIds().length; coreIndex++) {
            long netId = coreNetworkIds.getNetworkIds()[coreIndex];
            int userIndex = userNetworkIds.getIndexForId(netId);
            assertEquals(coreIndex, userIndex);

        }

        // check KtT cleanup
        for (int branch=0; branch<Constants.goBranches.length; branch++) {
            Matrix origKtT = randomCacheBuilder.getCache().getKtT(Data.CORE, organismId, Constants.goBranches[branch]).getData();
            Matrix newKtT = randomCacheBuilder.getCache().getKtT(namespace, organismId, Constants.goBranches[branch]).getData();
            assertEquals(origKtT.numRows(), newKtT.numRows());
            assertEquals(1, newKtT.numColumns());
            // subsetindices should be the same as above
            Utils.elementWiseCompare(origKtT, newKtT, 10e-10);
        }        
    }
    
    /*
     * add a user network to the test dataset. 
     */
    public void addUserNetworkHelper(String user, int organismId, int networkId) throws ApplicationException {

        UserNetworkProcessor instance = new UserNetworkProcessor(randomCacheBuilder.getCache(), randomCacheBuilder.getCacheDir());

        // populate a request
        UploadNetworkEngineRequestDto request = new UploadNetworkEngineRequestDto();
        request.setLayout(DataLayout.PROFILE);
        request.setMethod(NetworkProcessingMethod.PEARSON);
        request.setNamespace(user);
        request.setProgressReporter(NullProgressReporter.instance());
        request.setOrganismId(organismId);
        request.setNetworkId(networkId);
        request.setSparsification(50);

        String data = "id\tf1\tf2\tf3\n10000\t1.5\t3.0\t4.0\n10001\t2.0\t2.2\t2.1\n";
        request.setData(new StringReader(data));

        // process load request
        UploadNetworkEngineResponseDto result = instance.process(request);

        assertNotNull(result);        
    }

}