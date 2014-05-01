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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.genemania.engine.cache;

import org.genemania.engine.core.data.Data;
import org.genemania.engine.core.data.Network;
import org.genemania.engine.matricks.custom.FlexSymDoubleMatrix;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 */
public class DataCacheTest {

    public DataCacheTest() {
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
     * Test of putNetwork method, of class DataCache.
     */
    @Test
    public void testPutGetNetwork() throws Exception {
        System.out.println("putNetwork");
        Network network = new Network(Data.CORE, 1, 2);
        network.setData(new FlexSymDoubleMatrix(5));
        DataCache cache = new DataCache(new FileSerializedObjectCache("target/"));
        cache.putNetwork(network);

        Network network2 = cache.getNetwork(Data.CORE, 1, 2);
        assertNotNull(network2);
        assertEquals(1L, network2.getOrganismId());
        assertEquals(2L, network2.getId());
        assertEquals(5, network2.getData().numRows());
        assertEquals(5, network2.getData().numCols());
    }
}