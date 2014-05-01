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

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
/*
 * tests TODO:
 * 
 *   * entire matrix fits in buffer, so one pass only
 *   * one line of matrix is too large to fit in the given buffer,
 *     fail with useful message
 *   * values on diagonal. we don't use these, but for completeness
 *   
 */
public class FloatSymMatrixAccumulatorTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    /*
     * add a bunch of matrices containing '1'
     */
    @Test
    public void testAddFlexSymFloatMatrixDouble() {

        int l = 100;
        FlexSymFloatMatrix m = new FlexSymFloatMatrix(l);
        for (int i=0; i<l; i++) {
            for (int j=0; j<i; j++) {
                m.set(j, i, 1d);
            }
        }        

        FlexSymFloatMatrix sum = new FlexSymFloatMatrix(100);

        int bufSizeBytes = 200*4;

        FloatSymMatrixAccumulator adder = new FloatSymMatrixAccumulator(sum, bufSizeBytes);

        int k=3;
        while (adder.nextBlock()) {
            for (int i=1; i<=k; i++) {
                adder.add(2d, m); 
            }
        }

        double total = sum.elementSum(); 
        double expected = 2*k*(l*l - l);
        assertEquals(expected, total, 0d);
    }

    /*
     * poke a few random values into some matrices to make
     * sure the accumulator works over matrices with differing
     * sparsity structures
     */
    @Test
    public void testrandom() {
        for (int repeat = 0; repeat<10; repeat++) {
            long seed = System.currentTimeMillis();        
            Random random = new Random(seed);

            int l = 100; // size
            double sparsity = 0.2;
            int n = 10;  // # matrices
            FlexSymFloatMatrix[] dataset =  new FlexSymFloatMatrix[10];

            double expected_total = 0;
            for (int i=0; i<n; i++) {
                dataset[i] = randomData(random, l, sparsity);
                expected_total = expected_total + 2*dataset[i].elementSum();
            }


            FlexSymFloatMatrix sum = new FlexSymFloatMatrix(l);       
            int bufSizeBytes = 200*4;

            FloatSymMatrixAccumulator adder = new FloatSymMatrixAccumulator(sum, bufSizeBytes);

            while (adder.nextBlock()) {
                for (int i=0; i<n; i++) {
                    adder.add(2d, dataset[i]); 
                }
            }

            double total = sum.elementSum(); 
            assertEquals("seed "+seed, expected_total, total, 1e-6);
        }
    }


    FlexSymFloatMatrix randomData(Random random, int size, double sparsity) {

        FlexSymFloatMatrix matrix = new FlexSymFloatMatrix(size);

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < i; j++) {
                double x = random.nextDouble();
                if (x < sparsity) {
                    double weight = random.nextDouble();
                    weight = Math.ceil(weight*10.0);
                    matrix.set(i, j, weight);
                }
            }
        }

        return matrix;
    }
}


