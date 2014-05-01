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

import org.genemania.engine.core.evaluation.ProfileData;
import org.genemania.engine.core.evaluation.correlation.MutualInformationData.SizeType;

import no.uib.cipr.matrix.Vector;

/**
 * Calculates Mutual Information between X and Y. MI(X,Y) = H(X) + H(Y) - H(X,Y), where H(X) is
 * the entropy of X and H(X,Y) is the joint entropy of X and Y.
 * 
 * Computes the joint entropy on the fly as suggested by Khalid. This gives
 * flexibility in calculating arbitrary number of genes, as memory will not be a problem. To 
 * calculate all the genes and their interaction = O(n^2) as the individual entropies are calculated
 * and stored on the fly as well.
 * 
 * Because the gene expression data are continuous data and mutual information works with discrete
 * values, binning is needed to get the continuous values into discrete values. There are two types
 * of binning - equal element or equal range. The bins can have equal number of elements or the
 * differences of the range of the elements in a bin are the same. 
 * 
 * No binning is needed for binary data. With no binning, the implementation only works for binary
 * profiles, i.e. with values of 0, 1. Will not work otherwise.
 * 
 * If binning is needed, the size of the bins need to be determined. A paper ( 
 * http://www.biomedcentral.com/1471-2105/8/111) states that mutual information performed the best
 * compared to other measures using number of bins within the lower bound of floor(logn + 1) and 
 * upper bound of squareRoot(n), . Hence, we have three different bin sizes: the lower bound, 
 * the upper bound, and the average between the lower and upper bound.
 * 
 * @author Pauline
 *
 */
public abstract class AbstractMutualInformation implements Correlation {
	
	protected int numGenes;
	protected int numFeatures;
	protected int numBins;
	protected List<Vector> geneExpressions;
	
	private double[] individualEntropies;   // array storing entropies for each gene
	private boolean[] calculated;           // array keep track of which gene has its entropy stored
	                                        // initialized to false;
	
	public void init(ProfileData data){
		geneExpressions = data.getGeneExpression();
		numGenes = geneExpressions.size();
		numFeatures = geneExpressions.get(0).size();
		
		individualEntropies = new double[numGenes];
		calculated = new boolean[numGenes];
		
		init();
	}
	
	/**
	 * method for concrete classes to perform initialization
	 */
	protected abstract void init();
	
	/**
	 * Computes mutual information between gene i and gene j.
	 * 
	 * If the individual entropy for gene i or gene j has not been calculated and stored yet, the
	 * entropy will be calculated and stored in the individualEntropies array. If the entropy is 
	 * calculated already, the values will be loaded from the individualEntropies array. Then the
	 * joint entropy for the two genes will be calculated.
	 * 
	 */
	public double computeCorrelations(int i, int j) {
		double jointEntropy;
		double indEntropy_i;
		double indEntropy_j;
		
		Vector values_i = geneExpressions.get(i);
		Vector values_j = geneExpressions.get(j);
		int[] indCounts_i = new int[numBins];
		int[] indCounts_j = new int[numBins];
		int[] jointCounts = new int[numBins*numBins];
		
		/*
		 * look through the values for the ith and jth vector to calculate:
		 * 1) the individual entropy for the i-th vector 
		 * 2) the individual entropy for the j-th vector
		 * 3) the joint entropy for the i-th and j-th vector
		 */
		for ( int k = 0; k < numFeatures; k++ ){
			int binNumber_i = getBinNumber(i, values_i, k);
			int binNumber_j = getBinNumber(j, values_j, k);
			
			// increment the count for i and j;
			if ( !calculated[i] ){
				indCounts_i[binNumber_i] += 1;
			}
			if ( !calculated[j] ){
				indCounts_j[binNumber_j] += 1;
			}
			
			// increment the count for i-j
			int index = binNumber_i * numBins + binNumber_j;
			jointCounts[index] += 1;
		}
		
		if ( !calculated[i] ){
			individualEntropies[i] = computeEntropy(indCounts_i, numFeatures);
			calculated[i] = true;
		} 
		indEntropy_i = individualEntropies[i];
		
		if ( !calculated[j] ){
			individualEntropies[j] = computeEntropy(indCounts_j, numFeatures);
			calculated[j] = true;
		} 
		indEntropy_j = individualEntropies[j];
		
		jointEntropy = computeEntropy(jointCounts, numFeatures);
		
		return indEntropy_i + indEntropy_j - jointEntropy;
	}
	
	/**
	 * @param index  
	 * @param values
	 * @param k
	 * @return the bin number of an element at the kth index of vector values which represents 
	 *         the index-th gene from the profileData 
	 */
	protected abstract int getBinNumber(int index, Vector values, int k);
	
	/**
	 * @param counts The counts for each bin
	 * @param total Total number of elements
	 * @return The entropy given the counts for each bin and the total number of elements
 	 */
	private double computeEntropy(int[] counts, int total){
		double entropy = 0;
		for ( double count: counts ){
			if ( count > 0 ){
				double frequency = (double) count / total; 
				entropy += (-1) * frequency * Math.log10(frequency) / Math.log10(2);
			}
		}
		return entropy;
	}
	
	/**
	 * @param numGenes
	 * @param sizeType
	 * @return number of bins given the number of genes and what kind of bin size.
	 *         If sizeType is Lower, floor( log(numGenes) + 1 ) is returned.
	 *         If sizeType is Upper, floor( sqaureRoot(numGenes) ) is returned.
	 *         If sizeType is Median (average), the average of lower bound and upper bound is returned.
	 */
	protected int getNumberOfBins(int numGenes, SizeType sizeType){
		switch ( sizeType ){
			default:
			case MEDIAN:
				int low = (int) Math.floor( Math.sqrt((double)numGenes) );
				int upper = (int) Math.ceil( Math.log10((double)numGenes + 1) / Math.log10(2) );
				return (low + upper) / 2;
			case LOWER:
				return (int) Math.floor( Math.sqrt((double)numGenes) );
			case UPPER:
				return (int) Math.ceil( Math.log10((double)numGenes + 1) / Math.log10(2) );
		}
	}
	
	/**
	 * @param value
	 * @param start
	 * @param end
	 * @return Which bin does this value belong to with the range of start to end and numBin bins.
	 * 		   First bin is indexed as 0.
	 */
	protected int getBinNumber( double value, int numBin, double start, double end ){
		return (int) Math.floor( (value - start) / ((end - start + 1) / numBin) );
	}
	
	/**
	 * don't have a useful threshold here yet (MI is non-negative) 
	 */
	public double getThresholdValue() {
	    return 0d;
	}
}
