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
import java.util.Iterator;
import org.genemania.dto.ListNetworksEngineRequestDto;
import org.genemania.dto.ListNetworksEngineResponseDto;
import org.genemania.dto.UploadNetworkEngineRequestDto;
import org.genemania.dto.UploadNetworkEngineResponseDto;
import org.genemania.engine.actions.support.UserNetworkProcessor;
import org.genemania.engine.cache.NetworkMemCache;
import org.genemania.engine.cache.RandomDataCacheBuilder;
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
public class Mania2ListQueryTest {

    RandomDataCacheBuilder randomCacheBuilder = new RandomDataCacheBuilder(2112);
    long [] networkIds;
    
    public Mania2ListQueryTest() {
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
        ListNetworksEngineRequestDto request = new ListNetworksEngineRequestDto();
        ListNetworksEngineResponseDto response = mania.listNetworks(request);
    }

    @Test (expected=org.genemania.exception.ApplicationException.class)
    public void testNoOrganism() throws Exception {
        String namespace = "user1";
        int organismId = 1;
        int networkId = -1;
        addUserNetworkHelper(namespace, organismId, networkId);

        IMania mania = new Mania2(randomCacheBuilder.getCache());
        ListNetworksEngineRequestDto request = new ListNetworksEngineRequestDto();
        request.setNamespace(namespace);
        ListNetworksEngineResponseDto response = mania.listNetworks(request);
    }

    @Test
    public void testListUserNetwork() throws Exception {
        String namespace = "user1";
        int organismId = 1;
        int networkId = -1;
        addUserNetworkHelper(namespace, organismId, networkId);

        IMania mania = new Mania2(randomCacheBuilder.getCache());
        ListNetworksEngineRequestDto request = new ListNetworksEngineRequestDto();
        request.setNamespace(namespace);
        request.setOrganismId(organismId);

        ListNetworksEngineResponseDto response = mania.listNetworks(request);
        assertNotNull(response);
        assertNotNull(response.getNetworkIds());
        assertEquals(1, response.getNetworkIds().size());
        Iterator<Long> iter = response.getNetworkIds().iterator();
        assertEquals((long) networkId, (long) iter.next());
    }

    /*
     * get an exception when the use is unknown. 
     */
    @Test (expected=org.genemania.exception.ApplicationException.class)
    public void testListUserNetworkInvalidUser() throws Exception {
        String namespace = "user1";
        int organismId = 1;
        int networkId = -1;

        IMania mania = new Mania2(randomCacheBuilder.getCache());
        ListNetworksEngineRequestDto request = new ListNetworksEngineRequestDto();
        request.setNamespace(namespace);
        request.setOrganismId(organismId);

        ListNetworksEngineResponseDto response = mania.listNetworks(request);
    }
    
    @Test
    public void testMultipleUserNetworks() throws Exception{
        String namespace = "user1";
        int organismId = 1;
        int networkId1 = -1;
        int networkId2 = -2;
        addUserNetworkHelper(namespace, organismId, networkId1);
        addUserNetworkHelper(namespace, organismId, networkId2);

        IMania mania = new Mania2(randomCacheBuilder.getCache());
        ListNetworksEngineRequestDto request = new ListNetworksEngineRequestDto();
        request.setNamespace(namespace);
        request.setOrganismId(organismId);

        ListNetworksEngineResponseDto response = mania.listNetworks(request);
        assertNotNull(response);
        assertNotNull(response.getNetworkIds());
        assertEquals(2, response.getNetworkIds().size());       
    }
    
    /*
     * add a user network to the test dataset. 
     */
    public void addUserNetworkHelper(String user, int organismId, int networkId) throws ApplicationException {

//        INetworkMatrixProvider provider = new CacheNetworkMatrixProvider(user, 1, randomCacheBuilder.getCache());
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