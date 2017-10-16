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

import org.genemania.domain.Attribute;

public class ResultAttributeNetworkImpl extends AbstractNetwork<Attribute> {

	private Attribute network;

	public ResultAttributeNetworkImpl(Attribute attribute, double weight) {
		super(weight);
		network = attribute;
	}

	@Override
	public Attribute getModel() {
		return network;
	}

	@Override
	public String getName() {
		return network.getName();
	}

	@Override
	public boolean isDefaultSelected() {
		return false;
	}

	@Override
	public boolean hasInteractions() {
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T adapt(Class<T> type) {
		if (!type.equals(Attribute.class)) {
			return null;
		}
		return (T) network;
	}

}
