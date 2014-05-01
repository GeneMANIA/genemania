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
 * Ontology
 */
public class Ontology implements java.io.Serializable {

    private static final long            serialVersionUID = -5020793692980460231L;

    private long                         id;
    private String                       name;
    private Collection<OntologyCategory> categories       = new ArrayList<OntologyCategory>(0);

    public Ontology() {
    }

    public Ontology(String name) {
        this.name = name;
    }

    public Ontology(String name, Collection<OntologyCategory> categories) {
        this.name = name;
        this.categories = categories;
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

    public Collection<OntologyCategory> getCategories() {
        return this.categories;
    }

    public void setCategories(Collection<OntologyCategory> categories) {
        this.categories = categories;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(7, 11).appendSuper(super.hashCode()).append(getId()).toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Ontology)) return false;
        Ontology o = (Ontology) obj;
        return new EqualsBuilder().append(getId(), o.getId()).isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("id", getId()).append("name", getName())
                .toString();
    }

}
