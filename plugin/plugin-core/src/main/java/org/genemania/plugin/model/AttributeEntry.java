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

/**
 * 
 */
package org.genemania.plugin.model;

import java.io.Serializable;

public class AttributeEntry implements Serializable {
	
	private static final long serialVersionUID = 8808905964273217390L;
	
	private final String displayName;
	private final String attributeName;
	private boolean selected;
	
	public AttributeEntry(String displayName, String attributeName) {
		this.displayName = displayName;
		this.attributeName = attributeName;
	}
	
	public String getDisplayName() {
		return displayName;
	}
	
	public String getAttributeName() {
		return attributeName;
	}
	
	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	
	public boolean isSelected() {
		return selected;
	}
}
