/**
 * This file is part of GeneMANIA.
 * Copyright (C) 2018 University of Toronto.
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
package org.genemania.plugin.cytoscape3.model;

import java.io.Serializable;

import org.genemania.domain.InteractionNetwork;
import org.genemania.domain.NetworkMetadata;

/**
 * Used only to return the necessary info as JSON through CyREST.
 */
public class InteractionNetworkDto implements Serializable {

	private static final long serialVersionUID = -1443726034669275528L;

	private long id;
	private String name;
	private String description;
	private NetworkMetadata metadata;
	private boolean defaultSelected;

	public InteractionNetworkDto() {
	}

	public InteractionNetworkDto(InteractionNetwork in) {
		this.id = in.getId();
		this.name = in.getName();
		this.description = in.getDescription();
		this.metadata = in.getMetadata();
		this.defaultSelected = in.isDefaultSelected();
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public NetworkMetadata getMetadata() {
		return metadata;
	}

	public void setMetadata(NetworkMetadata metadata) {
		this.metadata = metadata;
	}

	public boolean isDefaultSelected() {
		return defaultSelected;
	}

	public void setDefaultSelected(boolean defaultSelected) {
		this.defaultSelected = defaultSelected;
	}

	@Override
	public String toString() {
		return name;
	}
}
