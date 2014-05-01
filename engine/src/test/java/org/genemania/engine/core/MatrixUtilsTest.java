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

package org.genemania.engine.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;
import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.Vector;
import no.uib.cipr.matrix.sparse.CompColMatrix;
import no.uib.cipr.matrix.sparse.SparseVector;

import org.genemania.engine.Utils;
import org.genemania.engine.core.evaluation.correlation.PearsonRow;

public class MatrixUtilsTest {

    /**
     * empty, dense matrix should give zeros 
     */
    @Test
    public void testSimple() {
        Matrix m = new DenseMatrix(3,2);
        Vector sums = MatrixUtils.columnSums(m);
        assertEquals(2, sums.size());
        assertEquals(0.0d, sums.get(0), 0d);
        assertEquals(0.0d, sums.get(1), 0d);

        sums = MatrixUtils.rowSums(m);
        assertEquals(3, sums.size());
        assertEquals(0.0d, sums.get(0), 0d);
        assertEquals(0.0d, sums.get(1), 0d);		
        assertEquals(0.0d, sums.get(2), 0d);	
    }

    /**
     * dense matrix
     */
    @Test
    public void testDense() {
        Matrix m = new DenseMatrix(new double[][] {{1,2},{3,4},{5,6}});
        Vector sums = MatrixUtils.columnSums(m);
        assertEquals(2, sums.size());
        assertEquals(9.0d, sums.get(0), 0d);
        assertEquals(12.0d, sums.get(1), 0d);

        sums = MatrixUtils.rowSums(m);
        assertEquals(3, sums.size());
        assertEquals(3.0d, sums.get(0), 0d);
        assertEquals(7.0d, sums.get(1), 0d);	
        assertEquals(11.0d, sums.get(2), 0d);
    }

    /**
     * sparse matrix, only top-left element is non-zero
     */
    @Test
    public void testSparse() {
        Matrix m = new CompColMatrix(3, 2, new int[][] {{0,0}, {}});
        m.set(0,0,10.0d);

        Vector sums = MatrixUtils.columnSums(m);
        assertEquals(2, sums.size());
        assertEquals(10.0d, sums.get(0), 0d);
        assertEquals(0.0d, sums.get(1), 0d);

        sums = MatrixUtils.rowSums(m);
        assertEquals(3, sums.size());
        assertEquals(10.0d, sums.get(0), 0d);
        assertEquals(0.0d, sums.get(1), 0d);	
        assertEquals(0.0d, sums.get(2), 0d);
    }

    @Test
    public void testGetSubMatrix() {
        Matrix m = new CompColMatrix(3, 3, new int[][] {{0,1,2}, {0}, {0,1}});

        m.set(0,0,1d);
        m.set(1,0,2d);
        m.set(2,0,3d);

        m.set(0,1,4d);

        m.set(0,2,5d);
        m.set(1,2,6d);

        int [] indices = new int[] {0,2};
        Matrix subm = MatrixUtils.getSubMatrix(m, indices, indices);

        assertNotNull(subm);
        assertEquals(subm.numRows(), m.numRows());
        assertEquals(subm.numColumns(), m.numColumns());

        assertEquals(1d, subm.get(0,0), 0d); // should be in both m and subm
        assertEquals(0d, subm.get(1,1), 0d); // should be in neither m nor subm
        assertEquals(0d, subm.get(0,1), 0d); // was in m but should not be in subm


        // test with different row/col index arrays
        int [] indices2 = new int[] {1};

        subm = MatrixUtils.getSubMatrix(m, indices, indices2);
        assertNotNull(subm);
        assertEquals(subm.numRows(), m.numRows());
        assertEquals(subm.numColumns(), m.numColumns());

        assertEquals(0d, subm.get(0,0), 0d);  
        assertEquals(0d, subm.get(1,1), 0d);  
        assertEquals(4d, subm.get(0,1), 0d);  

    }

    @Test
    public void testArrayJoin() {
        int [] a = {1, 2, 3};
        int [] b = {4, 5, 6};
        int [] expectedResult = {1, 2, 3, 4, 5, 6};

        int [] result = MatrixUtils.arrayJoin(a, b);
        assertNotNull(result);
        assertEquals(expectedResult.length, result.length);

        for (int i=0; i<expectedResult.length; i++) {
            assertEquals(expectedResult[i], result[i]);
        }

    }

