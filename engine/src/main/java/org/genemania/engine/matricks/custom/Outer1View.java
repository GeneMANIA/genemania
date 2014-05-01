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

import org.genemania.engine.core.MatrixUtils;
import org.genemania.engine.matricks.MatricksException;
import org.genemania.engine.matricks.Matrix;
import org.genemania.engine.matricks.MatrixCursor;
import org.genemania.engine.matricks.SymMatrix;

public class Outer1View extends AbstractMatrix implements SymMatrix {

    private static final long serialVersionUID = -7955896586715295648L;
    int size;
    FlexFloatArray newData;
    double scale;
    boolean zeroDiag;

    /*
     * from subset of backing specified by indices
     */
    public static Outer1View fromColumn(Matrix backingMatrix, int backingColumn, int [] indices, double scale, boolean zeroDiag) {
        if (backingMatrix instanceof FlexFloatColMatrix) {
            FlexFloatColMatrix m = (FlexFloatColMatrix) backingMatrix;
            FlexFloatArray backing = m.getColumn(backingColumn);
            return new Outer1View(backing, indices, scale, zeroDiag);
        }
        else {
            throw new RuntimeException("matrix type not supported: " + backingMatrix.getClass().getName());
        } 
    }
    
    /*
     * from entire backing
     */
    public static Outer1View fromColumn(Matrix backingMatrix, int backingColumn, double scale, boolean zeroDiag) {
        if (backingMatrix instanceof FlexFloatColMatrix) {
            FlexFloatColMatrix m = (FlexFloatColMatrix) backingMatrix;
            FlexFloatArray backing = m.getColumn(backingColumn);
            return new Outer1View(backing, scale, zeroDiag);
        }
        else {
            throw new RuntimeException("matrix type not supported: " + backingMatrix.getClass().getName());
        } 
    }
    
    /*
     * view of a subset of the indices. we actually take a copy of the subset array, and use
     * that as backing
     */
    public Outer1View(FlexFloatArray backing, int [] indices, double scale, boolean zeroDiag) {
        
        this.size = indices.length;
        this.scale = scale;
        this.zeroDiag = zeroDiag;
        
        int [] common = getCommonIndices(indices, indices.length, backing.indices, backing.used);
        int [] positions = getPositionsOfCommonIndicesOfAInB(indices, indices.length, backing.indices, backing.used);
        
        newData = new FlexFloatArray();
        for (int index=0; index<common.length; index++) {
            float val = (float) backing.get(common[index]); // oops, why the float cast ... interface is not uniform!
            newData.set(positions[index], val);
        }
    }
    
    /*
     * use entire array as backing
     */
    public Outer1View(FlexFloatArray backing, double scale, boolean zeroDiag) {
    	this.size = backing.used;
    	this.scale = scale;
    	this.zeroDiag = zeroDiag;
    	
    	newData = backing;
    }
    
    @Override
    public int numRows() {
        return this.size;    
    }

    @Override
    public int numCols() {
        return this.size;  
    }

    @Override
    public double get(int row, int col) {
        if (zeroDiag && row == col) {
            return 0d;
        }
        else {
            return scale * newData.get(row) * newData.get(col);
        }
    }

    @Override
    public void set(int row, int col, double val) throws MatricksException {
        throw new RuntimeException("read-only");    
    }
    
    @Override
    public double elementMultiplySum(Matrix m) throws MatricksException {
        if (m instanceof FlexSymFloatMatrix) {
            return m.elementMultiplySum(this); // for now, this ought to be somewhat optimized already
        }
        else if (m instanceof Outer1View) {   
            return elementMultiplySum((Outer1View) m);
        }
        else {
            return super.elementMultiplySum(m);
        }
    }
    
    public double elementMultiplySum(Outer1View m) throws MatricksException {
        double dot = newData.dot(m.newData);
        double sum = dot*dot;
        if (zeroDiag || m.zeroDiag) {
            sum = sum - newData.squaredDot(m.newData);
        }
        
        return sum*scale*m.scale;
    }

    @Override
    public double elementSum() throws MatricksException {
        double sum = newData.elementSum();
        
        sum = sum*sum;
        
        if (zeroDiag) {
            sum = sum-newData.elementSquaredSum();
        }
        
        return sum*scale;
    }
    
    @Override
    public MatrixCursor cursor() {

        return new MatrixCursor() {

            int i = -1;
            int j = 0;
            final int k = newData.used-1;

            @Override
            public boolean next() {
                if (i < k) {
                    i += 1;
                    return true;
                }
                else if (j < k) {
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
                return newData.indices[i];
            }

            @Override
            public int col() {
                return newData.indices[j];
            }

            @Override
            public double val() {
                if (zeroDiag && i == j) {
                    return 0d;
                }
                else {
                    return scale * newData.data[i] * newData.data[j];
                }
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

    /*
     * TODO: unit test please!
     * 
     * (non-Javadoc)
     * @see org.genemania.engine.matricks.Matrix#mult(double[], double[])
     */
    @Override
    public void mult(double[] x, double[] y) {
        Arrays.fill(y, 0);
        for (int i=0; i<newData.used; i++) {
            int row = newData.indices[i];
            for (int j=0; j<newData.used; j++) {
                int col = newData.indices[j];
                if (zeroDiag && i == j) {
                    continue;
                }
                y[row] += scale*x[col]; // could actually just multiply y through by scale at the end, 
            }
        } 
    }

    @Override
    public SymMatrix subMatrix(int[] rowcols) {
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
