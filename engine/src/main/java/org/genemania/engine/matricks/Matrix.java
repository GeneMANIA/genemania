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

package org.genemania.engine.matricks;

import java.io.IOException;
import java.io.Serializable;

/**
 *
 */
public interface Matrix extends Serializable {
    public int numRows();
    public int numCols();

    public double get(int row, int col);
    public void set(int row, int col, double val) throws MatricksException;

    /*
     * A = a*A
     */
    public void scale(double a) throws MatricksException;

    /*
     * C = A*B;
     */
    //public void mult(Matrix B, Matrix C);

    /*
     * 
     */
    public void setAll(double a) throws MatricksException;

    public MatrixCursor cursor();

    public void add(Matrix B) throws MatricksException;
    public void add(double a, Matrix B) throws MatricksException;

    public double elementSum();
    public double elementMultiplySum(Matrix m) throws MatricksException;

    public void CG(Vector x, Vector y) throws MatricksException;
    public void QR(Vector x, Vector y) throws MatricksException;

    public Vector rowSums() throws MatricksException;
    public Vector columnSums() throws MatricksException;

    public void rowSums(double [] result) throws MatricksException;
    public void columnSums(double [] result) throws MatricksException;

    public void setToMaxTranspose() throws MatricksException;

    /*
     * y = alpha*A*x + y
     */
    public void multAdd(double alpha, Vector x, Vector y);

    /*
     * y = A*x;
     */
    public void mult(Vector x, Vector y);
    public Matrix subMatrix(int [] rows, int [] cols);

    public void add(int i, int j, double alpha);
    
    public void mult(double [] x, double []y);
    public void multAdd(double [] x, double [] y);
    
//    public Matrix subMatrix(Matrix m, int [] rows, int [] cols);

    
    /*
     * y = A'*x
     */
    public void transMult(double [] x, double [] y);
    public void compact();
    
    public MatrixAccumulator accumulator();
    
    /*
     * Portable I/O in text format, see 
     * http://math.nist.gov/MatrixMarket/formats.html#MMformat
     * 
     * trying to avoid the urge to over-engineer here, e.g.
     * creating a hierarchy of matrix reader and write objects
     * to be able to support different formats etc. can do that
     * if/when needed.
     */    
    public void mmwrite(String filename) throws IOException;
    public Matrix mmread(String filename) throws IOException;
}
