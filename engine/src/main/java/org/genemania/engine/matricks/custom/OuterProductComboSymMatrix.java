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
import org.genemania.engine.matricks.SymMatrix;
import org.genemania.engine.matricks.Vector;

/*
 * Symmetric matrix implemented as a (fixed) linear combination
 * of the outer product of a set of vectors. data is specified
 * as a matrix for the set of vectors, plus a single vector
 * for the combination weights. So given an n-by-m matrix A
 * and an m-vector w, this defines the n-by-n symmetric matrix
 * B as:
 *
 *    B = A * diag(w) * A'
 * 
 * which expands as (where A_i is the i'th column of A and
 * w_i the i'th element of w):
 * 
 *    B = sum_{i=1}^{m} (w_i * A_i * A_i')
 * 
 * Implements only a small subset of matrix operations,
 * and does not support updating matrix elements. intended
 * use case is optimized (for storage) matrix-vector 
 * multiplication.
 * 
 * Since our use case is interaction networks with no self-interactions,
 * the constructor flag zeroDiag indicates if we should remove the effect
 * of self-interactions when computing the outer products.
 */
public class OuterProductComboSymMatrix extends AbstractMatrix implements SymMatrix {
    private static final long serialVersionUID = -3004938530505371965L;

    int size;
    Matrix vectorData;
    Vector weights;
    boolean zeroDiag;
    
    // reusable partial result
    DenseDoubleVector diag;
    
    public OuterProductComboSymMatrix(Matrix vectorData, Vector weights, boolean zeroDiag) {
        super();
        
        if (vectorData.numCols() != weights.getSize()) {
            throw new MatricksException("inconsistent size, matrix cols must equal vector len");
        }
        this.vectorData = vectorData;
        this.weights = weights;
        this.zeroDiag = zeroDiag;
        this.size = vectorData.numRows();
    }

    public int numRows() {
        return this.size;
    }

    public int numCols() {
        return this.size;
    }
    
    public int nnz() {
        // what to compute here? pretend to be dense
        return size*size;
    }

    /*
     * loop over columns of data matrix, extract element of outer product
     * of each and compute weighted sum
     */
    public double get(int row, int col) {       
        double val = 0;
        
        if (zeroDiag && (row == col)) {
            return 0;
        }
        
        for (int j=0; j<vectorData.numCols(); j++) {
            val += weights.get(j)*vectorData.get(row, j)*vectorData.get(col, j);
        }
  
        return val;
    }

    /*
     * Return the diagonal of the matrix as
     * a vector. 
     * 
     * if d is the diagonal, then its elements
     * are:
     *   
     *   d_i = \sum_{j=1}^{m} w_j * A_{ij}^2
     *  
     * since this is matrix implementation is
     * read-only, we can cache it the first time its
     * called for later reuse.
     * 
     * Note: common use will be for several of the weights
     * to be zero, so we check and short-circuit. this could
     * be made more efficient still by structuring our iteration
     * using knowledge of the data layout, instead of using
     * the cursor which is convenient but abstracts out data
     * access.
     */
    public DenseDoubleVector getDiagAsVector() {
        
        if (diag != null) {
            return diag;
        }
        
        diag = new DenseDoubleVector(size);
        
        MatrixCursor cursor = vectorData.cursor();
        while (cursor.next()) {
            final double weight = weights.get(cursor.col());
            if (weight != 0d) { // yeah, exact check for zero no epsilon
                final int row = cursor.row();
                final double val = cursor.val();
                double diag_val = diag.get(row);
                diag_val += val*val*weight; 
                diag.set(row, diag_val);                
            }
        }
        
        return diag;
    }
    
    /*
     * since we allocate a partial result to reuse in future computations,
     * it makes sense to provide a way to clean it up. fortunately we
     * have this compact() method!
     */
    @Override
    public void compact() {
        diag = null;
    }
    
    public void set(int row, int col, double val) throws MatricksException {
        throw new MatricksException("Not implemented");    
    }

    public void scale(double a) throws MatricksException {
        throw new MatricksException("Not implemented");
    }

    public void setAll(double a) throws MatricksException {
        throw new MatricksException("Not implemented");    
    }

    public MatrixCursor cursor() {
        throw new MatricksException("Not implemented");
    }

    public void add(Matrix B) throws MatricksException {
        throw new MatricksException("Not implemented");    
    }

    public void add(double a, Matrix B) throws MatricksException {
        throw new MatricksException("Not implemented");    
    }

