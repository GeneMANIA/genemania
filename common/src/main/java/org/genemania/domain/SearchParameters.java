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

package org.genemania.domain;

import java.util.Collection;
import java.util.LinkedList;

import org.genemania.type.CombiningMethod;

public class SearchParameters {
	private Organism organism;
	private CombiningMethod weighting;
	private int resultsSize;
	private int attributeResultsSize;
	private String namespace;
	private Collection<InteractionNetwork> networks = new LinkedList<InteractionNetwork>();
	private Collection<Gene> genes = new LinkedList<Gene>();
	private Collection<AttributeGroup> attributeGroups = new LinkedList<AttributeGroup>();

	public SearchParameters() {
	}

	public SearchParameters(Organism organism, Collection<Gene> genes,
			Collection<InteractionNetwork> networks,
			Collection<AttributeGroup> attributeGroups,
			CombiningMethod weighting, int resultsSize, int attrResultsSize, String namespace) {
		super();
		this.organism = organism;
		this.weighting = weighting;
		this.resultsSize = resultsSize;
		this.networks = networks;
		this.attributeGroups = attributeGroups;
		this.genes = genes;
		this.namespace = namespace;
		this.attributeResultsSize = attrResultsSize;
	}

	public int getAttributeResultsSize() {
		return attributeResultsSize;
	}

	public void setAttributeResultsSize(int attributeResultsSize) {
		this.attributeResultsSize = attributeResultsSize;
	}

	public Organism getOrganism() {
		return organism;
	}

	public void setOrganismId(Organism organism) {
		this.organism = organism;
	}

	public CombiningMethod getWeighting() {
		return weighting;
	}

	public void setWeighting(CombiningMethod weighting) {
		this.weighting = weighting;
	}

	public int getResultsSize() {
		return resultsSize;
	}

	public void setResultsSize(int resultsSize) {
		this.resultsSize = resultsSize;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public void setOrganism(Organism organism) {
		this.organism = organism;
	}

	public Collection<InteractionNetwork> getNetworks() {
		return networks;
	}

	public void setNetworks(Collection<InteractionNetwork> networks) {
		this.networks = networks;
	}

	public Collection<Gene> getGenes() {
		return genes;
	}

	public void setGenes(Collection<Gene> genes) {
		this.genes = genes;
	}

	public Collection<AttributeGroup> getAttributeGroups() {
		return attributeGroups;
	}

	public void setAttributeGroups(Collection<AttributeGroup> attributeGroups) {
		this.attributeGroups = attributeGroups;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((attributeGroups == null) ? 0 : attributeGroups.hashCode());
		result = prime * result + attributeResultsSize;
		result = prime * result + ((genes == null) ? 0 : genes.hashCode());
		result = prime * result
				+ ((namespace == null) ? 0 : namespace.hashCode());
		result = prime * result
				+ ((networks == null) ? 0 : networks.hashCode());
		result = prime * result
				+ ((organism == null) ? 0 : organism.hashCode());
		result = prime * result + resultsSize;
		result = prime * result
				+ ((weighting == null) ? 0 : weighting.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SearchParameters other = (SearchParameters) obj;
		if (attributeGroups == null) {
			if (other.attributeGroups != null)
				return false;
		} else if (!attributeGroups.equals(other.attributeGroups))
			return false;
		if (attributeResultsSize != other.attributeResultsSize)
			return false;
		if (genes == null) {
			if (other.genes != null)
				return false;
		} else if (!genes.equals(other.genes))
			return false;
		if (namespace == null) {
			if (other.namespace != null)
				return false;
		} else if (!namespace.equals(other.namespace))
			return false;
		if (networks == null) {
			if (other.networks != null)
				return false;
		} else if (!networks.equals(other.networks))
			return false;
		if (organism == null) {
			if (other.organism != null)
				return false;
		} else if (!organism.equals(other.organism))
			return false;
		if (resultsSize != other.resultsSize)
			return false;
		if (weighting != other.weighting)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "SearchParameters [organism=" + organism + ", weighting="
				+ weighting + ", resultsSize=" + resultsSize
				+ ", attributeResultsSize=" + attributeResultsSize
				+ ", namespace=" + namespace + ", networks=" + networks
				+ ", genes=" + genes + ", attributeGroups=" + attributeGroups
				+ "]";
	}

}
