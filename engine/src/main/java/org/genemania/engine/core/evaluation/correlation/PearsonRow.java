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

/**
 * Each gene record, g_i = (r1_i, r_i, ..., rn_i), is represented by:
 * (r1_i, r2_i, ..., rn_i) = (s1_i, s2_i, ..., sn_i) + k_i * (d1, d2,..., dn) + m_i * (1, 1, ..., 1)
 * 
 * So the dot product of g_i and g_j to calculate pearson/pearson rank/spearman becomes:
 * (r1_i, r2_i, ..., rn_i) * (r1_j, r2_j, ..., rn_j)
 * = [ (s1_i, s2_i, ..., sn_i) + k_i*(d1, d2,..., dn) + m_i*(1, 1, ..., 1) ] . 
 *   [ (s1_j, s2_j, ..., sn_j) + k_j*(d1, d2,..., dn) + m_j*(1, 1, ..., 1) ]
 * = ...
 * = (s1_i, s2_i, ..., sn_i) * (d1, d2,..., dn) * k_j +
 *   (s1_j, s2_j, ..., sn_j) * (d1, d2,..., dn) * k_i +
 *   ss_i * m_j + 
 *   ss_j * m_i +
 *   k_i * k_j * dtd + 
 *   (k_i * m_j + m_i * m_j) * dd +
 *   m_i * m_j * n
 *  
 *  where ss_i = sum of all the elements in s_i (s1_i, s2_i, ..., sn_i)
 *        dtd = d transposed multiplied by d
 *        dd = d transposed multiplied by one-vector
 *        n = number of elements in s_i
 *  
 * @author Pauline
 */
public class PearsonRow {
	private Vector s;
	private Vector d;
	private double k;
	private double m;
	private double ss; // ss = sum of elements in s
	private double sd; // sd = s * d
	
	public PearsonRow(Vector s, Vector d, double k, double m){
		this.s = s;
		this.d = d;
		this.k = k;
		this.m = m;
		calculateSS();
		calculateSD();
	}
	
	public Vector getS(){
		return this.s;
	}
	
	/**
	 * (r1_i, r_i, ..., rn_i) + k 
	 * = (s1_i, s2_i, ..., sn_i) + k_i * (d1, d2,..., dn) + (m_i + k) * (1, 1, ..., 1)
	 */
	public void add(double k){
		this.m += k;
	}
	
	/**
	 * a*(r1_i, r_i, ..., rn_i)
	 * = a*(s1_i, s2_i, ..., sn_i) + a* k_i * (d1, d2,..., dn) + a* m_i * (1, 1, ..., 1)
	 * @param a
	 */
	public void multiply(double a){
		this.s.scale(a);	
		this.k *= a;
		this.m *= a;
		calculateSS();
		calculateSD();
	}
	
	/**
	 * normalize by subtracting from the mean and dividing by stdev
	 * @param mean
	 * @param stdev
	 */
	public void normalize(double mean, double stdev){
		this.s.scale(1/(stdev + 0.0000000000001d));
		this.k = this.k / (stdev + 0.0000000000001d);
		this.m = (this.m - mean) / (stdev + 0.0000000000001d);
		calculateSS();
		calculateSD();
	}
	
	/**
	 * Performs the dot product
	 */
	public static double dot(PearsonRow r1, PearsonRow r2, double dtd, double dd, int n){
		double retVal = r1.s.dot(r2.s) +
		         		r1.sd * r2.k + 
		         		r2.sd * r1.k +
		         		r1.ss * r2.m + 
		         		r2.ss * r1.m +
		         		r1.k * r2.k * dtd + 
		         		(r1.k * r2.m + r2.k * r1.m) * dd +
		         		r1.m * r2.m * n;
		return retVal;
	}
	
	/*
	 * how do you zero-out this object? set the per-row elements
	 * s, k, and m to 0. remember not to touch d, that is
	 * shared between a set of pearson-row objects (and multiplied by
	 * k anyways)
	 */
	public void zero() {
	    s.zero();
	    m = 0;
	    k = 0;
	}
	
	/**
	 * Replace any missing data with value
	 */
	public void replaceMissingData(double value){
		boolean hasNan = false;
		
		for ( VectorEntry e: s ){
			if ( Double.isNaN(e.get()) ){
				double newValue; 
				
				if ( d.size() > 0 ){
					newValue = value - k * d.get(e.index()) - m;
				} else {
					newValue = value - m;
				}
				
				e.set(newValue);
				hasNan = true;
			}
		}
		if ( hasNan ){
			calculateSS();
			calculateSD();
		}
	}
	
	public int getNumberOfElements(){
		return this.s.size();
	}
	
	/**
	 * @param i
	 * @return the actual gene expression at index i
	 */
	public double getValueAt(int i){
		if ( d.size() > 0 ){
			return s.get(i) + k * d.get(i) + m;
		} else {
			// no d, k * d.get(i) = 0
			return s.get(i) + m;
		}
	}
	
	/**
	 * updates sum of elements in s
	 */
	private void calculateSS(){
		this.ss = MatrixUtils.sum(s);
	}
	
	/**
	 * updates dot product of s and d
	 */
	private void calculateSD(){
		if ( this.d.size() > 0 ){
			this.sd = this.s.dot(this.d);
		}
	}
}
