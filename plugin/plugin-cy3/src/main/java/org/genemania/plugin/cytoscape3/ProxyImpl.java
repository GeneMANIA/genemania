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
package org.genemania.plugin.cytoscape3;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.List;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.genemania.plugin.proxies.Proxy;

@Deprecated
public abstract class ProxyImpl<T extends CyIdentifiable> implements Proxy<T> {
	
	private Reference<T> reference;
	private Reference<CyNetwork> networkReference;
	
	public ProxyImpl(T proxied, CyNetwork network) {
		reference = new WeakReference<T>(proxied);
		networkReference = new WeakReference<CyNetwork>(network);
	}
	
	@Override
	public T getProxied() {
		return reference.get();
	}
	
	protected CyNetwork getNetwork() {
		return networkReference.get();
	}
	
	@Override
	public String getIdentifier() {
		return getNetwork().getRow(getProxied()).get(CyNetwork.NAME, String.class);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <U> U getAttribute(String name, Class<U> type) {
		CyRow row = getNetwork().getRow(getProxied());
		if (type.equals(List.class)) {
			CyTable table = row.getTable();
			CyColumn column = table.getColumn(name);
			if (column == null) {
				return null;
			}
			Class<?> elementType = column.getListElementType();
			return (U) row.getList(name, elementType);
		}
		if (type.equals(Object.class)) {
			CyTable table = row.getTable();
			CyColumn column = table.getColumn(name);
			type = (Class<U>) column.getType();
		}
		return row.get(name, type);
	}
	
	@Override
	public <U> void setAttribute(String name, U value) {
		CyRow row = getNetwork().getRow(getProxied());
		CyTable table = row.getTable();
		CyColumn column = table.getColumn(name);
		if (column == null) {
			if (value instanceof List<?>) {
				List<?> list = (List<?>) value;
				Class<?> elementType;
				if (list.size() == 0) {
					elementType = String.class;
				} else {
					elementType = list.get(0).getClass();
				}
				table.createListColumn(name, elementType, false);
			} else {
				table.createColumn(name, value.getClass(), false);
			}
		}
		row.set(name, value);
	};
	
	@Override
	public Class<?> getAttributeType(String name) {
		return getNetwork().getRow(getProxied()).getTable().getColumn(name).getType();
	}
}
