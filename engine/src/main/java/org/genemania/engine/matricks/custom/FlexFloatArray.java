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

package org.genemania.engine.matricks.custom;

import java.io.Serializable;
import org.genemania.engine.matricks.MatricksException;
import org.genemania.engine.matricks.MatrixCursor;
import org.genemania.engine.matricks.Utils;

/**
 * actual # of elements allocated = used
 * 
 * data.length > used since we overallocate
 * 
 * size is just a limit, so when our overallocation grows 
 * by e.g. factor of 2 but we know we'll never need more than
 * size, we can limit our growth to not waste space.
 *
 * data.length == indices.length always
 * 
 * TODO: size is confusing, should rename to max_size.
 *
 * TODO: don't think i want an initial size of k (eg 8), i want an
 * initial size of 0, and then a size of k on first real allocation, then
 * doubling. eg if mostly we have 0 sized entries, but in the cases where its
 * non zero we don't expect to have just 1 or 2, but a few. Could do this
 * one level up in flexsymmatrix as well, allocating null entries in the array.
 */
public class FlexFloatArray implements Serializable {
    private static final long serialVersionUID = 3079385376377680800L;

    public static final int FIRST_ALLOC_SIZE = 8;
    
    int size;
    int used;
    float [] data;
    int [] indices;

    public FlexFloatArray() {
        this(Integer.MAX_VALUE, FIRST_ALLOC_SIZE); // grow crazy
    }
    
    public FlexFloatArray(int size) {
        this(size, FIRST_ALLOC_SIZE);
    }

    public FlexFloatArray(int size, int nz) {
        this.size = size;
        alloc(nz);
    }

    private void alloc(int nz) {
        data = new float[nz];
        indices = new int[nz];
    }
    
    public double get(int index) {
        int pos = Utils.binarySearch(indices, index, 0, used);

        if (pos >= 0) {
            return data[pos];
        }
        else {
            return 0;
        }
    }

    public void set(int index, float val) throws MatricksException {
        int pos = Utils.myBinarySearch(indices, index, 0, used);

        if (pos >= 0) {
            data[pos] = val;
        }
        else {
            insert(-pos-1, index, val);
        }
    }

    /*
     * insert val before index
     */
    private void insert(int pos, int index, float val) throws MatricksException {
        //System.out.println("want to insert at pos " + pos + " an index of " + index + " with value " + val);
        if (used < data.length) {
            // we have enough space, shift data around
            // and add this data in

            System.arraycopy(indices, pos, indices, pos+1, used-pos);
            System.arraycopy(data, pos, data, pos+1, used-pos);
            indices[pos] = index;
            data[pos] = val;
            used += 1;

        }
        else {

            // need to allcoate new space         
            int newsize = getNewSize();
//            System.out.println("re-allocing! " + newsize);
            float[] newData = new float[newsize];
            int[] newIndices = new int[newsize];

            System.arraycopy(indices, 0, newIndices, 0, pos);
            System.arraycopy(data, 0, newData, 0, pos);

            System.arraycopy(indices, pos, newIndices, pos+1, used-pos);
            System.arraycopy(data, pos, newData, pos+1, used-pos);

            newIndices[pos] = index;
            newData[pos] = val;

            used += 1;
            indices = newIndices;
            data = newData;
        }

    }

    private int getNewSize() throws MatricksException {
        int newsize;
        if (data == null || data.length == 0) {
            newsize = FIRST_ALLOC_SIZE;
        }
        else if (data.length >= size) {
            throw new MatricksException(String.format("already at max size of %s", size));
        }
        else {
            newsize = data.length * 2;
            if (newsize > size) {
                newsize = size;
            }
        }

        return newsize;
    }
    
    /*
     * just get the next larger power of two to min, up to a max of size
     */
    private static int getNewSize(int size, int maxsize) throws MatricksException {
        int newsize;
        
        if (size == 0) {
            newsize = FIRST_ALLOC_SIZE;
        }
        else {
         double exponent = Math.ceil(Math.log(size)/Math.log(2)); 
         newsize = (int) Math.round(Math.pow(2, exponent));
            newsize = Math.min(newsize, maxsize);
        }

        return newsize;
    }

    public MatrixCursor cursor() {
       return new FlexFloatArrayCursor();
    }

    /*
     * implement matrix cursor interface as a one column matrix
     */
    private class FlexFloatArrayCursor implements MatrixCursor {
        private int index = -1;

        public boolean next() {
            index += 1;
            if (index >= used) {
                return false;
            }

            return true;
        }

        public int row() {
            return indices[index];
        }

        public int col() {
            return 0;
        }

        public double val() {
            return data[index];
        }

        /*
         * note loss of precision!
         */
        public void set(double val) {
            data[index] = (float) val;
        }
    }

    public double dot(DenseDoubleVector v) {
        return dot(v.data);
//        double result = 0d;
//        for (int index=0; index<used; index++) {
//            int row = indices[index];
//            double x = data[index];
//            x = x * v.data[row];
//            result += x;
//        }
//
//        return result;
    }

