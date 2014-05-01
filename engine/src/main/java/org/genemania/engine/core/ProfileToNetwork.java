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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.log4j.Logger;

import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Matrices;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.MatrixEntry;
import no.uib.cipr.matrix.Vector;
import no.uib.cipr.matrix.VectorEntry;
import no.uib.cipr.matrix.sparse.FlexCompColMatrix;

/**
 * This version does some handling of missing values -- which show up
 * as NaNs in the input matrix
 * 
 * Given an MxN matrix of profile data, compute an NxN network of correlations
 * between the observables, sparsified to the largest ~k correlations
 * for each node.
 *
 * M = number of features, rows
 * N = number of genes, columns
 * 
 * TODO: should probably make this abstract or an interface, and implement
 * separate classes for binary and real-valued profile data, instead of
 * all the methods grouped together here.
 */
public class ProfileToNetwork {

	private static Logger logger = Logger.getLogger(ProfileToNetwork.class);
	static boolean isNaNCheckingEnabled = false;

	public static Matrix continuousProfile(Matrix profile, int k) {
		return continuousProfile(profile, k, 25.0); // TODO: get rid of default and make caller decide?
	}
	
	public static Matrix continuousProfile(Matrix profile, int k, double maxMissingPercentage) {

		int numGenes = profile.numColumns();
		int numFeatures = profile.numRows();
		
		Matrix network = new FlexCompColMatrix(numGenes, numGenes);
		
		// filter out any genes with too many unknown values
		boolean [] goodGenes = MatrixUtils.checkColumnsforMissingDataThreshold(profile, maxMissingPercentage);
		
		Vector counts = MatrixUtils.columnCountsIgnoreMissingData(profile);
		Vector means = MatrixUtils.columnMeanIgnoreMissingData(profile, counts);
		Vector stdevs = MatrixUtils.columnVarianceIgnoreMissingData(profile, means);
		MatrixUtils.sqrt(stdevs);

		computeCorrelationTerms(profile, means, stdevs, counts);

		// fill in NaNs with 0's
		MatrixUtils.replaceMissingData(profile, 0d);
		
		// also fill in columns with possibly some good values but mostly NaNs with 0's
		MatrixUtils.maskFalseColumns(profile, goodGenes, 0d);
		
		findNaNs(profile);
		
		for (int i=0; i<numGenes; i++) {
			
			// this gene was filtered out. skipping is just
			// an optimization, since the values in the profile for this
			// gene have been set to 0 by this point, letting the calculation
			// go through would produce 0's.
			if (goodGenes[i] == false) {
				continue;
			}
			
			Matrix correlations = computeCorrelations(profile, i);
			//System.out.println("correlations: " + correlations.numRows() + " " + correlations.numColumns());
			//for (int z=0; z<correlations.numColumns(); z++) {
			//	System.out.println("z, corr: " + z + " " + correlations.get(0, z));
			//}
			findNaNs(correlations);
			
			setTopK(network, correlations, i, k+1);  // the +1 is because we haven't removed the diagonal term yet
		}

		MatrixUtils.setDiagonalZero(network);
		MatrixUtils.setToMaxTranspose(network);
		
		return network;
	}
	
