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

import org.genemania.engine.core.MatrixUtils;
import org.genemania.engine.core.evaluation.correlation.MutualInformationData.SizeType;

import no.uib.cipr.matrix.Vector;

public class MutualInformationEqualElem extends AbstractMutualInformation {

	private final SizeType sizeType;
	private boolean[] ranked;
	
	public MutualInformationEqualElem(SizeType sizeType){
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
		if ( !ranked[index] ){
			MatrixUtils.rank(values);
			ranked[index] = true;
		}
		
		return getBinNumber(values.get(k)-1, numBins, 0, numFeatures - 1);
	}
	
	protected void init() {
		numBins = getNumberOfBins(numFeatures, sizeType);
		ranked = new boolean[numGenes];
	}
	
}
