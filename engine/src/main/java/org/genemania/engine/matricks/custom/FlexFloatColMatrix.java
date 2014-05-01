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

import java.util.Arrays;

import org.genemania.engine.matricks.MatricksException;
import org.genemania.engine.matricks.Matrix;
import org.genemania.engine.matricks.MatrixCursor;

/**
 * column-wise storage version
 */
public class FlexFloatColMatrix extends AbstractMatrix {
    private static final long serialVersionUID = 8262113357006375706L;
    
    int rows;
    int cols;

    // col-wise storage
    FlexFloatArray [] data;

    public FlexFloatColMatrix(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;

        alloc();
    }

    private void alloc() {
        data = new FlexFloatArray[this.cols];
        for (int j=0; j<this.cols; j++) {
            data[j] = new FlexFloatArray(rows);
        };
    }

    public int numRows() {
        return rows;
    }

    public int numCols() {
        return cols;
    }

    public double get(int row, int col) {
        checkIdx(row, col);
        return data[col].get(row);
    }

    public void set(int row, int col, double val) throws MatricksException {
        checkIdx(row, col);
        data[col].set(row, (float) val);
    }

    private void checkIdx(final int row, final int col) {
        if (row < 0 || row >= rows) {
            throw new IndexOutOfBoundsException(String.format("invalid row index: %d", row));
        }
        if (col < 0 || col >= cols) {
            throw new IndexOutOfBoundsException(String.format("invalid column index: %d", col));
        }
    }

    public MatrixCursor cursor() {
        return new FlexFloatColMatrixCursor();
    }

    private class FlexFloatColMatrixCursor implements MatrixCursor {

        public int col = -1;
        public MatrixCursor colCursor;

        public boolean next() {

            // init if necessary
            boolean next;
            if (colCursor == null) {
               next = getNextCursor();
            }
            else {
                next = colCursor.next();
            }

            // advance cursor, alloc new cursor if exhausted
            if (next) {
                return true;
            }
            else {
                return getNextCursor();
            }
        }

        private boolean getNextCursor() {
            while (++col < data.length) {
                colCursor = data[col].cursor();
                boolean next = colCursor.next();
                if (next) {
                    return true;
                }
            }

            return false;
        }

        public int row() {
            return colCursor.row();
        }

        public int col() {
            return col;
        }

        public double val() {
            return colCursor.val();
        }

        public void set(double val) {
            colCursor.set(val);
        }
    }

    public double elementMultiplySum(Matrix m) {
        if (m instanceof FlexFloatColMatrix) {
            return elementMultiplySum((FlexFloatColMatrix) m);
        }
        else {
            throw new RuntimeException("not implemented for: " + m.getClass().getName());
        }


    }
    public double elementMultiplySum(FlexFloatColMatrix m) {
        if (m.numRows() != rows && m.numCols() != cols) {
            throw new MatricksException("inconsistent dimensions");
        }

        double sum = 0d;
        for (int j=0; j<cols; j++) {
            sum += data[j].dot(m.data[j]);
        }

        return sum;
    }

    @Override
    public void rowSums(double [] result) {
        for (int col=0; col<cols; col++) {
            data[col].addTo(result);
        }
    }

    @Override
    public void columnSums(double [] result) {
        // TODO: size check
        for (int col=0; col<cols; col++) {
            result[col] = result[col] + data[col].elementSum();
        }
    }

    @Override
    public void mult(double [] x, double [] y) {        
        Arrays.fill(y, 0d);
        
        for (int col=0; col<cols; col++) {
            data[col].add(x[col], y);            
        }        
    }
    
    @Override
    public void multAdd(double [] x, double [] y) {
        for (int col=0; col<cols; col++) {
            data[col].add(x[col], y);            
        } 
    }

    @Override
    public Matrix subMatrix(int [] rows, int [] cols) {
        FlexFloatColMatrix subMatrix = new FlexFloatColMatrix(rows.length, cols.length);

        for (int i=0; i<rows.length; i++) {
            int idx = rows[i];

            for (int j=0; j<cols.length; j++) {
                int jdx = cols[j];

                double v = get(idx, jdx);
                if (v != 0d) {
                    subMatrix.set(i, j, v);
                }
            }
        }
        return subMatrix;
    }

    @Override
    public void transMult(double [] x, double [] y) {
        // TODO: size check

        Arrays.fill(y, 0d);

        for (int col=0; col<cols; col++) {
            y[col] = data[col].dot(x);
        }
    }
    
    @Override
    public MatrixCursor columnCursor(final int columnIndex) {
        return new MatrixCursor() {

            MatrixCursor columnCursor = data[columnIndex].cursor();

            @Override
            public boolean next() {
                return columnCursor.next();
            }

            @Override
            public int row() {
                return columnCursor.row();
            }

            @Override
            public int col() {
                return columnIndex;
            }

            @Override
            public double val() {
                return columnCursor.val();
            }

            @Override
            public void set(double val) {
                columnCursor.set(val);
            }
        };
    }
    
    // just for testing, TODO: remove
    public FlexFloatArray getColumn(final int columnIndex) {
        return data[columnIndex];
    }
}
