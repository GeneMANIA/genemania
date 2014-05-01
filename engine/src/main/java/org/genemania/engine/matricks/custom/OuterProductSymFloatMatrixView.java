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
import org.genemania.engine.matricks.MatrixCursor;
import org.genemania.engine.matricks.SymMatrix;

public class OuterProductSymFloatMatrixView extends AbstractMatrix implements
        SymMatrix {

    OuterProductFlexFloatSymMatrix backing;
    int [] indices;
    int [] commonIndices;
    
    public OuterProductSymFloatMatrixView(OuterProductFlexFloatSymMatrix backing, int [] indices) {
        this.backing = backing;
        this.indices = indices;
        this.commonIndices = getCommonIndices(indices, indices.length, backing.data.indices, backing.data.used);
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
        throw new RuntimeException("read-only");

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
}
