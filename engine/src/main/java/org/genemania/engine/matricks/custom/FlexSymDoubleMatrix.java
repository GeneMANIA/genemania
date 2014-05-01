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

/**
 *
 */
public class FlexSymDoubleMatrix  extends AbstractMatrix implements SymMatrix  {

    int size; // rows == cols
    double [] diag;

    // row-wise storage
    FlexDoubleArray [] data;

    public FlexSymDoubleMatrix(int size) {
        this.size = size;

        alloc();
    }
    
    private void alloc() {
        data = new FlexDoubleArray[this.size];
        for (int i=0; i<this.size; i++) {
            data[i] = new FlexDoubleArray(i); // row i can have max i lower-diagonal elements ... could skip allocating the empty one for the first row
        }
        diag = new double[size];
    }

    public int numRows() {
        return size;
    }

    public int numCols() {
        return size;
    }
    
    public double get(int row, int col) {
        checkIdx(row, col);

        if (row > col) {
            return data[row].get(col);
        }
        else if (row < col) {
            return data[col].get(row);
        }
        else {
            return diag[row];
        }
    }

    public void set(int row, int col, double val) throws MatricksException {
        checkIdx(row, col);

        if (row > col) {
            data[row].set(col, val);
        }
        else if (row < col) {
            data[col].set(row, val);
        }
        else {
            diag[row] = val;
        }
    }

    private void checkIdx(final int row, final int col) {
        if (row < 0 || row >= size) {
            throw new IndexOutOfBoundsException(String.format("invalid row index: %d", row));
        }
        if (col < 0 || col >= size) {
            throw new IndexOutOfBoundsException(String.format("invalid column index: %d", col));
        }
    }

    public void scale(final double a) throws MatricksException {
        for (int row=0; row<size; row++) {
            FlexDoubleArray d = data[row];
            d.scale(a);
            diag[row] = a*diag[row];
        }
    }
    /*
     * C = A*B;
     */
    //public void mult(Matrix B, Matrix C);

    /*
     *
     */
    public void setAll(double a) throws MatricksException {
        throw new RuntimeException("not implemented");
    }

    public MatrixCursor cursor() {
        return new FlexSymDoubleMatrixCursor();
    }

    /*
     * this is a quick hack
     */
    private class FlexSymDoubleMatrixCursor implements MatrixCursor {
        boolean symToggle = true;
        boolean onDiag = false;
        
        int row = 0;
        int index = -1;
        int col = -1;
        double val; // extra copying of data into cursor, TODO: remove
        
        public boolean next() {

            // visiting diagonal?
            if (onDiag) {
                row += 1;
                
                if (row >= size) {
                    return false;
                }
                else {
                    col = row;
                    val = diag[row];
                    return true;
                }
            }

            // visiting off-diagonal
            else {
                if (!symToggle) {
                    symToggle = true;
                }
                else {
                    index += 1;
                    boolean ok = true;
                    if (index >= data[row].data.length) {
                        ok = advanceOffDiagRow();
                    }
                    
                    if (!ok) {
                        onDiag = true;
                        row = 0;
                        col = 0;
                        val = diag[row];
                    }
                    else {
                        val = data[row].data[index];
                        col = data[row].indices[index];
                    }

                    symToggle = false;

                }                
                return true;
            }
        }

        private boolean advanceOffDiagRow() {

            row += 1;
            while (row < size) {
                if (data[row].data.length > 0) {
                    index = 0;
                    return true;
                }
                else {
                    row += 1; // keep looking
                }
            }

            return false;

        }
        
        public int row() {
            if (symToggle) {
                return col;
            }
            else {
                return row;
            }
        }

        public int col() {
            if (symToggle) {
                return row;
            }
            else {
                return col;
            }
        }

        public double val() {
            return val;
        }

        public void set(double val) {
            throw new RuntimeException("not implemented");
        }

    }

    public void add(Matrix B) throws MatricksException {
        throw new RuntimeException("not implemented");
    }
    
    public void add(final double a, Matrix B) throws MatricksException {
        if (B instanceof FlexSymDoubleMatrix) {
            FlexSymDoubleMatrix BB = (FlexSymDoubleMatrix) B;
            add(a, BB);
        }
        else {
            throw new RuntimeException("not implemented for: " + B.getClass().getName());
        }
    }

    public void add(final double alpha, FlexSymDoubleMatrix B) throws MatricksException {
        for (int row=0; row<B.size; row++) {
            data[row].add(alpha, B.data[row]);
        }

        for (int row=0; row<B.size; row++) {
            diag[row] = diag[row] + alpha*B.diag[row];
        }
    }

    /*
     * twice the row sums, plus the diag
     */
    public double elementSum() {
        double sum = 0d;
        for (int row=0; row<size; row++) {
            double rowSum = data[row].elementSum();
            sum += 2*rowSum;

            sum += diag[row];
        }

        return sum;
    }

