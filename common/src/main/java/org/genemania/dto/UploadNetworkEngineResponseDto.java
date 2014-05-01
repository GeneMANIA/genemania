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

/**
 * UploadNetworkEngineResponseDto: Engine-specific Upload Network response data transfer object   
 * Created Oct 19, 2009
 * @author Ovi Comes
 */
package org.genemania.dto;

import java.io.Serializable;

public class UploadNetworkEngineResponseDto implements Serializable {

    // __[static]______________________________________________________________
    private static final long serialVersionUID = 8000605340540063791L;

    // __[attributes]__________________________________________________________
    int numInteractions;
    double minValue;
    double maxValue;

    // __[constructors]________________________________________________________
    public UploadNetworkEngineResponseDto() {
    }

    // __[accessors]___________________________________________________________
    /**
     * @return the numInteractions
     */
    public int getNumInteractions() {
        return numInteractions;
    }

    /**
     * @param numInteractions the numInteractions to set
     */
    public void setNumInteractions(int numInteractions) {
        this.numInteractions = numInteractions;
    }

    /**
     * @return the minValue
     */
    public double getMinValue() {
        return minValue;
    }

    /**
     * @param minValue the minValue to set
     */
    public void setMinValue(double minValue) {
        this.minValue = minValue;
    }

    /**
     * @return the maxValue
     */
    public double getMaxValue() {
        return maxValue;
    }

    /**
     * @param maxValue the maxValue to set
     */
    public void setMaxValue(double maxValue) {
        this.maxValue = maxValue;
    }
    
    // __[public helpers]______________________________________________________
    @Override
    public String toString() {
    	StringBuffer ret = new StringBuffer("UploadNetworkEngineResponseDto[");
    	ret.append("numInteractions=" + numInteractions + ", ");
        ret.append("minValue=" + minValue + ", ");
        ret.append("maxValue=" + maxValue);
    	ret.append("]");
    	return ret.toString();
    }
    
}