	public static Object [] continuousProfileWithNames(Matrix profile, int k, String [] names, Map<String, String> identifierMap, double maxMissingPercentage) {

		int numFeatures = profile.numRows();
				

		Vector counts = MatrixUtils.columnCountsIgnoreMissingData(profile);
		Vector means = MatrixUtils.columnMeanIgnoreMissingData(profile, counts);
		Vector stdevs = MatrixUtils.columnVarianceIgnoreMissingData(profile, means);
		MatrixUtils.sqrt(stdevs);
		
		computeCorrelationTerms(profile, means, stdevs, counts);
		
		
		
		Object [] returnValues = averageRepeatedIdentifiers(profile, names, identifierMap);
		profile = (Matrix) returnValues[0];
		names = (String []) returnValues[1];
		
		

		
		// filter out any genes with too many unknown values: TODO: what about averaging, do we filter before or after?
		boolean [] goodGenes = MatrixUtils.checkColumnsforMissingDataThreshold(profile, maxMissingPercentage);
		
		// fill in NaNs with 0's
		MatrixUtils.replaceMissingData(profile, 0d);
		
		// also fill in columns with possibly some good values but mostly NaNs with 0's
		MatrixUtils.maskFalseColumns(profile, goodGenes, 0d);
		
		findNaNs(profile);

		int numGenes = profile.numColumns();
		Matrix network = new FlexCompColMatrix(numGenes, numGenes);

		for (int i=0; i<numGenes; i++) {
			
			// this gene was filtered out. skipping is just
			// an optimization, since the values in the profile for this
			// gene have been set to 0 by this point, letting the calculation
			// go through would produce 0's.
			if (goodGenes[i] == false) {
				continue;
			}
			
//			if ( i == 1139 ){
//				
//				double[] values = new double[24];
//				
//				for ( int l = 0; l < 24; l++ ){
//					values[l] = profile.get(l, i);
//				}
//				System.out.println("here");
//			}
			
			Matrix correlations = computeCorrelations(profile, i);
			//System.out.println("correlations: " + correlations.numRows() + " " + correlations.numColumns());
			//for (int z=0; z<correlations.numColumns(); z++) {
			//	System.out.println("z, corr: " + z + " " + correlations.get(0, z));
			//}
			findNaNs(correlations);
			
//			if ( names[i].equals("Sc:122400") ){
//				System.out.println("here...");
//			}
//			if ( names[i].equals("Sc:119951") ){
//				System.out.println("here...");
//			}
			setTopK(network, correlations, i, k+1);  // the +1 is because we haven't removed the diagonal term yet
		}

		MatrixUtils.setDiagonalZero(network);
		MatrixUtils.setToMaxTranspose(network);
		
		Object [] newReturnValues = new Object[2];
		newReturnValues[0] = network;
		newReturnValues[1] = names;
		return newReturnValues;
	}

	public static Matrix ThresholdOnly(Matrix profile, double low, double hi) {

		int numGenes = profile.numColumns();
		int numFeatures = profile.numRows();
		
		Matrix network = new FlexCompColMatrix(numGenes, numGenes);
		
		Vector counts = MatrixUtils.columnCountsIgnoreMissingData(profile); //TODO this just in ... the rest of this function doesn't do NaN handling
		Vector means = MatrixUtils.columnMean(profile);
		Vector stdevs = MatrixUtils.columnVariance(profile, means);
		MatrixUtils.sqrt(stdevs);

		computeCorrelationTerms(profile, means, stdevs, counts);
				
		for (int i=0; i<numGenes; i++) {
					
			Matrix correlations = computeCorrelationsDiag(profile, i);
			//System.out.println("correlations: " + correlations.numRows() + " " + correlations.numColumns());
			//for (int z=0; z<correlations.numColumns(); z++) {
			//	System.out.println("z, corr: " + z + " " + correlations.get(0, z));
			//}
			findNaNs(correlations);
			
			//setHiLow(network, correlations, i, hi, low);
			//setTopK(network, correlations, i, k+1);  // the +1 is because we haven't removed the diagonal term yet
		}

		//MatrixUtils.setDiagonalZero(network);
		//MatrixUtils.setToMaxTranspose(network);
		
		return network;
	}

	/**
	 * In the matlab ver, use repmat to construct matrics with repeated values
	 * to compute the correlation terms with matrix ops. We'll just do it
	 * directly and avoid allocating the repeated matrix (which matlab is likely
	 * optimizing anyway).
	 * 
	 * This method 
	 * 
	 * @param profile
	 * @param means
	 * @param vars
	 * @return
	 */
	protected static void computeCorrelationTerms(Matrix profile, Vector means, Vector stdevs, Vector counts) {
		
		Vector sqrtCounts = counts.copy();
		MatrixUtils.sqrt(sqrtCounts);
		MatrixUtils.elementMult(stdevs, sqrtCounts);
		
		for (MatrixEntry e: profile) {
			double x = (e.get() - means.get(e.column())) / (stdevs.get(e.column()) + 0.0000000000001d);
			e.set(x);
		}
	}
	
	private static Matrix computeCorrelations(Matrix terms, int i) {
		findNaNs(terms);
		int numGenes = terms.numColumns();
		int numFeatures = terms.numRows();
		
		int [] all = Matrices.index(0, numFeatures);  
		Matrix iTerms = Matrices.getSubMatrix(terms, all, new int[] {i}).copy(); // TODO: do we really need this copy?
		Matrix correlations = new DenseMatrix(1, numGenes); // TODO: how about preallocating this once?
		
		iTerms.transAmult(terms, correlations);
		//correlations.scale(1d/numFeatures);
		
		return correlations;
	}
	
