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

import java.lang.ref.SoftReference;
import java.util.HashMap;

/**
 * simple, generic softreference based object cache
 *
 * returns null for requests that aren't cache hits
 */
public class SoftRefObjectCache {

    RefMap softCache = new RefMap();
    static SoftRefObjectCache instance = new SoftRefObjectCache();

    SoftRefObjectCache() {
    }

    public static SoftRefObjectCache instance() {
        return instance;
    }

    public Object get(String key) {
        SoftReference<Object> ref = softCache.get(key);

        if (ref == null) {
            softCache.remove(key);
            return null;
        }

        Object object = ref.get();
        if (object == null) {
            softCache.remove(key);
            return null;
        }

        return object;
    }

    public void put(String key, Object object) {
        SoftReference<Object> ref = new SoftReference<Object>(object);
        softCache.put(key, ref);
    }

    public void remove(String key) {
        softCache.remove(key);
    }
    
    public void clear() {
        softCache.clear();
    }

    public void compact() {
        for (String key: softCache.keySet()) {
            SoftReference<Object> ref = softCache.get(key);
            if (ref == null) {
                softCache.remove(key);
            }
            else if (ref.get() == null) {
                softCache.remove(key);
            }
        }
    }

    protected class RefMap extends HashMap<String, SoftReference<Object>> {
    }
}
