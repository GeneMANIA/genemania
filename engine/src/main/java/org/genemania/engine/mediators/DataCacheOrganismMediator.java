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


package org.genemania.engine.mediators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.genemania.domain.Gene;
import org.genemania.domain.InteractionNetwork;
import org.genemania.domain.InteractionNetworkGroup;
import org.genemania.domain.NetworkMetadata;
import org.genemania.domain.Organism;
import org.genemania.engine.cache.DataCache;
import org.genemania.engine.core.data.Data;
import org.genemania.engine.core.data.NetworkIds;
import org.genemania.exception.ApplicationException;
import org.genemania.exception.DataStoreException;
import org.genemania.mediator.NodeCursor;
import org.genemania.mediator.OrganismMediator;

/**
 * create Organism domain objects based on engine data cache
 */
public class DataCacheOrganismMediator implements OrganismMediator {
    private DataCache cache;

    public DataCacheOrganismMediator(DataCache cache) {
        this.cache = cache;
    }

    public NodeCursor createNodeCursor(long organismId) throws DataStoreException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public List<Organism> getAllOrganisms() throws DataStoreException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public List<Gene> getDefaultGenes(long organismId) throws DataStoreException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public List<InteractionNetwork> getDefaultNetworks(long organismId) throws DataStoreException {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    public Organism getOrganism(long organismId) throws DataStoreException {        
        Organism organism = new Organism("org" + organismId);
        organism.setId(organismId);
        organism.setInteractionNetworkGroups(makeGroups(organismId));
        return organism;
    }

    private Collection<InteractionNetworkGroup> makeGroups(long organismId)throws DataStoreException {

        NetworkIds ids;
        try {
            ids = cache.getNetworkIds(Data.CORE, organismId);
        }
        catch (ApplicationException e) {
            throw new DataStoreException(e);
        }

        Collection<InteractionNetworkGroup> groups = new ArrayList<InteractionNetworkGroup>();
        InteractionNetworkGroup group = new InteractionNetworkGroup();

        long groupId = 1;
        group.setName("group " + groupId);
        group.setId(groupId);
        groups.add(group);


        Collection<InteractionNetwork> networks = new ArrayList();
        for (int i = 0; i < ids.getNetworkIds().length; i++) {
            long networkId = ids.getNetworkIds()[i];
            InteractionNetwork interactionNetwork = new InteractionNetwork();
            interactionNetwork.setName("network " + networkId);
            interactionNetwork.setId(networkId);

            NetworkMetadata md = new NetworkMetadata();
            interactionNetwork.setMetadata(md);

            networks.add(interactionNetwork);

        }

        group.setInteractionNetworks(networks);

        return groups;
    }
    
    public List hqlSearch(String queryString) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Organism getOrganismForGroup(long groupId) throws DataStoreException {
		throw new UnsupportedOperationException("Not supported yet.");
    }
}
