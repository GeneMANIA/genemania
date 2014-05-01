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

import org.genemania.dto.ListAttributeGroupsEngineRequestDto;
import org.genemania.dto.ListAttributeGroupsEngineResponseDto;
import org.genemania.engine.cache.DataCache;
import org.genemania.engine.core.data.AttributeGroups;
import org.genemania.exception.ApplicationException;

public class ListAttributeGroups {
    DataCache cache;
    ListAttributeGroupsEngineRequestDto request;
    
    public ListAttributeGroups(DataCache cache,
            ListAttributeGroupsEngineRequestDto request) {
        this.cache = cache;
        this.request = request;
    }
    
    public ListAttributeGroupsEngineResponseDto process() throws ApplicationException {

        if (request.getNamespace() == null) {
            throw new ApplicationException("no namespace given");
        }

        if (request.getOrganismId() == 0) {
            throw new ApplicationException("no organism id specified, but don't know how to list all organisms. yet.");
        }
        
        AttributeGroups data = cache.getAttributeGroups(request.getNamespace(), request.getOrganismId());
        data.getAttributeGroups();
        
        ListAttributeGroupsEngineResponseDto response = new ListAttributeGroupsEngineResponseDto();
        
        Collection<Long> ids = new ArrayList<Long>(data.getAttributeGroups().keySet());
        response.setAttributeGroupIds(ids);
       
        return response;
    }
}
