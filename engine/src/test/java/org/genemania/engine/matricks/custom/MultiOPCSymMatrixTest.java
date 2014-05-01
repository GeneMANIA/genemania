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


import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class MultiOPCSymMatrixTest {
    FlexSymFloatMatrix c;
    FlexFloatMatrix a;
    DenseDoubleVector w;
    DenseDoubleVector ones;
    
    /* matlab
    c =

       0.00000   0.88715   0.67974   0.69090
       0.88715   0.00000   0.99739   0.70729
       0.67974   0.99739   0.00000   0.97997
       0.69090   0.70729   0.97997   0.00000
       
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
    */
    @Before
    public void setUp() throws Exception {
        c = new FlexSymFloatMatrix(4);
        c.set(0, 1, 0.88715);
        c.set(0, 2, 0.67974);
        c.set(0, 3, 0.69090);
        
        c.set(1, 2, 0.99739);
        c.set(1, 3, 0.70729);
       
        c.set(2, 3, 0.97997);
        
        // 6 attribute vectors 
        a = new FlexFloatMatrix(4, 6); 
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
        w = new DenseDoubleVector(6);
        w.set(0, 0.18519);
        w.set(1, 0.70168);
        w.set(2, 0.57522);
        w.set(3, 0.56964);
        w.set(4, 0.59336);
        w.set(5, 0.99504);
    
        // ones vector
        ones = new DenseDoubleVector(4);
        ones.set(0, 1);
        ones.set(1, 1);
        ones.set(2, 1);
        ones.set(3, 1);
    
    }
    
    @Test
    public void testMult() throws Exception {
        /* matlab
 
          result = (c + a * diag(w) * a') * x

            8.6538
            8.1370
           10.2398
            7.1902

        */
        
        // create & multiply
        DenseDoubleVector result = new DenseDoubleVector(4);
        OuterProductComboSymMatrix m = new OuterProductComboSymMatrix(a, w, false);
        
        MultiOPCSymMatrix m2 = new MultiOPCSymMatrix(c, m);
        m2.mult(ones, result);
        
        // test result       
        assertEquals(8.6538, result.get(0), 1e-4);
        assertEquals(8.1370, result.get(1), 1e-4);
        assertEquals(10.2398, result.get(2), 1e-4);
        assertEquals(7.1902, result.get(3), 1e-4);
    }
    
    @Test
    public void testGet() throws Exception {
        /* matlab
         
           result = c + a * diag(w) * a'
           
           format long
           result(2, 3)
           2.14855000000000
         */
        
        OuterProductComboSymMatrix m = new OuterProductComboSymMatrix(a, w, false);
        MultiOPCSymMatrix m2 = new MultiOPCSymMatrix(c, m);
        
        assertEquals(2.14855000000000, m2.get(2,3), 1e-5);
    }
}
