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

import org.genemania.engine.matricks.MatrixCursor;
import org.junit.Test;


public class FlexFloatColMatrixTest {

    /*
     * construct matrix, test sums, iteration, multiplication
     */
    @Test
    public void test() {
        FlexFloatColMatrix m = new FlexFloatColMatrix(3,4);
        
        assertEquals(3, m.numRows());
        assertEquals(4, m.numCols());
        
        MatrixCursor cursor = m.cursor();
        double sum = 0;
        while (cursor.next()) {
            sum += cursor.val();
        }
        
        assertEquals(0d, sum, 0d);
        
        /* 
         *     1 0 0 0
         * m = 1 0 0 0
         *     0 0 0 1
         */
        m.set(0,0,1);
        m.set(1,0,1);
        m.set(2,3,1);
        assertEquals(1d, m.get(0,0), 0d);
        assertEquals(1d, m.get(1,0), 0d);
        assertEquals(0d, m.get(0,1), 0d);
        
        cursor = m.cursor();
        sum = 0;
        while (cursor.next()) {
            sum += cursor.val();
        }
        
        assertEquals(3d, sum, 0d);        
    
        // test some column cursors
        cursor = m.columnCursor(0);
        sum = 0;
        while (cursor.next()) {
            sum += cursor.val();
        }

        assertEquals(2d, sum, 0d);
        
        cursor = m.columnCursor(1);
        sum = 0;
        while (cursor.next()) {
            sum += cursor.val();
        }

        assertEquals(0d, sum, 0d);
        
        
        
        FlexFloatColMatrix m2 = new FlexFloatColMatrix(3,4);   
        m2.set(1,0,1);
        m2.set(2,3,1);
        
        sum = m.elementMultiplySum(m2);
        
        assertEquals(2d, sum, 0d);
        
        double [] rowSums = new double [3];
        m.rowSums(rowSums);

        assertEquals(1d, rowSums[0], 0d);
        assertEquals(1d, rowSums[1], 0d);
        assertEquals(1d, rowSums[2], 0d);
        
        double [] colSums = new double[4];
        m.columnSums(colSums);
        
        assertEquals(2d, colSums[0], 0d);
        assertEquals(0d, colSums[1], 0d);
        assertEquals(0d, colSums[2], 0d);
        assertEquals(1d, colSums[3], 0d);
        
        double [] ones = {1, 1, 1, 1};
        double [] result = {-1, -1, -1}; // values should not affect computation
        m.mult(ones, result);

        assertEquals(1d, result[0], 0d);
        assertEquals(1d, result[1], 0d);
        assertEquals(1d, result[2], 0d);
        
        double [] result2 = {-1, -1, -1}; // values should affect computation
        m.multAdd(ones, result2);

        assertEquals(0d, result2[0], 0d);
        assertEquals(0d, result2[1], 0d);
        assertEquals(0d, result2[2], 0d);
        
        double [] more_ones = {1, 1, 1};
        double [] result3 = {-1, -1, -1, -1};
        m.transMult(more_ones, result3);
        
        assertEquals(2d, result3[0], 0d);
        assertEquals(0d, result3[1], 0d);
        assertEquals(0d, result3[2], 0d);
        assertEquals(1d, result3[3], 0d);
    }
       
}