    @Test
    public void testMultiplySum() {
        Matrix a = new CompColMatrix(new DenseMatrix(new double [][] {{1,2},{0,4}}));
        Matrix b = new CompColMatrix(new DenseMatrix(new double [][] {{4,0},{5,6}}));

        assertEquals(28d, MatrixUtils.elementMultiplySum(a, b), 0d);
    }

    @Ignore("what was this about again?") @Test
    public void testNaNs() {
        Matrix m = new DenseMatrix(new double[][] {{1,Double.NaN},{Double.NaN,4},{5,6}});
        Vector sums = MatrixUtils.columnSums(m);
        assertEquals(2, sums.size());
        assertEquals(9.0d, sums.get(0), 0d);
        assertEquals(12.0d, sums.get(1), 0d);

        sums = MatrixUtils.rowSums(m);
        assertEquals(3, sums.size());
        assertEquals(3.0d, sums.get(0), 0d);
        assertEquals(7.0d, sums.get(1), 0d);	
        assertEquals(11.0d, sums.get(2), 0d);
    }

    @Test
    public void testColumnCountsIgnoreMissingData() {
        Matrix m = new DenseMatrix(new double[][] {{1,Double.NaN, 3},{Double.NaN,4, 6},{5,6, 9}});
        Vector counts = MatrixUtils.columnCountsIgnoreMissingData(m);
        assertNotNull(counts);
        assertEquals(m.numColumns(), counts.size());
        assertEquals(2d, counts.get(0), 0d);
        assertEquals(2d, counts.get(1), 0d);
        assertEquals(3d, counts.get(2), 0d);
    }

    @Test
    public void testColumnMeanIgnoreMissingData() {
        Matrix m = new DenseMatrix(new double[][] {{1,Double.NaN, 3},{Double.NaN,4, 6},{5,6, 9}});
        Vector counts = MatrixUtils.columnCountsIgnoreMissingData(m);
        Vector means = MatrixUtils.columnMeanIgnoreMissingData(m, counts);
        assertNotNull(means);
        assertEquals(m.numColumns(), means.size());

        assertEquals(3d, means.get(0), 0d);
        assertEquals(5d, means.get(1), 0d);
        assertEquals(6d, means.get(2), 0d);
    }

    @Test
    public void testColumnVarianceIgnoreMissingData() {
        Matrix m = new DenseMatrix(new double[][] {{1,Double.NaN, 3},{Double.NaN,4, 6},{5,6, 9}});

        Vector counts = MatrixUtils.columnCountsIgnoreMissingData(m);
        Vector means = MatrixUtils.columnMeanIgnoreMissingData(m, counts);

        Vector variances = MatrixUtils.columnVarianceIgnoreMissingData(m, means);
        assertNotNull(variances);
        assertEquals(m.numColumns(), variances.size());

        assertEquals(4d, variances.get(0), 0d);
        assertEquals(1d, variances.get(1), 0d);
        assertEquals(6d, variances.get(2), 0d);		
    }

    @Test
    public void testPearson() {
        double [] x = {0.2, 0.1, 0.3, 0.4};
        double [] y = {0.8, 0.9, 0.8, 0.7};
        double [] z = {0.8, 0.2, 0.3, Double.NaN};

        assertEquals(-9.48683e-01, MatrixUtils.pearson(x, y), 0.00001d);
        assertEquals(1.55542e-01, MatrixUtils.pearson(x, z), 0.00001d);
        assertEquals(-6.28618e-01, MatrixUtils.pearson(y, z), 0.00001d);
    }

    @Test
    public void testMakeSortedMap() {
        HashMap<String, Double> unsorted = new HashMap<String, Double>();

        int numElements = 50;

        for (int i=0; i<numElements; i++) {
            unsorted.put("e" + i, Math.random());
        }

        Comparator<Double> reverse = Collections.reverseOrder();
        Map<String, Double> sorted = MatrixUtils.makeValueSortedMap(unsorted, reverse);

        double last = Double.POSITIVE_INFINITY;
        for (String key: sorted.keySet()) {
            Double value = sorted.get(key);
            assertTrue(value <= last);
            assertEquals(unsorted.get(key), value);
            last = value;
        }
    }

