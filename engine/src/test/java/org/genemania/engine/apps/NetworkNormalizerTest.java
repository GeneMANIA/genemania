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

package org.genemania.engine.apps;

import org.junit.Test;
import static org.junit.Assert.*;
import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.sparse.CompColMatrix;

import org.genemania.engine.Utils;
import org.genemania.engine.apps.NetworkNormalizer;

public class NetworkNormalizerTest {

    @Test
    public void testArguments() {
        String [] args = {"-in", "f1.txt", "-out", "f2.txt"};		
        NetworkNormalizer normalizer = new NetworkNormalizer();

        normalizer.getCommandLineArgs(args);

        assertEquals("f1.txt", normalizer.getInFilename());
        assertEquals("f2.txt", normalizer.getOutFilename());
        assertTrue(normalizer.isNormalizationEnabled());
        assertEquals("uid", normalizer.getOutType());
    }

    @Test
    public void testNormalizationDisabledArgument() {
        String [] args = {"-in", "f1.txt", "-out", "f2.txt", "-norm", "false"};		
        NetworkNormalizer normalizer = new NetworkNormalizer();

        normalizer.getCommandLineArgs(args);

        assertEquals("f1.txt", normalizer.getInFilename());
        assertEquals("f2.txt", normalizer.getOutFilename());
        assertFalse(normalizer.isNormalizationEnabled());		
    }

    @Test
    public void testOutputNamesArgument() {
        String [] args = {"-in", "f1.txt", "-out", "f2.txt", "-outtype", "name"};		
        NetworkNormalizer normalizer = new NetworkNormalizer();

        normalizer.getCommandLineArgs(args);

        assertEquals("f1.txt", normalizer.getInFilename());
        assertEquals("f2.txt", normalizer.getOutFilename());
        assertEquals("name", normalizer.getOutType());
    }

    @Test
    public void testSparsify() {
        Matrix data = new CompColMatrix(new DenseMatrix(new double [][] {{1,2,3,4},{6,4,2,5},{7,8,9,0}}));
        Matrix expectedResult = new CompColMatrix(new DenseMatrix(new double[][] {{0,0,3,4},{6,0,0,5},{0,8,9,0}}));

        NetworkNormalizer.sparsifyRowsTopKPositives(data, 2);
        Utils.elementWiseCompare(expectedResult, data, 0d);
    }
}
