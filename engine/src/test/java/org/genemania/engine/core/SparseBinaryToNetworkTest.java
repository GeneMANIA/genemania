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

import java.util.Vector;

import org.genemania.engine.core.SparseBinaryToNetwork;

import org.junit.Test;
import static org.junit.Assert.*;

public class SparseBinaryToNetworkTest {

    @Test
    public void testCountNonZerosPerFeature() {
        int numFeatures = 10;
        int [] g1 = {2,4};
        int [] g2 = {3,4,9};
        int [] g3 = {};

        Vector<int[]> profileData = new Vector<int[]>();
        profileData.add(g1);
        profileData.add(g2);
        profileData.add(g3);

        int numGenes = profileData.size();

        SparseBinaryToNetwork p2n = new SparseBinaryToNetwork(numGenes, numFeatures);
        p2n.countNonZerosPerFeature(profileData);

        assertNotNull(p2n.numNonZerosPerFeature);
        assertEquals(10, p2n.numNonZerosPerFeature.length);
        assertEquals(0, p2n.numNonZerosPerFeature[0]);
        assertEquals(0, p2n.numNonZerosPerFeature[1]);
        assertEquals(1, p2n.numNonZerosPerFeature[2]);
        assertEquals(1, p2n.numNonZerosPerFeature[3]);
        assertEquals(2, p2n.numNonZerosPerFeature[4]);
        assertEquals(0, p2n.numNonZerosPerFeature[5]);
        assertEquals(0, p2n.numNonZerosPerFeature[6]);
        assertEquals(0, p2n.numNonZerosPerFeature[7]);
        assertEquals(0, p2n.numNonZerosPerFeature[8]);
        assertEquals(1, p2n.numNonZerosPerFeature[9]);		
    }

    @Test
    public void testComputeFactors() {
        int numFeatures = 10;
        int [] g1 = {2,4};
        int [] g2 = {3,4,9};
        int [] g3 = {};

        Vector<int[]> profileData = new Vector<int[]>();
        profileData.add(g1);
        profileData.add(g2);
        profileData.add(g3);

        int numGenes = profileData.size();

        SparseBinaryToNetwork p2n = new SparseBinaryToNetwork(numGenes, numFeatures);
        p2n.countNonZerosPerFeature(profileData);
        p2n.computeFactors();

        assertNotNull(p2n.nnf);
        assertNotNull(p2n.pnf);
        assertNotNull(p2n.ppf);

    }

    @Test
    public void testComputeBaseline() {
        int numFeatures = 10;
        int [] g1 = {2,4};
        int [] g2 = {3,4,9};
        int [] g3 = {};

        Vector<int[]> profileData = new Vector<int[]>();
        profileData.add(g1);
        profileData.add(g2);
        profileData.add(g3);

        int numGenes = profileData.size();

        SparseBinaryToNetwork p2n = new SparseBinaryToNetwork(numGenes, numFeatures);
        p2n.countNonZerosPerFeature(profileData);
        p2n.computeFactors();
        p2n.computeNegBaseline();
        //assertEquals(0d, p2n.baseline);

    }

    @Test
    public void testComputeCorrelations() {
        int numFeatures = 10;
        int [] g1 = {2,4};
        int [] g2 = {3,4,9};
        int [] g3 = {};


        Vector<int[]> profileData = new Vector<int[]>();
        profileData.add(g1);
        profileData.add(g2);
        profileData.add(g3);

        int numGenes = profileData.size();

        SparseBinaryToNetwork p2n = new SparseBinaryToNetwork(numGenes, numFeatures);
        p2n.countNonZerosPerFeature(profileData);
        p2n.computeFactors();
        p2n.computeNegBaseline();
        p2n.computeCorrelation(g1, g2);
    }
    /*
     * A script:

	o1 = [ 2 5 6];
	o2 = [ 3 4 5];
	o3 = [2 3 5 6];
	o4 = [ 1 4 6];
	o5 = [ 1 2 3 5 6];

	p = sparse(5, 6);
	p(1, o1) = 1;
	p(2, o2) = 1;
	p(3, o3) = 1;
	p(4, o4) = 1;
	p(5, o5) = 1;


	full(p)

	p = p';

	n = makeBinaryKernel(p, 3);

	full(n)


     * and some results:

	>> testMakeBinaryProfile

	ans =

	     0     1     0     0     1     1
	     0     0     1     1     1     0
	     0     1     1     0     1     1
	     1     0     0     1     0     1
	     1     1     1     0     1     1

	makeKernel called with 5 datapoints.  One dot / 1000 datapoints


	ans =

	         0         0    0.4143   -0.8739   -0.3147
	         0         0   -0.7236    0.0248         0
	    0.4143   -0.7236         0         0    0.4143
	   -0.8739    0.0248         0         0   -0.8739
	   -0.3147         0    0.4143   -0.8739         0

     */
    @Test
    public void testAnotherBecauseImDumb() {
        int numFeatures = 6;
        int [] g1 = {1, 4, 5};
        int [] g2 = {2, 3, 4};
        int [] g3 = {1, 2, 4, 5};
        int [] g4 = {0, 3, 5};
        int [] g5 = {0, 1, 2, 4, 5};

        Vector<int[]> profileData = new Vector<int[]>();
        profileData.add(g1);
        profileData.add(g2);
        profileData.add(g3);
        profileData.add(g4);
        profileData.add(g5);

        int numGenes = profileData.size();

        SparseBinaryToNetwork p2n = new SparseBinaryToNetwork(numGenes, numFeatures);
        p2n.countNonZerosPerFeature(profileData);
        p2n.computeFactors();
        p2n.computeNegBaseline();
        double c = p2n.computeCorrelation(g1, g3);
        assertEquals(.4143, c, 0.001);


    }
}
