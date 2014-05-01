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

package org.genemania.engine.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.Vector;

import org.genemania.engine.core.KHeap;

import org.junit.Test;
import static org.junit.Assert.*;

public class KHeapTest {

    @Test
    public void test1() {

        // TODO: add test with the same value repeated multiple times so not all unique

        int [] a = {5, 2, 3, 8, 4};
        double [] x = {5, 2, 3, 8, 4};

        KHeap q = new KHeap(3);
        for (int i=0; i<x.length; i++) {
            System.out.println("offering " + x[i]);
            q.offer(a[i], x[i]);
            q.dump();
        }

        assertEquals(3, q.size());

    }

    @Test
    public void testRand() {

        for (int trial = 0; trial < 50; trial++) {
            double [] x = new double[20000];
            for (int i=0; i<x.length; i++) {
                x[i] = Math.random();			
            }

            long t1 = System.nanoTime();		
            KHeap q = new KHeap(50);
            for (int i=0; i<x.length; i++) {
                q.offer(i, x[i]);
            }
            long t2 = System.nanoTime();

            System.out.println("=========");

            System.out.println("Insert duration (ns): " + (t2-t1) + " " + q.getId(0) + " " + q.getWeight(0));

            // sort a collection of objects
            Vector<Double> y = new Vector<Double>();
            for (int i=0; i<x.length; i++) {
                y.add(x[i]);
            }

            long t5 = System.nanoTime();
            Collections.sort(y);
            long t6 = System.nanoTime();
            System.out.println("Insert duration (ns): " + (t6-t5) + " " + y.get(y.size() - 50));

            // sort an array of primitives
            long t3 = System.nanoTime();
            Arrays.sort(x);
            long t4 = System.nanoTime();
            System.out.println("Insert duration (ns): " + (t4-t3) + " " + x[x.length - 50]);

        }
    }

    @Test
    public void testExtensible(){
        KHeap nonExtensible = new KHeap(3);
        for ( int i = 0; i < 5; i++ ){
            nonExtensible.offer(i, 1);
        }
        assertEquals(nonExtensible.size(), 3);

        nonExtensible.offer(6, 2);
        boolean hasTwo = false;
        for ( int i = 0; i < nonExtensible.size(); i++ ){
            if (nonExtensible.getId(i) == 2){
                hasTwo = true;
                break;
            }
        }
        assertEquals(hasTwo, true);


        KHeap extensible = new KHeap(3, true);
        for ( int i = 0; i < 5; i++ ){
            extensible.offer(i, 1);
        }

        assertEquals(extensible.size(), 5);
        for ( int i = 0; i < 5; i++ ){
            assertEquals(extensible.getId(i), i);
            assertEquals(extensible.getWeight(i), 1.0, 0d);
        }


        // note that when pushing a value larger than
        // the min when there is additionalData can result in the same value staying
        // at the top (because there could be more occurrences of the min value than
        // just the top and additionalData). this should get pushed onto the additional
        // data, not cleared.
        // 
        // did that make any sense? this is tested next 	     		
        extensible.offer(6, 2);
        assertEquals(extensible.size(), 6);
        hasTwo = false;
        for ( int i = 0; i < nonExtensible.size(); i++ ){
            if (nonExtensible.getId(i) == 2){
                hasTwo = true;
                break;
            }
        }
        assertEquals(hasTwo, true);
    }

    @Test
    public void testPop() {
        KHeap heap = new KHeap(3, true);
        for ( int i = 0; i < 5; i++ ){
            heap.offer(i, i);
        }

        // should have the values 3, 4, 5
        assertEquals(3, heap.size());
        assertEquals(2, heap.getId(0));
        assertEquals(2d, heap.getWeight(0), 0d);

        // pop one and checks
        boolean popped = heap.pop();
        assertTrue(popped);
        assertEquals(2, heap.size());
        assertEquals(3, heap.getId(0));
        assertEquals(3d, heap.getWeight(0), 0d);

        assertEquals(4d, heap.getWeight(1), 0d);
        assertEquals(4, heap.getId(1));

        // pop the next
        popped = heap.pop();
        assertTrue(popped);
        assertEquals(1, heap.size());
        assertEquals(4d, heap.getWeight(0), 0d);
        assertEquals(4, heap.getId(0));

        // pop the last element, empties the heap
        popped = heap.pop();
        assertTrue(popped);
        assertEquals(0, heap.size());

        // pop on the empty-heap, should get false
        popped = heap.pop();
        assertFalse(popped);
        assertEquals(0, heap.size());

        // now put the values 2 and 4 instances of 1 on the heap, 
        // the extension should keep all five values
        heap.offer(1, 1);
        heap.offer(2, 2);
        heap.offer(3, 1);
        heap.offer(4, 1);
        heap.offer(5, 1);

        assertEquals(5, heap.size());
        assertEquals(2, heap.getExtensionSize());

        // pop, should come off extension
        popped = heap.pop();
        assertTrue(popped);
        assertEquals(4, heap.size());
        assertEquals(1, heap.getExtensionSize());

        // pop some more
        popped = heap.pop();
        assertTrue(popped);
        assertEquals(3, heap.size());
        assertEquals(0, heap.getExtensionSize());

        popped = heap.pop();
        assertTrue(popped);
        assertEquals(2, heap.size());

        popped = heap.pop();
        assertTrue(popped);
        assertEquals(1, heap.size());

        // last value should be the 2
        assertEquals(2d, heap.getWeight(0), 0d);

    }

    @Test
    public void testPopLE() {
        KHeap heap = new KHeap(3, true);
        heap.offer(1, 1);
        heap.offer(2, 2);
        heap.offer(3, 1);
        heap.offer(4, 1);
        heap.offer(5, 0); // this one should get ignored

        assertEquals(4, heap.size());

        int numPopped = heap.popLE(0);
        assertEquals(0, numPopped);
        assertEquals(4, heap.size());

        // pop at threshold 1
        numPopped = heap.popLE(1d);
        assertEquals(3, numPopped);
        assertEquals(1, heap.size());

        // the 2 should be left
        assertEquals(2d, heap.getWeight(0), 0d);

    }
}
