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

package org.genemania.engine.core;

import java.io.PrintWriter;
import java.util.List;


import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.sparse.FlexCompColMatrix;

public class SparseBinaryToNetwork {
	
	int numFeatures;
	int numGenes;
	int [] numNonZerosPerFeature;
	double [] pf; 
	double [] nf;
	double [] ppf;
	double [] pnf;
	double [] nnf;
	double baseline;
	KHeap[] topInteractions;
		
	public SparseBinaryToNetwork(int numGenes, int numFeatures) {
		this.numGenes = numGenes;
		this.numFeatures = numFeatures;
	}
	
	
	public void convert(List<int[]> profileData, int k) {				
		countNonZerosPerFeature(profileData);
		computeFactors();
		computeNegBaseline();
		computeCorrelations(profileData, k);  
	}
	
	
	/**
	 * loop over data and build up counts
	 * 
	 * @param numFeatures
	 * @param profileData
	 * @return
	 */
	protected void countNonZerosPerFeature(List<int[]> profileData) {
		numNonZerosPerFeature = new int[numFeatures];

		for (int [] g: profileData) {
			for (int i=0; i<g.length; i++) {
				numNonZerosPerFeature[g[i]] += 1;
			}
		}
	}
	
	protected void computeFactors() {
		pf = new double[numFeatures];
		nf = new double[numFeatures];
		ppf = new double[numFeatures];
		pnf = new double[numFeatures];
		nnf = new double[numFeatures];
		
		for (int i=0; i<numFeatures; i++) {
			double mean = (1.0 * numNonZerosPerFeature[i]) / numGenes;
			
			if (mean == 0) { // TODO: this works out okay? Or should we filter out such data?			
				continue;
			}
			
			pf[i] = -Math.log(mean);
			nf[i] = Math.log(1-mean);
			ppf[i] = pf[i]*pf[i];
			pnf[i] = pf[i]*nf[i];
			nnf[i] = nf[i]*nf[i];
		}		
	}

	protected void computeNegBaseline() {
		double baseline = 0d;
		for (int i=0; i<numFeatures; i++) {
			baseline += nnf[i];
		}
		this.baseline = baseline;
	}
	
	protected double computeCorrelation(int [] a, int [] b) {
		int i=0;
		int j=0;
		
		double correction = 0;
		
		int u=0, v=0;
		
		while (i < a.length || j < b.length) {
			
			// this indexing is incomprehensible, set to numFeatures because it is one larger than the
			// largest possible index, and will never be dereferenced due to the while condition			
			if (i<a.length)
				u = a[i];
			else
				u = numFeatures;
			
			if (j<b.length)
				v = b[j];
			else
				v = numFeatures;
		
			// TODO: think of ways to compute this without all these branches
			// in the inner loop, eg expand to two dense arrays ...
			// would that be faster or slower?
			//
			// and what about significant digits? maybe better to add up all the
			// -nnf corrections and all the pnf/ppf corrections separately			
			if (u < v) {
				correction += -nnf[u] + pnf[u];
				i += 1;
			}
			else if (u > v) {
				correction += -nnf[v] + pnf[v];
				j += 1;
			}
			else { // u = v
				correction += -nnf[u] + ppf[u];
				i += 1;
				j += 1;
			}
		}
		
		return baseline + correction;
	}
	
	/**
	 * compute correlations for all the genes in the data set, retaining
	 * only the top K for each.
	 * 
	 * @param profileData
	 * @param k
	 */
	protected void computeCorrelations(List<int[]> profileData, int k) {
		// initialize our heaps ... allocates storage
		int numGenes = profileData.size();
		topInteractions = new KHeap[numGenes];
		for (int i=0; i<numGenes; i++) {
			topInteractions[i] = new KHeap(k);
		}
		
		System.out.println("computing correlations");
		
		for (int i=0; i<numGenes; i++) {
			for (int j=0; j<i; j++) {
				double weight = computeCorrelation(profileData.get(i), profileData.get(j));
				topInteractions[i].offer(j, weight);
				topInteractions[j].offer(i, weight);
			}
		}
		
		System.out.println("done");
	}
	
	public Matrix convertToInteractionMatrix() {
		int numGenes = topInteractions.length;
		
		FlexCompColMatrix matrix = new FlexCompColMatrix(numGenes, numGenes);

		for (int i=0; i<numGenes; i++) {
			int n = topInteractions[i].size();
			for (int j=0; j<n; j++) {
				int i2 = (int) topInteractions[i].getId(j);
				double weight = topInteractions[i].getWeight(j);
				matrix.set(i, i2, weight);
			}
		}

		// ensure symmetric
		matrix = MatrixUtils.computeMaxTranspose(matrix);		
		return matrix;
	}
	
	public int dump(PrintWriter writer, String[] geneNames) {
		int numGenes = topInteractions.length;
		
		int totalInteractions = 0;
		
		for (int i=0; i<numGenes; i++) {
			String name1 = geneNames[i];
			int n = topInteractions[i].size();
			for (int j=0; j<n; j++) {
				long id = topInteractions[i].getId(j);
				double weight = topInteractions[i].getWeight(j);
				String name2 = geneNames[(int)id];
				writer.println(name1 + "\t" + name2 + "\t" + weight);
				totalInteractions += 1;
			}
		}
		
		return totalInteractions;
	}	
}