    public double elementMultiplySum(Matrix m) throws MatricksException {
        if (m instanceof FlexSymDoubleMatrix) {
            return elementMultiplySum((FlexSymDoubleMatrix) m);
        }
        else {
            throw new RuntimeException("not implemented");
        }
    }

    /*
     * twice the row dots, plug diag dot
     */
    public double elementMultiplySum(FlexSymDoubleMatrix m) throws MatricksException {
        double sum = 0d;
        for (int row=0; row<size; row++) {
            double rowDot = data[row].dot(m.data[row]);
            sum += 2*rowDot;

            sum += diag[row]*m.diag[row];
        }

        return sum;
    }

    /*
     * adapted from CG in mtj
     *
     */
    public void CG(Vector b, Vector x) throws MatricksException {

        int n = x.getSize();
        if (n != b.getSize() || n != size || n != size) {
            throw new MatricksException("inconsistent data sizes");
        }

        // TODO: shouldn't be using the factories here i think
//        MatrixFactory mf = Config.instance().getMatrixFactory();
        Vector p = new DenseDoubleVector(n);
        Vector z = new DenseDoubleVector(n);
        Vector q = new DenseDoubleVector(n);
        Vector r = new DenseDoubleVector(n);

        double alpha = 0, beta = 0, rho = 0, rho_1 = 0;

        r.setEqual(b);
        multAdd(-1, x, r);

        double initR = Math.sqrt(r.dot(r));
        double currentR = initR;

        int iter = 0;
//        for (iter.setFirst(); !iter.converged(r, x); iter.next()) {
        while (!converged(iter, initR, currentR, x)) {
//            M.apply(r, z);
            z.setEqual(r); // TODO: can we get rid of z since we aren't using a preconditioner?
            rho = r.dot(z);

            if (iter == 0)
                p.setEqual(z);
            else {
                beta = rho / rho_1;
                p.scale(beta);
                p.add(z);
            }

            mult(p, q);
            alpha = rho / p.dot(q);

            x.add(alpha, p);
            r.add(-alpha, q);

            rho_1 = rho;
            currentR = Math.sqrt(r.dot(r));
            iter++;
        }
    }

    static int maxIter = 100000;
    static double rtol = 1e-5;
    static double atol = 1e-50;
    static double dtol = 1e+5;

    /*
     * based on mtj
     */
    public boolean converged(int iter, double initR, double currentR, Vector x) throws MatricksException {

        if (currentR < Math.max(rtol * initR, atol))
            return true;

        if (Double.isNaN(currentR))
            throw new MatricksException("diverged");
        if (currentR > dtol * initR)
            throw new MatricksException("diverged");

        if (iter >= maxIter)
            throw new MatricksException("max iterations reached");

        return false;
    }

    public void QR(Vector x, Vector y) throws MatricksException {
        throw new RuntimeException("not implemented");
    }

    public Vector rowSums() throws MatricksException {
        double [] y = new double[size];
        
        for (int row=0; row<size; row++) {
            FlexDoubleArray d = data[row];
            d.partialSum(y, row);
        }

        addDiag(y);
        return new DenseDoubleVector(y);
    }
    
    public Vector columnSums() throws MatricksException {
        return rowSums(); // since symmetric
    }

    public void rowSums(double [] result) {
        for (int row=0; row<size; row++) {
            FlexDoubleArray d = data[row];
            d.partialSum(result, row);
        }

        addDiag(result);
    }

    public void columnSums(double [] result) {
        rowSums(result);
    }

    public void setToMaxTranspose() throws MatricksException {
        return; // no-op for this implementation
    }

    /*
     * y = alpha*A*x + y
     */
    public void multAdd(final double alpha, Vector x, Vector y) {
        if (x instanceof DenseDoubleVector && y instanceof DenseDoubleVector) {
            DenseDoubleVector xx = (DenseDoubleVector) x;
            DenseDoubleVector yy = (DenseDoubleVector) y;
            multAdd(alpha, xx, yy);
        }
        else {
            throw new RuntimeException("not implemented");
        }
    }

    public void multAdd(final double alpha, DenseDoubleVector x, DenseDoubleVector y) {
        multAdd(alpha, x.data, y.data);
    }

    public void multAdd(final double alpha, double [] x, double [] y) {
        for (int row=0; row<size; row++) {
            FlexDoubleArray d = data[row];
            d.partialMult(alpha, x, y, row);
        }

        addDiag(alpha, x, y);
    }

    public void multAdd(double [] x, double [] y) {
        for (int row=0; row<size; row++) {
            FlexDoubleArray d = data[row];
            d.partialMult(x, y, row);
        }

        addDiag(x, y);
    }

    /*
     * 
     */
    protected void addDiag(final double alpha, double [] x, double [] y) {
        for (int row=0; row<size; row++) {
            y[row] = y[row] + alpha*x[row]*diag[row];
        }
    }

