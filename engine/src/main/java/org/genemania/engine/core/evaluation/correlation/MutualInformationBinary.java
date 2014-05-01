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

import no.uib.cipr.matrix.Vector;

/**
 * Calculates the mutual information between two genes from a binary profile.
 * 
 * @author Pauline
 *
 */
public class MutualInformationBinary extends AbstractMutualInformation {

	/**
	 * The bin number is just the value, e.g. 0 or 1
	 */
	protected int getBinNumber(int index, Vector values, int k) {
		return (int) values.get(k);
	}

	protected void init() {
		numBins = 2;
	}

}
