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

import org.genemania.engine.config.Config;
import org.genemania.engine.matricks.MatricksException;
import org.genemania.engine.matricks.SymMatrix;
import org.junit.Test;

/* 
 * view into symmetric matrix has limited functionality,
 * can't change the backing data.
 */
public class SymMatrixViewTest {

    @Test
    public void viewTest() throws Exception {
        int size = 10;
        
        // init a test matrix
        SymMatrix matrix = Config.instance().getMatrixFactory().symSparseMatrix(size);        
        for (int i=0; i<size; i++) {
            for (int j=0; j<=i; j++) {
                matrix.set(i, j, (i+1)*(j+1));
            }
        }
        
        // view of last 2 rows/cols
        int [] indices = {8, 9};
        
        SymMatrixView view = new SymMatrixView(matrix, indices);
        assertEquals(2, view.numRows());
        assertEquals(2, view.numCols());
        
        // data must match
        assertEquals(matrix.get(8, 8), view.get(0, 0), 0d);
        assertEquals(matrix.get(8, 9), view.get(0, 1), 0d);
        assertEquals(matrix.get(9, 8), view.get(1, 0), 0d);
        assertEquals(matrix.get(9, 9), view.get(1, 1), 0d);
        
        // can't access out of bounds
        try {
            view.get(2, 0);
            fail("expected exception accessing out of bound data");
        }
        catch (IndexOutOfBoundsException e) {
            // ok
        }
        
        // can't change values
        try {
          view.set(0,0,0);
          fail("expected exception");
        }
        catch (MatricksException e) {
            // ok
        }
        
        double sum = view.elementSum();
        assertEquals(361, sum, 0d);
    }
}
