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

import org.genemania.dto.RemoveAttributeGroupEngineRequestDto;
import org.genemania.dto.RemoveAttributeGroupEngineResponseDto;
import org.genemania.engine.core.data.AttributeData;
import org.genemania.engine.core.data.AttributeGroups;
import org.genemania.exception.ApplicationException;
import org.junit.Test;

public class RemoveAttributeGroupTest extends AbstractAttributeTest {
    @Test
    public void test() throws Exception {
        String namespace = "user1";
        long attributeGroupId = -1;
        createUserAttributeGroup(namespace, config.getOrg1Id(), attributeGroupId, 10);
 
        // pre-verify
        AttributeGroups groups = cacheBuilder.getCache().getAttributeGroups(namespace, config.getOrg1Id());
        assertEquals(config.getNumAttributeGroups() + 1, groups.getAttributeGroups().size());
        assertTrue(groups.getAttributeGroups().containsKey(attributeGroupId));
        
        AttributeData attributeData = cacheBuilder.getCache().getAttributeData(namespace, config.getOrg1Id(), attributeGroupId);
        assertNotNull(attributeData);
        
        // remove
        RemoveAttributeGroupEngineRequestDto request = new RemoveAttributeGroupEngineRequestDto();
        request.setNamespace(namespace);
        request.setOrganismId(config.getOrg1Id());
        request.setAttributeGroupId(attributeGroupId);
        
        RemoveAttributeGroup removeAttributeGroupAction = new RemoveAttributeGroup(cacheBuilder.getCache(), request);
        RemoveAttributeGroupEngineResponseDto result = removeAttributeGroupAction.process();
        assertNotNull(result);
        
        // verify
        groups = cacheBuilder.getCache().getAttributeGroups(namespace, config.getOrg1Id());
        assertEquals(config.getNumAttributeGroups(), groups.getAttributeGroups().size());
        assertFalse(groups.getAttributeGroups().containsKey(attributeGroupId));
        
        try {
            attributeData = cacheBuilder.getCache().getAttributeData(namespace, config.getOrg1Id(), attributeGroupId);
            fail("should not find attribute data!");
        }
        catch (ApplicationException e) {
            // ok
        }
    }
}