    public double dot(double [] v) {
        double result = 0d;
        for (int index=0; index<used; index++) {
            int row = indices[index];
            double x = data[index];
            x = x * v[row];
            result += x;
        }

        return result;
    }

    public double dot(float [] v) {
        double result = 0d;
        for (int index=0; index<used; index++) {
            int row = indices[index];
            double x = data[index];
            x = x * v[row];
            result += x;
        }

        return result;
    }

    /*
     * v = v + alpha*x
     */
    public void add(final double alpha, DenseDoubleVector v) {
        add(alpha, v.data);
    }
    
    /*
     * v = v + alpha*x
     */
    public void add(final double alpha, double [] v) {
        for (int index=0; index<used; index++) {
            int row = indices[index];
            double x = data[index];
            v[row] = v[row] + alpha*x;
        }
    }
    
    /*
     * v[i+offset] = v[i+offset] + alpha*x[i]
     */
    public void add(final double alpha, final float [] v, final int offset) {
        for (int index=0; index<used; index++) {
            final int row = indices[index];
//            v[offset+row] = v[offset+row] + (float)alpha*data[index];
            v[offset+row] = (float) (v[offset+row] + alpha*data[index]);
        }
    }

    public void add(final double alpha, final double [] v, final int offset) {
        for (int index=0; index<used; index++) {
            final int row = indices[index];
            v[offset+row] = v[offset+row] + alpha*data[index];
        }
    }
    
    /*
     * v[i+offset] = v[i+offset] + x[i]
     */
    public void add(final float [] v, final int offset) {
        for (int index=0; index<used; index++) {
            final int row = indices[index];
            v[offset+row] = v[offset+row] + data[index];
        }
    }

    public void add(final double [] v, final int offset) {
        for (int index=0; index<used; index++) {
            final int row = indices[index];
            v[offset+row] = v[offset+row] + data[index];
        }
    }
    
    protected void partialMult(double [] x, double [] y, final int k) {
        final double z = x[k];
        double s = 0d;
        
        for (int index=0; index<used; index++) {
            int row = indices[index];
            double w = data[index];
            s += w*x[row];
            y[row] += w*z;
        }

        y[k] += s;
    }

    protected void partialMult(final double alpha, double [] x, double [] y, final int k) {
        final double z = x[k];
        double s = 0d;

        for (int index=0; index<used; index++) {
            int row = indices[index];
            double w = data[index];
            s += w*x[row];
            y[row] += alpha*w*z;
        }

        y[k] += alpha*s;
    }

    protected void partialSum(double [] y, final int k) {
        double s = 0d;

        for (int index=0; index<used; index++) {
            int row = indices[index];
            double x = data[index];
            s += x;
            y[row] += x;
        }

        y[k] += s;
    }

    public void scale(final double alpha) {
        for (int index=0; index<used; index++) {
            data[index] = (float) (alpha*data[index]);
        }
    }

    /*
     *
     *    y[i] = y[i] + alpha*this[i]
     *
     */
    public void dotMultAdd(double [] y, final double alpha) {
        for (int index=0; index<used; index++) {
            int row = indices[index];
            double x = data[index];
            y[row] += alpha*x;
        }      
    }

    /*
     * this = this ./ (alpha*x)
     */
    public void dotDiv(final double alpha, double [] x) {
        for (int index=0; index<used; index++) {
            int row = indices[index];
            data[index] = (float) (data[index] / (alpha * x[row]));
        }
    }

    /*
     * this = this ./ x
     */
    public void dotDiv(final double [] x) {
        for (int index=0; index<used; index++) {
            int row = indices[index];
            data[index] = (float) (data[index] / x[row]);
        }
    }

    /*
     * this = this + x
     */
    public void add(FlexFloatArray x) throws MatricksException {
        for (int index=0; index<x.used; index++) {

            int row = x.indices[index];
            float x_at_row = x.data[index];

            add(row, x_at_row);
        }
    }

    /*
     * x = x + this
     */
    public void addTo(double [] x) throws MatricksException {
        for (int index=0; index<used; index++) {
            int row = indices[index];
            x[row] = x[row] + data[index];
        }
    }

    public void add(final double alpha, FlexFloatArray x) throws MatricksException {
        for (int index=0; index<x.used; index++) {

            int row = x.indices[index];
            float x_at_row = x.data[index];

            add(row, (float) (alpha*x_at_row));
        }
    }

