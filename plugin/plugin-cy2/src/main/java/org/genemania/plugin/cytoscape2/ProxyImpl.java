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
/**
 * 
 */
package org.genemania.plugin.cytoscape2;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.List;

import org.genemania.plugin.proxies.Proxy;

import cytoscape.data.CyAttributes;

abstract class ProxyImpl<T> implements Proxy<T> {
	private final Reference<T> reference;
	
	ProxyImpl(T proxied) {
		reference = new WeakReference<T>(proxied);
	}
	
	abstract protected CyAttributes getAttributes();
	
	@Override
	public <U> U getAttribute(String name, Class<U> type) {
		return CytoscapeUtilsImpl.getAttributeInternal(getAttributes(), (String) getIdentifier(), name, type);
	}

	@Override
	public T getProxied() {
		return reference.get();
	}

	@Override
	public <U> void setAttribute(String name, U value) {
		CytoscapeUtilsImpl.setAttributeInternal(getAttributes(), (String) getIdentifier(), name, value);
	}
	
	@Override
	public Class<?> getAttributeType(String name) {
		switch (getAttributes().getType(name)) {
		case CyAttributes.TYPE_BOOLEAN:
			return Boolean.class;
		case CyAttributes.TYPE_FLOATING:
			return Double.class;
		case CyAttributes.TYPE_INTEGER:
			return Integer.class;
		case CyAttributes.TYPE_SIMPLE_LIST:
			return List.class;
		case CyAttributes.TYPE_STRING:
			return String.class;
		}
		return null;
	}
}