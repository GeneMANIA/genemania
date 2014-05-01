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

package org.genemania.dto;

import java.io.Serializable;

/*
 * request for network ids corresponding to cached user data sets.
 */
public class ListNetworksEngineRequestDto implements Serializable {

    // __[static]______________________________________________________________
    private static final long serialVersionUID = 5023365914374129651L;

    // __[attributes]__________________________________________________________
    long organismId;
    String namespace;

    // __[constructors]________________________________________________________
    public ListNetworksEngineRequestDto() {
    }

    /**
     * @return the organismId
     */
    public long getOrganismId() {
        return organismId;
    }

    /**
     * @param organismId the organismId to set
     */
    public void setOrganismId(long organismId) {
        this.organismId = organismId;
    }

    /**
     * @return the namespace
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * @param namespace the namespace to set
     */
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
}
