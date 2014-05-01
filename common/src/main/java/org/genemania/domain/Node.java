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
 * Node
 */
public class Node implements java.io.Serializable {

    private static final long serialVersionUID = 1381891291857803933L;

    private long              id;
    private String            name;
    private Collection<Gene>  genes            = new ArrayList<Gene>(0);
    private GeneData          geneData;

    public Node() {
    }

    public Node(String name, Collection<Gene> genes, GeneData geneData) {
        this.name = name;
        this.genes = genes;
        this.geneData = geneData;
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

    public Collection<Gene> getGenes() {
        return this.genes;
    }

    public void setGenes(Collection<Gene> genes) {
        this.genes = genes;
    }

    public GeneData getGeneData() {
        return this.geneData;
    }

    public void setGeneData(GeneData geneData) {
        this.geneData = geneData;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(7, 13).appendSuper(super.hashCode()).append(getId()).toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Node)) return false;
        Node o = (Node) obj;
        return new EqualsBuilder().append(getId(), o.getId()).isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("id", getId()).append("name", getName())
                .toString();
    }
    
}
