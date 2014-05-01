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

import org.genemania.engine.core.evaluation.correlation.MutualInformationData.SizeType;

import no.uib.cipr.matrix.Vector;
import no.uib.cipr.matrix.VectorEntry;

/**
 * Each bin has the same range of elements as other bins.
 * 
 * @author Pauline
 *
 */
public class MutualInformationEqualRange extends AbstractMutualInformation {

	private final SizeType sizeType;
	private double[] range;
	
	public MutualInformationEqualRange(SizeType sizeType){
		this.sizeType = sizeType;
	}
	
	/**
	 * @param index  
	 * @param values
	 * @param k
	 * @return the bin number of an element at the kth index of vector values which represents 
	 *         the index-th gene from the profileData 
	 */
	protected int getBinNumber(int index, Vector values, int k) {
		return getBinNumber( values.get(k), numBins, range[index*2], range[index*2 + 1] );
	}

	/**
	 * Stores the mininum and maximum value of each gene for binning purposes
	 */
	protected void init() {
		numBins = getNumberOfBins(numFeatures, sizeType);
		
		/* array of min and max value for each gene
		 * index i is the min value of the ith gene
		 * index i+1 is the max value of the ith gene
		 */
		range = new double[numGenes*2]; 

		for ( int i = 0; i < numGenes; i++ ){
			Vector v = geneExpressions.get(i);
			boolean first = true;
			
			for ( VectorEntry e: v ){
				double value = e.get();
				
				// set the start of the range
				// set the first value to start
				if ( first || range[i*2] > value ){
					range[i*2] = value; 
				}
				
				// set the end of the range
				// set the first value to end
				if ( first || range[i*2 + 1] < value ) {
					range[i*2 + 1] = value; 
				}
				
				if ( first ){ 
					first = false;
				}
			}
		}
	}

}
