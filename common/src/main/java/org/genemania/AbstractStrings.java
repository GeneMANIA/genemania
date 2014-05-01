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

package org.genemania;

import java.lang.reflect.Field;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public abstract class AbstractStrings {
	protected static ResourceBundle load(Class<?> c) {
		ResourceBundle bundle = PropertyResourceBundle.getBundle(c.getName());
		Enumeration<String> keys = bundle.getKeys();
		
		HashSet<String> sourceKeys = new HashSet<String>();
		
		while (keys.hasMoreElements()) {
			String key = keys.nextElement();
			sourceKeys.add(key);
		}
		
		for (Field field : c.getFields()) {
			String name = field.getName();
			if (!sourceKeys.remove(name)) {
				throw new RuntimeException(String.format("Resources for '%s' is missing key '%s'", c.getName(), name)); //$NON-NLS-1$
			}
			try {
				field.set(null, bundle.getString(name));
			} catch (IllegalArgumentException e) {
				throw new RuntimeException(e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
		return bundle;
	}
}
