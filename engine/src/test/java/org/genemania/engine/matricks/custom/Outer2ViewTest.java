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
import org.junit.Test;


public class Outer2ViewTest {

    @Test
    public void test() throws Exception{
        FlexFloatColMatrix m = new FlexFloatColMatrix(4, 5);
        m.set(1, 0, 1);
        m.set(3, 0, 1);
        
        OuterProductFlexFloatSymMatrix m2 = new OuterProductFlexFloatSymMatrix(4, m.getColumn(0), 1, false);
        double sum = m2.elementSum();
        assertEquals(4d, sum, 0d);
        
        int [] rowIndices = {0,1,2};
        int [] colIndices = {0,1};
        
        Outer2View m3 = new Outer2View(m.getColumn(0), rowIndices, colIndices, 1d, false);
        
        sum = m3.elementSum();
        assertEquals(1d, sum, 0d);
        
        // scale by half
        m3 = new Outer2View(m.getColumn(0), rowIndices, colIndices, 0.5d, false);
                
        sum = m3.elementSum();
        assertEquals(0.5d, sum, 0d);
        
        // clear diagonal ... not implemented
        try {
            m3 = new Outer2View(m.getColumn(0), rowIndices, colIndices, 0.5d, true);
            fail("wasn't expecting to get here, maybe you wanna finish this test?");
        }
        catch (RuntimeException e) {
            // ok ... for the moment
        }
        
    }
}
