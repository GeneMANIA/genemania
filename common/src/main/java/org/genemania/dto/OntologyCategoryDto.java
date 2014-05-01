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

package org.genemania.dto;

import java.io.Serializable;

public class OntologyCategoryDto implements Serializable {

	// __[static]______________________________________________________________
	private static final long serialVersionUID = 1361271226874556288L;

	// __[attributes]__________________________________________________________
	private long id;
	private double pValue;
	private double qValue;
	private int numAnnotatedInSample;
	private int numAnnotatedInTotal;

	// __[constructors]________________________________________________________

	// __[accessors]___________________________________________________________
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
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
    
    public String toString() {
    	StringBuffer ret = new StringBuffer("OntologyCategoryVO{");
    	ret.append("[" + id + "]");
    	ret.append("[" + pValue + "]");
    	ret.append("[" + qValue + "]");
    	ret.append("[" + numAnnotatedInSample + "/" + numAnnotatedInTotal + "]");
    	ret.append("}");
    	return ret.toString();
    }
    
}
