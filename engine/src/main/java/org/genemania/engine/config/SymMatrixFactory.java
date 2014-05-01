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

package org.genemania.engine.config;

import org.genemania.engine.matricks.Matrix;
import org.genemania.engine.matricks.SymMatrix;
import org.genemania.engine.matricks.custom.FlexDoubleMatrix;
import org.genemania.engine.matricks.custom.FlexFloatColMatrix;
import org.genemania.engine.matricks.custom.FlexSymDoubleMatrix;

/**
 * symmetric sparse
 * 
 * wait, what happened here? shouldn't this be DoubleMatrixFactory?
 */
public class SymMatrixFactory implements MatrixFactory {

    @Override
    public Matrix sparseMatrix(int rows, int cols) {
        return new FlexDoubleMatrix(rows, cols);
    }

    @Override
    public SymMatrix symSparseMatrix(int size) {
        return new FlexSymDoubleMatrix(size);
    }
    
    @Override
    public Matrix sparseColMatrix(int rows, int cols) {
        return new FlexFloatColMatrix(rows, cols); // haven't implemented double type for this! TODO
    }
}
