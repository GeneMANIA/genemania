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

import java.util.Arrays;
import org.genemania.engine.matricks.MatricksException;
import org.genemania.engine.matricks.Vector;
import org.genemania.engine.matricks.VectorCursor;

/**
 *
 */
public class DenseDoubleVector extends AbstractVector implements DoubleVector {
    int size;
    double [] data;

    public DenseDoubleVector(int size) {
        this.size = size;
        data = new double[size];
    }

    public DenseDoubleVector(double [] data) {
        this.size = data.length;
        this.data = data;
    }

    public int getSize() {
        return size;
    }

    public double get(int index) {
        return data[index];
    }

    public void set(int index, double val) {
        data[index] = val;
    }

    public void setAll(double a) {
        Arrays.fill(data, a);
    }

    public void scale(final double a) {
       for (int i=0; i<size; i++) {
                data[i] = data[i]*a;
        }
    }

    public VectorCursor cursor() {
        return new DenseDoubleVectorCursor();
    }

    private class DenseDoubleVectorCursor implements VectorCursor {
        int index=-1;

        public boolean next() {
            index += 1;
            if (index == size) {
                return false;

            }
            return true;
        }

        public int index() {
            return index;
        }

        public double val() {
            return data[index];
        }

        public void set(double val) {
            data[index] = val;
        }
    }

    public void setEqual(Vector b) {
        if (b instanceof DenseDoubleVector) {
            DenseDoubleVector bb = (DenseDoubleVector) b;
            setEqual(bb);
        }
        else {
            throw new RuntimeException("not implemented for type: " + b.getClass().getName());
        }
    }

    public void setEqual(DenseDoubleVector b) {
        int n = b.getSize();
        if (n != data.length) {
            throw new RuntimeException("inconsisten data sizes");
        }
        System.arraycopy(b.data, 0, data, 0, n);
    }

    
    public int countMatches(final double x) {
        int matches = 0;

        for (int i=0; i<data.length; i++) {
            if (data[i] == x) {
                matches ++;
            }
        }
        return matches;
    }

    public void findReplace(final double oldValue, final double newValue) {
        for (int i=0; i<data.length; i++) {
            if (data[i] == oldValue) {
                data[i] = newValue;
            }
        }
    }

    public double dot(Vector b) throws MatricksException {
        if (b instanceof DenseDoubleVector) {
            DenseDoubleVector bb = (DenseDoubleVector) b;
            return dot(bb);
        }
        else {
            throw new RuntimeException("unsupported type: " + b.getClass().getName());
        }
    }

    public double dot(DenseDoubleVector b) throws MatricksException {
        if (getSize() != b.getSize()) {
            throw new MatricksException("inconsistent size");
        }

        double result = 0d;
        for (int i=0; i<getSize(); i++) {
            result += data[i]*b.data[i];
        }

        return result;
    }
}
