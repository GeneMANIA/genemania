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

import java.io.Serializable;
import java.util.Iterator;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.MatrixNotSPDException;
import no.uib.cipr.matrix.MatrixSingularException;
import no.uib.cipr.matrix.Vector;
import org.genemania.engine.matricks.MatricksException;

/**
 * wrap one of our custom symmetric types in an mtj interface,
 * implementing the minimal subset of methods we need in order
 * to reuse some of the mtj code (eg iterative solvers).
 */
public class SymWrap implements no.uib.cipr.matrix.Matrix, Serializable {

    org.genemania.engine.matricks.SymMatrix backingMatrix;

    public SymWrap(org.genemania.engine.matricks.SymMatrix m) {
        this.backingMatrix = m;
    }

    /* implemented methods of mtj matrix interface */
    public int numRows() {
        return backingMatrix.numRows();
    }

    public int numColumns() {
        return backingMatrix.numCols();
    }

    public void set(int row, int column, double value) {

        // TODO: change our exceptions to runtime?
        try {
            backingMatrix.set(row, column, value);
        }
        catch(MatricksException e) {
            throw new RuntimeException("failed: ", e);
        }

    }

    public double get(int row, int column) {
        return backingMatrix.get(row, column);
    }

    public Vector multAdd(double alpha, Vector x, Vector y) {
        if (x instanceof DenseVector && y instanceof DenseVector) {
            multAdd(alpha, (DenseVector) x, (DenseVector) y);
            return y;
        }
        else {
           throw new RuntimeException("not implemented");
        }
    }
    
    public Vector multAdd(double alpha, DenseVector x, DenseVector y) {
        backingMatrix.multAdd(alpha, x.getData(), y.getData());
        return y;
    }

    public Vector multAdd(Vector x, Vector y) {
        if (x instanceof DenseVector && y instanceof DenseVector) {
            multAdd((DenseVector) x, (DenseVector) y);
            return y;
        }
        else {
           throw new RuntimeException("not implemented");
        }
    }

    public Vector multAdd(DenseVector x, DenseVector y) {
        backingMatrix.multAdd(x.getData(), y.getData());
        return y;
    }

    public Vector mult(Vector x, Vector y) {
        if (x instanceof DenseVector && y instanceof DenseVector) {
            mult((DenseVector) x, (DenseVector) y);
            return y;
        }
        else {
           throw new RuntimeException("not implemented");
        }
    }
    public Vector mult(DenseVector x, DenseVector y) {
        backingMatrix.mult(x.getData(), y.getData());
        return y;
    }

    public boolean isSquare() {
        return true;
    }

    /* unimplemented methods of mtj matrix interface */
    

    public void add(int row, int column, double value) {
        throw new RuntimeException("not implemented");
    }

    public Matrix copy() {
        throw new RuntimeException("not implemented");
    }

    public Matrix zero() {
        throw new RuntimeException("not implemented");
    }

    public Vector mult(double alpha, Vector x, Vector y) {
        throw new RuntimeException("not implemented");
    }

    public Vector transMult(Vector x, Vector y) {
        throw new RuntimeException("not implemented");
    }

    public Vector transMult(double alpha, Vector x, Vector y) {
        throw new RuntimeException("not implemented");
    }

    public Vector transMultAdd(Vector x, Vector y) {
        throw new RuntimeException("not implemented");
    }

    public Vector transMultAdd(double alpha, Vector x, Vector y) {
        throw new RuntimeException("not implemented");
    }

    public Vector solve(Vector b, Vector x) throws MatrixSingularException,
            MatrixNotSPDException {
        throw new RuntimeException("not implemented");
    }

    public Vector transSolve(Vector b, Vector x) throws MatrixSingularException,
            MatrixNotSPDException {
        throw new RuntimeException("not implemented");
    }

    public Matrix rank1(Vector x) {
        throw new RuntimeException("not implemented");
    }

    public Matrix rank1(double alpha, Vector x) {
        throw new RuntimeException("not implemented");
    }

    public Matrix rank1(Vector x, Vector y) {
        throw new RuntimeException("not implemented");
    }

    public Matrix rank1(double alpha, Vector x, Vector y) {
        throw new RuntimeException("not implemented");
    }

