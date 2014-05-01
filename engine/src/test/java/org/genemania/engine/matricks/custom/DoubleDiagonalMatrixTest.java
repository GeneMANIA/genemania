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

import org.genemania.engine.matricks.MatricksException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class DoubleDiagonalMatrixTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testDoubleDiagonalMatrixInt() {
        
        // create empty 10-by-10 diagonal matrix
        DoubleDiagonalMatrix matrix = new DoubleDiagonalMatrix(10);
       
        // test shape, get & set elements
        assertEquals(10, matrix.numRows());
        assertEquals(10, matrix.numCols());
        
        assertEquals(0, matrix.get(0, 0), 0d);

        matrix.set(0, 0, 1);
        assertEquals(1, matrix.get(0, 0), 0d);

        // off-diagonals are zero
        assertEquals(0, matrix.get(0, 1), 0d);
        
        // trying to change off-diagonal raises exception
        try {
            matrix.set(0, 1, 1);
            fail("expected exception when setting off-diagonal element of diagonal matrix");
        }
        catch (MatricksException e) {
            // ok!
        }
    }

    @Test
    public void testDoubleDiagonalMatrixDenseDoubleVector() {
        
        // initialize backing vector
        DenseDoubleVector vector = new DenseDoubleVector(10);
        for (int i=0; i<10; i++) {
            vector.set(i, i);
        }
        
        // create matrix
        DoubleDiagonalMatrix matrix = new DoubleDiagonalMatrix(vector);
        
        // test shape, get & set elements
        assertEquals(10, matrix.numRows());
        assertEquals(10, matrix.numCols());
        
        assertEquals(5, matrix.get(5, 5), 0d);

        matrix.set(0, 0, 1);
        assertEquals(1, matrix.get(0, 0), 0d);

        // off-diagonals are zero        
        assertEquals(0, matrix.get(0, 1), 0d);
        
        // trying to change off-diagonal raises exception
        try {
            matrix.set(0, 1, 1);
            fail("expected exception when setting off-diagonal element of diagonal matrix");
        }
        catch (MatricksException e) {
            // ok!
        }        
    }
    
    @Test
    public void testMultAdd() {
        int size = 10;
        double [] x = new double[size];
        double [] y = new double[size];
        
        // initialize backing vector
        DenseDoubleVector vector = new DenseDoubleVector(size);
        for (int i=0; i<size; i++) {
            vector.set(i, i);
            x[i] = i;
            y[i] = 2*i;
        }
                
        // create matrix
        DoubleDiagonalMatrix matrix = new DoubleDiagonalMatrix(vector);
        
        matrix.multAdd(x, y);
        
        for (int i=0; i<size; i++) {
            double expected = 2*i + i*i;
            assertEquals(expected, y[i], 1e-7d);
        }
    }
}