    /*
     * experimental optimization. note we always set the work arrays so as never
     * to have to clear them first. 
     */
    public void addWithWorkArrays(final double alpha, FlexFloatArray x,
            int[] workIndices, float[] workData) throws MatricksException {
        
        // adding nothing? do nothing!
        if (x.used == 0) {
            return;
        }
        
        int i = 0; // looping over this object
        int ix = 0; // looping over x
        int iwork = 0; // looping over work arrays

        int nextpos = Integer.MAX_VALUE;
        int nextxpos = Integer.MAX_VALUE;
        if (i < used) {
            nextpos = indices[i];
        }
        if (ix < x.used) {
            nextxpos = x.indices[ix];
        }

        while (i < used || ix < x.used) {
            if (nextpos < nextxpos) {
                workIndices[iwork] = nextpos;
                workData[iwork] = data[i];
                i++;
                iwork++;
                if (i < used) {
                    nextpos = indices[i];
                } else {
                    nextpos = Integer.MAX_VALUE;
                }
            } else if (nextpos > nextxpos) {
                workIndices[iwork] = nextxpos;
                workData[iwork] = (float) (alpha * x.data[ix]);
                ix++;
                iwork++;
                if (ix < x.used) {
                    nextxpos = x.indices[ix];
                } else {
                    nextxpos = Integer.MAX_VALUE;
                }
            } else {
                workIndices[iwork] = nextpos; // == nextxpos
                workData[iwork] = (float) (data[i] + alpha * x.data[ix]);
                i++;
                ix++;
                iwork++;
                if (i < used) {
                    nextpos = indices[i];
                } else {
                    nextpos = Integer.MAX_VALUE;
                }
                if (ix < x.used) {
                    nextxpos = x.indices[ix];
                } else {
                    nextxpos = Integer.MAX_VALUE;
                }
            }
        }

        // iwork now equals new used size; copy into target object, reallocing larger
        // if necessary
        if (iwork <= data.length) {
            System.arraycopy(workIndices, 0, indices, 0, iwork);
            System.arraycopy(workData, 0, data, 0, iwork);
        } else {
            int newsize = getNewSize(iwork, size);
            indices = new int[newsize];
            data = new float[newsize];
            System.arraycopy(workIndices, 0, indices, 0, iwork);
            System.arraycopy(workData, 0, data, 0, iwork);
        }

        used = iwork;
    }
    
    /*
     * add val to the current val at pos. if pos
     * doesn't exist it will be created ad set to pos
     */
    public void add(final int index, final float val) throws MatricksException {
        int pos = Utils.myBinarySearch(indices, index, 0, used);

        if (pos >= 0) {
            data[pos] = data[pos] + val;
        }
        else {
            insert(-pos-1, index, val);
        }
    }

    public double elementSum() {
        double sum = 0d;
        for (int index=0; index<used; index++) {
            sum += data[index];
        }

        return sum;
    }
    
    public double elementSquaredSum() {
        double sum = 0d;
        double d;
        for (int index=0; index<used; index++) {
            d = data[index];
            sum += d*d;
        }

        return sum;
    }


    public double dot(FlexFloatArray x) {
        double sum = 0d;


        // TODO: could check which has fewer entries, and
        // iterate over that to reduce the number of bisection
        // index search calls.
        //
        // TODO: or unroll one of the arrays into a work array
        // and dot to avoid bisection?

        for (int index=0; index<x.used; index++) {

            int row = x.indices[index];
            double x_at_row = x.data[index];
        
            int pos = Utils.myBinarySearch(indices, row, 0, used);

            if (pos >= 0) {
                sum += data[pos]*x_at_row;
            }
        }
        
        return sum;
    }

    /*
     *    (a .* a)' * (b .* b)
     */
    public double squaredDot(FlexFloatArray x) {
        double sum = 0d;


        // TODO: could check which has fewer entries, and
        // iterate over that to reduce the number of bisection
        // index search calls.
        //
        // TODO: or unroll one of the arrays into a work array
        // and dot to avoid bisection?

        for (int index=0; index<x.used; index++) {

            int row = x.indices[index];
            double x_at_row = x.data[index];
        
            int pos = Utils.myBinarySearch(indices, row, 0, used);

            if (pos >= 0) {
                double d = data[pos];
                sum += d*d*x_at_row*x_at_row;
            }
        }
        
        return sum;
    }

    /*
     * since flex storage overallocates on resize, this
     * method replaces the storage arrays with versions sized
     * to fit only the currently allocated points
     */
    public void compact() {
        if (data.length == used) { // already compact
            return;
        }
        
        float [] newData = new float[used];
        int [] newIndices = new int [used];
        
        System.arraycopy(data, 0, newData, 0, used);
        System.arraycopy(indices, 0, newIndices, 0, used);

        data = newData;
        indices = newIndices;
    }
    
    public FlexFloatArray copy() {
    	FlexFloatArray newArray = new FlexFloatArray(used);
    	System.arraycopy(data, 0, newArray.data, 0, used);
    	System.arraycopy(indices,  0, newArray.indices, 0, used);    	
    	return newArray;
    }
    
    /*
     * allocate and return a dense array
     */
    public float[] toDense() {
        float [] denseData = new float[indices[used-1]+1];
        for (int i=0; i<used; i++) {
            denseData[indices[i]] = data[i];
        }
        
        return denseData;
    }
}
