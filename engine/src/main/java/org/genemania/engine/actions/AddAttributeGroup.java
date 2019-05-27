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
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.genemania.dto.AddAttributeGroupEngineRequestDto;
import org.genemania.dto.AddAttributeGroupEngineResponseDto;
import org.genemania.engine.cache.DataCache;
import org.genemania.engine.config.Config;
import org.genemania.engine.core.data.AttributeData;
import org.genemania.engine.core.data.AttributeGroups;
import org.genemania.engine.core.data.Data;
import org.genemania.engine.core.data.NodeIds;
import org.genemania.engine.core.utils.Logging;
import org.genemania.engine.exception.CancellationException;
import org.genemania.engine.matricks.Matrix;
import org.genemania.exception.ApplicationException;

public class AddAttributeGroup {
    private static Logger logger = Logger.getLogger(AddAttributeGroup.class);
    private DataCache cache;
    private AddAttributeGroupEngineRequestDto request;
    AddAttributeGroupEngineResponseDto response = null;
        
    private long requestStartTimeMillis;
    private long requestEndTimeMillis;
    
    public AddAttributeGroup(DataCache cache, AddAttributeGroupEngineRequestDto request) {
        this.cache = cache;
        this.request = request;
    }

    public AddAttributeGroupEngineResponseDto process() throws ApplicationException {
        try {
            requestStartTimeMillis = System.currentTimeMillis();

            logStart();
            checkQuery();
            logQuery();

            response = new AddAttributeGroupEngineResponseDto();
            addAttributeGroup();
            addAttributes();
            addAttributeData();
            
            requestEndTimeMillis = System.currentTimeMillis();
            logEnd();

            return response;
        }
        catch (CancellationException e) {
            logger.info("request was cancelled");
            return null;
        }
    }
    
    /*
     * add attribute group with empty attribute set. also create an associated, empty
     * attribute data container.
     */
    AddAttributeGroupEngineResponseDto addAttributeGroup() throws ApplicationException {        
        AttributeGroups attributeGroups = loadCreateGroups();
        
        // the query check should have verified the group doesn't already exist,
        // so we add here without checking.
        HashMap<Long, ArrayList<Long>> groupIds = attributeGroups.getAttributeGroups();
        groupIds.put(request.getAttributeGroupId(), new ArrayList<Long>());
        cache.putAttributeGroups(attributeGroups);      
        
        AttributeData attributeData = new AttributeData(request.getNamespace(), request.getOrganismId(), request.getAttributeGroupId());
        cache.putAttributeData(attributeData);
        
        return new AddAttributeGroupEngineResponseDto();
    }
    
    /*
     * fetch the object holding list of attribute groups, or 
     * create a new one if none exist 
     */
    AttributeGroups loadCreateGroups() throws ApplicationException {
        
        cache.initNamespace(request.getNamespace(), request.getOrganismId());
        
        AttributeGroups attributeGroups = null;
        try {
            attributeGroups = cache.getAttributeGroups(request.getNamespace(), request.getOrganismId());
        }
        catch (ApplicationException e) {
            if (Data.CORE.equals(request.getNamespace())) {
                attributeGroups = new AttributeGroups(request.getNamespace(), request.getOrganismId());
                HashMap<Long, ArrayList<Long>> groupIds= new HashMap<Long, ArrayList<Long>>();
                attributeGroups.setAttributeGroups(groupIds);
            }
            else {
                // what went wrong? initNamespace() should have setup things for the user
                // namespace. rethrow.
                throw(e);
            }
        }
        
        return attributeGroups;
    }

    /*
     * set the attributes, and update the attribute data matrix to the appropriate size.
     * fow now we assume just create once but in future (TODO) this is where we'll put
     * resize code for adding new attribute ids to an existing set.
     */
    void addAttributes() throws ApplicationException {
        
        AttributeGroups attributeGroups = cache.getAttributeGroups(request.getNamespace(), request.getOrganismId());
        
        attributeGroups.getAttributeGroups().get(request.getAttributeGroupId()).addAll(request.getAttributeIds());
        cache.putAttributeGroups(attributeGroups);
        
        AttributeData attributeData = cache.getAttributeData(request.getNamespace(), request.getOrganismId(), request.getAttributeGroupId());
        if (attributeData.getData() != null) { // must be null until we add resize support
            throw new ApplicationException("consistency error");
        }
        
        NodeIds nodeIds = cache.getNodeIds(request.getOrganismId());
        int numNodes = nodeIds.getNodeIds().length;
        int numAttributes = attributeGroups.getAttributeGroups().get(request.getAttributeGroupId()).size(); // TODO: this needs a convenience method
        
        Matrix data = Config.instance().getMatrixFactory().sparseColMatrix(numNodes, numAttributes);
        attributeData.setData(data);
        
        cache.putAttributeData(attributeData);
        
    }

    void addAttributeData() throws ApplicationException {
        
        AttributeData attributeData = cache.getAttributeData(request.getNamespace(), request.getOrganismId(), request.getAttributeGroupId());
        NodeIds nodeIds = cache.getNodeIds(request.getOrganismId());
        //System.out.println(nodeIds.getNodeIds().toString());
	AttributeGroups attributeGroups = cache.getAttributeGroups(request.getNamespace(), request.getOrganismId());
        Matrix data = attributeData.getData();

	//System.out.println(attributeGroups.getKey().toString());
	//System.out.println(data.getKey().toString());

        for (List<Long> assocs: request.getNodeAttributeAssociations()) {
            if (assocs != null && assocs.size() > 1) {
		    //System.out.println(String.format("length of associations:%d", assocs.size()));
                long nodeId = assocs.get(0);
                for (int k=1; k<assocs.size(); k++) {
                    long attributeId = assocs.get(k);

		    //System.out.println(String.format("nodeId: %d, attributeid: %d",nodeId, attributeId));

                    int i = nodeIds.getIndexForId(nodeId);
		    try{
                    	int j = attributeGroups.getIndexForAttributeId(request.getAttributeGroupId(), attributeId);
                    	data.set(i, j, 1);
			} catch (Exception e){
				System.out.println(String.format("Unable to get index for attribute id: %d, (associated with nodeid: %d)",attributeId,nodeId));
			}		
                }
            }
         }
            
         cache.putAttributeData(attributeData);
    }
    
    void logStart() {

    }

    void logEnd() {
        logger.info("completed processing request, duration = " + Logging.duration(requestStartTimeMillis, requestEndTimeMillis));
    }

    void logQuery() {
        logger.info(String.format("request for new attribute group for organism %s in namespace %s with id %d",
                request.getOrganismId(), request.getNamespace(), request.getAttributeGroupId()));
    }
    
    void checkQuery() throws ApplicationException {
        if (request == null) {
            throw new ApplicationException("request object was null");
        }

        if (request.getProgressReporter() == null) {
            throw new ApplicationException("ProgressReporter was null");
        }
        
        
        if (request.getNamespace().equals(Data.CORE)) {
            if (request.getAttributeGroupId() < 0) {
                throw new ApplicationException("CORE attribute groups must have id >= 0");
            }
        }
        else if (request.getAttributeGroupId() >= 0) {
            throw new ApplicationException("user attribute groups must have id < 0");
        }        
    }
}
