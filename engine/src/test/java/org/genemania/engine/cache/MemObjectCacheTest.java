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

package org.genemania.engine.cache;

import org.genemania.engine.core.data.Data;
import org.genemania.engine.core.data.NetworkIds;
import org.genemania.exception.ApplicationException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 */
public class MemObjectCacheTest {
    static RandomDataCacheBuilder cacheBuilder;
    // params for test organism
    static int org1Id = 1;
    static int org1numGenes = 50;
    static int org1numNetworks = 10;
    static double org1networkSparsity = .5;
    static int numCategories = 20;
    static double org1AnnotationSparsity = .5;

    static long[] org1networkIds;

    public MemObjectCacheTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        cacheBuilder = new RandomDataCacheBuilder(7132, true);
        cacheBuilder.setUp();

        // random organism 1
        org1networkIds = cacheBuilder.addOrganism(org1Id, org1numGenes, org1numNetworks,
                org1networkSparsity, numCategories, org1AnnotationSparsity);

    }

    @After
    public void tearDown() {
        cacheBuilder.tearDown();
    }

     @Test
     public void testVolatileObjects() throws Exception {

        FileSerializedObjectCache fileCache = new FileSerializedObjectCache(cacheBuilder.getCacheDir());
        MemObjectCache memCache = new MemObjectCache(fileCache);

        DataCache dcache = cacheBuilder.getCache();

        // fetch the core networkids object
        NetworkIds networkIds = dcache.getNetworkIds(Data.CORE, org1Id);
        assertNotNull(networkIds);

        int numNetworks = networkIds.getNetworkIds().length;

        // push a copy for user 1
        NetworkIds userNetworkIds = networkIds.copy("user1");
        dcache.putNetworkIds(userNetworkIds);


        // hoist a second datacache, this one not using the memcache layer
        // use it to push a a change to the user networks behind the back of the mem cache
        DataCache dcache2 = new DataCache(new FileSerializedObjectCache(cacheBuilder.getCacheDir()));

        long SILLY_NETWORK_TEST_VALUE = 55;
        userNetworkIds.setNetworkIds(new long[] {SILLY_NETWORK_TEST_VALUE});
        dcache2.putNetworkIds(userNetworkIds);

        // ok, now pulling the same object back through the mem-cache backed dcache should pick up the change
        NetworkIds userNetworkIds2 = dcache.getNetworkIds("user1", org1Id);
        assertNotNull(userNetworkIds2);
        assertEquals(1, userNetworkIds2.getNetworkIds().length);
        assertEquals(SILLY_NETWORK_TEST_VALUE, userNetworkIds2.getNetworkIds()[0]);

     }

     @Test
     public void testRemove() throws Exception {
        FileSerializedObjectCache fileCache = new FileSerializedObjectCache(cacheBuilder.getCacheDir());
        MemObjectCache memCache = new MemObjectCache(fileCache);

        DataCache dcache = cacheBuilder.getCache();

        // fetch the core networkids object
        NetworkIds networkIds = dcache.getNetworkIds(Data.CORE, org1Id);
        assertNotNull(networkIds);

        // remove it
        dcache.removeData(networkIds);

        // fetching it again should bomb
        try {
            NetworkIds networkIds2 = dcache.getNetworkIds(Data.CORE, org1Id);
            fail("removing object should have given error");
        }
        catch (ApplicationException e) {
            // ok
        }
                
     }
}