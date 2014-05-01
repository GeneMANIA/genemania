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

package org.genemania.engine.matricks;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class UtilsTest {

    public UtilsTest() {
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
     * Test of binarySearch method, of class Utils.
     */
    @Test
    public void testBinarySearch() {
        System.out.println("binarySearch");
        int[] index = {10, 11, 12, 13, 14};
        int key = 12;
        int begin = 0;
        int end = 5;
        int expResult = 2;
        int result = Utils.binarySearch(index, key, begin, end);
        assertEquals(expResult, result);
    }

    @Test
    public void testBinaryLast() {
        System.out.println("binarySearch");
        int[] index = {10, 11, 12, 13, 14};
        int key = 14;
        int begin = 0;
        int end = 5;
        int expResult = 4;
        int result = Utils.binarySearch(index, key, begin, end);
        assertEquals(expResult, result);
    }

    @Test
    public void testBinarySearchNotFound() {
        System.out.println("binarySearch");
        int[] index = {10, 13, 15, 17, 19};
        int key = 14;
        int begin = 0;
        int end = 5;
        int expResult = -1;
        int result = Utils.binarySearch(index, key, begin, end);
        assertEquals(expResult, result);
    }



    @Test
    public void testMyBinarySearch() {
        System.out.println("binarySearch");
        int[] index = {10, 11, 12, 13, 14};
        int key = 12;
        int begin = 0;
        int end = 5;
        int expResult = 2;
        int result = Utils.myBinarySearch(index, key, begin, end);
        assertEquals(expResult, result);
    }

    @Test
    public void testMyBinaryLast() {
        System.out.println("binarySearch");
        int[] index = {10, 11, 12, 13, 14};
        int key = 14;
        int begin = 0;
        int end = 5;
        int expResult = 4;
        int result = Utils.myBinarySearch(index, key, begin, end);
        assertEquals(expResult, result);
    }

    @Test
    public void testMyBinarySearchNotFound() {
        System.out.println("binarySearch");
        int[] index = {10, 13, 15, 17, 19};
        int key = 16;
        int begin = 0;
        int end = 5;
        int expResult = -4;
        int result = Utils.myBinarySearch(index, key, begin, end);
        assertEquals(expResult, result);
    }

    @Test
    public void testMissingBeforeFirst() {
        System.out.println("binarySearch");
        int[] index = {10, 13, 15, 17, 19};
        int key = 8;
        int begin = 0;
        int end = 5;
        int expResult = -1;
        int result = Utils.myBinarySearch(index, key, begin, end);
        assertEquals(expResult, result);
    }

    @Test
    public void testMissingBeforeSecond() {
        System.out.println("binarySearch");
        int[] index = {10, 13, 15, 17, 19};
        int key = 11;
        int begin = 0;
        int end = 5;
        int expResult = -2;
        int result = Utils.myBinarySearch(index, key, begin, end);
        assertEquals(expResult, result);
    }

    @Test
    public void testMissingAfterLast() {
        System.out.println("binarySearch");
        int[] index = {10, 13, 15, 17, 19};
        int key = 25;
        int begin = 0;
        int end = 5;
        int expResult = -6;
        int result = Utils.myBinarySearch(index, key, begin, end);
        assertEquals(expResult, result);
    }

    @Test
    public void testMissingAfterSecondLast() {
        System.out.println("binarySearch");
        int[] index = {10, 13, 15, 17, 19};
        int key = 18;
        int begin = 0;
        int end = 5;
        int expResult = -5;
        int result = Utils.myBinarySearch(index, key, begin, end);
        assertEquals(expResult, result);
    }
}
