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
 * Calculate precision at a fixed recall. 
 * 
 * Checked against Sara's matlab code - calcPR2
 * 
 * @author Pauline
 *
 */
public class PrecisionFixedRecall extends EvaluationMeasure {
	
	private double recallRate;

	/**
	 * @param name Name given to this evaluation measure
	 * @param recall Percentage of the fixed recall
	 */
	public PrecisionFixedRecall(String name, double recall) {
		super(name);
		this.recallRate = recall/100;
	}

	public double computeResult(boolean[] classes, double[] scores) {
		MatrixUtils.mergeSort(scores, classes);
		
		int totalPos = countPositives(classes);
		int cumPosCounts = 0;
		
		for ( int i = 1; i <= classes.length; i++ ){
			/* start from the end because mergeSort sorts the scores ascending,
			 * but we want descending scores.
			 */
			if ( classes[classes.length-i] ){
				cumPosCounts += 1; 
			}

			double recall = (double) cumPosCounts / totalPos;
			if ( recall >= recallRate ){
				return (double) cumPosCounts/i;
			}
		}
		return 0;
	}
	
	private int countPositives(boolean[] label){
		int pos = 0;
		for ( boolean value: label ){
			if ( value ){
				pos++;
			}
		}
		
		return pos;
	}
}
