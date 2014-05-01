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

import static org.junit.Assert.*;

import org.genemania.engine.matricks.MatricksException;
import org.genemania.engine.matricks.OuterProductSymMatrix;
import org.genemania.engine.matricks.SymMatrix;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class OuterProductSymFloatMatrixTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testOuterProductSymFloatMatrix() {
        int size = 10;
        SymMatrix m = new OuterProductSymFloatMatrix(size);
        assertEquals(size, m.numRows());
        assertEquals(size, m.numCols());
        assertEquals(0.0d, m.get(0, 0), 0d);
    }

    @Test(expected=MatricksException.class)
    public void testSet() {
        int size = 10;
        OuterProductSymMatrix m = new OuterProductSymFloatMatrix(size);
        
        m.set(1, 5d);
        assertEquals(25d, m.get(1,1), 0d);
        assertEquals(0d, m.get(0,0), 0d);
        
        // following will fail with exception
        m.set(0, 0, 1d);
    }
}
