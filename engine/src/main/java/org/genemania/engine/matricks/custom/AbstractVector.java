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
import org.genemania.engine.matricks.Vector;
import org.genemania.engine.matricks.VectorCursor;

/**
 *
 */
public abstract class AbstractVector implements Vector {

    public void scale(final double a) throws MatricksException {
        VectorCursor cursor = this.cursor();
        while (cursor.next()) {
            this.set(cursor.index(), cursor.val()*a);
        }
    }

    public void setAll(final double a) throws MatricksException {
        VectorCursor cursor = this.cursor();
        while (cursor.next()) {
            this.set(cursor.index(), a);
        }
    }

    /*
     * common fallback implementation
     */
    public void add(Vector B) throws MatricksException {
        if (B.getSize() != this.getSize()) {
            throw new MatricksException("incompatible size for addition");
        }

        VectorCursor cursor = B.cursor();
        while (cursor.next()) {
            final int index = cursor.index();
            double v = cursor.val() + this.get(index);
            this.set(index, v);
        }
    }

    public void add(double a, Vector B) throws MatricksException {
        if (B.getSize() != this.getSize()) {
            throw new MatricksException("incompatible size for addition");
        }

        VectorCursor cursor = B.cursor();
        while (cursor.next()) {
            final int index = cursor.index();
            double v = a*cursor.val() + this.get(index);
            this.set(index, v);
        }
    }

    public double elementSum() {
        double sum = 0d;

        VectorCursor cursor = this.cursor();
        while (cursor.next()) {
           sum += cursor.val();
        }

        return sum;
    }

    public int [] findIndexesOf(double val) {
        throw new RuntimeException("not implemented");
    }
}
