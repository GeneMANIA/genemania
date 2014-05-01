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
 * Calculates the area under the PR curve. 
 * Area under the PR curve is defined to be 0 if there are no positives in the validation set.
 * 
 * Formula taken from http://www.oracle.com/technology/products/text/htdocs/imt_quality.htm
 *  
 * @author Pauline
 *
 */
public class AucPr extends EvaluationMeasure {

	public AucPr(String name) {
		super(name);
	}

	public double computeResult(boolean[] classes, double[] scores) {
		double result = 0;
		MatrixUtils.mergeSort(scores, classes);
		
		double[] pre = new double[ classes.length ];
		int cumPosCounts = 0;
		
		for ( int i = 1; i <= classes.length; i++ ){
			/* start from the end because mergeSort sorts the scores ascending,
			 * but we want descending scores.
			 */
			if ( classes[classes.length-i] ){
				cumPosCounts += 1; 
			}
			
			pre[i-1] = (double) cumPosCounts/i;
		}
		
		
		for ( int i = classes.length - 1; i >= 0; i-- ){
			if ( classes[i] ){
				result += pre[classes.length - 1 - i];
			}
		}
		
		if ( cumPosCounts == 0 ){
			// if there are no positives in the validation set
			result = 0;
		} else {
			result /= cumPosCounts;
		}
		return result;
	}

}
