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

import org.genemania.engine.matricks.Vector;
import static org.junit.Assert.*;

import java.util.Random;

import org.genemania.engine.matricks.Matrix;
import org.junit.Test;

public class OuterProductComboSymMatrixTest {

    @Test
    public void testOuterProductComboSymMatrix() { 
        /* matlab
        
        a =
         
            0   1   1   0   0   1
            0   1   1   1   0   0
            0   1   1   0   1   1
            1   0   1   1   1   0
         
         w =
         
            0.18519
            0.70168
            0.57522
            0.56964
            0.59336
            0.99504
         
         x = ones(4, 1);
         
         result = a * diag(w) * a';
         format long
         result(3,4)

            1.16858000000000
         
        */
  
        // 6 attribute vectors 
        FlexFloatMatrix a = new FlexFloatMatrix(4, 6); 
        a.set(0, 1, 1);
        a.set(0, 2, 1);
        a.set(0, 5, 1);
        
        a.set(1, 1, 1);
        a.set(1, 2, 1);
        a.set(1, 3, 1);
        
        a.set(2, 1, 1);
        a.set(2, 2, 1);
        a.set(2, 4, 1);
        a.set(2, 5, 1);
        
        a.set(3, 0, 1);
        a.set(3, 2, 1);
        a.set(3, 3, 1);
        a.set(3, 4, 1);
        
        // weights
        DenseDoubleVector w = new DenseDoubleVector(6);
        w.set(0, 0.18519);
        w.set(1, 0.70168);
        w.set(2, 0.57522);
        w.set(3, 0.56964);
        w.set(4, 0.59336);
        w.set(5, 0.99504);
    
        // ones vector
        DenseDoubleVector ones = new DenseDoubleVector(4);
        ones.set(0, 1);
        ones.set(1, 1);
        ones.set(2, 1);
        ones.set(3, 1);
        
        // create & multiply
        DenseDoubleVector result = new DenseDoubleVector(4);
        OuterProductComboSymMatrix matrix = new OuterProductComboSymMatrix(a, w, true);
        
        assertEquals(4, matrix.numRows());
        assertEquals(4, matrix.numCols());
        assertEquals(0, matrix.get(0, 0), 0d);
        assertEquals(1.16858, matrix.get(2, 3), 0d);
        assertEquals(1.16858, matrix.get(3, 2), 0d);
    }

    @Test
    public void testMult() {
        long seed = 12345;
        int numAttributes = 10;
        int numGenes = 50;
        double sparsity = 0.5;
        
        OuterProductComboSymMatrix matrix = new OuterProductComboSymMatrix(randomAttributeMatrix(numGenes, numAttributes, seed, sparsity), randomWeightVector(numAttributes, seed), false);

        DenseDoubleVector x = randomWeightVector(numGenes, seed+1);
        
        double [] y = new double[numGenes];
        
        // TODO: getting this field is smells evil
        matrix.mult(x.data, y);
        
        // TODO: could compute a few elements by hand and check, 
        // but need a more general way.
    }
    
    @Test
    public void testGetDiagAsVector() {
        long seed = 12345;
        int numAttributes = 10;
        int numGenes = 50;
        double sparsity = 0.5;
        
        OuterProductComboSymMatrix matrix = new OuterProductComboSymMatrix(randomAttributeMatrix(numGenes, numAttributes, seed, sparsity), randomWeightVector(numAttributes, seed), true);
        Vector diag = matrix.getDiagAsVector();
        assertNotNull(diag);
        
    }
    
    /*
     * build me a test data matrix
     */
    public static Matrix randomAttributeMatrix(int rows, int cols, long seed, double sparsity) {
        FlexFloatMatrix matrix = new FlexFloatMatrix(rows, cols);
        Random random = new Random(seed);
        
        for (int i=0; i<rows; i++) {
            for (int j=0; j<cols; j++) {
                if (random.nextDouble() > sparsity) {
                    matrix.set(i, j, 1);
                }
            }
        }
        
        return matrix;
    }
    
    /*
     * random weight vector
     */
    public static DenseDoubleVector randomWeightVector(int size, long seed) {
        DenseDoubleVector vector = new DenseDoubleVector(size);
        Random random = new Random(seed);
        
        for (int i=0; i<size; i++) {
            vector.set(i, random.nextDouble());
        }
        
        return vector;    
    }
     
