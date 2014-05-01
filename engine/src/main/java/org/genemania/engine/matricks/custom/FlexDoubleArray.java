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
 * data.length > size since we overallocate
 *
 * data.length == indices.length
 *
 * TODO: don't think i want an initial size of k (eg 8), i want an
 * initial size of 0, and then a size of k on first real allocation, then
 * doubling. eg if mostly we have 0 sized entries, but in the cases where its
 * non zero we don't expect to have just 1 or 2, but a few. Could do this
 * one level up in flexsymmatrix as well, allocating null entries in the array.
 */
public class FlexDoubleArray implements Serializable {

    public static final int FIRST_ALLOC_SIZE = 8;
    
    int size;
    int used;
    double [] data;
    int [] indices;

    public FlexDoubleArray() {
        this(Integer.MAX_VALUE, FIRST_ALLOC_SIZE); // grow crazy
    }
    
    public FlexDoubleArray(int size) {
        this(size, FIRST_ALLOC_SIZE);
    }

    public FlexDoubleArray(int size, int nz) {
        this.size = size;
        alloc(nz);
    }

    public int getSize() {
        return size;        
    }
    
    public int nnz() {
        return used;
    }
    
    private void alloc(int nz) {
        data = new double[nz];
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

    public void set(int index, double val) throws MatricksException {
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
    private void insert(int pos, int index, double val) throws MatricksException {
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
            double[] newData = new double[newsize];
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
        else if (data.length == size) {
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

    public MatrixCursor cursor() {
       return new FlexDoubleArrayCursor();
    }

    /*
     * implement matrix cursor interface as a one column matrix
     */
    private class FlexDoubleArrayCursor implements MatrixCursor {
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

        public void set(double val) {
            data[index] = (double) val;
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

    /*
     * v = v + alpha*x
     */
    public void add(double alpha, DenseDoubleVector v) {
        double [] d = v.data;
        for (int index=0; index<used; index++) {
            int row = indices[index];
            double x = data[index];
            d[row] = d[row] + alpha*x;
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
            data[index] = alpha*data[index];
        }
    }

    /*
     * this = this ./ (alpha*x)
     */
    public void dotDiv(final double alpha, double [] x) {
        for (int index=0; index<used; index++) {
            int row = indices[index];
            data[index] = data[index] / (alpha * x[row]);
        }
    }

    /*
     * this = this ./ x
     */
    public void dotDiv(final double [] x) {
        for (int index=0; index<used; index++) {
            int row = indices[index];
            data[index] = data[index] / x[row];
        }
    }

    /*
     * this = this + x
     */
    public void add(FlexDoubleArray x) throws MatricksException {
        for (int index=0; index<x.used; index++) {

            int row = x.indices[index];
            double x_at_row = x.data[index];

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

    public void add(final double alpha, FlexDoubleArray x) throws MatricksException {
        for (int index=0; index<x.used; index++) {

            int row = x.indices[index];
            double x_at_row = x.data[index];

            add(row, alpha*x_at_row);
        }
    }

    /*
     * add val to the current val at pos. if pos
     * doesn't exist it will be created ad set to pos
     */
    public void add(final int index, final double val) throws MatricksException {
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

    public double dot(FlexDoubleArray x) {
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
     * since flex storage overallocates on resize, this
     * method replaces the storage arrays with versions sized
     * to fit only the currently allocated points
     */
    public void compact() {
        if (data.length == used) { // already compact
            return;
        }
        
        double [] newData = new double[used];
        int [] newIndices = new int [used];
        
        System.arraycopy(data, 0, newData, 0, used);
        System.arraycopy(indices, 0, newIndices, 0, used);

        data = newData;
        indices = newIndices;
    }
}
