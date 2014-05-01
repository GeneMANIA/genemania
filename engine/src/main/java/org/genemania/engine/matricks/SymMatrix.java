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

/**
 *
 */
public interface SymMatrix extends Matrix {
    public void multAdd(final double alpha, double [] x, double [] y);
    public void mult(double [] x, double [] y);
    public void multAdd(double [] x, double [] y);
    public SymMatrix subMatrix(int [] rowcols);
    public void setDiag(double alpha);
    /*
     * A = A ./ (x*x')
     */
    public void dotDivOuterProd(Vector x);

    /*
     * A = A + (x*x')
     */
    public void addOuterProd(double [] x);

    /*
     * return sum(A .* (x*x'))
     */
    public double sumDotMultOuterProd(double [] x);
}
