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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.genemania.domain.Gene;
import org.genemania.domain.InteractionNetwork;
import org.genemania.domain.InteractionNetworkGroup;
import org.genemania.domain.Organism;
import org.genemania.exception.ApplicationException;
import org.genemania.exception.DataStoreException;
import org.genemania.mediator.NodeCursor;
import org.genemania.mediator.OrganismMediator;

/**
 * for use in unit tests. only good for a single organism
 *
 * puts given network ids into groups of given size;
 */
public class MemOrganismMediator implements OrganismMediator {

    int organismId;
    int groupSize;
    int[] networkIds;

    public MemOrganismMediator(int organismId, int[] networkIds, int groupSize) {
        this.organismId = organismId;
        this.networkIds = networkIds;
        this.groupSize = groupSize;
    }

    public Organism getOrganism(final long organismId) {
        Organism organism = new Organism();

        try {

            organism.setId(organismId);
            organism.setName("organism" + organismId);
            organism.setDescription("for unit testing");
            organism.setInteractionNetworkGroups(buildNetworkGroups());
        }
        catch (ApplicationException e) {
            throw new RuntimeException("failed to get organism", e);
        }

        return organism;

    }

    /*
     * build network groups, based on network ids stored in
     * cache. since the cache doesn't have a notion of groups,
     * use groupSize to determine the number in each group, then
     * just loop over networks building up groups of that size
     */
    Collection<InteractionNetworkGroup> buildNetworkGroups() throws ApplicationException {

        Collection<InteractionNetworkGroup> groups = new ArrayList<InteractionNetworkGroup>();

        int groupId = 1;
        int added = 0;

        InteractionNetworkGroup group = new InteractionNetworkGroup();

        Collection<InteractionNetwork> networks = new ArrayList<InteractionNetwork>();
        for (int networkId: networkIds) {
            if (networks.size() == groupSize) {
                group = makeGroup(organismId, groupId, networks);
                groups.add(group);
                added += networks.size();
                networks = new ArrayList<InteractionNetwork>();
                groupId += 1;
            }

            InteractionNetwork network = new InteractionNetwork();
            network.setId(networkId);
            network.setName("test network " + networkId);
            //System.out.println("adding organism " + organismId + " network " + networkId + " for group " + groupId);
            networks.add(network);
        }

        // add last partial group if necessary
        if (networks.size() > 0) {
            group = makeGroup(organismId, groupId, networks);
            groups.add(group);
            added += networks.size();

        }

        if (added != networkIds.length) {
            throw new ApplicationException(String.format("braincramp exception. invariant violated. added %d, map size %d", added, networkIds.length));
        }

        return groups;

    }

    InteractionNetworkGroup makeGroup(int organismId, int groupId, Collection<InteractionNetwork> networks) {
        InteractionNetworkGroup group = new InteractionNetworkGroup();
        group.setId(groupId);
        group.setName("group 1");
        group.setDescription("for testing");
        group.setInteractionNetworks(networks);
        return group;
    }

    public List<Organism> getAllOrganisms() {
        throw new RuntimeException("not implemented");
    }

    public List<Gene> getDefaultGenes(long organismId) {
        throw new RuntimeException("not implemented");
    }

    public NodeCursor createNodeCursor(long organismId) {
        throw new RuntimeException("not implemented");
    }

    public List hqlSearch(final String queryString) {
        throw new RuntimeException("not implemented");
    }

    public List<InteractionNetwork> getDefaultNetworks(long organismId) {
        throw new RuntimeException("not implemented");
    }
    
    public Organism getOrganismForGroup(long groupId) throws DataStoreException {
        throw new RuntimeException("not implemented");
    }
}
