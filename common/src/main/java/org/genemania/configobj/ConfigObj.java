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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A partial Java implementation of the Python's ConfigObj class.
 */
public class ConfigObj extends Section {
	static final Pattern SECTION_PATTERN = Pattern.compile("\\s*(\\[+)([^\\]]+)(\\]+).*");

	/**
	 * Creates a new ConfigObj from a <code>Reader</code> containing an INI-
	 * formatted stream.
	 */
	public ConfigObj(Reader input) throws IOException {
		super(0, "");
		
		LinkedList<Section> sections = new LinkedList<Section>();
		BufferedReader reader = new BufferedReader(input);
		String line = stripComments(reader.readLine());
		sections.add(this);
		
		while (line != null) {
			Section current = sections.peek();
			Section section = parseSection(line);
			if (section == null) {
				// Key/value pair
				String[] parts = line.split("=", 2);
				if (parts.length == 2) {
					String key = parts[0].trim();
					if (key.length() > 0) {
						current.addEntry(key, parts[1].trim());
					}
				} else if (parts.length == 1) {
					// Key only -- it happens
					String key = parts[0].trim();
					if (key.length() > 0) {
						current.addEntry(key, "");
					}
				}
			} else {
				// New section
				if (current.getLevel() < section.getLevel()) {
					// Subsection
					sections.addFirst(section);
					current.addSection(section);
				} else if (current.getLevel() == section.getLevel()) {
					// Sibling-section
					sections.removeFirst();
					sections.peek().addSection(section);
					sections.addFirst(section);
				} else {
					// Sibling-section of parent
					while (current.getLevel() > section.getLevel()) {
						current = sections.removeFirst();
					}

					sections.peek().addSection(section);
					sections.addFirst(section);
				}
			}
			line = stripComments(reader.readLine());
		}
	}
	
	private static String stripComments(String text) {
		if (text == null) {
			return null;
		}
		
		int start = text.indexOf('#');
		if (start == 0) {
			return "";
		} else if (start > 0) {
			return text.substring(0, start - 1);
		}
		return text;
	}

	private static Section parseSection(String line) {
		Matcher matcher = SECTION_PATTERN.matcher(line);
		if (matcher.matches()) {
			int level = matcher.group(1).length(); 
			if (level == matcher.group(3).length()) {
				return new Section(level, matcher.group(2)); 
			}
		}
		return null;
	}
}
