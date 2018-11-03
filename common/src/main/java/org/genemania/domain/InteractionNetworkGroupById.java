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
import org.genemania.domain.InteractionByNodeId;
import org.genemania.domain.InteractionNetworkById;

/**
 * InteractionNetworkGroup
 */
public class InteractionNetworkGroupById implements java.io.Serializable {

    private static final long              serialVersionUID    = 2080552201580597913L;

    private long                           id;
    private String                         name;
    private String                         code;
    private String                         description;
    private Collection<InteractionNetworkById> interactionNetworks = new ArrayList<InteractionNetworkById>();

    public InteractionNetworkGroupById() {
    }
    
    public InteractionNetworkGroupById(InteractionNetworkGroup interactionNetworkGroup) {
    	this.id = interactionNetworkGroup.getId();
    	this.name = interactionNetworkGroup.getName();
    	this.code = interactionNetworkGroup.getCode();
    	this.description = interactionNetworkGroup.getDescription();
    	
    	Collection<InteractionNetworkById> newInteractionNetworks = new ArrayList<InteractionNetworkById>();
    	for (InteractionNetwork oldInteractionNet : interactionNetworkGroup.getInteractionNetworks()) {
    		InteractionNetworkById newInteractionNet = new InteractionNetworkById(oldInteractionNet);
    		newInteractionNetworks.add(newInteractionNet);
//    		newInteractionNet.setId(oldInteractionNet.getId());
		}
    	this.interactionNetworks = newInteractionNetworks;
    }

    public InteractionNetworkGroupById(String name,
                                   String code,
                                   String description,
                                   Collection<InteractionNetworkById> interactionNetworks) {
        this.name = name;
        this.code = code;
        this.description = description;
        this.interactionNetworks = interactionNetworks;
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

    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Collection<InteractionNetworkById> getInteractionNetworks() {
        return this.interactionNetworks;
    }
    
    public void setInteractionNetworks(Collection<InteractionNetwork> interactionNetworks) {
    	Collection<InteractionNetworkById> byIdNetworks = new ArrayList<>();
    	for (InteractionNetwork oldNetwork : interactionNetworks) {
    		byIdNetworks.add(new InteractionNetworkById(oldNetwork));
    	}
    	this.interactionNetworks = byIdNetworks;
    }

//    public void setInteractionNetworks(Collection<InteractionNetworkById> interactionNetworks) {
////    	Collection<InteractionNetworkById> byIdNetworks = new ArrayList<>();
////    	for (InteractionNetwork oldNetwork : interactionNetworks) {
////    		byIdNetworks.add(new InteractionNetworkById(oldNetwork));
////		}
//        this.interactionNetworks = interactionNetworks;
//    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(7, 19).appendSuper(super.hashCode()).append(getId()).toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof InteractionNetworkGroupById)) return false;
        InteractionNetworkGroupById o = (InteractionNetworkGroupById) obj;
        return new EqualsBuilder().append(getId(), o.getId()).isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("id", getId()).append("code", getCode())
                .append("name", getName()).toString();
    }

}
