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
 * ProfileRowVO: Individual gene profile 
 * Created Oct 29, 2009
 * @author Ovi Comes
 */
package org.genemania.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

public class ProfileRowDto implements Serializable {

	// __[static]______________________________________________________________
	private static final long serialVersionUID = -8945834357000457255L;
	
	// __[attributes]__________________________________________________________
	private long nodeId;
	private Collection<String> features = new ArrayList<String>();

	// __[constructors]________________________________________________________
	public ProfileRowDto() {
	}

	// __[accessors]___________________________________________________________
	public long getNodeId() {
		return nodeId;
	}

	public void setNodeId(long nodeId) {
		this.nodeId = nodeId;
	}

	public Collection<String> getFeatures() {
		return features;
	}

	public void setFeatures(Collection<String> features) {
		this.features = features;
	}
	
	// __[public helpers]______________________________________________________
	public void addFeature(String feature) {
		this.features.add(feature);
	}
	
	@Override
	public String toString() {
		StringBuffer ret = new StringBuffer();
		return ret.toString().trim();
	}

}
