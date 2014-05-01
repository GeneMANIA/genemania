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

import org.genemania.engine.matricks.MatricksException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 */
public class FlexSymDoubleMatrixTest {

    public FlexSymDoubleMatrixTest() {
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

    public FlexSymDoubleMatrix makeTestMatrix(int size) throws MatricksException {
        FlexSymDoubleMatrix m = new FlexSymDoubleMatrix(size);
        for (int i=0; i<size; i++) {
            for (int j=0; j<=i; j++) {
                m.set(i, j, Math.random());
            }
        }
        return m;
    }

    public double [] makeTestVectorData(int size) {
        double [] vec = new double[size];

        for (int i=0; i<size; i++) {
            vec[i] = Math.random();
        }

        return vec;
    }
    
    @Test
    public void testCreate() throws MatricksException {
        FlexSymDoubleMatrix m = makeTestMatrix(5);
    }

    @Test
    public void testMultAdd() throws MatricksException {
        int size = 5;
        FlexSymDoubleMatrix m = makeTestMatrix(size);
        double [] vec = makeTestVectorData(size);
        double [] result = new double[size];

        m.multAdd(1d, vec, result);

        System.out.println("out: " + result);
    }
}