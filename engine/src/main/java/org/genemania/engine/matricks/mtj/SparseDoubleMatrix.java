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

package org.genemania.engine.matricks.mtj;

import java.io.IOException;
import java.util.Iterator;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Matrices;
import no.uib.cipr.matrix.MatrixEntry;
import no.uib.cipr.matrix.sparse.CG;
import no.uib.cipr.matrix.sparse.FlexCompColMatrix;
import no.uib.cipr.matrix.sparse.IterativeSolverNotConvergedException;
import org.genemania.engine.core.MatrixUtils;
import org.genemania.engine.matricks.MatricksException;
import org.genemania.engine.matricks.Matrix;
import org.genemania.engine.matricks.MatrixAccumulator;
import org.genemania.engine.matricks.MatrixCursor;
import org.genemania.engine.matricks.Vector;
import org.genemania.engine.matricks.custom.SimpleMatrixAccumulator;

/**
 *
 */
public class SparseDoubleMatrix implements Matrix {

    FlexCompColMatrix m;

    public SparseDoubleMatrix(int rows, int cols) {
        m = new FlexCompColMatrix(rows, cols);
    }

    public SparseDoubleMatrix(FlexCompColMatrix m) {
        this.m = m;
    }

    public double elementSum() {
        return MatrixUtils.sum(m);
    }

    public double elementMultiplySum(Matrix B) {
        if (B instanceof SparseDoubleMatrix) {
            SparseDoubleMatrix BB = (SparseDoubleMatrix) B;
            return elementMultiplySum(BB);
        }
        else {
            throw new RuntimeException("not implemented");
        }
    }

    public double elementMultiplySum(SparseDoubleMatrix B) {
        return MatrixUtils.elementMultiplySum(m, B.m);
    }

    public void add(Matrix B) {
        throw new RuntimeException("not implemented for given type: " + B.getClass().getName());
    }

    public void add(double a, Matrix B) {
        if (B instanceof SparseDoubleMatrix) { // this because method resolution isn't selecting the wanted match based on runtime type
            SparseDoubleMatrix BB = (SparseDoubleMatrix) B;
            //m.add(a, BB.m);
            add(a, BB);
        }
        else {
            throw new RuntimeException("not implemented for given type: " + B.getClass().getName());
        }
    }

    public void add(SparseDoubleMatrix B) {
        m.add(B.m);

    }

    public void add(double a, SparseDoubleMatrix B) {
        m.add(a, B.m);
    }

    public MatrixCursor cursor() {
        return new SparseDoubleMatrixCursor();
    }

    public void setAll(double a) {
        throw new RuntimeException("not implemented");
    }

    public void scale(double a) {
        m.scale(a);
    }

    public void set(int i, int j, double val) {
        m.set(i, j, val);
    }

    public double get(int i, int j) {
        return m.get(i, j);
    }

    public int numCols() {
        return m.numColumns();
    }

    public int numRows() {
        return m.numRows();
    }

    public void CG(Vector x, Vector y) throws MatricksException {
        if (!(x instanceof DenseDoubleVector)) {
            throw new MatricksException("unexpected implementation: " + x.getClass().getName());
        }
        if (!(y instanceof DenseDoubleVector)) {
            throw new MatricksException("unexpected implementation: " + y.getClass().getName());
        }

        DenseDoubleVector xx = (DenseDoubleVector) x;
        DenseDoubleVector yy = (DenseDoubleVector) y;

        CG cg = new CG(new DenseVector(x.getSize()));
        try {
            cg.solve(m, xx.v, yy.v);
        }
        catch (IterativeSolverNotConvergedException e) {
            throw new MatricksException("failed to converge");
        }
    }

    public void QR(Vector x, Vector y) throws MatricksException {
        throw new RuntimeException("not implemented");
    }

    public Vector rowSums() throws MatricksException {
        return new DenseDoubleVector(MatrixUtils.rowSums(m));
    }

    public Vector columnSums() throws MatricksException {
        return new DenseDoubleVector(MatrixUtils.columnSums(m));
    }

    /**
     * a <- max(a,a')
     *
     * @param a
     * @param b
     */
    public void setToMaxTranspose() {
        // TODO: bail if not square

        for (MatrixEntry e: m) {
            double u = e.get();
            double v = m.get(e.column(), e.row());
            if (u > v) {
                m.set(e.column(), e.row(), u);
            }
            else if (v > u) {
                e.set(v);
            }
        }
    }

    private class SparseDoubleMatrixCursor implements MatrixCursor {

        Iterator<MatrixEntry> iter = m.iterator();
        MatrixEntry e;

        public boolean next() {
            if (iter.hasNext()) {
                e = iter.next();
                return true;
            }
            else {
                return false;
            }
        }

        public int row() {
            return e.row();
        }

        public int col() {
            return e.column();
        }

        public double val() {
            return e.get();
        }

        public void set(double val) {
            e.set(val);
        }
    }

    public void multAdd(double a, Vector x, Vector y) {
        if (x instanceof DenseDoubleVector && y instanceof DenseDoubleVector) {
            DenseDoubleVector xx = (DenseDoubleVector) x;
            DenseDoubleVector yy = (DenseDoubleVector) y;
            multAdd(a, xx, yy);
        }
        else {
            throw new RuntimeException("not implemented");
        }
    }

    public void multAdd(double a, DenseDoubleVector x, DenseDoubleVector y) {
        m.multAdd(a, x.v, y.v);
    }

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
        m.mult(x.v, y.v);
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

    /*
     * create a sparse flex matrix from the given matrix,
     * by simply iterating over its elements and ignoring those
     * that are exactly 0.
     */
    public static FlexCompColMatrix sparseFlexCopy(no.uib.cipr.matrix.Matrix m) {
        FlexCompColMatrix f = new FlexCompColMatrix(m.numRows(), m.numColumns());
        for (MatrixEntry e: m) {
            if (e.get() != 0d) {
                f.set(e.row(), e.column(), e.get());
            }
        }

        return f;
    }

    public Matrix subMatrix(int [] rows, int [] cols) {
        no.uib.cipr.matrix.Matrix subMatrix = Matrices.getSubMatrix(m, rows, cols);
        FlexCompColMatrix subMatrixCopy = SparseDoubleMatrix.sparseFlexCopy(subMatrix);
        return new SparseDoubleMatrix(subMatrixCopy);
    }

    public void rowSums(double [] result) {
        MatrixUtils.rowSums(m, result);
    }

    public void columnSums(double [] result) {
        MatrixUtils.columnSums(m, result);
    }

    public void add(int i, int j, double alpha) {
        this.m.add(i, j, alpha);
    }


    public void mult(double [] x, double [] y) {
        throw new RuntimeException("not implemented");        
    }
    
    public void multAdd(double [] x, double [] y) {
        DenseVector xx = new DenseVector(x, false);
        DenseVector yy = new DenseVector(y, false);
        this.m.multAdd(xx, yy);
    }

    public void compact() {
        return;
    }

     public void transMult(double [] x, double [] y) {
        throw new RuntimeException("not implemented");
    }

    public void mmwrite(String filename) throws IOException {
        throw new RuntimeException("not implemented");
    }

    public Matrix mmread(String filename) throws IOException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public MatrixAccumulator accumulator() {
        return new SimpleMatrixAccumulator(this);
    }
}
