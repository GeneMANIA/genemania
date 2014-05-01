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

package org.genemania.engine.Data;

import org.genemania.engine.core.data.Data;
import org.genemania.engine.core.data.DataSupport;
import org.genemania.engine.core.data.KtK;
import org.genemania.engine.core.data.KtT;
import org.genemania.engine.core.data.NetworkIds;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 */
public class DataTest {

    public DataTest() {
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

    /*
     * a rew of our data objects should be marked volatile if they
     * appear in the non-core namespace.
     */
    @Test
    public void testIsVolatile() {

        Data data = new NetworkIds(Data.CORE, 1);
        assertFalse(DataSupport.isVolatile(data));

        data = new NetworkIds("user1", 1);
        assertTrue(DataSupport.isVolatile(data));

        data = new KtK("user1", 1, "some_branch");
        assertTrue(DataSupport.isVolatile(data));

        data = new KtT("user1", 1, "some_branch");
        assertTrue(DataSupport.isVolatile(data));
    }

}