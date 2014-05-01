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

package org.genemania.engine.core.evaluation.correlation;

import org.junit.Test;
import static org.junit.Assert.*;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Vector;
import no.uib.cipr.matrix.sparse.SparseVector;


public class AbstractPearsonTest {

    @Test
    public void testGetZeroRank(){
        // v = {1, 2, 3, 4, 5}
        Vector v = new DenseVector(new double[]{1, 2, 3, 4, 5});
        assertEquals( AbstractPearson.getZeroRank(v), 0.0, 0d );

        // v = { , , 1, 1, }		
        v = new SparseVector(5, new int[]{2, 3}, new double[]{1,1});
        assertEquals( AbstractPearson.getZeroRank(v), 2.0, 0d);

        v = new SparseVector(5, new int[]{2}, new double[]{1});
        assertEquals( AbstractPearson.getZeroRank(v), 2.5, 0d);
    }
}
