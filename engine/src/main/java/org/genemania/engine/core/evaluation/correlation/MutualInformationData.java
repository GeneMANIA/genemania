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

/**
 * Holds information for mutual information
 * 
 * @author Pauline
 *
 */
public class MutualInformationData {

	public enum SizeType {
		LOWER, MEDIAN, UPPER
	}
	
	// do the bins have equal number of elements?
	private boolean equalElementBin;
	
	// how the size of the bin is determined
	private SizeType sizeType;
	
	// should the values be discretized via binning?
	private final boolean binning;
	
	public MutualInformationData(boolean binning) {
		this.binning = binning;
	}

	public void setBinningInfo(boolean equalElementBin, SizeType sizeType){
		this.equalElementBin = equalElementBin;
		this.sizeType = sizeType;
	}
	
	public boolean isEqualElementBin() {
		return equalElementBin;
	}

	public SizeType getSizeType() {
		return sizeType;
	}

	public boolean useBinning() {
		return binning;
	}
}
