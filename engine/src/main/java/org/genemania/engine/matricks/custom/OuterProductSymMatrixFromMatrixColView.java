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

/*
 * read-only view of a given column of a given matrix
 * as a symmetric matrix formed by the outer product of
 * the given column.
 */
public class OuterProductSymMatrixFromMatrixColView extends AbstractMatrix implements
        SymMatrix {
    private static final long serialVersionUID = 6552288568343974052L;
    final Matrix backing;
    final int backingCol;
    double scale;
    boolean zeroDiag;
    
    public OuterProductSymMatrixFromMatrixColView(Matrix backing, int backingCol, double scale, boolean zeroDiag) {
        this.backing = backing;
        this.backingCol = backingCol;
        this.scale = scale;
        this.zeroDiag = zeroDiag;
    }
    
    @Override
    public int numRows() {
        return backing.numRows();
    }

    @Override
    public int numCols() {
        return backing.numRows(); // symmetric
    }

    @Override
    public double get(int row, int col) {
        if (zeroDiag && row == col) {
            return 0;
        }
        return scale*backing.get(row, backingCol) * backing.get(col, backingCol);
    }

    @Override
    public void set(int row, int col, double val) throws MatricksException {
        throw new MatricksException("read only view");
    }

    @Override
    public MatrixCursor cursor() {
        throw new MatricksException("Not implemented");    
    }


    @Override
    public void multAdd(double alpha, double[] x, double[] y) {
        throw new MatricksException("Not implemented");    
    }


    @Override
    public void mult(double[] x, double[] y) {
        throw new MatricksException("Not implemented");    
    }


    @Override
    public SymMatrix subMatrix(int[] rowcols) {
        throw new MatricksException("Not implemented");    
    }


    @Override
    public void setDiag(double alpha) {
        throw new MatricksException("Not implemented");    
    }


    @Override
    public void addOuterProd(double[] x) {
        throw new MatricksException("Not implemented");    
    }


    @Override
    public double sumDotMultOuterProd(double[] x) {
        throw new MatricksException("Not implemented");    
    }
    
    /*
     * returns a matrix view into this (outer-product of a vector)
     * matrix. idea is to optimize the matrix iterator with knowledge
     * of the underlying data
     */
    public Matrix getMatrixView(int [] rowIndices, int [] colIndices) {
        return null;        
    }
}