    @Test
    public void testTieRankDense(){
        Vector test1 = null, test2 = null, test3 = null, test4 = null, test5 = null, 
        test6 = null, test7 = null, test8 = null, test9 = null;

        test1 = new DenseVector(new double []{1,2,3,4,5}); 
        MatrixUtils.tiedRank( test1 );
        test2 = new DenseVector(new double []{10,6,2,4,8});
        MatrixUtils.tiedRank( test2 );
        test3 = new DenseVector(new double []{7,2,6,4,10});
        MatrixUtils.tiedRank( test3 );
        test4 = new DenseVector(new double []{2,8,4,10,6});
        MatrixUtils.tiedRank( test4 );
        test5 = new DenseVector(new double []{2,2,4,10,6});
        MatrixUtils.tiedRank( test5 );
        test6 = new DenseVector(new double []{2,6,4,10,6});
        MatrixUtils.tiedRank( test6 );
        test7 = new DenseVector(new double []{6,6,6,6,6});
        MatrixUtils.tiedRank( test7 );
        test8 = new DenseVector(new double []{2,2,2,10,6});
        MatrixUtils.tiedRank( test8 );
        test9 = new DenseVector(new double []{Double.NaN,2,2,2,10,6});
        MatrixUtils.tiedRank( test9 );

        Vector res1 = new DenseVector(new double []{1, 2, 3, 4, 5});
        Vector res2 = new DenseVector(new double []{5, 3, 1, 2, 4});
        Vector res3 = new DenseVector(new double []{4, 1, 3, 2, 5});
        Vector res4 = new DenseVector(new double []{1, 4, 2, 5, 3});
        Vector res5 = new DenseVector(new double []{1.5, 1.5, 3, 5, 4});
        Vector res6 = new DenseVector(new double []{1, 3.5, 2, 5, 3.5});
        Vector res7 = new DenseVector(new double []{3, 3, 3, 3, 3});
        Vector res8 = new DenseVector(new double []{2, 2, 2, 5, 4});
        Vector res9 = new DenseVector(new double []{Double.NaN, 2, 2, 2, 5, 4});

        for ( int i = 0; i < res1.size(); i++){
            assertEquals(res1.get(i), test1.get(i), 0d);
            assertEquals(res2.get(i), test2.get(i), 0d);
            assertEquals(res3.get(i), test3.get(i), 0d);
            assertEquals(res4.get(i), test4.get(i), 0d);
            assertEquals(res5.get(i), test5.get(i), 0d);
            assertEquals(res6.get(i), test6.get(i), 0d);
            assertEquals(res7.get(i), test7.get(i), 0d);
            assertEquals(res8.get(i), test8.get(i), 0d);
            assertEquals(res9.get(i), test9.get(i), 0d);
        }

    }

    @Test
    public void testTiedRankSparse(){
        // v = { ,1,2, , }
        Vector test1 = new SparseVector(5, new int[]{1, 2}, new double []{1,2});
        Vector res1 = new SparseVector(5, new int[]{1, 2}, new double []{1,2});
        MatrixUtils.tiedRank(test1);
        for ( int i = 0; i < res1.size(); i++){
            assertEquals(res1.get(i), test1.get(i), 0d);
        }

        // v = { ,2, , , }
        test1 = new SparseVector(5, new int[]{1}, new double []{2});
        res1 = new SparseVector(5, new int[]{1}, new double []{1});
        MatrixUtils.tiedRank(test1);
        for ( int i = 0; i < res1.size(); i++){
            assertEquals(res1.get(i), test1.get(i), 0d);
        }
    }

