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

import java.util.ArrayList;
import java.util.Collection;

import org.genemania.plugin.model.Group;
import org.genemania.plugin.model.Network;

public abstract class AbstractGroup<G, N> implements Group<G, N> {
	
	private Collection<Network<N>> networks;
	private boolean hasInteractions;

	protected AbstractGroup() {
		networks = new ArrayList<>();
	}
	
	protected AbstractGroup(Collection<Network<N>> networks) {
		this.networks = new ArrayList<>(networks);
		hasInteractions = hasInteractions(networks);
	}
	
	@Override
	public Collection<Network<N>> getNetworks() {
		return new ArrayList<>(networks);
	}
	
	public void setNetworks(Collection<Network<N>> networks) {
		this.networks.clear();
		
		if (networks != null)
			this.networks.addAll(networks);
	}
	
	@Override
	public double getWeight() {
		double weight = 0;
		
		for (Network<N> network : networks)
			weight += network.getWeight();
		
		return weight;
	}
	
	private boolean hasInteractions(Collection<Network<N>> networks) {
		for (Network<N> network : networks) {
			if (network.hasInteractions())
				return true;
		}
		
		return false;
	}

	@Override
	public boolean hasInteractions() {
		return hasInteractions;
	}
	
	@Override
	public Group<G, N> filter(Collection<Network<?>> filter) {
		Collection<Network<N>> filtered = new ArrayList<>();
		
		for (Network<N> network : networks) {
			if (filter.contains(network))
				filtered.add(network);
		}
		
		return create(filtered);
	}
	
	protected abstract Group<G, N> create(Collection<Network<N>> networks);
}
