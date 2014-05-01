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
 * MultiOuterProductComboSymMatrix except that's too long
 * a name even for me.
 * 
 * This is a read-only symmetric matrix backed by one symmetric
 * matrix and any number of OuterProductCombo symmetric matrices.
 * The matrix represented is the sum of all these matrices:
 * 
 *   TODO: install latex :)
 * 
 * Main use is implementing matrix-vector multiplication without
 * materializing all the outer-products. 
 */
public class MultiOPCSymMatrix extends AbstractMatrix implements SymMatrix {
    private static final long serialVersionUID = 4356797702972735183L;
    
    private int size;
    private SymMatrix matrix;
    private OuterProductComboSymMatrix [] combos;
    
    public MultiOPCSymMatrix(SymMatrix matrix, OuterProductComboSymMatrix... combos) {
        this.matrix = matrix;
        this.combos = combos;
        size = matrix.numRows();
        checkSizes();
    }
    
    private void checkSizes() {
        for (OuterProductComboSymMatrix combo: this.combos) {
            if (combo.numRows() != size) {
                throw new MatricksException("inconsistent matrix sizes");
            }
        }
    }
    
    public int numRows() {
        return this.size;
    }

    public int numCols() {
        return this.size;
    }

    public double get(int row, int col) {
        double val = matrix.get(row, col);
        for (OuterProductComboSymMatrix combo: this.combos) {
            val += combo.get(row, col);
        }

        return val;
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
        matrix.rowSums(result);
        
        double [] ones = new double[result.length];
        for (int i=0; i<ones.length; i++) {
            ones[i] = 1d;
        }
        
        for (OuterProductComboSymMatrix combo: combos) {
            combo.multAdd(ones, result);
        }        
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

    public void compact() {
        throw new MatricksException("Not implemented");  
    }

    public void multAdd(double alpha, double[] x, double[] y) {
        throw new MatricksException("Not implemented");  
    }

    public void mult(double[] x, double[] y) {
        matrix.mult(x, y);
        for (OuterProductComboSymMatrix combo: this.combos) {
            combo.multAdd(x, y);
        }
    }

    public void multAdd(double[] x, double[] y) {
        matrix.multAdd(x, y);
        for (OuterProductComboSymMatrix combo: this.combos) {
            combo.multAdd(x, y);
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

    public SymMatrix getMatrix() {
        return matrix;
    }

    public OuterProductComboSymMatrix[] getCombos() {
        return combos;
    }
}
