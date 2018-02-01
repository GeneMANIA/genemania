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
import java.util.ArrayList;
import java.util.Collection;

import org.genemania.domain.InteractionNetwork;
import org.genemania.domain.InteractionNetworkGroup;

/**
 * Used only to return the necessary info as JSON through CyREST.
 */
public class InteractionNetworkGroupDto implements Serializable {

	private static final long serialVersionUID = 1831766997644341394L;

	private String code;
	private String name;
	private String description;
	private Collection<InteractionNetworkDto> interactionNetworks = new ArrayList<>();

	public InteractionNetworkGroupDto() {
    }

	public InteractionNetworkGroupDto(InteractionNetworkGroup group) {
		this.code = group.getCode();
		this.name = group.getName();
		this.description = group.getDescription();
		
		if (group.getInteractionNetworks() != null) {
			Collection<InteractionNetworkDto> networks = new ArrayList<>();
			
			for (InteractionNetwork in : group.getInteractionNetworks())
				networks.add(new InteractionNetworkDto(in));
			
			setInteractionNetworks(networks);
		}
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
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

	public Collection<InteractionNetworkDto> getInteractionNetworks() {
		return new ArrayList<>(interactionNetworks);
	}

	public void setInteractionNetworks(Collection<InteractionNetworkDto> interactionNetworks) {
		this.interactionNetworks.clear();
		
		if (interactionNetworks != null)
			this.interactionNetworks.addAll(interactionNetworks);
	}
	
	@Override
	public String toString() {
		return name;
	}
}
