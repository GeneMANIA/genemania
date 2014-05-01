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

import java.util.HashMap;
import java.util.Map;
import no.uib.cipr.matrix.Matrices;
import no.uib.cipr.matrix.Matrix;

import org.genemania.engine.Constants;
import org.genemania.engine.Utils;
import org.genemania.engine.actions.support.UserDataPrecomputer;
import org.genemania.engine.cache.NetworkMemCache;
import org.genemania.engine.cache.RandomDataCacheBuilder;
import org.genemania.engine.cache.RandomDataCacheConfig;
import org.genemania.engine.core.data.Data;
import org.genemania.engine.core.data.Network;
import org.genemania.engine.core.data.NetworkIds;
import org.genemania.engine.matricks.SymMatrix;
import org.genemania.exception.ApplicationException;
import org.genemania.util.NullProgressReporter;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class UserDataPrecomputerTest {

    RandomDataCacheBuilder cacheBuilder;
    RandomDataCacheConfig config = RandomDataCacheConfig.getStandardConfig2();

    public UserDataPrecomputerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws ApplicationException {
        cacheBuilder = new RandomDataCacheBuilder(config.getSeed());
        cacheBuilder.setUp();

        cacheBuilder.addOrganism(config);

        NetworkMemCache.instance().clear();
    }

    @After
    public void tearDown() {
        cacheBuilder.tearDown();
    }

    /**
     * Test of checkNetwork method, of class UserDataSet.
     */
    @Test (expected=ApplicationException.class)
    public void testCheckNetworkInvalidId() throws Exception {
        System.out.println("checkNetworkInvalidId");
        String namespace = "user1";
        int organismId = 1;
        int id = 1;
        Map<Integer, Integer> userColumnMap = new HashMap<Integer, Integer>();
        userColumnMap.put(-1, 0);
        userColumnMap.put(-2, 1);
        userColumnMap.put(-5, 2);
        UserDataPrecomputer instance = new UserDataPrecomputer(namespace, organismId, cacheBuilder.getCache(), NullProgressReporter.instance());

        instance.checkNetwork(id);
    }

    @Test (expected=ApplicationException.class)
    public void testCheckDuplicateId() throws Exception {
        System.out.println("checkNetworkDuplicateId");
        String namespace = "user1";
        int organismId = 1;
        long id1 = config.getOrg1NetworkIds()[0];
        UserDataPrecomputer instance = null;

        try {
            instance = new UserDataPrecomputer(namespace, organismId, cacheBuilder.getCache(), NullProgressReporter.instance());

            // pull out one of the existing networks, and add back in as a user network
            System.out.println("copying network " + id1);
            SymMatrix userNetwork1 = cacheBuilder.getCache().getNetwork(Data.CORE, organismId, id1).getData();
            int userNetworkId1 = -1;

            Network userNetworkObj1 = new Network(namespace, organismId, userNetworkId1);
            userNetworkObj1.setData(userNetwork1);
            cacheBuilder.getCache().putNetwork(userNetworkObj1);

            instance.addNetwork(userNetworkId1, userNetwork1);
        }
        catch (ApplicationException e) {
            fail("didn't expect an exception at this point!");
        }

        // now the check should fail;
        instance.checkNetwork(id1);
    }

    @Test
    public void testCheckAllOK() throws Exception {
        System.out.println("checkNetworkAllOK");
        String namespace = "user1";
        int organismId = 1;
        int id = -6;
        Map<Integer, Integer> userColumnMap = new HashMap<Integer, Integer>();
        userColumnMap.put(-1, 0);
        userColumnMap.put(-2, 1);
        userColumnMap.put(-5, 2);
        UserDataPrecomputer instance = new UserDataPrecomputer(namespace, organismId, cacheBuilder.getCache(), NullProgressReporter.instance());
        instance.load();
        instance.checkNetwork(id);
    }

    /**
     * Test of load method, of class UserDataSet.
     */
    @Test
    public void testLoad() throws Exception {
        System.out.println("load");
        String namespace = "user1";
        int organismId = 1;
        UserDataPrecomputer instance = new UserDataPrecomputer(namespace, organismId, cacheBuilder.getCache(), NullProgressReporter.instance());

        instance.load();
        assertNotNull(instance.userKtK);
        assertNotNull(instance.userKtT);
        assertEquals(3, instance.userKtT.length);
        assertNotNull(instance.userKtT[0]);
        assertNotNull(instance.userKtT[1]);
        assertNotNull(instance.userKtT[2]);
    }

    /**
     * Test of loadInit method, of class UserDataSet.
     */
    @Test
    public void testLoadInit() throws Exception {
        System.out.println("loadInit");
        String namespace = "user1";
        int organismId = 1;
        UserDataPrecomputer instance = new UserDataPrecomputer(namespace, organismId, cacheBuilder.getCache(), NullProgressReporter.instance());

        instance.loadInit();
        assertNotNull(instance.userKtK);
        assertNotNull(instance.userKtT);
        assertEquals(3, instance.userKtT.length);
        assertNotNull(instance.userKtT[0]);
        assertNotNull(instance.userKtT[1]);
        assertNotNull(instance.userKtT[2]);
    }

    @Test
    public void testAddNetwork() throws Exception {
        System.out.println("addNetwork");
        String namespace = "user1";
        int organismId = 1;
        UserDataPrecomputer instance = new UserDataPrecomputer(namespace, organismId, cacheBuilder.getCache(), NullProgressReporter.instance());
        
        // pull out one of the existing networks, and add back in as a user network
        long id = config.getOrg1NetworkIds()[0];
        System.out.println("copying network " + id);
        SymMatrix userNetwork = cacheBuilder.getCache().getNetwork(Data.CORE, organismId, id).getData();
        int userNetworkId = -1;

        Network userNetworkObj = new Network(namespace, organismId, userNetworkId);
        userNetworkObj.setData(userNetwork);
        cacheBuilder.getCache().putNetwork(userNetworkObj);

        instance.addNetwork(userNetworkId, userNetwork);

        // check the column id map
        NetworkIds coreNetworkIds = cacheBuilder.getCache().getNetworkIds(Data.CORE, organismId);
        NetworkIds userNetworkIds = cacheBuilder.getCache().getNetworkIds(namespace, organismId);
        
        assertEquals(coreNetworkIds.getNetworkIds().length+1, userNetworkIds.getNetworkIds().length);

        // the user network should have been assigned to the new row/col
        assertEquals(userNetworkIds.getNetworkIds().length-1, userNetworkIds.getIndexForId(userNetworkId));

        // all other elements should be the same
        for (int coreIndex =0; coreIndex<coreNetworkIds.getNetworkIds().length; coreIndex++) {
            long netId = coreNetworkIds.getNetworkIds()[coreIndex];
            
            int userIndex = userNetworkIds.getIndexForId(netId);
            assertEquals(coreIndex, userIndex);            
        }
        
        // check the user's precomputed KtK
        Matrix userKtK_BASIC = cacheBuilder.getCache().getKtK(namespace, organismId, Constants.DataFileNames.KtK_BASIC.getCode()).getData();
        assertNotNull(userKtK_BASIC);
        //System.out.println("user ktk basic: " + userKtK_BASIC);
        // compare with original KtK
        Matrix KtK_BASIC = cacheBuilder.getCache().getKtK(Data.CORE, organismId, Constants.DataFileNames.KtK_BASIC.getCode()).getData();

        //System.out.println("ktk basic: " + KtK_BASIC);

        // size should be one bigger
        assertEquals(KtK_BASIC.numRows()+1, userKtK_BASIC.numRows());
        assertEquals(KtK_BASIC.numColumns()+1, userKtK_BASIC.numColumns());

        // other than the last row and column the contents of the new
        // KtK should be the same as the orig
        int [] subsetIndices = new int[KtK_BASIC.numRows()];
        for (int i=0; i<subsetIndices.length; i++) {
            subsetIndices[i] = i;
        }

        Matrix subset = Matrices.getSubMatrix(userKtK_BASIC, subsetIndices, subsetIndices);
        //System.out.println("subset: " + subset);
        Utils.elementWiseCompare(KtK_BASIC, subset, 10e-10d);

        // the last row & col should match with the row & col corresponding to the copied network
//        int origIndex = userMap.get(id)+1;
        int origIndex = userNetworkIds.getIndexForId(id)+1;
//        int newIndex = userMap.get(userNetworkId)+1;
        int newIndex = userNetworkIds.getIndexForId(userNetworkId)+1;
        for (int i=0; i< userKtK_BASIC.numRows(); i++) {
            assertEquals(userKtK_BASIC.get(origIndex, i), userKtK_BASIC.get(newIndex, i), 10e-10);
            // and check symmetry of new data
            assertEquals(userKtK_BASIC.get(0, i), userKtK_BASIC.get(i, 0), 10e-10);
        }

        // check the user's precomputed KtT's
        for (int branch=0; branch<Constants.goBranches.length; branch++) {
//            Matrix origKtT = randomCacheBuilder.getCache().getMatrix(organismId, "Ktt_" + Constants.goBranches[branch]);
            Matrix origKtT = cacheBuilder.getCache().getKtT(Data.CORE, organismId, Constants.goBranches[branch]).getData();
//            Matrix newKtT = randomCacheBuilder.getCache().getUserMatrix(namespace, organismId, "Ktt_" + Constants.goBranches[branch]);
            Matrix newKtT = cacheBuilder.getCache().getKtT(namespace, organismId, Constants.goBranches[branch]).getData();
            assertEquals(origKtT.numRows()+1, newKtT.numRows());
            assertEquals(1, newKtT.numColumns());
            // subsetindices should be the same as above
            subset = Matrices.getSubMatrix(newKtT, subsetIndices, new int[] {0});
            Utils.elementWiseCompare(origKtT, subset, 10e-10);
            // new element should have been populated
            assertNotSame(0d, newKtT.get(newKtT.numRows()-1,0));
        }   
    }

    /*
     * adding a second user network is a bit different from the first one,
     * since the second one requires computations against other user networks
     * in addition to the core ones
     */
    @Test
    public void testAddTwoNetworks() throws Exception {
        System.out.println("addNetwork");
        String namespace = "user1";
        int organismId = 1;
        UserDataPrecomputer instance = new UserDataPrecomputer(namespace, organismId, cacheBuilder.getCache(), NullProgressReporter.instance());

        // pull out one of the existing networks, and add back in as a user network
        long id1 = config.getOrg1NetworkIds()[0];
        System.out.println("copying network " + id1);
        SymMatrix userNetwork1 = cacheBuilder.getCache().getNetwork(Data.CORE, organismId, id1).getData();
        int userNetworkId1 = -1;

        Network userNetworkObj1 = new Network(namespace, organismId, userNetworkId1);
        userNetworkObj1.setData(userNetwork1);
        cacheBuilder.getCache().putNetwork(userNetworkObj1);

        instance.addNetwork(userNetworkId1, userNetwork1);

        // and another one
        long id2 = config.getOrg1NetworkIds()[1];
        System.out.println("copying network " + id2);
        SymMatrix userNetwork2 = cacheBuilder.getCache().getNetwork(Data.CORE, organismId, id2).getData();
        int userNetworkId2 = -2;

        Network userNetworkObj2 = new Network(namespace, organismId, userNetworkId2);
        userNetworkObj2.setData(userNetwork2);
        cacheBuilder.getCache().putNetwork(userNetworkObj2);

        instance.addNetwork(userNetworkId2, userNetwork2);

        Matrix userKtK_BASIC = cacheBuilder.getCache().getKtK(namespace, organismId, Constants.DataFileNames.KtK_BASIC.getCode()).getData();
        Matrix KtK_BASIC = cacheBuilder.getCache().getKtK(Data.CORE, organismId, Constants.DataFileNames.KtK_BASIC.getCode()).getData();
        assertEquals(KtK_BASIC.numRows()+2, userKtK_BASIC.numRows());
        assertEquals(KtK_BASIC.numColumns()+2, userKtK_BASIC.numColumns());
        
    }
}