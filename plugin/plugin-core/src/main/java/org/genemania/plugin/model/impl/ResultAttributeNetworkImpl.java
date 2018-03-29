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

import org.genemania.domain.Attribute;

public class ResultAttributeNetworkImpl extends AbstractNetwork<Attribute> implements Serializable {

	private static final long serialVersionUID = -5955219402264581919L;
	
	public ResultAttributeNetworkImpl() {
	}
	
	public ResultAttributeNetworkImpl(Attribute model, double weight) {
		super(model, weight);
	}

	@Override
	public String getName() {
		return model.getName();
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
		if (!type.equals(Attribute.class))
			return null;
		
		return (T) model;
	}
	
	@Override
	public int hashCode() {
		final int prime = 13;
		int result = 3;
		result = prime * result + ((model == null) ? 0 : (int) (model.getId() ^ (model.getId() >>> 32)));
		
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof ResultAttributeNetworkImpl))
			return false;
		
		ResultAttributeNetworkImpl other = (ResultAttributeNetworkImpl) obj;
		
		if (model == null || other.model == null)
			return false;
		
		return model.getId() == other.model.getId();
	}
}
