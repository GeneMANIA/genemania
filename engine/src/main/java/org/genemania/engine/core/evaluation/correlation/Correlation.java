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

import org.genemania.engine.core.evaluation.ProfileData;

/**
 * Interface for calculating the correlation between two genes in a profile
 * 
 * @author Pauline
 *
 */
public interface Correlation {

	/**
	 * Allows the implementation class to process profile data and store necessary things to 
	 * compute the correlations later
	 * 
	 * @param data the profile data
	 */
	public void init( ProfileData data );
	
	/**
	 * provide a method and possibly data-set dependent
     * threshold hint to callers to filter out values we
     * don't think are positively correlated.
	 * 
	 * this can only be called after init(), and should not change
	 */
	public double getThresholdValue();
	
	/**
	 * @param i
	 * @param j
	 * @return the correlation between the ith gene and the jth gene
	 */
	public double computeCorrelations( int i, int j );
}
