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

package org.genemania.engine.core.evaluation.correlation;

import java.util.List;

import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Vector;
import no.uib.cipr.matrix.sparse.SparseVector;

import org.genemania.engine.core.MatrixUtils;
import org.genemania.engine.core.evaluation.ProfileData;

/**
 * Generalized way to calculate Pearson, Spearman and Pearson Rank correlation. 
 * 
 * Each gene record is represented with PearsonRow
 *   
 * @author Pauline
 *
 */
public abstract class AbstractPearson implements Correlation {
	
	protected ProfileData geneData;
	protected PearsonRow[] rows;
	
	/**
	 * Template method for each concrete class to create the representation of profile data
	 */
	protected abstract void createPearsonRow();
	
	/**
	 * Template method to return dtd, d transposed multiplied by d
	 */
	protected abstract double getDTD();

	/**
	 * Template method to return dd, d transposed multiplied by one-vector
	 */
	protected abstract double getDD();
	
	/**
	 * computes the correlation between gene i and gene j
	 */
	public double computeCorrelations(int i, int j) {
		try {
			return PearsonRow.dot( rows[i], rows[j], getDTD(), getDD(), geneData.getGeneExpression().get(0).size() );
		} catch (NullPointerException e){
			throw new RuntimeException("Please initialize the correlation with Correlation.init(ProfileData)");
		}
	}

	public void init(ProfileData data) {
		this.geneData = data;
		rows = new PearsonRow[ data.getGeneExpression().size() ];
		createPearsonRow();
		normalize();
		replaceMissingData();
	}
	
	/* 
     * default implementation gives 0 as the threshold.
	 */
	public double getThresholdValue() {
	    return 0d;
	}
	
	/**
	 * Normalizes the gene expressions. note if the stdev is very small,
	 * we zero-out the row instead
	 */
	public static final double MIN_STDEV = 1e-12;
	protected void normalize(){
		List<Vector> geneExpressions = geneData.getGeneExpression();
		
		// calculating the mean and the stdev
		Vector counts = MatrixUtils.rowCountsIgnoreMissingData(geneExpressions);
		Vector means = MatrixUtils.rowMeanIgnoreMissingDataPearsonRow(rows, counts);
		Vector stdevs = MatrixUtils.rowVarianceIgnoreMissingDataPearsonRow(rows, means, counts);
		MatrixUtils.sqrt(stdevs);
		
		// normalizing geneExpressions with the mean and stdev
		Vector sqrtCounts = counts.copy();
		MatrixUtils.sqrt(sqrtCounts);
		MatrixUtils.elementMult(stdevs, sqrtCounts);
		
		for ( int i = 0; i < geneExpressions.size(); i++ ){
			double mean = means.get(i);
			double stdev = stdevs.get(i);
			
			if (stdev < MIN_STDEV) {
	            rows[i].zero();
	        }
	        else {
	            rows[i].normalize(mean, stdev);
	        }
		}
	}
	
	/**
	 * Replaces the missing data to a value so it is equivalent to 0
	 */
	protected void replaceMissingData(){
		for ( int i = 0; i < rows.length; i++ ){
			rows[i].replaceMissingData(0);
		}
	}

	/**
	 * @param v
	 * @return the rank of 0's for vector v
	 */
	public static double getZeroRank(Vector v){
		if ( v instanceof DenseVector ){
			return 0;
		} else if ( v instanceof SparseVector ){
			int numZeroes = v.size() - ((SparseVector) v).getUsed();
			if ( numZeroes == 0 ) {
				return 0;
			} else {
				return (double) ( (1 + numZeroes) * numZeroes ) / (2 * numZeroes);
			}
		} else {
			throw new RuntimeException( "unexpected vector type of " + v.getClass().getName() );
		}
	}
}
