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

import org.genemania.domain.AttributeGroup;
import org.genemania.plugin.Strings;
import org.genemania.plugin.model.Group;
import org.genemania.plugin.model.Network;

public class QueryAttributeGroupImpl extends AbstractGroup<Object, AttributeGroup> {

	private Object group = new Object();

	public QueryAttributeGroupImpl(Collection<Network<AttributeGroup>> networks) {
		super(networks);
	}

	@Override
	public Object getModel() {
		return group;
	}

	@Override
	public String getName() {
		return Strings.attributeGroup_label;
	}

	@Override
	public String getCode() {
		return "other"; //$NON-NLS-1$
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T, S> Group<T, S> adapt(Class<T> groupType, Class<S> networkType) {
		if (groupType.equals(Object.class) && networkType.equals(AttributeGroup.class)) {
			return (Group<T, S>) this;
		}
		return null;
	}
	
	@Override
	protected Group<Object, AttributeGroup> create(Collection<Network<AttributeGroup>> networks) {
		return new QueryAttributeGroupImpl(networks);
	}
}
