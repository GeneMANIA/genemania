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


import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;

import org.genemania.dto.AddAttributeGroupEngineRequestDto;
import org.genemania.dto.AddAttributeGroupEngineResponseDto;
import org.genemania.dto.InteractionDto;
import org.genemania.dto.NetworkDto;
import org.genemania.dto.RelatedGenesEngineRequestDto;
import org.genemania.dto.RelatedGenesEngineResponseDto;
import org.genemania.engine.Mania2;
import org.genemania.engine.cache.RandomDataCacheBuilder;
import org.genemania.engine.cache.RandomDataCacheConfig;
import org.genemania.engine.core.data.AttributeData;
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
import org.junit.Test;

public class AddAttributeGroupTest extends AbstractAttributeTest {

    long newCoreAttributeGroupId = 51;
    
    String namespace = "user1";
    long newUserAttributeGroupId = -1;

    /*
     * create a set of attributes as part of core
     * dataset for an organism. this exercises the
     * add group, add ids, and add data api's for attributes.
     */
    @Test
    public void addCoreAttributeGroup() throws Exception {
        
        // create a group        
        int numAttributes = 10;
        createUserAttributeGroup(Data.CORE, config.getOrg1Id(), newCoreAttributeGroupId, numAttributes);
        
        // load the newly created data structures
        AttributeGroups attributeGroups = cacheBuilder.getCache().getAttributeGroups(Data.CORE, config.getOrg1Id());
        assertNotNull(attributeGroups);
        assertEquals(config.getNumAttributeGroups() + 1, attributeGroups.getAttributeGroups().size());
        
        AttributeData attributeData = cacheBuilder.getCache().getAttributeData(Data.CORE, config.getOrg1Id(), newCoreAttributeGroupId);
        assertNotNull(attributeData);
        assertNotNull(attributeData.getData());

        // verify ids
        attributeGroups = cacheBuilder.getCache().getAttributeGroups(Data.CORE, config.getOrg1Id());
        assertNotNull(attributeGroups);
        assertEquals(config.getNumAttributeGroups() + 1, attributeGroups.getAttributeGroups().size());
        assertEquals(10, attributeGroups.getAttributesForGroup(newCoreAttributeGroupId).size());
        
        // shouldn't fail
        int index = attributeGroups.getIndexForAttributeId(newCoreAttributeGroupId, 1);
        assertTrue(index >= 0);
        assertTrue(index < numAttributes);
        
        // should fail
        try {
            index = attributeGroups.getIndexForAttributeId(newCoreAttributeGroupId, 50);
            fail("expected exception");
        }
        catch (ApplicationException e) {
            // ok
        }
        
        // perform a query
        RelatedGenesEngineResponseDto relatedResult = query(Data.CORE, config.getOrg1Id());
    }
    
    @Test
    public void addUserAttributeGroup() throws Exception {
        
        AttributeGroups attributeGroups = null;
        
        // creating a group in user namespace will fail if not negative id
        long invalidAttributeGroupId = 100; // because namespace is non-core
        int numAttributes = 10;
               
        try {
            createUserAttributeGroup(namespace, config.getOrg1Id(), invalidAttributeGroupId, numAttributes);
            fail("can't add user attribute group with positive id");
        }
        catch (ApplicationException e) {
            // ok
        }
                    
        // create a group properly        
        createUserAttributeGroup(namespace, config.getOrg1Id(), newUserAttributeGroupId, numAttributes);
        
        // load the newly created attribute data structures for the new group
        attributeGroups = cacheBuilder.getCache().getAttributeGroups(namespace, config.getOrg1Id());
        assertNotNull(attributeGroups);
        assertEquals(config.getNumAttributeGroups() + 1, attributeGroups.getAttributeGroups().size());
        assertEquals(10, attributeGroups.getAttributesForGroup(newUserAttributeGroupId).size());
        
        AttributeData attributeData = cacheBuilder.getCache().getAttributeData(namespace, config.getOrg1Id(), newUserAttributeGroupId);
        assertNotNull(attributeData);
        assertNotNull(attributeData.getData());
        
        // but asking for core attribute groups should be unchanged
        attributeGroups = cacheBuilder.getCache().getAttributeGroups(Data.CORE, config.getOrg1Id());
        assertNotNull(attributeGroups);
        assertEquals(config.getNumAttributeGroups(), attributeGroups.getAttributeGroups().size());        

        // perform a query
        RelatedGenesEngineResponseDto relatedResult = query(namespace, config.getOrg1Id());    
    }
    
    @Test
    public void addAttributesToNewOrganism() throws Exception {
        
        long newOrganismId = -1;
        long [] nodeIds = new long[] {2,3,5,7,11,13,17,19};

        // new organism
        createOrganism(newOrganismId, nodeIds);
        
        // new attribute in user namespace
        String namespace = "user2";
        long newAttributeGroupId = -2;
        int numAttributes = 10;
        createUserAttributeGroup(namespace, newOrganismId, newAttributeGroupId, numAttributes);

        // perform a query
        RelatedGenesEngineResponseDto relatedResult = query(namespace, newOrganismId);    
        assertNotNull(relatedResult);
    
    }     
}
