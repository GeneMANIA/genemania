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

package org.genemania.engine.matricks;

import java.util.Arrays;
import java.util.Collection;
import org.genemania.engine.matricks.custom.FlexSymDoubleMatrix;
import org.genemania.engine.matricks.mtj.SymDoubleMatrix;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import static org.junit.Assert.*;

/**
 * test implementations of SymMatrix. implementation classes
 * plugged into the static configure() method.
 */
@RunWith(Parameterized.class)
public class SymMatrixTest {

    static final int SIZE = 5;
    SymMatrix instance;
    double [] x = new double[SIZE];
   
    public SymMatrixTest(SymMatrix data) {
        this.instance = data;
    }

    @Parameters
    public static Collection<Object[]> configure() {

        SymMatrix matrix1 = new FlexSymDoubleMatrix(SIZE);

        SymMatrix matrix2 = new SymDoubleMatrix(SIZE);

        Object[][] data = {{matrix1}, {matrix2}};
        return Arrays.asList(data);
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /*
     * very simple test data matrix and x vector
     */
    @Before
    public void setUp() {
        for (int i=0; i<SIZE; i++) {
            x[i] = i+1;
            
            for (int j=0; j<=i; j++) {
                instance.set(i, j, (i+1)*(j+1));
            }
        }
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of multAdd method, of class SymMatrix.
     */
    @Test
    @Ignore
    public void testMultAdd_3args() {
        System.out.println("multAdd");
        double alpha = 0.0;
        double[] y = null;
        instance.multAdd(alpha, x, y);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of mult method, of class SymMatrix.
     */
    @Test
    @Ignore
    public void testMult() {
        System.out.println("mult");
        double[] y = null;
        instance.mult(x, y);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of multAdd method, of class SymMatrix.
     */
    @Test
    @Ignore
    public void testMultAdd_doubleArr_doubleArr() {
        System.out.println("multAdd");
        double[] y = null;
        instance.multAdd(x, y);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of subMatrix method, of class SymMatrix.
     */
    @Test
    @Ignore
    public void testSubMatrix() {
        System.out.println("subMatrix");
        int[] rowcols = null;
        SymMatrix expResult = null;
        SymMatrix result = instance.subMatrix(rowcols);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setDiag method, of class SymMatrix.
     */
    @Test
    @Ignore
    public void testSetDiag() {
        System.out.println("setDiag");
        double alpha = 0.0;
        instance.setDiag(alpha);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of dotDivOuterProd method, of class SymMatrix.
     */
    @Test
    @Ignore
    public void testDotDivOuterProd() {
        System.out.println("dotDivOuterProd");
        Vector x = null;
        instance.dotDivOuterProd(x);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addOuterProd method, of class SymMatrix.
     */
    @Test
    @Ignore
    public void testAddOuterProd() {
        System.out.println("addOuterProd");
        instance.addOuterProd(x);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of sumDotMultOuterProd method, of class SymMatrix.
     */
    @Test
    public void testSumDotMultOuterProd() {
        System.out.println("sumDotMultOuterProd");
        double expResult = 3025;
        double result = instance.sumDotMultOuterProd(x);
        assertEquals(expResult, result, 0.0);
    }

}