    /*
     * y = diag + y
     */
    protected void addDiag(double [] y) {
        for (int row=0; row<size; row++) {
            y[row] = y[row] + diag[row];
        }
    }

    protected void addDiag(double [] x, double [] y) {
        for (int row=0; row<size; row++) {
            y[row] = y[row] + x[row]*diag[row];
        }
    }

    /*
     * y = A*x;
     */
    public void mult(Vector x, Vector y) {
        if (x instanceof DenseDoubleVector && y instanceof DenseDoubleVector) {
            DenseDoubleVector xx = (DenseDoubleVector) x;
            DenseDoubleVector yy = (DenseDoubleVector) y;
            mult(xx, yy);
        }
        else {
            throw new RuntimeException("not implemented");
        }
    }

    public void mult(DenseDoubleVector x, DenseDoubleVector y) {
        mult(x.data, y.data);
    }

    public void mult(double [] x, double [] y) {
        clear(y);

        for (int row=0; row<size; row++) {
            FlexDoubleArray d = data[row];
            d.partialMult(x, y, row);
        }

        addDiag(x, y);
    }

    protected static void clear(double [] y) {
        for (int i=0; i<y.length; i++) {
            y[i] = 0d;
        }
    }

    /*
     * A = A .* (x*y')
     */
    public void dotMultOuterProd(double [] x, double [] y) {
        throw new RuntimeException("not implemented");
    }

    /*
     * A = A ./ (x*y')
     */
    public void dotDivOuterProd(double [] x, double [] y) {
        throw new RuntimeException("not implemented");
    }

    /*
     * A = A .* (x*x')
     */
    public void dotMultOuterProd(double [] x) {
        throw new RuntimeException("not implemented");
    }

    /*
     * A = A ./ (x*x')
     */
    public void dotDivOuterProd(double [] x) {

        for (int row=0; row<size; row++) {
            FlexDoubleArray d = data[row];
            d.dotDiv(x[row], x);
        }

        for (int row=0; row<size; row++) {
            diag[row] = diag[row] / (x[row] * x[row]);
        }

    }
    
    /*
     * A = A ./ (x*x')
     */
    public void dotDivOuterProd(Vector x) {
        if (x instanceof DenseDoubleVector) {
            DenseDoubleVector xx = (DenseDoubleVector) x;
            dotDivOuterProd(xx);
        }
    }

    public void dotDivOuterProd(DenseDoubleVector x) {
        dotDivOuterProd(x.data);
    }

    public void addOuterProd(double [] x) {

        // TODO: size check
        
        // non diag
        double prod;
        for (int i=0; i<x.length; i++) {
            for (int j=0; j<i; j++) {
                prod = x[i] * x[j];
                FlexDoubleArray d = data[i];
                if (prod != 0d) {
                    d.add(j, prod);
                }
            }
        }

        // diag
        for (int row=0; row<size; row++) {
            diag[row] = diag[row] + (x[row] * x[row]);
        }
        
    }

    public double sumDotMultOuterProd(double [] x) {
        double sum = 0d;

        for (int i=0; i<x.length; i++) {
            FlexDoubleArray d = data[i];
            double s = d.dot(x);
            sum += s * x[i];
        }

        // by symmetry, double the result
        sum = 2*sum;

        // add in diag
        for (int row=0; row<size; row++) {
            sum += diag[row]*(x[row] * x[row]);
        }
        
        return sum;
    }
    
    /*
     * note we are checking for zeros to maintain sparsity.
     * if we happen to have some structured zero's in this matrix
     * we will lose them in the returned matrix
     */
    public SymMatrix subMatrix(int [] rowcols) {
        FlexSymDoubleMatrix subMatrix = new FlexSymDoubleMatrix(rowcols.length);

        for (int i=0; i<rowcols.length; i++) {
            int idx = rowcols[i];

            for (int j=0; j<rowcols.length; j++) {
                int jdx = rowcols[j];
                
                // only need to set lower triangle
                if (idx > jdx) {
                    double v = get(idx, jdx);
                    if (v != 0d) {
                        subMatrix.set(i, j, v);
                    }
                }
            }
            // set diag
            double v = get(idx, idx);
            if (v != 0d) {
                subMatrix.set(i, i, v);
            }
        }

        return subMatrix;
    }

    public Matrix subMatrix(int [] rows, int [] cols) {
        FlexDoubleMatrix subMatrix = new FlexDoubleMatrix(rows.length, cols.length);

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

    public void setDiag(double alpha) {
        for (int i=0; i<size; i++) {
            diag[i] = alpha;
        }
    }

    public void add(int i, int j, double alpha) {
        if (i!=j) {
            data[i].add(j, alpha);
        }
        else {
            diag[i] = diag[i] + alpha;
        }
    }

    /*
     * trim storage
     */
    public void compact() {
        for (int row=0; row<size; row++) {
            data[row].compact();
        }
    }

    public void transMult(double [] x, double [] y) {
        throw new RuntimeException("not implemented");
    }

}
