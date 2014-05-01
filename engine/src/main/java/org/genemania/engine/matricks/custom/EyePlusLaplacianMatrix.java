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

import no.uib.cipr.matrix.DenseVector;

import org.genemania.engine.core.MatrixUtils;
import org.genemania.engine.matricks.MatricksException;
import org.genemania.engine.matricks.MatrixCursor;
import org.genemania.engine.matricks.SymMatrix;
import org.genemania.engine.matricks.Vector;

/*
 * read-only matrix class to facilitate computations
 * of the form:
 * 
 *   y = y + M * x
 *   
 * where
 * 
 *   M = I + L = I + D - W
 *   
 * for some given symmetric weight matrix W with zero diag. 
 * W is not modified and needs only itself implement matrix-vector 
 * multiplication. The main use-case for this is in a CG-solver.
 */
public class EyePlusLaplacianMatrix extends AbstractMatrix implements SymMatrix {
    private static final long serialVersionUID = 4350267948942777088L;
    
    SymMatrix backing;
    DenseVector diag;
    
    public EyePlusLaplacianMatrix(SymMatrix backing) {
        this.backing = backing;
        computeDiag();
    }
    
    private void computeDiag() {
        diag = new DenseVector(backing.numRows());
        backing.rowSums(diag.getData());
        MatrixUtils.add(diag, 1d);
    }
    
    @Override
    public int numRows() {
        return backing.numRows();
    }

    @Override
    public int numCols() {
        return backing.numCols();
    }

    @Override
    public double get(int row, int col) {
        if (row == col) {
            return diag.get(row);
        }
        
        return -backing.get(row, col);
    }

    @Override
    public void set(int row, int col, double val) throws MatricksException {
        throw new MatricksException("read-only");            
    }

    @Override
    public MatrixCursor cursor() {
        throw new MatricksException("Not implemented");    
    }
    
    @Override
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

    @Override
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
    
    @Override
    // todo: update to use vector ops
    public void multAdd(double alpha, double[] x, double[] y) {
        int l = backing.numRows();
        for (int i=0; i<l; i++) {
            y[i] = -y[i];
        }

        backing.multAdd(x, y);

        for (int i=0; i<l; i++) {
            y[i] = diag.get(i)*x[i] - y[i]; 
        }
    }

    @Override
    // todo: update to use vector ops
    public void mult(double[] x, double[] y) {
        int l = backing.numRows();
        for (int i=0; i<l; i++) {
            y[i] = -y[i];
        }

        backing.mult(x, y);
        
        // todo: update to use vector ops

        for (int i=0; i<l; i++) {
            y[i] = diag.get(i)*x[i] - y[i]; 
        }
    }

    @Override
    public SymMatrix subMatrix(int[] rowcols) {
        throw new MatricksException("Not implemented");    
    }

    @Override
    public void setDiag(double alpha) {
        throw new MatricksException("read-only");      
    }

    @Override
    public void addOuterProd(double[] x) {
        throw new MatricksException("Not implemented");    
    }

    @Override
    public double sumDotMultOuterProd(double[] x) {
        throw new MatricksException("Not implemented");   
    }

}
