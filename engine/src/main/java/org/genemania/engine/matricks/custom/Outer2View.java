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
import org.genemania.engine.matricks.Matrix;
import org.genemania.engine.matricks.MatrixCursor;

public class Outer2View extends AbstractMatrix implements Matrix {

    int numRows;
    int numCols;
    FlexFloatArray newRowData;
    FlexFloatArray newColumnData;
    double scale;
    boolean zeroDiag;
    
    public static Outer2View fromColumn(Matrix backingMatrix, int backingColumn,int [] rowIndices, int [] columnIndices, double scale, boolean zeroDiag) {
        if (backingMatrix instanceof FlexFloatColMatrix) {
            FlexFloatColMatrix m = (FlexFloatColMatrix) backingMatrix;
            FlexFloatArray backing = m.getColumn(backingColumn);
            return new Outer2View(backing, rowIndices, columnIndices, scale, zeroDiag);
        }
        else {
            throw new RuntimeException("matrix type not supported: " + backingMatrix.getClass().getName());
        }        
    }
    public Outer2View(FlexFloatArray backing, int [] rowIndices, int [] columnIndices, double scale, boolean zeroDiag) {
        
        this.numRows = rowIndices.length;
        this.numCols = columnIndices.length;
        this.scale = scale;
        this.zeroDiag = zeroDiag;
        
        // actually, don't think i need this yet, but leave explicit
        // in constructor to force me to think about it again when using.
        // just blow up here when selected :)
        if (zeroDiag) {  
            throw new RuntimeException("zeroDiag not supported, yet");
        }
        
        int [] commonRowIndices = getCommonIndices(rowIndices, rowIndices.length, backing.indices, backing.used);
        int [] commonColumnIndices = getCommonIndices(columnIndices, columnIndices.length, backing.indices, backing.used);
        
        int [] positionsRow = getPositionsOfCommonIndicesOfAInB(rowIndices, rowIndices.length, backing.indices, backing.used);
        int [] positionsCol = getPositionsOfCommonIndicesOfAInB(columnIndices, columnIndices.length, backing.indices, backing.used);
        
        newRowData = new FlexFloatArray();
        for (int index=0; index<commonRowIndices.length; index++) {
            float val = (float) backing.get(commonRowIndices[index]); // oops, why the float cast ... interface is not uniform!
            newRowData.set(positionsRow[index], val);
        }
        
        newColumnData = new FlexFloatArray();
        for (int index=0; index<commonColumnIndices.length; index++) {
            float val = (float) backing.get(commonColumnIndices[index]); // oops, why the float cast ... interface is not uniform!
            newColumnData.set(positionsCol[index], val);
        }
    }
    
    @Override
    public int numRows() {
        return numRows;
    }

    @Override
    public int numCols() {
        return numCols;
    }

    @Override
    public double get(int row, int col) {
        return scale * newRowData.get(row) * newColumnData.get(col);
    }

    @Override
    public void set(int row, int col, double val) throws MatricksException {
        throw new RuntimeException("read-only");        
    }

    @Override
    public MatrixCursor cursor() {

        return new MatrixCursor() {

            int i = -1;
            int j = 0;
            final int k = newRowData.used-1;
            final int l = newColumnData.used-1;

            @Override
            public boolean next() {
                if (i < k) {
                    i += 1;
                    return true;
                }
                else if (j < l) {
                    i = 0;
                    j += 1;
                    return true;
                }
                else {
                    return false;
                }
            }

            @Override
            public int row() {
                return newRowData.indices[i];
            }

            @Override
            public int col() {
                return newColumnData.indices[j];
            }

            @Override
            public double val() {
                return scale * newRowData.data[i] * newColumnData.data[j];
            }

            @Override
            public void set(double val) {
                throw new RuntimeException("read-only");
            }
        };
    }

    @Override
    public void mult(double[] x, double[] y) {
        throw new RuntimeException("not implemented");        
    }    
}
