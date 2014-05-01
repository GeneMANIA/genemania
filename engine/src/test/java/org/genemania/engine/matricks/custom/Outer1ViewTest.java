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

public class Outer1ViewTest {

    @Test
    public void test() throws Exception {
        FlexFloatColMatrix m = new FlexFloatColMatrix(4, 5);
        m.set(1, 0, 1);
        m.set(3, 0, 1);
        
        OuterProductFlexFloatSymMatrix m2 = new OuterProductFlexFloatSymMatrix(4, m.getColumn(0), 1, false);
        double sum = m2.elementSum();
        assertEquals(4d, sum, 0d);
        
        int [] indices = {0,1,2};
        
        Outer1View m3 = new Outer1View(m.getColumn(0), indices, 1d, false);
        
        sum = m3.elementSum();
        assertEquals(1d, sum, 0d);
        
        // scale by half
        m3 = new Outer1View(m.getColumn(0), indices, 0.5d, false);
        
        sum = m3.elementSum();
        assertEquals(0.5d, sum, 0d); 
        
        // clear diagonal
        m3 = new Outer1View(m.getColumn(0), indices, 0.5d, true);
        
        sum = m3.elementSum();
        assertEquals(0d, sum, 0d);     
    }
}
