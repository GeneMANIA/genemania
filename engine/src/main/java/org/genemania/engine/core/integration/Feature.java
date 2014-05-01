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
package org.genemania.engine.core.integration;

import java.io.Serializable;

import org.genemania.engine.Constants.NetworkType;

/*
 * GeneMANIA interaction data consists of network matrices
 * or attribute vectors, which are treated much the same way
 * by the algorithm but represented and optimized differently.
 * This container class simply specifies one of these data
 * types in a unified way.
 */
public class Feature implements Comparable<Feature>, Serializable {
    private static final long serialVersionUID = 523872447663612391L;

    public static final long FAKE_SPARSE_NETWORK_GROUP_ID = 1; // because backend has historically not known about this
    
    private final NetworkType type;
    private final long groupId;
    private final long id;
    
    public Feature(NetworkType type, long groupId, long id) {
        super();
        this.type = type;
        this.groupId = groupId;
        this.id = id;
    }
    
    public NetworkType getType() {
        return type;
    }
    
    public long getGroupId() {
        return groupId;
    }
    
    public long getId() {
        return id;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (groupId ^ (groupId >>> 32));
        result = prime * result + (int) (id ^ (id >>> 32));
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Feature other = (Feature) obj;
        if (groupId != other.groupId)
            return false;
        if (id != other.id)
            return false;
        if (type != other.type)
            return false;
        return true;
    }

    @Override
    public int compareTo(Feature that) {
        int cmp = this.getType().compareTo(that.getType());
   
        if (cmp != 0) {
            return cmp;
        }
        
        long lcmp = this.getGroupId() - that.getGroupId();
        if (lcmp != 0) {
            return shorten(lcmp);
        }
        
        lcmp = this.getId() - that.getId();
        return shorten(lcmp);
    }
    
    /*
     * for some reason i don't want to cast or box
     */
    private int shorten(long a) {
        if (a == 0) return 0;
        return a < 0 ? -1 : 1;
    }
    
    @Override
    public String toString() {
        return key(false);
    }
    
    /*
     * we never kept needed group ids for network in the backend, until
     * we introduced attributes. this makes some of our internal
     * book-keeping inconsistent, here we explicitly ignore they group id
     * for sparse-networks. TODO: fix when backend learns about network groups
     */
    public String key(boolean ignoreSparseNetworkGroup) {
        // ignore the network group id, since we don't have it
        long groupId = getGroupId();
        if (ignoreSparseNetworkGroup && getType() == NetworkType.SPARSE_MATRIX) {
            groupId = FAKE_SPARSE_NETWORK_GROUP_ID;
        }

        return String.format("%s-%d-%d", getType().getCode(), groupId, getId());
    }
}