    @Test
    public void testGetIndicesForTopScores() {

        no.uib.cipr.matrix.Vector scores = new no.uib.cipr.matrix.DenseVector(new double[] {0.1, 0.5, -.3, -.5, 0.2});
        List<Integer> indicesForPositiveNodes = new ArrayList<Integer>();
        indicesForPositiveNodes.add(1);
        int limitResults = 2;
        double threshold = 0.0;

        int [] indicesForTopScores = MatrixUtils.getIndicesForTopScores(scores, indicesForPositiveNodes, limitResults, threshold);
        assertNotNull(indicesForTopScores);
        assertEquals(3, indicesForTopScores.length);
        assertEquals(1, indicesForTopScores[0]);
        assertEquals(4, indicesForTopScores[1]);
        assertEquals(0, indicesForTopScores[2]);

        limitResults = 5;
        indicesForTopScores = MatrixUtils.getIndicesForTopScores(scores, indicesForPositiveNodes, limitResults, threshold);
        assertNotNull(indicesForTopScores);
        assertEquals(3, indicesForTopScores.length);
        assertEquals(1, indicesForTopScores[0]);
        assertEquals(4, indicesForTopScores[1]);
        assertEquals(0, indicesForTopScores[2]);


    }

    // these constants from a numerical example that came up
    static final double TEST_VAL = 5.64385619d;
    static final int TEST_DATA_LEN = 24;

    @Test
    public void testVarianceNumerical() {
        Matrix m = new DenseMatrix(TEST_DATA_LEN, 1);
        MatrixUtils.add(m, TEST_VAL);

        Vector counts = MatrixUtils.columnCountsIgnoreMissingData(m);
        Vector means = MatrixUtils.columnMeanIgnoreMissingData(m, counts);

        Vector variances = MatrixUtils.columnVarianceIgnoreMissingData(m, means);
        assertNotNull(variances);
        assertEquals(m.numColumns(), variances.size());

        assertEquals(TEST_VAL, means.get(0), 0d);
        assertEquals(0d, variances.get(0), 0d);

    }

    /*
     * hold some test data along with expected results
     */
    static class StatData {
        StatData(String name, DenseMatrix data, double mean, double variance) {
            this.name = name;
            this.data = data;
            this.mean = mean;
            this.variance = variance;
        }

        String name;
        DenseMatrix data;
        double mean;
        double variance;

        // similar to nist strd data, see http://www.itl.nist.gov/div898/strd/general/dataarchive.html
        //
        // exact mean 1.2, exact variance 0.01 (n denom)
        static StatData genStatsTestData1() {
            DenseMatrix m = new DenseMatrix(10000, 1);
            for (int i=0; i<5000; i++) {
                m.set(2*i, 0, 1.1d);
                m.set(2*i+1, 0, 1.3d);
            }

            return new StatData("1000 elems, nist-like, average difficulty", m, 1.2d, 0.01d);
        }

        // example from GSE627, constant vals, variance 0
        static final double TEST_VAL = 5.64385619d;
        static final int TEST_DATA_LEN = 24;
        static StatData genStatsTestData2() {
            DenseMatrix m = new DenseMatrix(TEST_DATA_LEN, 1);
            for (int i=0; i<TEST_DATA_LEN; i++) {
                m.set(i, 0, TEST_VAL);
            }
            return new StatData("constant, non-zero data", m, TEST_VAL, 0d);
        }

        static StatData [] getAllTestData() {
            return new StatData[] {genStatsTestData1(), genStatsTestData2()};
        }
    }


