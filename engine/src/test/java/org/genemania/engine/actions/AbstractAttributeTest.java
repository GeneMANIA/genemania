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

import static org.junit.Assert.assertNotNull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.genemania.dto.AddAttributeGroupEngineRequestDto;
import org.genemania.dto.AddAttributeGroupEngineResponseDto;
import org.genemania.dto.AddOrganismEngineRequestDto;
import org.genemania.dto.AddOrganismEngineResponseDto;
import org.genemania.dto.RelatedGenesEngineRequestDto;
import org.genemania.dto.RelatedGenesEngineResponseDto;
import org.genemania.engine.IMania;
import org.genemania.engine.Mania2;
import org.genemania.engine.cache.RandomDataCacheBuilder;
import org.genemania.engine.cache.RandomDataCacheConfig;
import org.genemania.engine.core.data.AttributeGroups;
import org.genemania.engine.core.data.Data;
import org.genemania.engine.core.data.NetworkIds;
import org.genemania.engine.core.data.NodeIds;
import org.genemania.exception.ApplicationException;
import org.genemania.type.CombiningMethod;
import org.genemania.type.ScoringMethod;
import org.genemania.util.NullProgressReporter;
import org.junit.After;
import org.junit.Before;

public abstract class AbstractAttributeTest {

    static RandomDataCacheBuilder cacheBuilder;
    RandomDataCacheConfig config = RandomDataCacheConfig.getStandardConfig2();
        
    @Before
    public void setUp() throws Exception {
        cacheBuilder = new RandomDataCacheBuilder(7132);
        cacheBuilder.setUp();

        // random organism 1
        cacheBuilder.addOrganism(config);
    }

    @After
    public void tearDown() throws Exception {
        cacheBuilder.tearDown();
    }
    
    // helper to create an attribute group along with a set of attributes.
    // attribute ids are numbered from 1 to numAttributes.
    public void createUserAttributeGroup(String namespace, long organismId, long attributeGroupId, int numAttributes) throws Exception {
        // create a group
        AddAttributeGroupEngineRequestDto addAttributeGroupRequest = new AddAttributeGroupEngineRequestDto();
        
        addAttributeGroupRequest.setNamespace(namespace);
        addAttributeGroupRequest.setOrganismId(organismId);
        addAttributeGroupRequest.setAttributeGroupId(attributeGroupId);
        addAttributeGroupRequest.setProgressReporter(NullProgressReporter.instance());
                
        // create attribute id list
        ArrayList<Long> ids = new ArrayList<Long>();
        for (long i=1; i<=numAttributes; i++) {
            ids.add(i);
        }

        addAttributeGroupRequest.setAttributeIds(ids);

        // create attribute data
        NodeIds nodeIds = cacheBuilder.getCache().getNodeIds(organismId);
        
        ArrayList<ArrayList<Long>> assocs = new ArrayList<ArrayList<Long>>();
        
        ArrayList<Long> assoc = new ArrayList<Long>();
        assoc.add(nodeIds.getIdForIndex(1));
        assoc.add(1L);
        
        assocs.add(assoc);
        
        addAttributeGroupRequest.setNodeAttributeAssociations(assocs);
    
        // process
        AddAttributeGroup addAttributeGroupAction = new AddAttributeGroup(cacheBuilder.getCache(), addAttributeGroupRequest);
        AddAttributeGroupEngineResponseDto addAttributeGroupResponse = addAttributeGroupAction.process();
        assertNotNull(addAttributeGroupResponse);
     
    }
    /*
     * helper to perform a query using average combining with all networks and attributes, 
     * on half of the genes.
     */
    RelatedGenesEngineResponseDto query(String namespace, long organismId) throws ApplicationException {
        // setup mania instance ... only data dependency is on the cache
        Mania2 mania = new Mania2(cacheBuilder.getCache());
    
        // create request
        RelatedGenesEngineRequestDto request = new RelatedGenesEngineRequestDto();
        request.setOrganismId(organismId);
        request.setNamespace(namespace);
        request.setCombiningMethod(CombiningMethod.AVERAGE);
        request.setScoringMethod(ScoringMethod.DISCRIMINANT);

        // first half of nodes as +ve
        NodeIds nodeIds = cacheBuilder.getCache().getNodeIds(organismId);
        ArrayList<Long> posNodes = new ArrayList<Long>();
        for (int i = 0; i < nodeIds.getNodeIds().length/2; i++) {
            posNodes.add((long) nodeIds.getNodeIds()[i]);
        }
        request.setPositiveNodes(posNodes);

        // all networks
        NetworkIds networkIds = null;
        
        try {
            networkIds = cacheBuilder.getCache().getNetworkIds(namespace, organismId);
        }
        catch (ApplicationException e) {
            // no user networks? use core
            if (!Data.CORE.equals(namespace)) {
                networkIds = cacheBuilder.getCache().getNetworkIds(Data.CORE, organismId);
            }
            else {
                throw e;
            }
        }
                
        Collection<Collection<Long>> requestNetworks = new ArrayList<Collection<Long>>();

        ArrayList<Long> group = new ArrayList<Long>();
        for (int i = 0; i < networkIds.getNetworkIds().length; i++) {
            group.add(networkIds.getNetworkIds()[i]);
        }

        requestNetworks.add(group);
        request.setInteractionNetworks(requestNetworks);
        request.setLimitResults(10);
        request.setProgressReporter(NullProgressReporter.instance());
        
        // all attributes
        AttributeGroups attributeGroups = cacheBuilder.getCache().getAttributeGroups(namespace, organismId);
        ArrayList<Long> attributeGroupIds = new ArrayList<Long>(attributeGroups.getAttributeGroups().keySet());
        request.setAttributeGroups(attributeGroupIds);
        
        // compute result
        RelatedGenesEngineResponseDto response = mania.findRelated(request);
        
        return response;
    }
    
    // create organism helper
    public void createOrganism(long organismId, long [] nodeIds) throws Exception {
        // create new organisms
        AddOrganismEngineRequestDto addOrgRequest = new AddOrganismEngineRequestDto();
        HashSet<Long> newNodeIds = new HashSet<Long>();
        for (Long i: nodeIds) {
            newNodeIds.add(i);
        }
        
        addOrgRequest.setOrganismId(organismId);
        addOrgRequest.setNodeIds(newNodeIds);
        addOrgRequest.setProgressReporter(NullProgressReporter.instance());

        IMania mania = new Mania2(cacheBuilder.getCache());
        AddOrganismEngineResponseDto addOrgResponse = mania.addOrganism(addOrgRequest);
        assertNotNull(addOrgResponse);
    }
}
