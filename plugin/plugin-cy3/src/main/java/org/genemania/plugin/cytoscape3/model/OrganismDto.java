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

import org.genemania.domain.InteractionNetworkGroup;
import org.genemania.domain.Organism;

/**
 * Used only to return the correct property names as JSON through CyREST.
 * Should be removed when org.genemania.domain.Organism is fixed.
 */
public class OrganismDto implements Serializable {

	private static final long serialVersionUID = -350812939432677533L;
	
	private long taxonomyId;
	private String scientificName;
	private String abbreviatedName;
	private String commonName;
	private Collection<InteractionNetworkGroupDto> interactionNetworkGroups = new ArrayList<>();

	public OrganismDto() {
	}

	public OrganismDto(Organism org) {
		taxonomyId = org.getTaxonomyId();
		scientificName = org.getAlias();
		abbreviatedName = org.getName();
		commonName = org.getDescription();
		
		if (org.getInteractionNetworkGroups() != null) {
			Collection<InteractionNetworkGroupDto> groups = new ArrayList<>();
			
			for (InteractionNetworkGroup ing : org.getInteractionNetworkGroups())
				groups.add(new InteractionNetworkGroupDto(ing));
			
			setInteractionNetworkGroups(groups);
		}
	}

	public long getTaxonomyId() {
		return taxonomyId;
	}

	public void setTaxonomyId(long taxonomyId) {
		this.taxonomyId = taxonomyId;
	}

	public String getScientificName() {
		return scientificName;
	}

	public void setScientificName(String scientificName) {
		this.scientificName = scientificName;
	}

	public String getAbbreviatedName() {
		return abbreviatedName;
	}

	public void setAbbreviatedName(String abbreviatedName) {
		this.abbreviatedName = abbreviatedName;
	}

	public String getCommonName() {
		return commonName;
	}

	public void setCommonName(String commonName) {
		this.commonName = commonName;
	}

	public Collection<InteractionNetworkGroupDto> getInteractionNetworkGroups() {
		return new ArrayList<>(interactionNetworkGroups);
	}

	public void setInteractionNetworkGroups(Collection<InteractionNetworkGroupDto> interactionNetworkGroups) {
		this.interactionNetworkGroups.clear();
		
		if (interactionNetworkGroups != null)
			this.interactionNetworkGroups.addAll(interactionNetworkGroups);
	}

	@Override
	public String toString() {
		return scientificName;
	}
}
