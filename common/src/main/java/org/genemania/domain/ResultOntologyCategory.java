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

package org.genemania.domain;

public class ResultOntologyCategory implements Comparable {

	private OntologyCategory ontologyCategory;
	private double pValue;
	private double qValue;
	private int numAnnotatedInSample;
	private int numAnnotatedInTotal;

	public ResultOntologyCategory() {
	}

	public ResultOntologyCategory(OntologyCategory ontologyCategory,
			double pValue, double qValue, int numAnnotatedInSample,
			int numAnnotatedInTotal) {
		super();
		this.ontologyCategory = ontologyCategory;
		this.pValue = pValue;
		this.qValue = qValue;
		this.numAnnotatedInSample = numAnnotatedInSample;
		this.numAnnotatedInTotal = numAnnotatedInTotal;
	}

	@Override
	public String toString() {
		return "ResultOntologyCategory [numAnnotatedInSample="
				+ numAnnotatedInSample + ", numAnnotatedInTotal="
				+ numAnnotatedInTotal + ", ontologyCategory="
				+ ontologyCategory + ", pValue=" + pValue + ", qValue="
				+ qValue + "]";
	}

	public long getId() {
		return this.ontologyCategory.getId();
	}

	public OntologyCategory getOntologyCategory() {
		return ontologyCategory;
	}

	public void setOntologyCategory(OntologyCategory ontologyCategory) {
		this.ontologyCategory = ontologyCategory;
	}

	public double getpValue() {
		return pValue;
	}

	public void setpValue(double pValue) {
		this.pValue = pValue;
	}

	public double getqValue() {
		return qValue;
	}

	public void setqValue(double qValue) {
		this.qValue = qValue;
	}

	public int getNumAnnotatedInSample() {
		return numAnnotatedInSample;
	}

	public void setNumAnnotatedInSample(int numAnnotatedInSample) {
		this.numAnnotatedInSample = numAnnotatedInSample;
	}

	public int getNumAnnotatedInTotal() {
		return numAnnotatedInTotal;
	}

	public void setNumAnnotatedInTotal(int numAnnotatedInTotal) {
		this.numAnnotatedInTotal = numAnnotatedInTotal;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + numAnnotatedInSample;
		result = prime * result + numAnnotatedInTotal;
		result = prime
				* result
				+ ((ontologyCategory == null) ? 0 : ontologyCategory.hashCode());
		long temp;
		temp = Double.doubleToLongBits(pValue);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(qValue);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ResultOntologyCategory other = (ResultOntologyCategory) obj;
		if (numAnnotatedInSample != other.numAnnotatedInSample)
			return false;
		if (numAnnotatedInTotal != other.numAnnotatedInTotal)
			return false;
		if (ontologyCategory == null) {
			if (other.ontologyCategory != null)
				return false;
		} else if (!ontologyCategory.equals(other.ontologyCategory))
			return false;
		if (Double.doubleToLongBits(pValue) != Double
				.doubleToLongBits(other.pValue))
			return false;
		if (Double.doubleToLongBits(qValue) != Double
				.doubleToLongBits(other.qValue))
			return false;
		return true;
	}

	@Override
	public int compareTo(Object o) {

		if (o instanceof ResultOntologyCategory) {
			ResultOntologyCategory other = (ResultOntologyCategory) o;
			
			if( this.getqValue() < other.getqValue() ){
				return -1;
			} else if( this.getqValue() > other.getqValue() ){
				return 1;
			} else {
				return 0;
			}
			
		}

		return 0;
	}

}
