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

import org.junit.Test;
import static org.junit.Assert.*;

public class OuterProductSymMatrixFromMatrixColViewTest {

    @Test
    public void testElementSum() {
        /* matlab
        
        a =
         
            0   1   1   0   0   1
            0   1   1   1   0   0
            0   1   1   0   1   1
            1   0   1   1   1   0

        c = a(:,2)
        c2 = c*c'
        
        result = sum(sum(c2))        
        result = 9
        
        result2 = sum(sum(c2-diag(diag(c2))))
        result2 = 6
        
        result3 = sum(sum(0.5*(c2-diag(diag(c2)))))
        result3 = 3
        */
        
        // 6 attribute vectors 
        FlexFloatMatrix a = new FlexFloatMatrix(4, 6); 
        a.set(0, 1, 1);
        a.set(0, 2, 1);
        a.set(0, 5, 1);
        
        a.set(1, 1, 1);
        a.set(1, 2, 1);
        a.set(1, 3, 1);
        
        a.set(2, 1, 1);
        a.set(2, 2, 1);
        a.set(2, 4, 1);
        a.set(2, 5, 1);
        
        a.set(3, 0, 1);
        a.set(3, 2, 1);
        a.set(3, 3, 1);
        a.set(3, 4, 1);

        // identity view
        int [] viewIndices = new int[4];
        for (int i=0; i<viewIndices.length; i++) {
            viewIndices[i] = i;
        }
        
        OuterProductSymMatrixFromMatrixColView m = new OuterProductSymMatrixFromMatrixColView(a, 1, 1, false);
        
        SymMatrixView view = new SymMatrixView(m, viewIndices);
        double sum = view.elementSum();
        assertEquals(9d, sum, 0d);
        
        m = new OuterProductSymMatrixFromMatrixColView(a, 1, 1, true);
        view = new SymMatrixView(m, viewIndices);
        sum = view.elementSum();
        assertEquals(6d, sum, 0d);
        
        // scale
        m = new OuterProductSymMatrixFromMatrixColView(a, 1, 0.5, true);
        view = new SymMatrixView(m, viewIndices);
        sum = view.elementSum();
        assertEquals(3d, sum, 0d);
    }

}
