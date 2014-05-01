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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.genemania.dto.AddOrganismEngineRequestDto;
import org.genemania.dto.AddOrganismEngineResponseDto;
import org.genemania.engine.cache.DataCache;
import org.genemania.engine.core.data.AttributeGroups;
import org.genemania.engine.core.data.Data;
import org.genemania.engine.core.data.DatasetInfo;
import org.genemania.engine.core.data.NetworkIds;
import org.genemania.engine.core.data.NodeIds;
import org.genemania.exception.ApplicationException;
import org.genemania.exception.DataStoreException;

/**
 * adds a new organism to the engine's data set.
 *
 */
public class AddOrganism {
    private static Logger logger = Logger.getLogger(AddOrganism.class);

    DataCache cache;
    AddOrganismEngineRequestDto request;

    public AddOrganism(DataCache cache, AddOrganismEngineRequestDto request) {
        this.cache = cache;
        this.request = request;
    }

    public AddOrganismEngineResponseDto process() throws ApplicationException {

        checkId();

        try {
            cache.initNamespace(Data.CORE, request.getOrganismId());
        	buildNodeIds(request.getOrganismId(), request.getNodeIds());
        	buildDatasetInfo(request.getOrganismId(), request.getNodeIds().size());
        	buildEmptyNetworkIds(request.getOrganismId());
        	buildEmptyAttributeGroupIds(request.getOrganismId());
        }
        catch (DataStoreException e) {
        	throw new ApplicationException("failed to add organism", e);
        }
        AddOrganismEngineResponseDto response = new AddOrganismEngineResponseDto();
        return response;

    }

    /*
     * given organism id must not exist
     */
    public void checkId() throws ApplicationException {
    	// TODO: this would be a good thing to implement!
    }

    public void buildDatasetInfo(long organismId, int numNodes) throws ApplicationException {
        DatasetInfo info = new DatasetInfo(organismId);
        info.setNumGenes(numNodes);
        cache.putDatasetInfo(info);
    }

    /*
     * create nodeIds object in the datacache, for the given organism
     */
    public void buildNodeIds(long organismId, Collection<Long> allNodeIds) throws ApplicationException, DataStoreException {
        logger.info("building node/index mapping for organism " + organismId);

        long[] table = AddOrganism.buildPrimitiveTable(allNodeIds);

        NodeIds nodeIds = new NodeIds(organismId);
        nodeIds.setNodeIds(table);
        cache.putNodeIds(nodeIds);
    }

    /*
     * create an empty network ids data structure in the cache
     */
    public void buildEmptyNetworkIds(long organimsId) throws ApplicationException, DataStoreException {
        logger.info("building empty network ids for organism " + organimsId);

        long [] table = new long[0];

        NetworkIds networkIds = new NetworkIds(Data.CORE, organimsId);
        networkIds.setNetworkIds(table);
        cache.putNetworkIds(networkIds);
    }

    /*
     * create empty attribute groups structure, no groups exist just
     * the container.
     */
    public void buildEmptyAttributeGroupIds(long organismId) throws ApplicationException {
        AttributeGroups attributeGroups = new AttributeGroups(Data.CORE, organismId);
        HashMap<Long, ArrayList<Long>> groupIds= new HashMap<Long, ArrayList<Long>>();
        attributeGroups.setAttributeGroups(groupIds);
        cache.putAttributeGroups(attributeGroups);
    }

	public static long[] buildPrimitiveTable(Collection<Long> list) {
	    long[] table = new long[list.size()];

	    int i = 0;
	    for (long id: list) {
	        table[i] = id;
	        i += 1;
	    }

	    return table;
	}
}
