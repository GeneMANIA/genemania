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
 * ConversionDto: base class for conversion data trasfer objects    
 * Created Oct 29, 2008
 * @author Ovi Comes
 */
package org.genemania.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.genemania.type.DataLayout;

public abstract class ConversionDto implements Serializable {

	// __[static]______________________________________________________________
	private static final long serialVersionUID = -7287204811040341384L;

	// __[attributes]__________________________________________________________
	private long totalLinesCount;
	private List<String> unrecognizedLines = new ArrayList<String>();
	private DataLayout layout = DataLayout.UNKNOWN;

	// __[accessors]___________________________________________________________
	public List<String> getUnrecognizedLines() {
		return unrecognizedLines;
	}

	public long getTotalLinesCount() {
		return totalLinesCount;
	}

	public void setTotalLinesCount(long totalLinesCount) {
		this.totalLinesCount = totalLinesCount;
	}

	public void setUnrecognizedLines(List<String> unrecognizedLines) {
		this.unrecognizedLines = unrecognizedLines;
	}

	public DataLayout getLayout() {
		return layout;
	}

	public void setLayout(DataLayout layout) {
		this.layout = layout;
	}

}
