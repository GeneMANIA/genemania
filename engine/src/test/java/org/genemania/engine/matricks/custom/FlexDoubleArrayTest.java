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

package org.genemania.engine.matricks.custom;

import org.genemania.engine.matricks.MatrixCursor;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 */
public class FlexDoubleArrayTest {

    public FlexDoubleArrayTest() {
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
     * Test of get method, of class FlexDoubleArray.
     */
    @Test
    public void testGet() {
        System.out.println("get");
        int index = 0;
        FlexDoubleArray instance = new FlexDoubleArray();
        double expResult = 0.0d;
        double result = instance.get(index);
        assertEquals(expResult, result, 0.0);
    }

    /**
     * Test of set method, of class FlexDoubleArray.
     */
    @Test
    public void testSet() throws Exception {
        System.out.println("set");

        int size = 10;
        double offset = 500.0d;

        double val = 1.0F;
        FlexDoubleArray instance = new FlexDoubleArray(size, 1);

        for (int i=0; i<size; i++) {
            instance.set(i, offset+i);
        }

        for (int i=0; i<size; i++) {
            assertEquals((double) offset+i, instance.get(i), 10e-10);
        }
    }

    /**
     * Test of cursor method, of class FlexDoubleArray.
     */
    @Test
    public void testCursor() throws Exception {
        System.out.println("cursor");
        FlexDoubleArray instance = new FlexDoubleArray(10);
        instance.set(3, 30d);
        instance.set(5, 50d);

        MatrixCursor result = instance.cursor();
        assertNotNull(result);

        assertTrue(result.next());
        assertEquals(0, result.col());
        assertEquals(3, result.row());
        assertEquals(30d, result.val(), 0.000001d);

        assertTrue(result.next());
        assertEquals(0, result.col());
        assertEquals(5, result.row());
        assertEquals(50d, result.val(), 0.000001d);

        assertFalse(result.next());
    }

    @Test
    public void testDot() throws Exception {
        FlexDoubleArray x = new FlexDoubleArray(10);
        x.set(0, 10);
        x.set(1, 11);
        x.set(3, 13);
        x.set(5, 15);
        x.set(9, 19);

        FlexDoubleArray y = new FlexDoubleArray(10);
        y.set(0, 10);
        y.set(1, 11);
        y.set(5, 15);
        y.set(8, 18);
        y.set(9, 19);

        double result = x.dot(y);
        assertEquals(807, result, 1e-10);        
    }   
}