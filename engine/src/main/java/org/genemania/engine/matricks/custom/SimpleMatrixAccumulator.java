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

import org.genemania.engine.matricks.Matrix;
import org.genemania.engine.matricks.MatrixAccumulator;

/*
 * fallback for matrix types that haven't implemented
 * an optimized version. just add each matrix directly
 */
public class SimpleMatrixAccumulator implements MatrixAccumulator {
    private static final long serialVersionUID = -8914447267073406867L;
    
    final Matrix sum;
    int state = 0; // 0 == before start, 1 == running, 2 = done
    
    public SimpleMatrixAccumulator(Matrix sum) {
        super();
        this.sum = sum;
    }   
    
    @Override
    public boolean nextBlock() {
        if (state == 0) {
            state += 1;
            return true;
        }
        else if (state == 1) {
            state += 1;
            return false;
        }
        else {
            return false;
        }
    }


    @Override
    public void add(Matrix m) {
        sum.add(m);

    }

    @Override
    public void add(double alpha, Matrix m) {
        sum.add(alpha, m);
    }
}
