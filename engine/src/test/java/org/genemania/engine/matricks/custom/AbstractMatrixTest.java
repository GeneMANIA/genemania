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

public class AbstractMatrixTest {

    @Test
    public void testGetCommonIndices() {
        int [] a = {1, 2, 7};
        int [] b = {2, 7, 8};
        int [] c = {3, 4, 9};
        
        int [] common = AbstractMatrix.getCommonIndices(a, a.length, b, b.length);
        assertEquals(2, common.length);
        assertEquals(2, common[0]);
        assertEquals(7, common[1]);
        
        common = AbstractMatrix.getCommonIndices(b, b.length, a, a.length);
        assertEquals(2, common.length);
        assertEquals(2, common[0]);
        assertEquals(7, common[1]);
 
        common = AbstractMatrix.getCommonIndices(a, a.length, c, c.length);
        assertEquals(0, common.length);
    }

}
