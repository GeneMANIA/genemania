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
package org.genemania.plugin.data.compatibility;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.genemania.engine.cache.FileSerializedObjectCache;

public class AdapterStrategyObjectCache extends FileSerializedObjectCache {
	List<AdapterStrategy> strategies;
	private boolean zipEnabled;
	
	public AdapterStrategyObjectCache(String cacheDir) {
		this(cacheDir, false);
	}
	
	public AdapterStrategyObjectCache(String cacheDir, boolean zipEnabled) {
		super(cacheDir, zipEnabled);
		this.zipEnabled = zipEnabled;
		strategies = new ArrayList<AdapterStrategy>();
	}
	
	public void addStrategy(AdapterStrategy strategy) {
		strategies.add(strategy);
	}
	
	@Override
	protected Object deserialize(String filename) throws IOException, ClassNotFoundException {
		try {
			// First, try deserializing the usual way.
			return new ObjectInputStream(createInputStream(filename)).readObject();
		} catch (InvalidClassException e) {
			// If that doesn't work, try our bag of tricks.
			return readObject(filename);
		}
	}
	
	private Object readObject(String path) throws IOException {
		for (AdapterStrategy strategy : strategies) {
			try {
				return new AdaptingObjectInputStream(createInputStream(path), strategy).readObject();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (InvalidClassException e) {
				e.printStackTrace();
			}
		}
		throw new IOException("Unable to deserialize " + path); //$NON-NLS-1$
	}
	
	private InputStream createInputStream(String path) throws IOException {
		InputStream stream = new BufferedInputStream(new FileInputStream(path));
		if (zipEnabled) {
			stream = new GZIPInputStream(stream);
		}
		return stream;
	}
}