    public double elementSum() {
        throw new MatricksException("Not implemented");    
    }

    public double elementMultiplySum(Matrix m) throws MatricksException {
        throw new MatricksException("Not implemented");    
    }

    public void CG(Vector x, Vector y) throws MatricksException {
        throw new MatricksException("Not implemented");    
    }

    public void QR(Vector x, Vector y) throws MatricksException {
        throw new MatricksException("Not implemented");    
    }

    public Vector rowSums() throws MatricksException {
        throw new MatricksException("Not implemented");    
    }

    public Vector columnSums() throws MatricksException {
        throw new MatricksException("Not implemented");    
    }

    public void rowSums(double[] result) throws MatricksException {
        
        throw new MatricksException("Not implemented");    
    }

    public void columnSums(double[] result) throws MatricksException {
        throw new MatricksException("Not implemented");    
    }

    public void setToMaxTranspose() throws MatricksException {
        throw new MatricksException("Not implemented");    
    }

    public void multAdd(double alpha, Vector x, Vector y) {
        if (x instanceof DenseDoubleVector && y instanceof DenseDoubleVector) {
            DenseDoubleVector xx = (DenseDoubleVector) x;
            DenseDoubleVector yy = (DenseDoubleVector) y;
            multAdd(xx.data, yy.data);
        }
        else {
            throw new MatricksException("Not implemented");
        }  
    }

    public void mult(Vector x, Vector y) {
        if (x instanceof DenseDoubleVector && y instanceof DenseDoubleVector) {
            DenseDoubleVector xx = (DenseDoubleVector) x;
            DenseDoubleVector yy = (DenseDoubleVector) y;
            mult(xx.data, yy.data);
        }
        else {
            throw new MatricksException("Not implemented");
        }
    }

    public Matrix subMatrix(int[] rows, int[] cols) {
        throw new MatricksException("Not implemented");    
    }

    public void add(int i, int j, double alpha) {
        throw new MatricksException("Not implemented");    
    }

    public void transMult(double[] x, double[] y) {
        throw new MatricksException("Not implemented");    
    }

    public void multAdd(double alpha, double[] x, double[] y) {
        throw new MatricksException("Not implemented");    
    }

    /*
     * compute the result as:
     * 
     *   y = B * x = A * (diag(w) * (A' * x))
     *   
     * this requires 3 matrix-vector multiplications (though
     * one is just a diagonal matrix), and some intermediate
     * m-vectors. 
     */
    public void mult(double[] x, double[] y) {
        double [] temp = multHelper(x);
        vectorData.mult(temp, y);
        if (zeroDiag) {
            compensateForDiag(x, y);
        }
    }

    public void multAdd(double[] x, double[] y) {
        double [] temp = multHelper(x);
        vectorData.multAdd(temp, y);
        if (zeroDiag) {
            compensateForDiag(x, y);
        }    
    }
    
    /*
     * common for mult and multAdd
     *  
     * return diag(weights) * A' * x 
     */
    private double [] multHelper(double [] x) {
        double [] temp1 = new double[vectorData.numCols()];
        double [] temp2 = new double[vectorData.numCols()];

        // TODO: pre-allocate diag? consider implementing mult and using
        // that instead for safety instead of multAdd. in general rationalize
        // the trans methods and vector/array methods.
        vectorData.transMult(x, temp1);
        DoubleDiagonalMatrix diag = new DoubleDiagonalMatrix(weights);
        diag.multAdd(temp1, temp2);
        return temp2;
    }
    
    /*
     * with a result stored in y, update y to remove the effect
     * of the diagonal part of the combo
     */
    private void compensateForDiag(double [] x, double [] y) {
        
        DenseDoubleVector diagonal = getDiagAsVector();
        
        // sooo seductively easy to roll your own loops
        // instead of using matrix/vector ops. oh well. 
        for (int i=0; i<y.length; i++) {
            y[i] = y[i] - x[i]*diagonal.data[i];
        }
    }

    public SymMatrix subMatrix(int[] rowcols) {
        throw new MatricksException("Not implemented");    
    }

    public void setDiag(double alpha) {
        throw new MatricksException("Not implemented");    
    }

    public void dotDivOuterProd(Vector x) {
        throw new MatricksException("Not implemented");    
    }

    public void addOuterProd(double[] x) {
        throw new MatricksException("Not implemented");    
    }

    public double sumDotMultOuterProd(double[] x) {
        throw new MatricksException("Not implemented");    
    }

}
