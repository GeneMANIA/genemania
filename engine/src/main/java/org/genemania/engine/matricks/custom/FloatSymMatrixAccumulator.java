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
import org.genemania.engine.matricks.Matrix;
import org.genemania.engine.matricks.MatrixAccumulator;
import org.genemania.engine.matricks.SymMatrix;

/*
 * use as follows (sorry, my pseudocode comes
 * out in python)
 * 
 *   sum = Matrix(rows, cols)
 *   adder = Adder(sum, bufbytes)
 *   
 *   while adder.nextBlock():
 *     for matrix in matrices:
 *       adder.add(matrix)
 * 
 * 
 * TODO: completely ignores diagonal! which is ok for us since
 * our networks have zero-diagonal, but c'mon ...
 */
public class FloatSymMatrixAccumulator implements MatrixAccumulator {
    private static final long serialVersionUID = -3631959211056416493L;

    final int numRows;
    final int numCols;
    final int bufSizeBytes;

    // it probably wouldn't cost us too much more in time to use a buffer
    // of type double for the intermdiate results, even though we are converting
    // to floats at the end. worth it??
    final float [] buffer;
    final FlexSymFloatMatrix sum;

    // these initial values matter to the block
    // update code
    int blockStart = -1;  // row num
    int blockLength = 0; // number of rows

    /*
     * it would be simpler to specify a fixed # of rows instad of bufsize,
     * but we are storing diagonal matrices and row sizes aren't constant. to 
     * best use a buffer we use a sliding # of rows
     */
    public FloatSymMatrixAccumulator(FlexSymFloatMatrix sum, int bufSizeBytes) {
        this.numRows = sum.numRows();
        this.numCols = sum.numCols();
        this.bufSizeBytes = bufSizeBytes;
        this.sum = sum;

        buffer = new float[bufSizeBytes/4];
    }

    @Override
    public void add(double weight, Matrix m) {
        if (m instanceof FlexSymFloatMatrix) {
            add(weight, (FlexSymFloatMatrix) m);
        }
        else {
            throw new MatricksException("not implemented for given matrix type");
        }
    }
    
    public void add(double weight, FlexSymFloatMatrix m) {

        int offset = 0;
        for (int row=blockStart; row<=blockStart+blockLength; row++) {
//            System.out.println("adding row " + row + " with offset " + offset);
            FlexFloatArray a = m.data[row];
            a.add(weight, buffer, offset);
            offset = offset + a.size;
        }
    }

    @Override
    public void add(Matrix m) {
        if (m instanceof FlexSymFloatMatrix) {
            add((FlexSymFloatMatrix) m);
        }
        else {
            throw new MatricksException("not implemented for given matrix type");
        }
    }
    
    public void add(FlexSymFloatMatrix m) {
        int offset = 0;
        for (int row=blockStart; row<=blockStart+blockLength; row++) {
//            System.out.println("adding row " + row + " with offset " + offset);
            FlexFloatArray a = m.data[row];
            a.add(buffer, offset);
            offset = offset + a.size;
        }
    }

    /*
     * not sure how to encapsulate the iteration over buffer
     * size strips, without bringing knowledge of the cache 
     * into here. Try a sort of cursor-ish thing
     */
    @Override
    public boolean nextBlock() {

        // if not first block compact current buffer into sum
        if (blockStart != -1) {
            compact();
        }

        // compute block indices
        updateBlockIndices();

        // if not done, clear buffer and load current sum into buffer        
        if (blockStart < numRows) {
            Arrays.fill(buffer, 0f);
            add(sum);
            return true;
        }
        else {        
            return false;
        }
    }

    void updateBlockIndices() {
        blockStart = blockStart + blockLength + 1;

        // all done?
        if (blockStart >= numRows) {
            return;
        }

        // how many rows can we accommodate in the available
        // buffer space? if the next row has length n (left
        // of diag), then everything would be n + (n-1) + ... + 1 = n*(n+1)/2.
        // we want smallest k such that n + (n-1) + ... + k <= buffersize        
        long done = 0;
        if (blockStart>0) {
            int n = sum.data[blockStart-1].size;
            done = n*(n+1)/2;
        }
        int all = numRows*(numRows+1)/2;
        long todo = all - done;

        long overflow = todo-buffer.length;

        // oh, enough space for all the rest, set to last row
        if (overflow <=0) {
            blockLength = numRows - blockStart - 1;
        }
        else {
            // via quadratic formula
            long p = buffer.length + done;
            int lastRow = (int) Math.floor((-1+Math.sqrt(1+8*p))/2d);
            blockLength = lastRow - blockStart;
        }

//        System.out.println(String.format("tricky block indices %d -> %d", blockStart, blockStart + blockLength));
    }

    void compact() {
        int offset = 0;

        // replace each row with a newly constructed one,
        // we just use the old one to keep track of the size
        for (int row=blockStart; row<=blockStart+blockLength; row++) {
//            System.out.println("compacting row " + row);
            FlexFloatArray newRow = sum.data[row];
            newRow = new FlexFloatArray(newRow.size);
            final int end = offset + newRow.size;
            for (int i=offset; i<end; i++) {
                if (buffer[i] != 0f) {
                    newRow.set(i-offset, (float) buffer[i]);
                }
            }

            offset = offset + newRow.size;           
            sum.data[row] = newRow;           
        }        
    }
}
