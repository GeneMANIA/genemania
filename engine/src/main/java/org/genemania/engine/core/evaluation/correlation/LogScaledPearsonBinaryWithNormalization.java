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
 * Transforms the data by replacing 1 with -log(B) and 0 with log(1-B), where B = proportion of 1 of
 * the feature. After scaling, the correlation is calculated by normalizing the columns, and taking
 * the dot product of the log terms. The difference between LogScaledPearsonBinaryNoNormalization
 * and LogScaledPearsonBinaryWithNormalization is that LogScaledPearsonBinaryNoNormalization doesn't
 * perform the normalizing step.
 * 
 * Each gene record, g_i = ( r1_i, r2_i, r3_i, ..., rn_i), is represented by:
 * (r1_i, r2_i, ..., rn_i) = (s1_i, s2_i, ..., sn_i) + k_i * (d1, d2,..., dn)
 * 
 * where sk_i is a vector of -log(B) - log(1-B) where rk_i = 1
 *       d is the vector of the log(1-B) for each column/feature
 *       k_i is initialized to 1
 *       
 * @author Pauline
 *
 */
public class LogScaledPearsonBinaryWithNormalization extends AbstractPearson {

	protected double dd;
	protected double dtd;
	
	protected void createPearsonRow() {
		int numGenes = geneData.getGeneExpression().size();
		int numFeatures = geneData.getGeneExpression().get(0).size();
		
		int[] nonZeroCounts = new int[numFeatures];  // counts of zeroes for each feature
		Vector d = new SparseVector(numFeatures);
		
		// counts all the zeroes in the profile data
		for ( int i = 0; i < rows.length; i++ ){
			Vector v = geneData.getGeneExpression().get(i);
			for ( VectorEntry e: v ){
				nonZeroCounts[e.index()]++;
			}
		}
		
		// calculate b
		for ( int i = 0; i < numFeatures; i++ ){
			double b = (double) nonZeroCounts[i] / numGenes;
			d.set(i, Math.log(1-b));
		}

		// transform the vector
		for ( int i = 0; i < rows.length; i++ ){
			Vector v = geneData.getGeneExpression().get(i);
			
			for ( VectorEntry e: v ){
				double b = (double) nonZeroCounts[e.index()] / numGenes;
				e.set( -1 * Math.log(b) - Math.log(1-b) );
			}
			rows[i] = new PearsonRow(v, d, 1, 0);
		}
		
		this.dd = MatrixUtils.sum(d);
		this.dtd = d.dot(d);
	}

	protected double getDD() {
		return this.dd;
	}

	protected double getDTD() {
		return this.dtd;
	}
}
