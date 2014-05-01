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

import org.genemania.domain.InteractionNetwork;

public class InteractionNetworkImpl extends AbstractNetwork<InteractionNetwork> {

	private InteractionNetwork network;

	public InteractionNetworkImpl(InteractionNetwork network, double weight) {
		super(weight);
		this.network = network;
	}
	
	@Override
	public InteractionNetwork getModel() {
		return network;
	}

	@Override
	public String getName() {
		return network.getName();
	}
	
	@Override
	public boolean isDefaultSelected() {
		return network.isDefaultSelected();
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof InteractionNetworkImpl)) {
			return false;
		}
		return ((InteractionNetworkImpl) other).getModel().getId() == network.getId();
	}
	
	@Override
	public int hashCode() {
		return (int) (network.getId() % Integer.MAX_VALUE);
	}
	
	@Override
	public boolean hasInteractions() {
		return network.getInteractions().size() > 0;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T adapt(Class<T> type) {
		if (!type.equals(InteractionNetwork.class)) {
			return null;
		}
		return (T) network;
	}
}
