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

import org.genemania.engine.core.MatrixUtils;

/**
 * Calculates the area under the ROC curve by calculating Mann-Whitney Statistic.
 * Area under the ROC curve is defined to be 0.5 if there are no positives in the validation set.
 * 
 * U = (sum of positive ranks - m(m+1)/2) / (m*n)
 * 
 * m = number of positives, n = number of negatives
 */
public class AucRoc extends EvaluationMeasure {

	public AucRoc(String name) {
		super(name);
	}

	public double computeResult(boolean[] classes, double[] scores) {
		double rank[] = scores.clone();
		
		MatrixUtils.tiedRank(rank);
		double sumPosRanks = 0;
		int numPos = 0;
		int numNeg = 0;
		
		for ( int i = 0; i < classes.length; i++ ){
			if ( classes[i] ){
				sumPosRanks += rank[i];
				numPos ++;
			} else {
				numNeg ++;
			}
		}
		
		if ( numPos == 0 || numNeg == 0 ){
			return 0.5;
		}
		
		double area = sumPosRanks - numPos * (numPos + 1) / 2;
		return area / (numPos * numNeg);
	}
}
