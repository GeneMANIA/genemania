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

import org.genemania.engine.cache.NetworkMemCache;
import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.Matrix;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author khalid
 */
public class NetworkMemCacheTest {

    public NetworkMemCacheTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getNetwork method, of class NetworkMemCache.
     */
    @Test
    public void testPutGetNetwork() {
        System.out.println("getNetwork");
        int organismId = 1;
        int networkId = 3;
        NetworkMemCache instance = NetworkMemCache.instance();
        instance.clear();
        
        Matrix expResult = null;

        Matrix result = instance.get(organismId, networkId);
        assertEquals(expResult, result);

        Matrix m = new DenseMatrix(5, 5);
        instance.put(organismId, networkId ,m);
        result = instance.get(organismId, networkId);
        assertEquals(m, result);
    }

    /**
     * Test of clear method, of class NetworkMemCache.
     */
    @Test
    public void testClear() {
        System.out.println("clear");
        int organismId = 1;
        int networkId = 3;
        NetworkMemCache instance = NetworkMemCache.instance();
        instance.clear();
        Matrix expResult = null;

        Matrix result = instance.get(organismId, networkId);
        assertEquals(expResult, result);

        Matrix m = new DenseMatrix(5, 5);
        instance.put(organismId, networkId ,m);
        result = instance.get(organismId, networkId);
        assertEquals(m, result);

        instance.clear();

        result = instance.get(organismId, networkId);
        assertEquals(null, result);
    }

    @Test
    public void testCompact() {
        System.out.println("compact");
        int organismId = 1;
        int networkId = 3;
        NetworkMemCache instance = NetworkMemCache.instance();
        instance.clear();
        Matrix expResult = null;

        Matrix result = instance.get(organismId, networkId);
        assertEquals(expResult, result);

        Matrix m = new DenseMatrix(5, 5);
        instance.put(organismId, networkId ,m);
        result = instance.get(organismId, networkId);
        assertEquals(m, result);

        instance.compact();

        result = instance.get(organismId, networkId);
        //assertEquals(null, result);
    }


}