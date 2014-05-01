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
import org.genemania.engine.matricks.OuterProductSymMatrix;
import org.genemania.engine.matricks.SymMatrix;
import org.genemania.engine.matricks.Vector;

/*
 * dense float implementation of a symmetric matrix
 * backed by a vector and with matrix elements computed
 * on the fly as outer products
 */
public class OuterProductSymFloatMatrix extends AbstractMatrix implements OuterProductSymMatrix {
    private static final long serialVersionUID = 327153796421326669L;
    int size; // rows == cols
    float [] data;

    public OuterProductSymFloatMatrix(float [] data) {
        this.data = data;
        this.size = data.length;
    }
    
    public OuterProductSymFloatMatrix(int size) {
        this.size = size;
        alloc();
    }
    
    private void alloc() {
        data = new float[size];
    }
    
    public int numRows() {
        return this.size;
    }

    public int numCols() {
        return this.size;
    }

    public double get(int row, int col) {
        return data[row]*data[col];
    }

    public void set(int pos, double val) {
        data[pos] = (float) val;
    }
    
    public void set(int row, int col, double val) throws MatricksException {
        throw new MatricksException("can't set matrix element for Outer-Product matrix");
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
        if (m instanceof OuterProductSymFloatMatrix) {
            return elementMultiplySum((OuterProductSymFloatMatrix) m);
        }
        throw new MatricksException("Not implemented");
    }
    
    /*
     * compute lower triangle sum, double, and add in diagonal
     */
    public double elementMultiplySum(OuterProductSymFloatMatrix m) throws MatricksException {
        if (size != m.size) {
            throw new MatricksException("size mismatch");
        }
        
        double sum = 0;
        for (int i=0; i<size; i++) {
            for (int j=0; j<j; j++) {
                sum += data[i]*data[j]*m.data[i]*m.data[j];
            }
        }
        
        sum = 2*sum;
        
        for (int i=0; i<size; i++) {
            sum += data[i]*data[i]*m.data[i]*m.data[i];
        }
        
        return sum;
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
        throw new MatricksException("Not implemented");
    }

    public void mult(Vector x, Vector y) {
        throw new MatricksException("Not implemented");
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

    public void compact() {
        throw new MatricksException("Not implemented");
    }

    public void multAdd(double alpha, double[] x, double[] y) {
        throw new MatricksException("Not implemented");
    }

    public void mult(double[] x, double[] y) {
        throw new MatricksException("Not implemented");
    }

    public void multAdd(double[] x, double[] y) {
        throw new MatricksException("Not implemented");
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
