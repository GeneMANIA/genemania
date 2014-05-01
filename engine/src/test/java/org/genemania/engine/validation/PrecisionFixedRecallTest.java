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

package org.genemania.engine.validation;

import org.junit.Test;
import static org.junit.Assert.*;


public class PrecisionFixedRecallTest {

    @Test
    public void test1(){
        boolean [] classes = {true, true, false, false, false, false, false};
        double [] scores = {9, 8, 7, 1, 5, 10, 3};

        double[] measureResults = new double[10];
        // correctResults obtained from Sara's calcPR2 Matlab code
        // correctResults[i] = precision at (i*10)% recall
        double[] correctResults = {0.5, 0.5, 0.5, 0.5, 0.5, 0.6667, 0.6667, 0.6667, 0.6667, 0.6667};
        PrecisionFixedRecall measure;

        for ( int i = 1; i <= 10; i++ ){
            measure = new PrecisionFixedRecall("PR", i*10);
            double result = measure.computeResult(classes, scores);
            measureResults[i-1] = result;
        }

        for ( int i = 0; i < 10; i++ ){
            assertEquals(measureResults[i], correctResults[i], 0.0001);
        }
    }

    @Test
    public void test2(){
        boolean [] classes = {false, true, false, true, false, true, true, true, false, false};
        double [] scores = {1, 10, 9, 8, 7, 6, 5, 4, 3, 2 };

        double[] measureResults = new double[10];
        // correctResults obtained from Sara's calcPR2 Matlab code
        // correctResults[i] = precision at (i*10)% recall
        double[] correctResults = {1, 1, 0.6667, 0.6667, 0.6, 0.6, 0.6667, 0.6667, 0.7143, 0.7143};
        PrecisionFixedRecall measure;

        for ( int i = 1; i <= 10; i++ ){
            measure = new PrecisionFixedRecall("PR", i*10);
            double result = measure.computeResult(classes, scores);
            measureResults[i-1] = result;
        }


        for ( int i = 0; i < 10; i++ ){
            assertEquals(measureResults[i], correctResults[i], 0.0001);
        }
    }

    @Test
    public void test3(){
        boolean [] classes = {false, true, false, true, true, true, false, true, false, false};
        double [] scores = {1, 10, 9, 8, 7, 6, 5, 4, 3, 2};

        double[] measureResults = new double[10];
        // correctResults obtained from Sara's calcPR2 Matlab code
        // correctResults[i] = precision at (i*10)% recall
        double[] correctResults = {1, 1, 0.6667, 0.6667, 0.75, 0.75, 0.8, 0.8, 0.7143, 0.7143};
        PrecisionFixedRecall measure;

        for ( int i = 1; i <= 10; i++ ){
            measure = new PrecisionFixedRecall("PR", i*10);
            double result = measure.computeResult(classes, scores);
            measureResults[i-1] = result;
        }

        for ( int i = 0; i < 10; i++ ){
            assertEquals(measureResults[i], correctResults[i], 0.0001);
        }
    }
}
