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

package org.genemania.data.normalizer;

import java.util.Set;

public class NormalizationResult {
	int droppedEntries;
	int totalEntries;
	Set<String> invalidSymbols;

	public int getDroppedEntries() {
		return droppedEntries;
	}

	public void setDroppedEntries(int droppedEntries) {
		this.droppedEntries = droppedEntries;
	}

	public int getTotalEntries() {
		return totalEntries;
	}

	public void setTotalEntries(int totalInteractions) {
		this.totalEntries = totalInteractions;
	}

	public Set<String> getInvalidSymbols() {
		return invalidSymbols;
	}

	public void setInvalidSymbols(Set<String> invalidSymbols) {
		this.invalidSymbols = invalidSymbols;
	}
	
	public boolean hasErrors() {
		return invalidSymbols.size() > 0 || droppedEntries > 0 || totalEntries == 0;
	}
}