    static final double STATS_TOL = 1e-14; // to catch regressions eg when refactoring code
    @Test
    public void testStats() {
        StatData [] datasets = StatData.getAllTestData();

        for (StatData dataset: datasets) {
            System.out.println("test data set: " +  dataset.name);

            Vector counts = MatrixUtils.columnCountsIgnoreMissingData(dataset.data);

            // direct implementations
            Vector simpleMeans = MatrixUtils.simpleColumnMeanIgnoreMissingData(dataset.data, counts);
            Vector simpleVariances = MatrixUtils.simpleColumnVarianceIgnoreMissingData(dataset.data, simpleMeans);

            // compensated variation
            Vector means = MatrixUtils.columnMeanIgnoreMissingData(dataset.data, counts);            
            Vector variances = MatrixUtils.columnVarianceIgnoreMissingData(dataset.data, means);

            System.out.println(String.format("%s: error in simple mean %g, error in compensated mean %g", dataset.name, simpleMeans.get(0)-dataset.mean, means.get(0) - dataset.mean));
            System.out.println(String.format("%s: error in simple variance %g, error in compensated variance %g", dataset.name, simpleVariances.get(0)-dataset.variance, variances.get(0) - dataset.variance));

            // older version that didn't do NaN handling ... to be removed eventually once dependencies are cleaned up
            Vector means2 = MatrixUtils.columnMean(dataset.data);
            Vector variances2 = MatrixUtils.columnVariance(dataset.data, means2);

            System.out.println(String.format("%s: error in simple mean %g, error in compensated mean2 %g", dataset.name, simpleMeans.get(0)-dataset.mean, means2.get(0) - dataset.mean));
            System.out.println(String.format("%s: error in simple variance %g, error in compensated variance2 %g", dataset.name, simpleVariances.get(0)-dataset.variance, variances2.get(0) - dataset.variance));

            // old row-wise version, also to be cleaned out
            Matrix rowdata = new DenseMatrix(dataset.data.numColumns(), dataset.data.numRows());
            dataset.data.transpose(rowdata);
            Vector means3 = MatrixUtils.rowMeanIgnoreMissingData(rowdata);

            System.out.println(String.format("%s: error in simple mean %g, error in compensated mean3 %g", dataset.name, simpleMeans.get(0)-dataset.mean, means3.get(0) - dataset.mean));

            // version that works directly on pearson-row data structure       
            ArrayList<Vector> expressionVectors = new ArrayList<Vector>();
            expressionVectors.add(MatrixUtils.extractColumnToVector(dataset.data, 0));

            // the following initialization along the lines of Pearson.createPearsonRow()
            Vector d = new SparseVector(0);            
            PearsonRow[] pearsonRows = new PearsonRow[dataset.data.numColumns()]; // data is column-wise
            for ( int i = 0; i < pearsonRows.length; i++ ){
                Vector v = MatrixUtils.extractColumnToVector(dataset.data, i);
                pearsonRows[i] = new PearsonRow(v, d, 0, 0);
            }

            Vector pearsonrowmeans = MatrixUtils.rowMeanIgnoreMissingDataPearsonRow(pearsonRows, counts);
            Vector pearsonrowvariances = MatrixUtils.rowVarianceIgnoreMissingDataPearsonRow(pearsonRows, means, counts);
            System.out.println(String.format("%s: error in simple mean %g, error in pearson-row compensated mean %g", dataset.name, simpleMeans.get(0)-dataset.mean, pearsonrowmeans.get(0) - dataset.mean));
            System.out.println(String.format("%s: error in simple variance %g, error in pearson-row compensated variance %g", dataset.name, simpleVariances.get(0)-dataset.variance, pearsonrowvariances.get(0) - dataset.variance));

            // check
            assertEquals(dataset.name, dataset.mean, means.get(0), STATS_TOL);
            assertEquals(dataset.name, dataset.variance, variances.get(0), STATS_TOL);            
            assertEquals(dataset.name, dataset.mean, means2.get(0), STATS_TOL);
            assertEquals(dataset.name, dataset.variance, variances2.get(0), STATS_TOL);            
            assertEquals(dataset.name, dataset.mean, means3.get(0), STATS_TOL);
            assertEquals(dataset.name, dataset.mean, pearsonrowmeans.get(0), STATS_TOL);
            assertEquals(dataset.name, dataset.variance, pearsonrowvariances.get(0), STATS_TOL);            
        }
    }

    @Test
    public void testNormalizeNetwork() throws Exception {

        /* matlab, using Sara's normalizeKernel.m

        >> format long e
        >> w = [0 3 5; 3 0 6; 5 6 0]

        w =

             0     3     5
             3     0     6
             5     6     0

        >> full(normalizeKernel(w))

        ans =

                                 0     3.535533905932737e-01     5.330017908890260e-01
             3.535533905932737e-01                         0     6.030226891555273e-01
             5.330017908890261e-01     6.030226891555273e-01                         0
         */
        Matrix m = new DenseMatrix(new double[][] {{0,3,5},{3,0,6},{5,6,0}});
        Matrix expectedResult = new DenseMatrix(new double[][] {
                {0,3.535533905932737e-01d,5.330017908890260e-01d},
                {3.535533905932737e-01d,0,6.030226891555273e-01d},
                {5.330017908890261e-01d,6.030226891555273e-01d,0}});

        MatrixUtils.normalizeNetwork(m);

        Utils.elementWiseCompare(expectedResult, m, 1.0E-7);
    }    
}
