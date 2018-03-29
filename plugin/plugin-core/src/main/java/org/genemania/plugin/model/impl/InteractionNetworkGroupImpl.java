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
package org.genemania.plugin.model.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import org.genemania.domain.InteractionNetwork;
import org.genemania.domain.InteractionNetworkGroup;
import org.genemania.plugin.model.Group;
import org.genemania.plugin.model.Network;

public class InteractionNetworkGroupImpl extends AbstractGroup<InteractionNetworkGroup, InteractionNetwork>
		implements Serializable {
	
	private static final long serialVersionUID = -4047182091791765891L;
	
	private InteractionNetworkGroup model;
	
	public InteractionNetworkGroupImpl() {
	}
	
	public InteractionNetworkGroupImpl(InteractionNetworkGroup model) {
		super(extractNetworks(model));
		this.model = model;
	}
	
	private static Collection<Network<InteractionNetwork>> extractNetworks(InteractionNetworkGroup group) {
		Collection<Network<InteractionNetwork>> result = new ArrayList<>();
		Collection<InteractionNetwork> interactionNetworks = group.getInteractionNetworks();
		
		if (interactionNetworks != null) {
			for (InteractionNetwork network : interactionNetworks)
				result.add(new InteractionNetworkImpl(network, 0));
		}
		
		return result;
	}

	public InteractionNetworkGroupImpl(InteractionNetworkGroup group, Collection<Network<InteractionNetwork>> networks) {
		super(networks);
		this.model = group;
	}

	@Override
	public InteractionNetworkGroup getModel() {
		return model;
	}
	
	@Override
	public String getName() {
		return model.getName();
	}
	
	@Override
	public String getCode() {
		return model.getCode();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T, S> Group<T, S> adapt(Class<T> groupType, Class<S> networkType) {
		if (!groupType.equals(InteractionNetworkGroup.class))
			return null;
		
		if (!networkType.equals(InteractionNetwork.class))
			return null;
		
		return (Group<T, S>) this;
	}
	
	@Override
	protected Group<InteractionNetworkGroup, InteractionNetwork> create(Collection<Network<InteractionNetwork>> networks) {
		return new InteractionNetworkGroupImpl(model, networks);
	}
	
	@Override
	public int hashCode() {
		final int prime = 3;
		int result = 5;
		result = prime * result + ((model == null) ? 0 : (int) (model.getId() ^ (model.getId() >>> 32)));
		
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof InteractionNetworkGroupImpl))
			return false;
		
		InteractionNetworkGroupImpl other = (InteractionNetworkGroupImpl) obj;
		
		if (model == null || other.model == null)
			return false;
		
		return model.getId() == other.model.getId();
	}
}
