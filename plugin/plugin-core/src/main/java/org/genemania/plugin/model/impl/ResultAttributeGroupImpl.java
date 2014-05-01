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

import java.util.Collection;

import org.genemania.domain.Attribute;
import org.genemania.domain.AttributeGroup;
import org.genemania.plugin.model.Group;
import org.genemania.plugin.model.Network;

public class ResultAttributeGroupImpl extends AbstractGroup<AttributeGroup, Attribute> {

	private AttributeGroup group;

	public ResultAttributeGroupImpl(AttributeGroup group, Collection<Network<Attribute>> networks) {
		super(networks);
		this.group = group;
	}

	@Override
	public AttributeGroup getModel() {
		return group;
	}

	@Override
	public String getName() {
		return group.getName();
	}

	@Override
	public String getCode() {
		return group.getCode();
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof ResultAttributeGroupImpl)) {
			return false;
		}
		return ((ResultAttributeGroupImpl) other).getModel().getId() == group.getId();
	}
	
	@Override
	public int hashCode() {
		return (int) (group.getId() % Integer.MAX_VALUE);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T, S> Group<T, S> adapt(Class<T> groupType, Class<S> networkType) {
		if (!groupType.equals(AttributeGroup.class)) {
			return null;
		}
		if (!networkType.equals(Attribute.class)) {
			return null;
		}
		return (Group<T, S>) this;
	}
	
	@Override
	protected Group<AttributeGroup, Attribute> create(Collection<Network<Attribute>> networks) {
		return new ResultAttributeGroupImpl(group, networks);
	}
}
