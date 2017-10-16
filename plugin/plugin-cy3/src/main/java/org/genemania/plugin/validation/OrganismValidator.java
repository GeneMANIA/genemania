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
package org.genemania.plugin.validation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.genemania.domain.Organism;

public class OrganismValidator {

	private final Set<String> existingNames;
	private final Set<String> existingAliases;
	private final Set<Long> existingTaxIds;
	
	private Organism currentOrganism;

	public OrganismValidator() {
		existingNames = new HashSet<String>();
		existingAliases = new HashSet<String>();
		existingTaxIds = new HashSet<Long>();
	}
	
	public void setOrganisms(List<Organism> organisms) {
		existingNames.clear();
		existingAliases.clear();
		existingTaxIds.clear();
		
		for (Organism organism : organisms) {
			existingNames.add(organism.getName());
			existingAliases.add(organism.getAlias());
			existingTaxIds.add(organism.getTaxonomyId());
		}
	}
	
	public void setCurrentOrganism(Organism organism) {
		currentOrganism = organism;
	}
	
	public boolean isValidName(String name) {
		return isValidId(name, existingNames) || (name != null && currentOrganism != null && name.equals(currentOrganism.getName()));
	}
	
	public boolean isValidAlias(String alias) {
		return isValidId(alias, existingAliases) || (alias != null && currentOrganism != null && alias.equals(currentOrganism.getAlias()));
	}
	
	public boolean isValidTaxonomyId(String text) {
		if (text == null || text.isEmpty()) {
			return true;
		}
		try {
			long taxId = Long.parseLong(text);
			return !existingTaxIds.contains(taxId) || (currentOrganism != null && taxId == currentOrganism.getTaxonomyId());
		} catch (NumberFormatException e) {
			return false;
		}
	}

	private boolean isValidId(String element, Set<String> set) {
		if (element == null || element.isEmpty()) {
			return false;
		}
		return !set.contains(element);
	}
}
