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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.genemania.dto;

import java.io.Serializable;

/**
 * given a particular user, should be able to:
 *
 *  * remove a single given user network
 *  * remove all user networks belonging to an organism
 *  * remove all user networks
 *
 * default values like 0 for organism or network id means 'all',
 * otherwise a specific id must be provided.
 *
 * however currently we don't suppose the middle usecase, removing all
 * networks for an organism. so give a namespace and 0 (default) for both
 * org and net id to remove the entire namespace, or specify both the org
 * and net id to remove a particular user network.
 *
 */
public class RemoveNetworkEngineRequestDto implements Serializable {
    // __[static]______________________________________________________________
    private static final long serialVersionUID = 4689311868856386402L;

    // __[attributes]__________________________________________________________
    long organismId;
    String namespace;
    long networkId;

    // __[constructors]________________________________________________________
    public RemoveNetworkEngineRequestDto() {
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

    /**
     * @return the networkId
     */
    public long getNetworkId() {
        return networkId;
    }

    /**
     * @param networkId the networkId to set
     */
    public void setNetworkId(long networkId) {
        this.networkId = networkId;
    }

}
