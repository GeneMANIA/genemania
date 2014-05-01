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
public interface Vector {
    public int getSize();

    public double get(int index);
    public void set(int index, double val) throws MatricksException;

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

    public VectorCursor cursor();

    public void add(Vector B) throws MatricksException;
    public void add(double a, Vector B) throws MatricksException;

    public double elementSum();

    public int [] findIndexesOf(double val);

    public int countMatches(double x);
    public void findReplace(double oldValue, double newValue);

    public void setEqual(Vector b);

    public double dot(Vector b) throws MatricksException;
}
