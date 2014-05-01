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

import no.uib.cipr.matrix.Vector;
import no.uib.cipr.matrix.VectorEntry;
import no.uib.cipr.matrix.sparse.SparseVector;

/**
 * Calculates the Spearman correlation
 * 
 * 
 * Each gene record, g_i = ( r1_i, r2_i, r3_i, ..., rn_i), is represented by:
 * (r1_i, r2_i, ..., rn_i) = (s1_i, s2_i, ..., sn_i) + m_i * (1, 1, ..., 1)
 * 
 * where s_i is the vector of the rank of the value - the rank of 0's
 *       m_i is the rank of 0's
 * 
 * @author Pauline
 *
 */
public class Spearman extends AbstractPearson {

	protected void createPearsonRow(){
		Vector d = new SparseVector(0);
		
		for ( int i = 0; i < rows.length; i++ ){
			Vector v = geneData.getGeneExpression().get(i);
			double zeroRank = getZeroRank(v);
			MatrixUtils.tiedRank(v);
			// take the zero ranks into account
			if ( zeroRank > 0 ){ 
				// we're dealing with sparse data
				for ( VectorEntry e: v ){
					v.set( e.index(), e.get() + zeroRank - 1 );
				}
			}

			rows[i] = new PearsonRow(v, d, 0, zeroRank);
		}
	}

	protected double getDD() {
		// d is empty, so dd = 0
		return 0;
	}

	protected double getDTD() {
		// d is empty, so dd = 0
		return 0;
	}
}
