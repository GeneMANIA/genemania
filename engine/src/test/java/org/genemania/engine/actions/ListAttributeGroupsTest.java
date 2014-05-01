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

import static org.junit.Assert.*;

import org.genemania.dto.ListAttributeGroupsEngineRequestDto;
import org.genemania.dto.ListAttributeGroupsEngineResponseDto;
import org.genemania.engine.core.data.Data;
import org.genemania.exception.ApplicationException;
import org.junit.Test;

public class ListAttributeGroupsTest extends AbstractAttributeTest {

    /*
     * list attribute groups in core namespace
     */
    @Test
    public void testListCore() throws Exception {
        
        ListAttributeGroupsEngineRequestDto request = new ListAttributeGroupsEngineRequestDto();
        request.setNamespace(Data.CORE);
        request.setOrganismId(config.getOrg1Id());
        
        ListAttributeGroups listAttributeGroupsAction = new ListAttributeGroups(cacheBuilder.getCache(), request);
        ListAttributeGroupsEngineResponseDto result = listAttributeGroupsAction.process();
        assertNotNull(result);
        assertEquals(config.getNumAttributeGroups(), result.getAttributeGroupIds().size());        
    }
    
    /*
     * list attributes in user namespace, when none exist. you would think this would just
     * return the core attributes, but it gives an error, you must ask for core explicitly. 
     * sorry about that.
     */
    @Test(expected=ApplicationException.class)
    public void testListCoreFromUser() throws Exception {

        String namespace = "user1";
        ListAttributeGroupsEngineRequestDto request = new ListAttributeGroupsEngineRequestDto();
        request.setNamespace(namespace);
        request.setOrganismId(config.getOrg1Id());
        
        ListAttributeGroups listAttributeGroupsAction = new ListAttributeGroups(cacheBuilder.getCache(), request);
        
        ListAttributeGroupsEngineResponseDto result = listAttributeGroupsAction.process();
//            assertNotNull(result);
//            assertEquals(config.getNumAttributeGroups(), result.getAttributeGroupIds().size());
        
    }
   
    @Test
    public void testListUser() throws Exception {
        
        String namespace = "user1";
        long attributeGroupId = -1;
        createUserAttributeGroup(namespace, config.getOrg1Id(), attributeGroupId, 10);
        
        ListAttributeGroupsEngineRequestDto request = new ListAttributeGroupsEngineRequestDto();
        request.setNamespace(namespace);
        request.setOrganismId(config.getOrg1Id());
        
        ListAttributeGroups listAttributeGroupsAction = new ListAttributeGroups(cacheBuilder.getCache(), request);
        ListAttributeGroupsEngineResponseDto result = listAttributeGroupsAction.process();
        assertNotNull(result);
        assertEquals(config.getNumAttributeGroups() + 1, result.getAttributeGroupIds().size());       
    }
}
