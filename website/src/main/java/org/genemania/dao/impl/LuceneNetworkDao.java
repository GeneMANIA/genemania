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

package org.genemania.dao.impl;

import org.genemania.connector.LuceneConnector;
import org.genemania.dao.NetworkDao;
import org.genemania.domain.InteractionNetwork;
import org.genemania.exception.DataStoreException;

import com.googlecode.ehcache.annotations.Cacheable;
import com.googlecode.ehcache.annotations.KeyGenerator;

public class LuceneNetworkDao implements NetworkDao {

    // __[attributes]__________________________________________________________
    private LuceneConnector connector;

    // __[constructors]________________________________________________________
    public LuceneNetworkDao() {
        connector = LuceneConnector.getInstance();
    }

    // __[interface implementation]____________________________________________
    @Cacheable(cacheName = "networkCache")
    public InteractionNetwork findNetwork(long networkId)
            throws DataStoreException {
        return connector.findNetworkById(networkId);
    }

    // need to use a string as a key, since ehcache doesn't hash two long params
    // correctly (we get collisions for the same networks for different
    // organisms)
    @Cacheable(cacheName = "networkIsValidCache", keyGenerator = @KeyGenerator(name = "StringCacheKeyGenerator"))
    public boolean isValidNetwork(long organismId, long networkId)
            throws DataStoreException {
        return connector.isValidNetwork(organismId, networkId);
    }

}
