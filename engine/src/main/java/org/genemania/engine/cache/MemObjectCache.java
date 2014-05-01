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

import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;
import org.genemania.exception.ApplicationException;

/**
 * wraps an underlying IObjectCache instance, providing
 * mem caching support
 */
public class MemObjectCache implements IObjectCache {
    private static Logger logger = Logger.getLogger(MemObjectCache.class);

    IObjectCache underlyingCache;
    SoftRefObjectCache memCache = SoftRefObjectCache.instance();
    public static final String CORE = "core"; // core namespace

    public MemObjectCache(IObjectCache underlyingCache) {
        this.underlyingCache = underlyingCache;
    }

    public String getCacheDir() throws ApplicationException {
        return underlyingCache.getCacheDir();
    }

    /*
     * just a little bit of extra decoration to the key prepending
     * the namespace used by the underlying cache
     */
    protected String makeMemCacheKey(String namespace, String key) {
        if (namespace == null) {
            return String.format("%s.%s", CORE, key);
        }
        else {
            return String.format("%s.%s", namespace, key);
        }
    }

    /*
     * don't bother mem-caching volatile objects, they'll have to be reloaded from
     * the backing storage anyway
     */
    public void put(String [] key, Object value, boolean isVolatile) throws ApplicationException {
        if (!isVolatile) {
            memCache.put(makeMemCacheKey(key), value);
        }
        underlyingCache.put(key, value, isVolatile);
    }

    public Object get(String [] key, boolean isVolatile) throws ApplicationException {
        Object object = null;
        String memCacheKey = makeMemCacheKey(key);

        // if volatile, force retrieval from underlying cache. and don't bother
        // putting it in the mem cache on retrieval. in future, we could instead
        // stat the file in the underyling store somehow, or add cache expiry time
        // or something ... (TODO)
        if (isVolatile) {
            logger.debug(String.format("volatile object %s, skipping memory cache", memCacheKey));
            object = underlyingCache.get(key, isVolatile);           
        }
        else {
            object = memCache.get(memCacheKey);

            if (object == null) {
                logger.debug(String.format("memory cache miss for %s", memCacheKey));
                object = underlyingCache.get(key, isVolatile);
                memCache.put(memCacheKey, object);
            }
        }

        return object;
    }

    public void remove(String [] key) throws ApplicationException {
        memCache.remove(makeMemCacheKey(key));
        underlyingCache.remove(key);
    }

    
    public boolean exists(String[] key) throws ApplicationException {
    	return underlyingCache.exists(key);
	}

	public List<String[]> list(String[] key) throws ApplicationException {
		return underlyingCache.list(key);
	}

	/*
     * so we could use the same path gen code as the file cache,
     * but i'm thinking of the key's as a bit more abstract, and how
     * they are used as an implementation detail (eg-multilevel hash's)
     *
     * but for now this looks suspiciously like a file path
     */
    private String makeMemCacheKey(String [] key) throws ApplicationException {
        StringBuilder path = new StringBuilder();

        for (int i=0; i<key.length; i++) {
            path.append(File.separator);
            if (key[i] == null || key[i].equals("")) {
                throw new ApplicationException("missing key part at position " + i);
            }
            path.append(key[i]);
        }

        return path.toString();
    }
}