    public Matrix rank2(Vector x, Vector y) {
        throw new RuntimeException("not implemented");
    }

    public Matrix rank2(double alpha, Vector x, Vector y) {
        throw new RuntimeException("not implemented");
    }

    public Matrix mult(Matrix B, Matrix C) {
        throw new RuntimeException("not implemented");
    }

    public Matrix mult(double alpha, Matrix B, Matrix C) {
        throw new RuntimeException("not implemented");
    }

    public Matrix multAdd(Matrix B, Matrix C) {
        throw new RuntimeException("not implemented");
    }

    public Matrix multAdd(double alpha, Matrix B, Matrix C) {
        throw new RuntimeException("not implemented");
    }

    public Matrix transAmult(Matrix B, Matrix C) {
        throw new RuntimeException("not implemented");
    }

    public Matrix transAmult(double alpha, Matrix B, Matrix C) {
        throw new RuntimeException("not implemented");
    }

    public Matrix transAmultAdd(Matrix B, Matrix C) {
        throw new RuntimeException("not implemented");
    }

    public Matrix transAmultAdd(double alpha, Matrix B, Matrix C) {
        throw new RuntimeException("not implemented");
    }

    public Matrix transBmult(Matrix B, Matrix C) {
        throw new RuntimeException("not implemented");
    }

    public Matrix transBmult(double alpha, Matrix B, Matrix C) {
        throw new RuntimeException("not implemented");
    }

    public Matrix transBmultAdd(Matrix B, Matrix C) {
        throw new RuntimeException("not implemented");
    }

    public Matrix transBmultAdd(double alpha, Matrix B, Matrix C) {
        throw new RuntimeException("not implemented");
    }

    public Matrix transABmult(Matrix B, Matrix C) {
        throw new RuntimeException("not implemented");
    }

    public Matrix transABmult(double alpha, Matrix B, Matrix C) {
        throw new RuntimeException("not implemented");
    }

    public Matrix transABmultAdd(Matrix B, Matrix C) {
        throw new RuntimeException("not implemented");
    }

    public Matrix transABmultAdd(double alpha, Matrix B, Matrix C) {
        throw new RuntimeException("not implemented");
    }

    public Matrix solve(Matrix B, Matrix X) throws MatrixSingularException,
            MatrixNotSPDException {
        throw new RuntimeException("not implemented");
    }

    public Matrix transSolve(Matrix B, Matrix X) throws MatrixSingularException,
            MatrixNotSPDException {
        throw new RuntimeException("not implemented");
    }

    public Matrix rank1(Matrix C) {
        throw new RuntimeException("not implemented");
    }

    public Matrix rank1(double alpha, Matrix C) {
        throw new RuntimeException("not implemented");
    }

    public Matrix transRank1(Matrix C) {
        throw new RuntimeException("not implemented");
    }

    public Matrix transRank1(double alpha, Matrix C) {
        throw new RuntimeException("not implemented");
    }

    public Matrix rank2(Matrix B, Matrix C) {
        throw new RuntimeException("not implemented");
    }

    public Matrix rank2(double alpha, Matrix B, Matrix C) {
        throw new RuntimeException("not implemented");
    }

    public Matrix transRank2(Matrix B, Matrix C) {
        throw new RuntimeException("not implemented");
    }

    public Matrix transRank2(double alpha, Matrix B, Matrix C) {
        throw new RuntimeException("not implemented");
    }

    public Matrix scale(double alpha) {
        throw new RuntimeException("not implemented");
    }

    public Matrix set(Matrix B) {
        throw new RuntimeException("not implemented");
    }

    public Matrix set(double alpha, Matrix B) {
        throw new RuntimeException("not implemented");
    }

    public Matrix add(Matrix B) {
        throw new RuntimeException("not implemented");
    }

    public Matrix add(double alpha, Matrix B) {
        throw new RuntimeException("not implemented");
    }

    public Matrix transpose() {
        throw new RuntimeException("not implemented");
    }

    public Matrix transpose(Matrix B) {
        throw new RuntimeException("not implemented");
    }

    public double norm(Norm type) {
        throw new RuntimeException("not implemented");
    }

    public Iterator iterator() {
        throw new RuntimeException("not implemented");
    }
}
