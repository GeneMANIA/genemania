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

import java.util.ArrayList;
import java.util.List;

import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Vector;
import no.uib.cipr.matrix.VectorEntry;
import no.uib.cipr.matrix.sparse.SparseVector;

import org.genemania.engine.core.MatrixUtils;

/**
 * Rank the features and computes the Pearson correlation.
 * 
 * Each gene record, g_i = ( r1_i, r2_i, r3_i, ..., rn_i), is represented by:
 * (r1_i, r2_i, ..., rn_i) = (s1_i, s2_i, ..., sn_i) + k_i * (d1, d2,..., dn)
 * 
 * where s_i is the vector of the rank of the value - the rank of 0's
 *       d is the vector of the rank of 0's for each column/feature
 *       k_i is initialized to 1
 *       
 * @author Pauline
 *
 */
public class PearsonColumnRank extends AbstractPearson {

	private double dtd;
	private double dd;
	
	protected void createPearsonRow() {
		List<Vector> geneExpressions = geneData.getGeneExpression();
		int numFeatures = geneExpressions.get(0).size();
		int numGenes = geneExpressions.size();
		
		// create the vector for each column
		List<Vector> columnValues = new ArrayList<Vector>(numFeatures);
		for ( int i = 0; i < numFeatures; i++ ){
			Vector v;
			// initialize v to the same type as the original vectors
			if ( geneExpressions.get(0) instanceof DenseVector ){
				v = new DenseVector(numGenes);
			} else if ( geneExpressions.get(0) instanceof SparseVector ) { 
				v = new SparseVector(numGenes);
			} else {
				throw new RuntimeException( "Unexpected vector type of " + geneExpressions.get(0).getClass().getName() );
			}
			columnValues.add(v);
		}
		
		// populate these column vectors 
		for ( int i = 0; i < numGenes; i++ ){
			Vector v = geneExpressions.get(i);
			for ( VectorEntry e: v ){
				columnValues.get( e.index() ).set( i, e.get() );
			}
		}

		Vector d = new DenseVector(numFeatures);
		
		// rank these column vectors
		for ( int i = 0; i < numFeatures; i++ ){
			Vector v = columnValues.get(i);
			double zeroRank = getZeroRank(v);
			d.set(i, zeroRank);
			MatrixUtils.tiedRank(v);
			// take the zero ranks into account
			if ( zeroRank > 0 ){ 
				// we're dealing with sparse data
				for ( VectorEntry e: v ){
					v.set( e.index(), e.get() + zeroRank - 1 );
				}
			}
		}
		
		// put these rank into geneExpression
		for ( int i = 0; i < numFeatures; i++ ){
			Vector v = columnValues.get(i);
			for ( VectorEntry e: v ){
				geneExpressions.get( e.index() ).set( i, e.get() );
			}
		}

		// create PearsonRow
		for ( int i = 0; i < rows.length; i++ ){
			Vector v = geneExpressions.get(i);
			rows[i] = new PearsonRow(v, d, 1, 0);
		}
		
		this.dd = MatrixUtils.sum(d);
		this.dtd = d.dot(d);
	}

	protected double getDD() {
		return dd;
	}

	protected double getDTD() {
		return dtd;
	}
}
