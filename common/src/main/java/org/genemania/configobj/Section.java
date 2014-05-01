/**
 * This file is part of GeneMANIA.
 * Copyright (C) 2010 University of Toronto.
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

package org.genemania.configobj;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Section {
	private List<Section> children;
	private int level;
	private String name;
	private ArrayList<Entry<String, String>> entries;
	
	public Section(int level, String name) {
		this.level = level;
		this.name = name;
		children = new ArrayList<Section>();
		entries = new ArrayList<Entry<String, String>>();
	}
	
	public List<Section> getChildren() {
		return Collections.unmodifiableList(children);
	}
	
	public Section getSection(String name) {
		for (Section section : children) {
			if (section.name.equals(name)) {
				return section;
			}
		}
		return null;
	}
	
	public String getName() {
		return name;
	}

	public void addSection(Section section) {
		children.add(section);
	}
	
	public void addEntry(String key, String value) {
		entries.add(new GenericEntry<String, String>(key, value));
	}

	public int getLevel() {
		return level;
	}
	
	public List<? extends Map.Entry<String, String>> getEntries() {
		return Collections.unmodifiableList(entries);
	}
	
	@Override
	public String toString() {
		return String.format("(%s, %d)", name, level);
	}
	
	static class GenericEntry <T, U> implements Entry<T, U> {
		private T key;
		private U value;

		public GenericEntry(T key, U value) {
			this.key = key;
			this.value = value;
		}
		
		public T getKey() {
			return key;
		}

		public U getValue() {
			return value;
		}

		public U setValue(U value) {
			U old = this.value;
			this.value = value;
			return old;
		}
		
		@Override
		public String toString() {
			return String.format("%s=%s", key, value);
		}
	}

	public String getEntry(String key) {
		for (Entry<String, String> entry : entries) {
			if (key.equals(entry.getKey())) {
				return entry.getValue();
			}
		}
		return null;
	}
	
	public List<String> getEntries(String key) {
		List<String> result = new ArrayList<String>();
		for (Entry<String, String> entry : entries) {
			if (key.equals(entry.getKey())) {
				result.add(entry.getValue());
			}
		}
		return result;
	}
}