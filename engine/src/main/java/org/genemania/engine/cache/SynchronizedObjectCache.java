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

package org.genemania.engine.cache;

import java.util.List;

import org.genemania.exception.ApplicationException;

public class SynchronizedObjectCache implements IObjectCache {
	private IObjectCache delegate;

	public SynchronizedObjectCache(IObjectCache delegate) {
		this.delegate = delegate;
	}
	
	public synchronized Object get(String[] key, boolean isVolatile) throws ApplicationException {
		return delegate.get(key, isVolatile);
	}

	public synchronized String getCacheDir() throws ApplicationException {
		return delegate.getCacheDir();
	}

	public synchronized void put(String[] key, Object value, boolean isVolatile) throws ApplicationException {
		delegate.put(key, value, isVolatile);
	}

	public synchronized void remove(String[] key) throws ApplicationException {
		delegate.remove(key);
	}

	public synchronized boolean exists(String[] key) throws ApplicationException {
		return delegate.exists(key);
	}

	public synchronized List<String[]> list(String[] key) throws ApplicationException {
		return delegate.list(key);
	}
}
