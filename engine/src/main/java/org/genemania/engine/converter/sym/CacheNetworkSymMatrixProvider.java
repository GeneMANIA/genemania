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

package org.genemania.engine.converter.sym;

import org.genemania.engine.cache.NetworkMemCache;
import org.genemania.util.ProgressReporter;

import org.genemania.engine.cache.DataCache;
import org.genemania.engine.core.data.Data;
import org.genemania.engine.matricks.SymMatrix;
import org.genemania.exception.ApplicationException;

/**
 * retrieve network from the cache. looks first in mem, then on disk
 */
public class CacheNetworkSymMatrixProvider implements INetworkSymMatrixProvider {

    long organismId;
    String namespace;
    DataCache cache;
    NetworkMemCache memCache = NetworkMemCache.instance();

    public CacheNetworkSymMatrixProvider(long organismId, DataCache cache) {
        this.namespace = Data.CORE;
        this.organismId = organismId;
        this.cache = cache;
    }

    public CacheNetworkSymMatrixProvider(String namespace, long organismId, DataCache cache) {
        this.namespace = namespace;
        this.organismId = organismId;
        this.cache = cache;
    }

    /*
     * just pull out of cache
     */
    public SymMatrix getNetworkMatrix(long networkId, ProgressReporter progress) throws ApplicationException {
        return cache.getNetwork(namespace, organismId, networkId).getData();
    }
}
