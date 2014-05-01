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
import no.uib.cipr.matrix.sparse.SparseVector;

/**
 * Computes the Pearson correlation
 * 
 * 
 * Each gene record, g_i = ( r1_i, r2_i, r3_i, ..., rn_i), is represented by:
 * (r1_i, r2_i, ..., rn_i) = (s1_i, s2_i, ..., sn_i)
 * 
 * @author Pauline
 *
 */
public class Pearson extends AbstractPearson {
	
	protected void createPearsonRow(){
		Vector d = new SparseVector(0);
		
		for ( int i = 0; i < rows.length; i++ ){
			Vector v = geneData.getGeneExpression().get(i);
			rows[i] = new PearsonRow(v, d, 0, 0);
		}
	}

	protected double getDD() {
		// d is empty, so dd = 0
		return 0;
	}

	protected double getDTD() {
		// d is empty, so dtd = 0
		return 0;
	}
}
