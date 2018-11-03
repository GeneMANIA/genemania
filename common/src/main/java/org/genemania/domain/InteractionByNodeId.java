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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.genemania.domain.Interaction;

public class InteractionByNodeId implements java.io.Serializable {

    private static final long serialVersionUID = -5035223995196204217L;

    private long              id;
    private long              fromNodeId;
    private long              toNodeId;
    private float             weight;
    private String            label;
    
    public InteractionByNodeId() {
    	
    }
    
    public InteractionByNodeId(Node fromNode, Node toNode, float weight, String label) {
    	this.fromNodeId = fromNode.getId();
        this.toNodeId = toNode.getId();
        this.weight = weight;
        this.label = label;
    }
    
    public InteractionByNodeId(long fromNodeId, long toNodeId, float weight, String label) {
    	this.fromNodeId = fromNodeId;
        this.toNodeId = toNodeId;
        this.weight = weight;
        this.label = label;
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }
    
    public long getFromNodeId() {
        return this.fromNodeId;
    }
    
    public void setFromNodeId(Node fromNode) {
    	this.fromNodeId = fromNode.getId();
    }

    public void setFromNodeId(long fromNodeId) {
        this.fromNodeId = fromNodeId;
    }
    
//    @Override
    public void setFromNode(Node fromNode) {
    	this.fromNodeId = fromNode.getId();
    }

    
    public long getToNodeId() {
        return this.toNodeId;
    }
    
//    @Override
    public void setToNode(Node toNode) {
    	this.toNodeId = toNode.getId();
    }
    
    public void setToNodeId(Node toNode) {
    	this.toNodeId = toNode.getId();
    }

    public void setToNodeId(long toNodeId) {
        this.toNodeId = toNodeId;
    }

    public float getWeight() {
        return this.weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public String getLabel() {
        return this.label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 19).appendSuper(super.hashCode()).append(getId()).toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof InteractionByNodeId)) return false;
        InteractionByNodeId o = (InteractionByNodeId) obj;
        return new EqualsBuilder().append(getId(), o.getId()).isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("id", getId()).append("fromNode",
                getFromNodeId()).append("toNode", getToNodeId()).append("label", getLabel()).toString();
    }


}
