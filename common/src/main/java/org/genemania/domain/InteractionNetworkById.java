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
import org.genemania.domain.InteractionNetwork;
import org.genemania.domain.InteractionByNodeId;

/**
 * InteractionNetwork
 */
public class InteractionNetworkById implements java.io.Serializable {

    private static final long       serialVersionUID = 9077993686081221864L;

    private long                    id;
    private String                  name;
    private NetworkMetadata         metadata;
    private String                  description;
    private boolean                 defaultSelected;
    private Collection<InteractionByNodeId> interactions     = new ArrayList<InteractionByNodeId>(0);
    private Collection<Tag>         tags             = new ArrayList<Tag>(0);
    
    public InteractionNetworkById() {
    }

    public InteractionNetworkById(InteractionNetwork interactionNetwork) {
    	this.id = interactionNetwork.getId();
    	this.name = interactionNetwork.getName();
    	this.metadata = interactionNetwork.getMetadata();
    	this.description = interactionNetwork.getDescription();
		this.defaultSelected = interactionNetwork.isDefaultSelected();
		this.tags = interactionNetwork.getTags();
		this.interactions = this.convertInteractions(interactionNetwork.getInteractions());
    }

//    public InteractionNetworkById(String name,
//                              NetworkMetadata metadata,
//                              String description,
//                              boolean defaultSelected,
//                              Collection<InteractionByNodeId> interactions,
//                              Collection<Tag> tags) {
//        this.name = name;
//        this.metadata = metadata;
//        this.description = description;
//        this.defaultSelected = defaultSelected;
//        this.interactions = interactions;
//        this.tags = tags;
//    }
    
    private Collection<InteractionByNodeId> convertInteractions(Collection<Interaction> oldInteractions){
    	Collection<InteractionByNodeId> newInteractions = new ArrayList<InteractionByNodeId>();
    	for (Interaction oldInteraction : oldInteractions) {
    		InteractionByNodeId newInteraction = new InteractionByNodeId(
    				oldInteraction.getFromNode(),
    				oldInteraction.getToNode(),
    				oldInteraction.getWeight(),
    				oldInteraction.getLabel());
    		newInteraction.setId(oldInteraction.getId());
			newInteractions.add(newInteraction);
		}
    	return newInteractions;
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

    public NetworkMetadata getMetadata() {
        return this.metadata;
    }

    public void setMetadata(NetworkMetadata metadata) {
        this.metadata = metadata;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isDefaultSelected() {
        return this.defaultSelected;
    }

    public void setDefaultSelected(boolean defaultSelected) {
        this.defaultSelected = defaultSelected;
    }

    public Collection<InteractionByNodeId> getInteractions() {
        return this.interactions;
    }

    public void setInteractions(Collection<InteractionByNodeId> interactions) {
        this.interactions = interactions;
    }

    public Collection<Tag> getTags() {
        return this.tags;
    }

    public void setTags(Collection<Tag> tags) {
        this.tags = tags;
    }

//    @Override
    public int hashCode() {
        return new HashCodeBuilder(11, 13).appendSuper(super.hashCode()).append(getId()).toHashCode();
    }

//    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof InteractionNetworkById)) return false;
        InteractionNetworkById o = (InteractionNetworkById) obj;
        return new EqualsBuilder().append(getId(), o.getId()).isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("id", getId()).append("name", getName())
                .toString();
    }

}
