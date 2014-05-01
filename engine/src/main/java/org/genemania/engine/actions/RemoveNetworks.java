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

package org.genemania.engine.actions;

import org.apache.log4j.Logger;
import org.genemania.dto.RemoveNetworkEngineRequestDto;
import org.genemania.dto.RemoveNetworkEngineResponseDto;
import org.genemania.engine.actions.support.UserDataPrecomputer;
import org.genemania.engine.cache.DataCache;
import org.genemania.engine.core.data.Network;
import org.genemania.exception.ApplicationException;
import org.genemania.util.NullProgressReporter;

/**
 * proess a list-networks request
 *
 * returns only the user networks and not core networks that the user has access to
 *
 */
public class RemoveNetworks {

    private static Logger logger = Logger.getLogger(RemoveNetworks.class);
    DataCache cache;
    RemoveNetworkEngineRequestDto request;

    public RemoveNetworks(DataCache cache, RemoveNetworkEngineRequestDto request) {
        this.cache = cache;
        this.request = request;
    }

    /*
     * action depends on parameter in the request (maybe too complex), dispatch
     * to appropriate supporting method.
     *
     */
    public RemoveNetworkEngineResponseDto process() throws ApplicationException {

        if (request.getNamespace() == null) {
            throw new ApplicationException("no namepspace specified");
        }
        // remove all user-uploaded and precomputed data for a given organism
        else if (request.getOrganismId() != 0 && request.getNetworkId() == 0) {
            return removeUserOrganism();
        }
        else if (request.getOrganismId() == 0 && request.getNetworkId() != 0) {
            throw new ApplicationException("must specify organism to remove individual network");
        }
        else if (request.getNetworkId() > 0) {
            throw new ApplicationException("can not remove core network");
        }
        // remove all networks in the given namespace
        else if (request.getOrganismId() == 0 && request.getNetworkId() == 0) {
            return removeUserNamespace();
        }
        // remove specific network for organism
        else if (request.getOrganismId() != 0 && request.getNetworkId() < 0) {
            return removeUserNetwork();
        }
        else {
            throw new ApplicationException("unexpected input");
        }
    }

    /*
     * remove the network, and update the precomputed data structures
     */
    private RemoveNetworkEngineResponseDto removeUserNetwork() throws ApplicationException {
        Network network = new Network(request.getNamespace(), request.getOrganismId(), request.getNetworkId());

        UserDataPrecomputer precomputer = new UserDataPrecomputer(request.getNamespace(), (int) request.getOrganismId(), cache, NullProgressReporter.instance());
        precomputer.removeNetwork((int) request.getNetworkId());

        cache.removeData(network);

        return new RemoveNetworkEngineResponseDto();

    }

    private RemoveNetworkEngineResponseDto removeUserOrganism() throws ApplicationException {
        cache.removeOrganism(request.getNamespace(), request.getOrganismId());
        return new RemoveNetworkEngineResponseDto();

    }

    private RemoveNetworkEngineResponseDto removeUserNamespace() throws ApplicationException {
        cache.removeNamespace(request.getNamespace());
        return new RemoveNetworkEngineResponseDto();
    }
}
