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

/*
 * view of a rectangular subset of a matrix, read-only.
 */
public class MatrixView extends AbstractMatrix {
    private static final long serialVersionUID = 2531376276219392342L;
    Matrix backing;
    int [] rows;
    int [] cols;
    
    public MatrixView(Matrix backing, int[] rows, int [] cols) {
        this.backing = backing;
        this.rows = rows;
        this.cols = cols;
    }
    
    @Override
    public int numRows() {
        return rows.length;
    }

    @Override
    public int numCols() {
        return cols.length;
    }

    @Override
    public double get(int row, int col) {
        return backing.get(rows[row], cols[col]);
    }

    @Override
    public void set(int row, int col, double val) throws MatricksException {
        throw new MatricksException("read-only");
    }

    @Override
    public MatrixCursor cursor() {
        return new MatrixViewCursor();
    }
    
    /*
     * this is not an efficient iteration, doesn't know about
     * sparsity structure of backing matrix so visits all elements
     */
    private class MatrixViewCursor implements MatrixCursor {
        int i = 0;
        int j = -1;
        final int k;
        final int l;
        
        public MatrixViewCursor() {
            k = cols.length - 1;
            l = rows.length - 1;
        }

        @Override
        public boolean next() {
            if (j == k) {
                if (i < l) {
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
            throw new MatricksException("view is read-only");
        }
        
    }
    
    @Override
    public void mult(double[] x, double[] y) {
        throw new MatricksException("not implemented");
    }
}
