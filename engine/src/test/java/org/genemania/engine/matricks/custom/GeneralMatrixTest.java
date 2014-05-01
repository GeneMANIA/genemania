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
import java.util.Collection;
import java.util.Random;
import org.genemania.engine.config.Config;
import org.genemania.engine.cache.NetworkMemCache;
import org.genemania.engine.matricks.Matrix;
import org.genemania.engine.matricks.Vector;
import org.genemania.engine.matricks.SymMatrix;
import org.genemania.exception.ApplicationException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import static org.junit.Assert.*;

/**
 *
 */
@RunWith(Parameterized.class)
public class GeneralMatrixTest {

    public GeneralMatrixTest(String configFile) throws ApplicationException {
        Config.reload(configFile);
    }

    @Parameters
    public static Collection<Object[]> configure() {
    	// not all functionality enabled by mtjconfig.properties is implemented, don't use that config
    	// since that's due to be removed anyway
        Object[][] data = {{"symconfig.properties"}, {"floatsymconfig.properties"}};
        return Arrays.asList(data);
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        Config.reload();
    }

    @Before
    public void setUp() {
        NetworkMemCache.instance().clear();
    }

    @After
    public void tearDown() {
    }

    public void setRandomSymmetric(Matrix m, long seed) throws Exception {
        Random random = new Random(seed);
        for (int i=0; i<m.numRows(); i++) {
            for (int j=0; j<=i; j++) {
                double x = random.nextDouble();
                m.set(i, j, x);
            }
        }
        m.setToMaxTranspose();
    }

    public void setRandomVec(Vector y, long seed) throws Exception {
        Random random = new Random(seed);
        for (int i = 0; i < y.getSize(); i++) {
            double x = random.nextDouble();
            y.set(i, x);
        }
    }
    

    @Test
    public void testMultAdd() throws Exception {
        int size = 5;
        long seed = 90125;

        Matrix m = Config.instance().getMatrixFactory().symSparseMatrix(size);
        setRandomSymmetric(m, seed);

        double result = m.elementSum();
        assertEquals(14.90186, result, 1e-5);
        
        Vector x = new DenseDoubleVector(size);
        setRandomVec(x, seed+1);

        Vector y = new DenseDoubleVector(size);
        setRandomVec(y, seed+2);
        m.multAdd(2d, x, y);

        result = y.elementSum();
        assertEquals(16.764973, result, 1e-5);

    }

    @Test
    public void testMult() throws Exception {
        int size = 5;
        long seed = 90125;

        Matrix m = Config.instance().getMatrixFactory().symSparseMatrix(size);
        setRandomSymmetric(m, seed);

        double result = m.elementSum();
        assertEquals(14.90186, result, 1e-5);
        
        Vector x = new DenseDoubleVector(size);
        setRandomVec(x, seed+1);

        Vector y = new DenseDoubleVector(size);
        setRandomVec(y, seed+2);
        m.mult(x, y);

        result = y.elementSum();
        assertEquals(6.7312545, result, 1e-5);
    }

    @Test
    public void testRowSums() throws Exception {
        int size = 5;
        long seed = 90125;

        Matrix m = Config.instance().getMatrixFactory().symSparseMatrix(size);
        setRandomSymmetric(m, seed);

        double result = m.elementSum();
        assertEquals(14.90186, result, 1e-5);

        Vector y = new DenseDoubleVector(size);
        y = m.rowSums();
        
        result = y.elementSum();
        assertEquals(14.90186, result, 1e-5);
    }

    @Test
    public void testColSums() throws Exception {
        int size = 5;
        long seed = 90125;

        Matrix m = Config.instance().getMatrixFactory().symSparseMatrix(size);
        setRandomSymmetric(m, seed);

        double result = m.elementSum();
        assertEquals(14.90186, result, 1e-5);

        Vector y = new DenseDoubleVector(size);
        y = m.columnSums();

        result = y.elementSum();
        assertEquals(14.90186, result, 1e-5);
    }

    @Test
    public void testDotDivOuterProd() throws Exception {
        int size = 5;
        long seed = 90125;

        SymMatrix m = Config.instance().getMatrixFactory().symSparseMatrix(size);
        setRandomSymmetric(m, seed);

        double result = m.elementSum();
        assertEquals(14.90186, result, 1e-5);

        Vector x = new DenseDoubleVector(size);
        setRandomVec(x, seed+1);

        m.dotDivOuterProd(x);

        result = m.elementSum();
        assertEquals(120.87008, result, 1e-5);
    }

    @Test
    public void testScale() throws Exception {
        int size = 5;
        long seed = 90125;

        Matrix m = Config.instance().getMatrixFactory().symSparseMatrix(size);
        setRandomSymmetric(m, seed);

        double result = m.elementSum();
        assertEquals(14.90186, result, 1e-5);

        m.scale(2d);

        result = m.elementSum();
        assertEquals(29.803721, result, 1e-5);
    }

    @Test
    public void testAdd() throws Exception {
        int size = 5;
        long seed = 90125;

        Matrix m = Config.instance().getMatrixFactory().symSparseMatrix(size);
        setRandomSymmetric(m, seed);

        double result = m.elementSum();
        assertEquals(14.90186, result, 1e-5);

        Matrix m2 = Config.instance().getMatrixFactory().symSparseMatrix(size);
        setRandomSymmetric(m2, seed+1);


        m.add(2d, m2);

        result = m.elementSum();
        assertEquals(39.115032, result, 1e-5);
        
    }

}
