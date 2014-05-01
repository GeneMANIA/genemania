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

package org.genemania.engine.core.integration;

import org.apache.log4j.Logger;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Vector;
import no.uib.cipr.matrix.sparse.FlexCompColMatrix;
import org.genemania.engine.config.Config;
import org.genemania.engine.core.MatrixUtils;
import org.genemania.engine.core.data.CoAnnotationSet;
import org.genemania.engine.matricks.Matrix;
import org.genemania.engine.matricks.MatrixCursor;
import org.genemania.engine.matricks.SymMatrix;

/**
 * Compute data structures for a sparse representation of a
 * simultaneous co-annotation network. This is used as the target
 * network in the weight calculation for the GO-based combining methods
 * 
 * Equivalent Matlab code in FastKernelWeightSW.m 
 * 
 * @author Quentin Shao
 *
 */
public class CoAnnoTargetBuilder {

    public static Logger logger = Logger.getLogger(CoAnnoTargetBuilder.class);

    /*
     * a simplified, straightforward implementation of the computing
     * the data structures required in simultaneous weights combining,
     * for comparative testing against current version. should produce the
     * same result as FastCoAnnotation().
     */
    public static CoAnnotationSet computeCoAnnoationSet(long organismId, String goBranch, Matrix labels) {

        int numGenes = labels.numRows();
        int numCategories = labels.numCols();

        double [] ratiosData = computeSumPosRatios(numGenes, labels);
        DenseVector ratios = new DenseVector(ratiosData, false);
        double constant = computeConstant(numGenes, ratios);
        logger.debug("constant: " + constant);

        DenseVector yHat = computeYHat(numGenes, ratiosData, labels);
        logger.debug("computed YHat");

        SymMatrix AHat = computeAHatLessMem(numGenes, numCategories, labels);
        logger.debug("computed AHat");

        CoAnnotationSet cas = new CoAnnotationSet(organismId, goBranch, AHat, yHat, constant);

        return cas;
    }

    /*
     * compute:
     *
     *   constant = \sum_{c} (n^{+}_{c}/n)^2
     * 
     */
    public static double computeConstant(int numGenes, no.uib.cipr.matrix.Vector ratios) {

        double constant = ratios.dot(ratios);
        return constant;

    }

    /*
     * compute:
     *
     *   yHat = \sum_{c} (-2n^{+}_{c})/n y^{\vec}_c
     *
     */
    public static DenseVector computeYHat(int numGenes, double [] ratios, Matrix labels) {
        double [] yHatTempData = new double[numGenes];

        labels.multAdd(ratios, yHatTempData);

        DenseVector yHat = new DenseVector(yHatTempData, false);
        yHat.scale(-2d);

        return yHat;
    }

    /*
     * compute:
     *
     *   \hat{A} = \sum_{c} A_c
     *
     * this version optimizes memory allocation by looping over label
     * matrix instead of materializing a coannotation matrix for each
     * category.
     *
     */
    public static SymMatrix computeAHatLessMem(int numGenes, int numCategories, Matrix labels) {

        SymMatrix AHat = Config.instance().getMatrixFactory().symSparseMatrix(numGenes);

        int [] rows = new int[numGenes];
        for(int i=0; i<rows.length; i++) {
            rows[i] = i;
        }


        for (int c=0; c<numCategories; c++) {
            int [] cols = {c};

            Matrix yc = labels.subMatrix(rows, cols);

            MatrixCursor e1 = yc.cursor();
            while (e1.next()) {
                MatrixCursor e2 = yc.cursor();
                while (e2.next()) {
                    int i = e1.row();
                    int j = e2.row();
                    if (i > j) { // can't assume we iterate through rows in ascending order, so must test TODO: implement a sparse version of addOuterProd ... could make more efficient
                        double z = e1.val() * e2.val();
                        if (z != 0d) {
                            AHat.add(i, j, z);
                        }
                    }
                }
            }
        }

        return AHat;
    }

    /*
     * compute vector of n+/n ratios, for each category
     */
    public static double [] computeSumPosRatios(int numGenes, Matrix labels) {
        double [] ratiosData = new double [labels.numCols()];
        labels.columnSums(ratiosData);
        Vector ratios = new DenseVector(ratiosData, false);
        ratios.scale(1d/numGenes);
        return ratiosData;
    }

    /*
     * compute KtT(0)
     */
    public static double computeKtT0(CoAnnotationSet annoSet, int numGenes) {
        double result = MatrixUtils.sum(annoSet.GetBHalf()) * numGenes + annoSet.GetCoAnnotationMatrix().elementSum() + annoSet.GetConstant() * numGenes * numGenes;
        return result;
    }

    /*
     * compute KtT(i) for i>0, with corresponding network
     *
     * networkSum could be computed from network, but we take it as an argument
     * since the sum is also computed elsewhere, as an optimization
     * 
     */
    public static double computeKtTi(CoAnnotationSet annoSet, int numGenes, Matrix network, double networkSum) {
        no.uib.cipr.matrix.Matrix tempSum2 = new FlexCompColMatrix(1, numGenes);

        MatrixCursor cursor = network.cursor();
        while (cursor.next()) {
            tempSum2.set(0, cursor.col(), tempSum2.get(0, cursor.col()) + cursor.val() * (annoSet.GetBHalf().get(cursor.row())));
        }

        double result = network.elementMultiplySum(annoSet.GetCoAnnotationMatrix()) +
                MatrixUtils.sum(tempSum2) + networkSum * annoSet.GetConstant();
        return result;
    }
}
