/**
 * This file is part of GeneMANIA.
 * Copyright (C) 2008-2011 University of Toronto.
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

package org.genemania.plugin.data;

public class DataDescriptor implements Comparable<DataDescriptor> {
	private final String id;
	private final String description;

	public DataDescriptor(String id, String description) {
		this.id = id;
		this.description = description;
	}
	
	public String getId() {
		return id;
	}
	
	public String getDescription() {
		return description;
	}

	public int compareTo(DataDescriptor o) {
		return id.compareTo(o.id);
	}
	
	@Override
	public String toString() {
		return description;
	}
	
	@Override
	public boolean equals(Object obj) {
		DataDescriptor other = (DataDescriptor) obj;
		return id.equals(other.id);
	}
	
	@Override
	public int hashCode() {
		return id.hashCode();
	}
}
