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

import org.genemania.engine.Utils;
import org.genemania.engine.core.ProfileToNetwork;

import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.Vector;
import org.junit.Test;
import static org.junit.Assert.*;

public class ProfileToNetworkTest {

    @Test
    public void testContinuousProfile() {
        //Matrix profile = Matrices.random(10, 10);
        //Matrix profile = new DenseMatrix(new double [][] {{1,2,3,4,5},{6,7,8,9,10},{11,12,13,14,15},{16,17,18,19,20},{21,22,23,24,25}});
        Matrix profile = new DenseMatrix(new double [][] {{10,2,3,4,5},{6,17,8,9,10},{11,12,23,14,15},{16,17,18,29,20},{21,22,23,24,35}});

        //System.out.println(profile);
        Matrix network = ProfileToNetwork.continuousProfile(profile, 3);
        //System.out.println(network);

        assertNotNull(network);
        assertEquals(5, network.numRows());
        assertEquals(5, network.numColumns());

        // from matlab results for same profile ...
        Matrix expectedNetwork = new DenseMatrix(new double [][] {
                {0.0000, 0.0000, 0.6733, 0.7975, 0.9054},
                {0.0000, 0.0000, 0.6534, 0.7314, 0.8020},
                {0.6733, 0.6534, 0.0000, 0.7433, 0.7891},
                {0.7975, 0.7314, 0.7433, 0.0000, 0.7960},
                {0.9054, 0.8020, 0.7891, 0.7960, 0.0000},		                                                        	  
        });

        Utils.elementWiseCompare(expectedNetwork, network, 0.0001d);		
    }

    // we should be able to verify that the sum of the normalized vectors is 0, and the sum of squares is 1
    @Test
    public void testNormalizaton() {		
        Matrix profile = new DenseMatrix(new double [][] {{10,2,3,4,5},{6,17,8,9,10},{11,12,23,14,15},{16,17,18,29,20},{21,22,23,24,35}});

        Vector counts = MatrixUtils.columnCountsIgnoreMissingData(profile);
        Vector means = MatrixUtils.columnMeanIgnoreMissingData(profile, counts);
        Vector stdevs = MatrixUtils.columnVarianceIgnoreMissingData(profile, means);
        MatrixUtils.sqrt(stdevs);

        ProfileToNetwork.computeCorrelationTerms(profile, means, stdevs, counts);

        Matrix result = new DenseMatrix(profile.numRows(), profile.numRows());
        profile.transAmult(profile, result);

        Vector ones = new DenseVector(profile.numRows());
        ones.add(1d, ones);

        Vector sums = new DenseVector(profile.numRows());
        profile.mult(ones, sums);

        // sums should be 0
        for (int i=0; i<sums.size(); i++) {
            assertEquals(0d, sums.get(i), .00001d);
        }

        // diagonal should be 1
        for (int i=0; i<result.numRows(); i++) {
            assertEquals(1d, result.get(i, i), .00001d);
        }
    }

    @Test
    public void testBinaryProfile() {
        Matrix profile = new DenseMatrix(new double [][] {{1,1,1,0,0},{1,1,0,1,1},{0,1,1,0,1},{1,0,1,1,0},{0,1,1,0,1}});	

        Matrix network = ProfileToNetwork.binaryProfile(profile, 3);

        assertNotNull(network);
        assertEquals(5, network.numRows());
        assertEquals(5, network.numColumns());

        // from matlab results for same profile ...
        Matrix expectedNetwork = new DenseMatrix(new double [][] {
                { 0.0000,-0.5442,-0.2579, 0.6125, 0.0000},
                {-0.5442, 0.0000,-0.0499, 0.0000, 0.5700},
                {-0.2579,-0.0499, 0.0000, 0.0000,-0.2579},
                { 0.6125, 0.0000, 0.0000, 0.0000,-0.3583},
                { 0.0000, 0.5700,-0.2579,-0.3583, 0.0000},		                                                        	  
        });

        Utils.elementWiseCompare(expectedNetwork, network, 0.0001d);		

    }
}
