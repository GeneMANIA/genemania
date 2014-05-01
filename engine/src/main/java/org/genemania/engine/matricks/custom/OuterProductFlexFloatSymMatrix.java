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

public class OuterProductFlexFloatSymMatrix extends AbstractMatrix implements
        SymMatrix {

    final int size;
    FlexFloatArray data;
    double scale;
    boolean zeroDiag;
    
    public OuterProductFlexFloatSymMatrix(int size, FlexFloatArray data, double scale, boolean zeroDiag) {
        super();
        this.size = size;
        this.data = data;
        this.scale = scale;
        this.zeroDiag= zeroDiag;
    }

    @Override
    public int numRows() {
        return size;
    }

    @Override
    public int numCols() {
        return size;
    }

    @Override
    public double get(int row, int col) {
        if (row == col) {
            return 0d;
        }
        
        double result = data.get(row);
        if (result != 0d) {  // save a lookup in sparse structure if 0
            return scale * result * data.get(col);
        }
        else {
            return 0d;
        }
    }

    @Override
    public void set(int row, int col, double val) throws MatricksException {
        throw new RuntimeException("not implemented");   
    }

    @Override
    public MatrixCursor cursor() {

        return new MatrixCursor() {

            int i = -1;
            int j = 0;

            @Override
            public boolean next() {
                if (i < data.used) {
                    i += 1;
                    return true;
                }
                else if (j < data.used) {
                    i = 0;
                    j += 1;
                    return true;
                }
                else {
                    return false;
                }
            }

            @Override
            public int row() {
                return data.indices[i];
            }

            @Override
            public int col() {
                return data.indices[j];
            }

            @Override
            public double val() {
                if (zeroDiag && i == j) {
                    return 0d;
                }
                
                return scale * data.data[i] * data.data[j];
            }

            @Override
            public void set(double val) {
                throw new RuntimeException("read-only");
            }
        };
    }

    @Override
    public void multAdd(double alpha, double[] x, double[] y) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void mult(double[] x, double[] y) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public SymMatrix subMatrix(int[] rowcols) {
        throw new RuntimeException("not implemented");
    }
    
    @Override 
    public Matrix subMatrix(int [] rows, int [] cols) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void setDiag(double alpha) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void addOuterProd(double[] x) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public double sumDotMultOuterProd(double[] x) {
        throw new RuntimeException("not implemented");
    }
}
