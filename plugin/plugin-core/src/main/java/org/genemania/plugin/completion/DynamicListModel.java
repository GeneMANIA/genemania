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

package org.genemania.plugin.completion;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractListModel;

public class DynamicListModel<T extends Comparable<T>> extends AbstractListModel {
	private static final long serialVersionUID = 1L;

	private final List<T> items;

	public DynamicListModel() {
		items = new ArrayList<T>();
	}
	
	public DynamicListModel(List<T> items) {
		this();
		setItems(items);
	}
	
	public Object getElementAt(int index) {
		return items.get(index);
	}

	public int getSize() {
		return items.size();
	}
	
	public void add(T item) {
		items.add(item);
		int index = items.size() - 1;
		fireIntervalAdded(this, index, index);
	}
	
	public void remove(int index) {
		items.remove(index);
		fireIntervalRemoved(this, index, index);
	}
	
	public void remove(int[] indices) {
		// Remove items in reverse order so that indices don't shift as we
		// delete.
		Arrays.sort(indices);
		for (int i = indices.length - 1; i >= 0; i--) {
			remove(indices[i]);
		}
	}

	public void setItems(List<T> items) {
		clear();
		this.items.addAll(items);
		fireIntervalAdded(this, 0, items.size() - 1);
	}
	
	public void clear() {
		if (items.size() == 0) {
			return;
		}
		int index = items.size() - 1;
		items.clear();
		fireIntervalRemoved(this, 0, index);
	}
	
	public void sort() {
		if (items.size() == 0) {
			return;
		}
		Collections.sort(items);
		fireContentsChanged(this, 0, items.size() - 1);
	}

	public List<T> getItems() {
		return Collections.unmodifiableList(items);
	}
	
	public boolean contains(T item) {
		return items.contains(item);
	}
}