    /*
     * comparison test vs reference matlab/octave code
     *
     */
    @Test
    public void testMultWithDiag() {
        /* matlab
        
        a =
         
            0   1   1   0   0   1
            0   1   1   1   0   0
            0   1   1   0   1   1
            1   0   1   1   1   0
         
         w =
         
            0.18519
            0.70168
            0.57522
            0.56964
            0.59336
            0.99504
         
         x = ones(4, 1);
         
         result = a * diag(w) * a' * x
         
            6.3960
            5.5452
            7.5827
            4.8121    
        */

        
        // 6 attribute vectors 
        FlexFloatMatrix a = new FlexFloatMatrix(4, 6); 
        a.set(0, 1, 1);
        a.set(0, 2, 1);
        a.set(0, 5, 1);
        
        a.set(1, 1, 1);
        a.set(1, 2, 1);
        a.set(1, 3, 1);
        
        a.set(2, 1, 1);
        a.set(2, 2, 1);
        a.set(2, 4, 1);
        a.set(2, 5, 1);
        
        a.set(3, 0, 1);
        a.set(3, 2, 1);
        a.set(3, 3, 1);
        a.set(3, 4, 1);
        
        // weights
        DenseDoubleVector w = new DenseDoubleVector(6);
        w.set(0, 0.18519);
        w.set(1, 0.70168);
        w.set(2, 0.57522);
        w.set(3, 0.56964);
        w.set(4, 0.59336);
        w.set(5, 0.99504);
    
        // ones vector
        DenseDoubleVector ones = new DenseDoubleVector(4);
        ones.set(0, 1);
        ones.set(1, 1);
        ones.set(2, 1);
        ones.set(3, 1);
        
        // create & multiply
        DenseDoubleVector result = new DenseDoubleVector(4);
        OuterProductComboSymMatrix m = new OuterProductComboSymMatrix(a, w, false);
        m.mult(ones, result);
        
        // test result       
        assertEquals(6.3960, result.get(0), 1e-4);
        assertEquals(5.5452, result.get(1), 1e-4);
        assertEquals(7.5827, result.get(2), 1e-4);
        assertEquals(4.8121, result.get(3), 1e-4);
    }
    
    // TODO: share common setup code
    @Test
    public void testMultZeroDiag() {
        /* matlab
        
        a =
         
            0   1   1   0   0   1
            0   1   1   1   0   0
            0   1   1   0   1   1
            1   0   1   1   1   0
         
         w =
         
            0.18519
            0.70168
            0.57522
            0.56964
            0.59336
            0.99504
         
         x = ones(4, 1);
         
         temp = a * diag(w) * a' * x
         
         result = (temp - diag(diag(temp)) * x
         
            4.1241
            3.6987
            4.7174
            2.8886
    
        */

        
        // 6 attribute vectors 
        FlexFloatMatrix a = new FlexFloatMatrix(4, 6); 
        a.set(0, 1, 1);
        a.set(0, 2, 1);
        a.set(0, 5, 1);
        
        a.set(1, 1, 1);
        a.set(1, 2, 1);
        a.set(1, 3, 1);
        
        a.set(2, 1, 1);
        a.set(2, 2, 1);
        a.set(2, 4, 1);
        a.set(2, 5, 1);
        
        a.set(3, 0, 1);
        a.set(3, 2, 1);
        a.set(3, 3, 1);
        a.set(3, 4, 1);
        
        // weights
        DenseDoubleVector w = new DenseDoubleVector(6);
        w.set(0, 0.18519);
        w.set(1, 0.70168);
        w.set(2, 0.57522);
        w.set(3, 0.56964);
        w.set(4, 0.59336);
        w.set(5, 0.99504);
    
        // ones vector
        DenseDoubleVector ones = new DenseDoubleVector(4);
        ones.set(0, 1);
        ones.set(1, 1);
        ones.set(2, 1);
        ones.set(3, 1);
        
        // create & multiply
        DenseDoubleVector result = new DenseDoubleVector(4);
        OuterProductComboSymMatrix m = new OuterProductComboSymMatrix(a, w, true);
        m.mult(ones, result);
        
        // test result       
        assertEquals(4.1241, result.get(0), 1e-4);
        assertEquals(3.6987, result.get(1), 1e-4);
        assertEquals(4.7174, result.get(2), 1e-4);
        assertEquals(2.8886, result.get(3), 1e-4);
    }
}