	/**
	 * just for some performance testing ... incomplete
	 * @param terms
	 * @param i
	 * @return
	 */
	private static Matrix computeCorrelationsDiag(Matrix terms, int i) {
		findNaNs(terms);
		int numGenes = terms.numColumns();
		int numFeatures = terms.numRows();

		int [] all = Matrices.index(0, numFeatures);  
		int [] submatrixIndices = Matrices.index(0, i); // TODO: includes self so one extra, should correlate exactly
		
		

		Matrix iTerms = Matrices.getSubMatrix(terms, all, new int[] {i}); // TODO: do we really need this copy?
		//Matrix correlation = new DenseMatrix(1,1);
		Matrix correlations = new DenseMatrix(i+1, 1);
		
		for (int j=0; j<=i; j++) { 
			//Matrix jTerms = Matrices.getSubMatrix(terms, all, new int[] {j});
			//iTerms.transAmult(jTerms, correlation);
			double correlation = sillyColumnMult(terms, i, j);
			correlations.set(j, 0, correlation);
		}
		

		Matrix bTerms = Matrices.getSubMatrix(terms, all, submatrixIndices).copy();
		
		Matrix correlations2 = new DenseMatrix(i, 1);
		
		bTerms.transAmult(iTerms, correlations2);
		
		correlations.scale(1d/numFeatures);

			 

		return correlations;

	}
	
	/**
	 * testing helper ... TODO: cleanup
	 * @param terms
	 * @param i
	 * @param j
	 * @return
	 */
	public static double sillyColumnMult(Matrix terms, int i, int j) {
		DenseMatrix dterms = (DenseMatrix) terms;
		double prod = 0;
		int numRows = terms.numRows();
        int starti = i * numRows;
        int startj = j * numRows;
		double [] data = dterms.getData();

        for (int k=0; k<terms.numRows(); k++) {
        	prod += data[starti+k] * data[startj+k];
		}
        return prod;
	}

	/**
	 * skip elements <= minVal
	 * 
	 * @param network
	 * @param correlations
	 * @param minVal
	 * @param i
	 * @param k
	 */
	private static void setTopK(Matrix network, Matrix correlations, int i, int k) {
		Vector v = MatrixUtils.extractRowToVector(correlations, 0);
		int [] indices = MatrixUtils.getIndicesForSortedValues(v);
		
		for (int j=0; j<indices.length && j<k; j++) {
			if (v.get(indices[j]) != 0) {  // leave 0's as sparse
				network.set(i, indices[j], v.get(indices[j]));
			}
		}
	}
	
	public static void setHiLow(Matrix network, Matrix correlations, int i, double hi, double low) {
		Vector v = MatrixUtils.extractRowToVector(correlations, 0);

		for (int j=0; j<correlations.numColumns(); j++) {
			double val = correlations.get(0, j);
			if (val <= low || val >= hi) {
				network.set(i, j, val);
			}
		}
	}
	
	public static Matrix binaryProfile(Matrix profile, int k) {
		transformBinaryProfile(profile);
		return continuousProfile(profile, k);
	}
	
	
	/**
	 * 1 -> -log(mean), 0 -> (log(1-mean)) where the means are row-wise (for each feature)
	 * @param profile
	 */
	private static void transformBinaryProfile(Matrix profile) {
		Vector means = MatrixUtils.columnMean(profile.transpose()); // TODO: implement row means directly to avoid allocating a transpose ... or is it just a view?
		profile.transpose(); // transpose back, not necessary when we fix the above todo
		
		// for the ones, -log(mean)
		Vector f1 = means.copy();
		MatrixUtils.log(f1);
		f1.scale(-1d);

		// for the zeros, log(1-mean)
		Vector f0 = means.copy();
		f0.scale(-1d);
		MatrixUtils.add(f0, 1d);
		MatrixUtils.log(f0);
		
		// don't have repmat, .* matlab niceness, just loop over and test for now
		for (MatrixEntry e: profile) {
			if (e.get() == 0) {
				e.set(f0.get(e.row()));
			}
			else if (e.get() == 1) {
				e.set(f1.get(e.row()));
			}
			else {
				System.out.println("not a binary profile"); // TODO: throw exception
			}
		}	
	}

