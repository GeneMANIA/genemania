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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import org.genemania.engine.matricks.MatricksException;
import org.genemania.engine.matricks.Matrix;
import org.genemania.engine.matricks.MatrixAccumulator;
import org.genemania.engine.matricks.MatrixCursor;
import org.genemania.engine.matricks.Vector;

/**
 * generic implementations of various matrix functions.
 */
public abstract class AbstractMatrix implements Matrix {
    private static final long serialVersionUID = 3593615653654436162L;
    
    public void scale(final double a) throws MatricksException {
        MatrixCursor cursor = this.cursor();
        while (cursor.next()) {
            this.set(cursor.row(), cursor.col(), cursor.val()*a);
        }
    }

    public void setAll(final double a) throws MatricksException {
        MatrixCursor cursor = this.cursor();
        while (cursor.next()) {
            this.set(cursor.row(), cursor.col(), a);
        }
    }

    /*
     * common fallback implementation
     */
    public void add(Matrix B) throws MatricksException {
        if (B.numRows() != this.numRows() && B.numCols() != this.numCols()) {
            throw new MatricksException("incompatible size for addition");
        }

        MatrixCursor cursor = B.cursor();
        while (cursor.next()) {
            final int row = cursor.row();
            final int col = cursor.col();
            double v = cursor.val() + this.get(row, col);
            this.set(row, col, v);
        }
    }

    public void add(double a, Matrix B) throws MatricksException {
        if (B.numRows() != this.numRows() && B.numCols() != this.numCols()) {
            throw new MatricksException("incompatible size for addition");
        }

        MatrixCursor cursor = B.cursor();
        while (cursor.next()) {
            final int row = cursor.row();
            final int col = cursor.col();
            double v = a*cursor.val() + this.get(row, col);
            this.set(row, col, v);
        }
    }

    public double elementSum() {
        double sum = 0d;

        MatrixCursor cursor = this.cursor();
        while (cursor.next()) {
           sum += cursor.val();
        }

        return sum;
    }

    /*
     * simple generic implementation, can take advantage of sparsity structor
     * of this matrix, but not the given one.
     * @see org.genemania.engine.matricks.Matrix#elementMultiplySum(org.genemania.engine.matricks.Matrix)
     */
    public double elementMultiplySum(Matrix m) {
        double sum = 0d;
        
        MatrixCursor cursor = this.cursor();
        while (cursor.next()) {
            sum += cursor.val()*m.get(cursor.row(), cursor.col());
        }
        
        return sum;
    }

    public void CG(Vector x, Vector y) throws MatricksException {
        throw new RuntimeException("not implemented");
    }

    public void QR(Vector x, Vector y) throws MatricksException {
        throw new RuntimeException("not implemented");
    }

    public Vector rowSums() throws MatricksException {
        // TODO: can change this to a lower level interface,
        // taking in an array of doubles and not allocating ... ?
        Vector sums = new DenseDoubleVector(numRows());

        // TODO: optimize out row() with local var? microopt, measure speed
        MatrixCursor cursor = cursor();
        while (cursor.next()) {
            sums.set(cursor.row(), cursor.val() + sums.get(cursor.row()));
        }

        return sums;
    }

    public Vector columnSums() throws MatricksException {
        Vector sums = new DenseDoubleVector(numCols());

        MatrixCursor cursor = cursor();
        while (cursor.next()) {
            sums.set(cursor.col(), cursor.val() + sums.get(cursor.col()));
        }

        return sums;
    }

    public void rowSums(double [] result) {
        throw new RuntimeException("not implemented");
    }

    public void columnSums(double [] result) {
        throw new RuntimeException("not implemented");
    }
    
    /**
     * a <- max(a,a')
     *
     * @param a
     * @param b
     */
    public void setToMaxTranspose() throws MatricksException {
//        throw new RuntimeException("not implemented");
        // TODO: bail if not square

        MatrixCursor cursor = cursor();
        while (cursor.next()) {
            double u = cursor.val();
            double v = get(cursor.col(), cursor.row());
            if (u > v) {
                set(cursor.col(), cursor.row(), u);
            }
            else if (v > u) {
                cursor.set(v);
            }
        }
    }
    
    public void multAdd(double alpha, Vector x, Vector y) {
        throw new RuntimeException("not implemented");
    }

