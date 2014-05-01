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
import org.genemania.engine.matricks.Vector;

/*
 * Backed by a dense vector. do we want sparse too?
 */
public class DoubleDiagonalMatrix extends AbstractMatrix {
    
    int size;
    private Vector data;

    public DoubleDiagonalMatrix(int size) {
        super();
        this.size = size;
        alloc();
    }
    
    public DoubleDiagonalMatrix(Vector data) {
        super();
        this.size = data.getSize();
        this.data = data;
    }

    private void alloc() {
        data = new DenseDoubleVector(size);
    }
    
    public int numRows() {
        return this.size;
    }

    public int numCols() {
        return this.size;
    }

    public double get(int row, int col) {
        if (row == col) {
            return data.get(row);
        }
        else {
            return 0;
        }
    }

    public void set(int row, int col, double val) throws MatricksException {
        if (row == col) {
            data.set(row, val);
        }
        else {
            throw new MatricksException("index error, diagonal matrix row must equal col"); 
        }
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

    public void mult(double [] x, double [] y) {
        checkSize(x);
        checkSize(y);
        
        for (int i=0; i<size; i++) {
            y[i] = x[i]*data.get(i);
        }
    }
    
    public void multAdd(double[] x, double[] y) {
        checkSize(x);
        checkSize(y);
        
        for (int i=0; i<size; i++) {
            y[i] = y[i] + x[i]*data.get(i);
        }
    }

    public void transMult(double[] x, double[] y) {
        throw new MatricksException("Not implemented"); 
    }

    public void compact() {
        throw new MatricksException("Not implemented"); 
    }
    
    private void checkSize(final double[] x) {
        if (x.length != size) {
            throw new IndexOutOfBoundsException(String.format("invalid size: %d", x.length));            
        }
    }
}
