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

import static org.junit.Assert.assertEquals;

import org.genemania.engine.matricks.custom.DenseDoubleVector;
import org.genemania.engine.matricks.custom.EyePlusLaplacianMatrix;
import org.genemania.engine.matricks.custom.FlexSymFloatMatrix;
import org.junit.Test;


public class EyePlusLaplacianMatrixTest {

    @Test
    public void testMult() throws Exception {
        /* matlab
        
        c =
        
           0.00000   0.88715   0.67974   0.69090
           0.88715   0.00000   0.99739   0.70729
           0.67974   0.99739   0.00000   0.97997
           0.69090   0.70729   0.97997   0.00000
        
        x =
        
           1
           1
           1
           1
        
        D = diag(sum(c, 2))
        
           2.25779   0.00000   0.00000   0.00000
           0.00000   2.59184   0.00000   0.00000
           0.00000   0.00000   2.65710   0.00000
           0.00000   0.00000   0.00000   2.37816
        
        L = D - c
        
           2.25779  -0.88715  -0.67974  -0.69090
          -0.88715   2.59184  -0.99739  -0.70729
          -0.67974  -0.99739   2.65710  -0.97997
          -0.69090  -0.70729  -0.97997   2.37816
        
        result = (eye(4) + L) * x
        
           1.00000
           1.00000
           1.00000
           1.00000
         
         */
        FlexSymFloatMatrix c = new FlexSymFloatMatrix(4);
        c.set(0, 1, 0.88715);
        c.set(0, 2, 0.67974);
        c.set(0, 3, 0.69090);
        
        c.set(1, 2, 0.99739);
        c.set(1, 3, 0.70729);
       
        c.set(2, 3, 0.97997);
 
        // ones vector
        DenseDoubleVector ones = new DenseDoubleVector(4);
        ones.set(0, 1);
        ones.set(1, 1);
        ones.set(2, 1);
        ones.set(3, 1);
        
        // create and multiply
        EyePlusLaplacianMatrix m = new EyePlusLaplacianMatrix(c);
        
        DenseDoubleVector result = new DenseDoubleVector(4);        
        m.mult(ones, result);
        
        // test result       
        assertEquals(1.0, result.get(0), 1e-4);
        assertEquals(1.0, result.get(1), 1e-4);
        assertEquals(1.0, result.get(2), 1e-4);
        assertEquals(1.0, result.get(3), 1e-4);        
    }
}