    public void mult(Vector x, Vector y) {
        throw new RuntimeException("not implemented");
    }

    /*
     * A = A ./ (x*x')
     */
    public void dotDivOuterProd(Vector x) {
        MatrixCursor mCursor = cursor();
        while (mCursor.next()) {
            mCursor.set(mCursor.val() / (x.get(mCursor.row()) * x.get(mCursor.col())));
        }
    }

    public Matrix subMatrix(int [] rows, int [] cols) {
        throw new RuntimeException("not implemented");        
    }

    public MatrixCursor columnCursor(int colIndex) {
        throw new RuntimeException("not implemented");        
    }

    public MatrixCursor rowCursor(int rowIndex) {
        throw new RuntimeException("not implemented");        
    }
    
    public void add(int i, int j, double alpha) {
        throw new RuntimeException("not implemented");        
    }

    public void multAdd(double [] x, double [] y) {
        throw new RuntimeException("not implemented");
    }

    public void transMult(double [] x, double [] y) {
        throw new RuntimeException("not implemented");
    }

    /*
     * does nothing by default. specific impl classes will override
     */
    public void compact() {
        return;
    }
    
    // both assumed to be in ascending order, no repeats!
    // result also in ascending order
    public static int [] getCommonIndices(int [] indices_a, final int len_a, int [] indices_b, final int len_b) {

        ArrayList<Integer> common = new ArrayList<Integer>();

        int i = 0;
        int j = 0;

        while (i < len_a && j < len_b) {
            if (indices_a[i] == indices_b[j]) {
                common.add(indices_a[i]);
                i += 1;
                j += 1;
            }
            else if (indices_a[i] < indices_b[j]) {
                i += 1;
            }
            else {
                j += 1;
            }
        }

        int [] result = new int[common.size()];
        for (int k=0; k<result.length; k++) {
            result[k] = common.get(k);
        }

        return result;        
    }
    
    public static int [] getPositionsOfCommonIndicesOfAInB(int [] indices_a, final int len_a, int [] indices_b, final int len_b) {

        ArrayList<Integer> positions = new ArrayList<Integer>();

        int i = 0;
        int j = 0;

        while (i < len_a && j < len_b) {
            if (indices_a[i] == indices_b[j]) {
                positions.add(i);
                i += 1;
                j += 1;
            }
            else if (indices_a[i] < indices_b[j]) {
                i += 1;
            }
            else {
                j += 1;
            }
        }

        int [] result = new int[positions.size()];
        for (int k=0; k<result.length; k++) {
            result[k] = positions.get(k);
        }

        return result;        
    }
    
    /*
     * fallback implementation
     */
    @Override
    public MatrixAccumulator accumulator() {
        return new SimpleMatrixAccumulator(this);
    }
    
    /*
     * number of non-zeroes. well not really, its the number of
     * elements allocated for storage, some of which may be zero.
     * eg for dense formats this is rows*cols, for sparse formats
     * its the number of elements stored.
     * 
     * the semantics of this are kind of fiddly with implementation
     * details, eg think of the case where a matrix is backed by
     * vectors and with elements computed as sums of outer-products.  
     * maybe the safest interpretation is the number
     * of elements that would be returned by a particular matrices
     * cursor. TODO: move this into the cursor?
     */
    protected int nnz() {
        throw new RuntimeException("not implemented");
    }
    
    protected static final String MM_COORDINATE_MATRIX_HEADER = "%%MatrixMarket matrix coordinate real general";
    public void mmwrite(String filename) throws IOException {
        
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
        try {
            writer.write(MM_COORDINATE_MATRIX_HEADER + "\n");
            int nnz = nnz();
            writer.write(String.format("%d %d %d\n", numRows(), numCols(), nnz));
            
            MatrixCursor cursor = cursor();
            int total = 0;
            while (cursor.next()) {
                total += 1;
                writer.write(String.format("%d %d %f\n", cursor.row()+1, cursor.col()+1, cursor.val()));
            }
            
            // safety check for internal data consistency
            if (total != nnz) {
                throw new MatricksException(String.format("expected %d elements, only found %d", nnz, total));
            }
        }
        finally {
            writer.close();
        }
    }
    
    public Matrix mmread(String filename) throws IOException {
        throw new RuntimeException("not implemented");
    }
}