	public static Object[] averageRepeatedIdentifiers(Matrix profile, String [] names, Map<String, String> identifierMap) {

		// look up repeats
		LinkedHashMap<String, java.util.Vector<Integer>> occurances = findRepeats(names, identifierMap);
		
		// create a new matrix, averaging together when appropriate
		int numFeatures = profile.numRows();
		int numGenes = occurances.size(); // so possibly fewer columns

		DenseMatrix averagedProfile = new DenseMatrix(numFeatures, numGenes); 
		String [] newNames = new String[numGenes];
		
		// naming hack
		HashMap<String, String> preferredNames = getPreferredNames(identifierMap);

		int [] allRows = Matrices.index(0, numFeatures);

		int col = 0;
		for (String uid: occurances.keySet()) {
			java.util.Vector<Integer> colList = occurances.get(uid);
			if (colList.size() == 1) {
				// just copy over the values			
				MatrixUtils.setColumn(averagedProfile, col, profile, colList.get(0));
			}
			else {
				int [] cols = new int[colList.size()];
				for (int i=0; i<colList.size(); i++) {  // this loop seems colossally stupid
					cols[i] = colList.get(i);  
				}
				
				Matrix subMatrix = Matrices.getSubMatrix(profile, allRows, cols);
				Vector ave = MatrixUtils.rowMeanIgnoreMissingData(subMatrix); // TODO: check that utility functions work correctly on matrix views
				
				MatrixUtils.setColumn(averagedProfile, col, ave);
			}
			newNames[col] = preferredNames.get(uid);
			col += 1;
		}

		Object [] returnValues = new Object[2]; // stupid multi-return value because i seem to like static methods instead of stateful objects
		returnValues[0] = averagedProfile;
		returnValues[1] = newNames;
		return returnValues;
	}
	
	private static HashMap<String, String> getPreferredNames(Map<String, String> identifierMap) {
		// since we don't have a way to represent identifier preferences yet (ordering), lets just
		// loop through all the mappings and pick off the first one we find for now

		HashMap<String, String> preferredNames = new HashMap<String, String>();
		for (String id: identifierMap.keySet()) {
			String uid = identifierMap.get(id);
			if (!preferredNames.containsKey(uid)) {
				preferredNames.put(uid, id);
			}
		}

		return preferredNames;
	}
	
	private static LinkedHashMap<String, java.util.Vector<Integer>> findRepeats(String [] names, Map<String, String> identifierMap) {
		// for each identifier appearing in the dataset, map it to a list of the columns in
		// which it appears, since it may appear multiple times or by different aliases
		LinkedHashMap<String, java.util.Vector<Integer>> occurances = new LinkedHashMap<String, java.util.Vector<Integer>>();
		
		for (int i=0; i<names.length; i++) {
			String key = names[i];
			String uid = identifierMap.get(key);
			if (uid == null) {
				// TODO: gene not in our naming table, kill it				
			}
			else {
				// add the index to our list for that unique id
				if (occurances.containsKey(uid)) {				
					java.util.Vector<Integer> colList = occurances.get(uid);
					colList.add(i);
				}
				else {					
					java.util.Vector<Integer> colList = new java.util.Vector<Integer>();
					colList.add(i);
					occurances.put(uid, colList);					
				}
			}
		}
		
		return occurances;
	}

	
	/**
	 * for debugging
	 * 
	 * @param m
	 */
	public static void findNaNs(Matrix m) {
		if (!isNaNCheckingEnabled) {
			return;
		}
		
		for (MatrixEntry e: m) {
			if (Double.isNaN(e.get())) {
				logger.info("found NaN at " + e.row() + " " + e.column());
			}
		}
	}
	
	
	
	/**
	 * for debugging
	 * 
	 * @param v
	 */
	public static void findNaNs(Vector v) {
		if (!isNaNCheckingEnabled) {
			return;
		}

		for (VectorEntry e: v) {
			if (Double.isNaN(e.get())) {
				logger.info("found NaN at " + e.index());
			}			
		}
	}
	
	
	/**
	 * Convert expression levels into ranks
 	 */
	public static void convertProfileToRanks(Matrix profile){
		int numGenes = profile.numRows();
		int numFeatures = profile.numColumns();
		
		for (int i = 0; i < numGenes; i++) {
			Vector v = new DenseVector(numFeatures);
			
			for (int j = 0; j < numFeatures; j++){
				v.set(j, profile.get(i, j));
			}
			
			MatrixUtils.tiedRank(v);
			
			for (int j = 0; j < numFeatures; j++){
				profile.set(i, j, v.get(j));
			}
		}
	}
}
