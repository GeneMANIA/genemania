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
import no.uib.cipr.matrix.Matrix;

/**
 * softref "magic"
 *
 * simple cache maintaining soft references to Matrix objects,
 * which can get evicted by the VM when under memory pressure.
 *
 * Returns null if requested network could not be found in queue.
 *
 * note that the underlying map can accumulate cleared softreference,
 * calling the compact() method periodically will tidy this up. An
 * alternative implementation could use RefereneQueues to drive the
 * cleanup process instead, though this seems simple enough for now
 * 
 */
public class NetworkMemCache {

    RefMap softCache = new RefMap();
    static NetworkMemCache instance = new NetworkMemCache();
    public static final String CORE = "core"; // core namespace

    NetworkMemCache() {}
    
    public static NetworkMemCache instance() {
        return instance;
    }

    public Matrix get(int organismId, int networkId) {
        return get(null, organismId, networkId);
    }

    public Matrix get(String namespace, int organismId, int networkId) {
        String key = makeKey(namespace, organismId, networkId);

        SoftReference<Matrix> ref = softCache.get(key);

        if (ref == null) {
            softCache.remove(key);
            return null;
        }

        Matrix m = ref.get();
        if (m == null) {
            softCache.remove(key);
            return null;
        }

        return m;
    }

    public void put(int organismId, int networkId, Matrix network) {
        put(null, organismId, networkId, network);
    }

    public void put(String namespace, int organismId, int networkId, Matrix network) {
        String key = makeKey(namespace, organismId, networkId);

        SoftReference<Matrix> ref = new SoftReference<Matrix>(network);
        softCache.put(key, ref);
    }

    public void clear() {
        softCache.clear();
    }

    public void compact() {
        for (String key: softCache.keySet()) {
            SoftReference<Matrix> ref = softCache.get(key);
            if (ref == null) {
                softCache.remove(key);
            }
            else if (ref.get() == null) {
                softCache.remove(key);
            }
        }
    }

    private String makeKey(String namespace, int organismId, int networkId) {
        if (namespace == null) {
            return String.format("%s.%d.%d", CORE, organismId, networkId);
        }
        else {
            return String.format("%s.%d.%d", namespace, organismId, networkId);
        }
    }

    protected class RefMap extends HashMap<String, SoftReference<Matrix>> {}
}
