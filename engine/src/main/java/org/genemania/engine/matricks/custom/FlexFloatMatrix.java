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
 *
 */
public class FlexFloatMatrix extends AbstractMatrix {
    private static final long serialVersionUID = 8262113357006375706L;
    
    int rows;
    int cols;

    // row-wise storage
    FlexFloatArray [] data;

    public FlexFloatMatrix(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;

        alloc();
    }

    private void alloc() {
        data = new FlexFloatArray[this.rows];
        for (int i=0; i<this.rows; i++) {
            data[i] = new FlexFloatArray(cols);
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
        return data[row].get(col);
    }

    public void set(int row, int col, double val) throws MatricksException {
        checkIdx(row, col);
        data[row].set(col, (float) val);
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
        return new FlexDoubleMatrixCursor();
    }

    private class FlexDoubleMatrixCursor implements MatrixCursor {

        public int row = -1;
        public MatrixCursor rowCursor;

        public boolean next() {

            // init if necessary
            boolean next;
            if (rowCursor == null) {
               next = getNextCursor();
            }
            else {
                next = rowCursor.next();
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
            while (++row < data.length) {
                rowCursor = data[row].cursor();
                boolean next = rowCursor.next();
                if (next) {
                    return true;
                }
            }

            return false;
        }

        public int row() {
            return row;
        }

        public int col() {
        	// note the "row" is a 1-col matrix, so return row pos as col
            return rowCursor.row();
        }

        public double val() {
            return rowCursor.val();
        }

        public void set(double val) {
            rowCursor.set(val);
        }
    }

    public double elementMultiplySum(Matrix m) {
        if (m instanceof FlexFloatMatrix) {
            return elementMultiplySum((FlexFloatMatrix) m);
        }
        else {
            throw new RuntimeException("not implemented for: " + m.getClass().getName());
        }


    }
    public double elementMultiplySum(FlexFloatMatrix m) {
        if (m.numRows() != rows && m.numCols() != cols) {
            throw new MatricksException("inconsistent dimensions");
        }

        double sum = 0d;
        for (int i=0; i<rows; i++) {
            sum += data[i].dot(m.data[i]);
        }

        return sum;
    }

    @Override
    public void rowSums(double [] result) {
        for (int row=0; row<rows; row++) {
            result[row] = result[row] + data[row].elementSum();
        }
    }

    @Override
    public void columnSums(double [] result) {
        // TODO: size check
        for (int row=0; row<rows; row++) {
            data[row].addTo(result);
        }
    }

    @Override
    public void mult(double [] x, double [] y) {
        for (int row=0; row<rows; row++) {
            y[row] = data[row].dot(x);
        }        
    }
    
    @Override
    public void multAdd(double [] x, double [] y) {
        for (int row=0; row<rows; row++) {
            y[row] = y[row] + data[row].dot(x);
        }
    }

    @Override
    public Matrix subMatrix(int [] rows, int [] cols) {
        FlexFloatMatrix subMatrix = new FlexFloatMatrix(rows.length, cols.length);

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

        for (int row=0; row<rows; row++) {
           data[row].add(x[row], y);
        }
    }
}
