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

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Organism
 */
public class Organism implements java.io.Serializable {

    private static final long                   serialVersionUID         = -2062779451690027505L;

    private long                                id;
    private String                              name;
    private String                              description;
    private Collection<InteractionNetworkGroup> interactionNetworkGroups = new ArrayList<InteractionNetworkGroup>(0);
    private String                              alias;
    private Ontology                            ontology;
    private long                                taxonomyId;
    private Collection<Gene> 					defaultGenes;

    public Organism() {
    }

    public Organism(String name) {
        this.name = name;
    }

    public Organism(String name,
                    String description,
                    Collection<InteractionNetworkGroup> interactionNetworkGroups,
                    String alias,
                    Ontology ontology,
                    long taxonomyId) {
        this.name = name;
        this.description = description;
        this.interactionNetworkGroups = interactionNetworkGroups;
        this.alias = alias;
        this.ontology = ontology;
        this.taxonomyId = taxonomyId;
    }

	public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Collection<InteractionNetworkGroup> getInteractionNetworkGroups() {
        return this.interactionNetworkGroups;
    }

    public void setInteractionNetworkGroups(Collection<InteractionNetworkGroup> interactionNetworkGroups) {
        this.interactionNetworkGroups = interactionNetworkGroups;
    }

    public String getAlias() {
        return this.alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public Ontology getOntology() {
        return this.ontology;
    }

    public void setOntology(Ontology ontology) {
        this.ontology = ontology;
    }

    public long getTaxonomyId() {
        return this.taxonomyId;
    }

    public void setTaxonomyId(long taxonomyId) {
        this.taxonomyId = taxonomyId;
    }
    
    public Collection<Gene> getDefaultGenes() {
		return defaultGenes;
	}

	public void setDefaultGenes(Collection<Gene> defaultGenes) {
		this.defaultGenes = defaultGenes;
	}

    @Override
    public int hashCode() {
        return new HashCodeBuilder(3, 11).appendSuper(super.hashCode()).append(getId()).toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Organism)) return false;
        Organism o = (Organism) obj;
        return new EqualsBuilder().append(getId(), o.getId()).isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("id", getId()).append("name", getName())
                .toString();
    }
    
}
