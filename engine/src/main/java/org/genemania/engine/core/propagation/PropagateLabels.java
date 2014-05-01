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

package org.genemania.engine.core.propagation;

import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Vector;
import no.uib.cipr.matrix.sparse.CG;
import no.uib.cipr.matrix.sparse.DefaultIterationMonitor;
import no.uib.cipr.matrix.sparse.IterativeSolverNotConvergedException;
import org.apache.log4j.Logger;
import org.genemania.engine.Constants;
import org.genemania.engine.core.MatrixUtils;

import org.genemania.engine.matricks.Matrix;
//import org.genemania.engine.matricks.Vector;
import org.genemania.engine.exception.PropagationFailedException;
import org.genemania.engine.matricks.MatricksException;
import org.genemania.engine.matricks.SymMatrix;
import org.genemania.engine.matricks.custom.EyePlusLaplacianMatrix;
import org.genemania.engine.matricks.mtj.SymWrap;
import org.genemania.exception.ApplicationException;
import org.genemania.util.ProgressReporter;

/**
 * This class performs labeling biasing and label propagation.
 *
 * if we want to make this interruptable (via the progress reporter),
 * we'll need to implement our own CG code. since this doesn't seem to
 * be timeconsuming step at this time, we won't try that yet.
 */
public class PropagateLabels {

    private static Logger logger = Logger.getLogger(PropagateLabels.class);

    /**
     * Returns a propagated vector. This uses average biasing for label biasing.
     * Any unknowns get (N+ - N-) / (N+ + N-) where N+ is the number of positives 
     * and N- is the number of negatives
     */

    public static Vector process(SymMatrix network, Vector labels, ProgressReporter progress) throws ApplicationException {
        progress.setStatus(Constants.PROGRESS_SCORING_MESSAGE);
        progress.setProgress(Constants.PROGRESS_SCORING);
        int n = network.numCols();
        // TODO: verify input sizes match

        DenseVector score = new DenseVector(n);

        setLabelBiases(labels);

        SymMatrix laplacian = new EyePlusLaplacianMatrix(network);
        try {
            SymWrap w = new SymWrap(laplacian);
            CG cg = new CG(new DenseVector(labels.size()));
            
            // tighten relative tolerance but leave other convergence params defaulted
            cg.setIterationMonitor(new DefaultIterationMonitor(100000, 1e-10, 1e-50, 1e+5));
            cg.solve(w, labels, score);
        }
        catch (IterativeSolverNotConvergedException e) {
            throw new PropagationFailedException("Label propagation failed", e);
        }

        return score;
    }


    static void setLabelBiases(Vector labels) {
        //System.out.println("labels: " + labels);
        int n_pos = MatrixUtils.countMatches(labels, 1.0d);
        int n_neg = MatrixUtils.countMatches(labels, -1.0d);

        double bias = (n_pos - n_neg) * 1.0d / ( (n_pos + n_neg) * 1.0d );
        logger.info(String.format("setting label biases, npos: %s, nneg %s, bias: %s", n_pos, n_neg, bias));
        MatrixUtils.setMatches(labels, Constants.EXCLUDED_ROW_VALUE, bias);
        //System.out.println("labels after: " + labels);

    }	
}
