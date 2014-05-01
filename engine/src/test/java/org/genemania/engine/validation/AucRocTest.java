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

import org.genemania.exception.ApplicationException;

import org.junit.Test;
import static org.junit.Assert.*;


public class AucRocTest {

    @Test
    public void test1(){
        boolean [] classes = {false, true, false, true, true, true, false, true, false, false};
        double [] scores = {1, 10, 9, 8, 7, 6, 5, 4, 3, 2};

        //tested against Sara's calcAreaROC in matlab
        AucRoc measure = new AucRoc("AucROC");
        double result = measure.computeResult(classes, scores);
        assertEquals(0.8, result, 0.0001);
    }

    @Test
    public void test2(){
        boolean [] classes = {false, true, false, true, false, true, true, true, false, false};
        double [] scores = {1, 10, 9, 8, 7, 6, 5, 4, 3, 2 };

        AucRoc measure = new AucRoc("AucRoc");
        double result = measure.computeResult(classes, scores);
        assertEquals(0.72, result, 0.0001);
    }

    @Test
    public void test3(){
        boolean [] classes = {false, false, false, false, false, false, false, false, false, false};
        double [] scores = {1, 10, 9, 8, 7, 6, 5, 4, 3, 2 };

        AucRoc measure = new AucRoc("AucRoc");
        double result = measure.computeResult(classes, scores);
        assertEquals(0.5, result, 0.0001);
    }

    @Test
    public void testAUC() throws ApplicationException {
        boolean [] classes = {true, false, true, false, true, false, false};
        double [] scores = {9, 8, 7, 6, 5, 4, 3};
        AucRoc measure = new AucRoc("AUC-ROC");
        double auc = measure.computeResult(classes, scores);
        assertEquals(9d/12, auc, 0.00001);
    }

    @Test
    public void testAUCShuffled() throws ApplicationException {
        boolean [] classes = {true, false, true, false, true, false, false};
        double [] scores = {9, 8, 7, 1, 5, 10, 3};
        AucRoc measure = new AucRoc("AUC-ROC");
        double auc = measure.computeResult(classes, scores);
        assertEquals(7d/12, auc, 0.00001);		
    }

    @Test
    public void testAUCShuffledAndOneTie() throws ApplicationException {
        boolean [] classes = {true, false, true, false, true, false, false};
        double [] scores = {7, 8, 7, 1, 5, 10, 3};
        AucRoc measure = new AucRoc("AUC-ROC");
        double auc = measure.computeResult(classes, scores);
        assertEquals(6d/12, auc, 0.00001);		

        classes = new boolean[] {true, false, true, false, true, false, false};
        scores = new double[] {7, 8, 7, 1, 5, 10, 7};
        auc = measure.computeResult(classes, scores);
        assertEquals(4d/12, auc, 0.00001);
    }

    @Test
    public void testAUCShuffledEndCase() throws ApplicationException {
        boolean [] classes = {true, false, false, false, false, false, false};
        double [] scores = {9, 8, 7, 1, 5, 10, 3};
        AucRoc measure = new AucRoc("AUC-ROC");
        double auc = measure.computeResult(classes, scores);
        assertEquals(5d/6, auc, 0.00001);		

        classes = new boolean[] {true, false, true, true, true, true, true};
        scores = new double[] {9, 8, 7, 1, 5, 10, 3};
        measure = new AucRoc("AUC-ROC");
        auc = measure.computeResult(classes, scores);
        assertEquals(2d/6, auc, 0.00001);
    }	

}
