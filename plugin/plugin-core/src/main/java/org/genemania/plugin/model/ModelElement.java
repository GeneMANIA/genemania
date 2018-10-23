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
package org.genemania.plugin.model;

import java.util.Comparator;

import org.genemania.plugin.formatters.IObjectFormatter;

public class ModelElement<T> implements Comparable<ModelElement<T>> {
	
	private final T item;
	private final Comparator<T> comparator;
	private final IObjectFormatter<T> formatter;
	
	public ModelElement(T item, Comparator<T> comparator, IObjectFormatter<T> formatter) {
		this.item = item;
		this.comparator = comparator;
		this.formatter = formatter;
	}

	public T getItem() {
		return item;
	}
	
	@Override
	public String toString() {
		return formatter == null ? item.toString() : formatter.format(item);
	}

	@Override
	public int compareTo(ModelElement<T> o) {
		return comparator.compare(item, o.getItem());
	}
}
