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
import java.util.Comparator;
import java.util.List;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

public abstract class DynamicTableModel<T> implements TableModel {

	protected final List<T> items;
	private final List<TableModelListener> listeners;
	
	public DynamicTableModel() {
		items = new ArrayList<T>();
		listeners = new ArrayList<TableModelListener>();
	}
	
	public void addTableModelListener(TableModelListener l) {
		listeners.add(l);
	}

	public int getRowCount() {
		return items.size();
	}

	public void removeTableModelListener(TableModelListener l) {
		listeners.remove(l);
	}
	
	public T get(int index) {
		return items.get(index);
	}
	
	public void add(T item) {
		items.add(item);
		int index = items.size() - 1;
		fireEvent(new TableModelEvent(this, index, index, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT));
	}
	
	public boolean contains(T item) {
		return items.contains(item);
	}
	
	public List<T> getItems() {
		return Collections.unmodifiableList(items);
	}
	
	public void clear() {
		fireEvent(new TableModelEvent(this, 0, Math.max(0, items.size() - 1), TableModelEvent.ALL_COLUMNS, TableModelEvent.DELETE));
		items.clear();
	}

	public void removeRows(int[] indices) {
		// Remove items in reverse order so that indices don't shift as we
		// delete.
		Arrays.sort(indices);
		for (int i = indices.length - 1; i >= 0; i--) {
			remove(indices[i]);
		}
	}

	public void remove(int index) {
		items.remove(index);
		fireEvent(new TableModelEvent(this, index, index, TableModelEvent.ALL_COLUMNS, TableModelEvent.DELETE));
	}
	
	void fireEvent(TableModelEvent event) {
		for (TableModelListener listener : listeners) {
			listener.tableChanged(event);
		}
	}

	public void sort(Comparator<? super T> comparator) {
		Collections.sort(items, comparator);
		fireEvent(new TableModelEvent(this, 0, Math.max(0, items.size() - 1), TableModelEvent.ALL_COLUMNS, TableModelEvent.UPDATE));
	}
}
