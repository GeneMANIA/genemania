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
 * symmetric view into a symmetric matrix. read-only
 */
public class SymMatrixView extends AbstractMatrix implements SymMatrix {
    private static final long serialVersionUID = -1085410256279321744L;
    
    SymMatrix backing;
    int [] indices; // size = indices.length
    
    public SymMatrixView(SymMatrix backing, int [] indices) {
        this.backing = backing;
        this.indices = indices;
    }
    
    @Override
    public int numRows() {
        return indices.length;
    }

    @Override
    public int numCols() {
        return indices.length;
    }

    @Override
    public double get(int row, int col) {
        return backing.get(indices[row], indices[col]);
    }

    @Override
    public void set(int row, int col, double val) throws MatricksException {
        throw new MatricksException("read-only");
    }

    @Override
    public MatrixCursor cursor() {
        return new SymMatrixViewCursor();
    }

    /*
     * this doesn't take into account any sparsity in the backing vector,
     * and so iterates over all size*size elements of the matrix
     * including zeros.
     */
    private class SymMatrixViewCursor implements MatrixCursor {
        int i = 0;
        int j = -1;
        final int k;

        public SymMatrixViewCursor() {
            k = indices.length - 1;
        }
        
        @Override
        public boolean next() {
            if (j == k) {
                if (i < k) {
                    i += 1;
                    j = 0;
                    return true;
                }
                else {
                    return false;
                }
            }
            else {
                j += 1;
                return true;
            }
        }

        @Override
        public int row() {
            return i;
        }

        @Override
        public int col() {
            return j;
        }

        @Override
        public double val() {
            return get(i, j);
        }

        @Override
        public void set(double val) {
            throw new MatricksException("read-only");           
        }        
    }
    
    @Override
    public void multAdd(double alpha, double[] x, double[] y) {
        throw new MatricksException("not implemented");
    }

    @Override
    public void mult(double[] x, double[] y) {
        throw new MatricksException("not implemented");
    }

    @Override
    public SymMatrix subMatrix(int[] rowcols) {
        throw new MatricksException("not implemented");
    }

    @Override
    public void setDiag(double alpha) {
        throw new MatricksException("not implemented");
    }

    @Override
    public void addOuterProd(double[] x) {
        throw new MatricksException("not implemented");
    }

    @Override
    public double sumDotMultOuterProd(double[] x) {
        throw new MatricksException("not implemented");
    }
}
