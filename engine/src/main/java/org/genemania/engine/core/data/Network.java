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

package org.genemania.engine.core.data;

import org.genemania.engine.matricks.SymMatrix;

/**
 * wrap's a symmetric matrix representing
 * an interaction network.
 *
 */
public class Network extends Data {
    private static final long serialVersionUID = -987223732188362293L;

    private long id;
    private SymMatrix data;
    
    public Network(String namespace, long organismId, long id) {
        this(namespace, organismId, id, null);
    }

    public Network(String namespace, long organismId, long id, SymMatrix data) {
        super(namespace, organismId);
        this.id = id;
        this.data = data;
    }

    /**
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * @return the data
     */
    public SymMatrix getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(SymMatrix data) {
        this.data = data;
    }

    @Override
    public String [] getKey() {
        return new String[]{getNamespace(), "" + getOrganismId(), "" + id};
    }
}
