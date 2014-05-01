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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.genemania.engine.matricks.mtj;

import java.util.Iterator;
import no.uib.cipr.matrix.VectorEntry;
import org.genemania.engine.core.MatrixUtils;
import org.genemania.engine.matricks.MatricksException;
import org.genemania.engine.matricks.Vector;
import org.genemania.engine.matricks.VectorCursor;

/**
 *
 */
public class DenseDoubleVector implements Vector {
    no.uib.cipr.matrix.Vector v;


    public DenseDoubleVector(int size) {
        v = new no.uib.cipr.matrix.DenseVector(size);
    }

    public DenseDoubleVector(no.uib.cipr.matrix.Vector v) {
        this.v = v;
    }
    
    public int [] findIndexesOf(double x) {
        return MatrixUtils.find(v, x);
    }

    public double elementSum() {
        return MatrixUtils.sum(v);
    }

    public void add(Vector y) {
        throw new RuntimeException("not implemented");
    }

    public void add(DenseDoubleVector y) {
        v.add(y.v);
    }

    public void add(double a, Vector  y) {
        throw new RuntimeException("not implemented");
    }

    public void add(double a, DenseDoubleVector y) {
        v.add(a, y.v);
    }

    public VectorCursor cursor() {
        return new DenseDoubleVectorCursor();
    }

    public void setAll(double x) {
        throw new RuntimeException("not implemented");
    }

    public void setEqual(Vector b) {
        if (b instanceof no.uib.cipr.matrix.Vector) {
            no.uib.cipr.matrix.Vector bb = (no.uib.cipr.matrix.Vector) b;
            setEqual(bb);
        }
        else {
            throw new RuntimeException("not implemented for type: " + b.getClass().getName());
        }
    }
    
    public void setEqual(no.uib.cipr.matrix.Vector b) {
        v.set(b);
    }

    public void scale(double x) {
        v.scale(x);
    }

    public void set(int index, double a) {
        v.set(index, a);
    }

    public double get(int index) {
        return v.get(index);
    }

    public int getSize() {
        return v.size();
    }

    public int countMatches(double x) {
        return MatrixUtils.countMatches(v, x);
    }
    
    public void findReplace(double oldValue, double newValue) {
        MatrixUtils.setMatches(v, oldValue, newValue);
    }
    
    private class DenseDoubleVectorCursor implements VectorCursor {
        Iterator<VectorEntry> iter = v.iterator();
        VectorEntry e;
        
        public boolean next() {
            if (iter.hasNext()) {
                e = iter.next();
                return true;
            }
            else {
                return false;
            }
        }

        public int index() {
            return e.index();
        }

        public double val() {
            return e.get();
        }

        public void set(double val) {
            e.set(val);
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
        return v.dot(b.v);
    }
}
