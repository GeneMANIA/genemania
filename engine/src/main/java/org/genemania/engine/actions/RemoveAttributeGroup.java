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

import org.genemania.dto.RemoveAttributeGroupEngineRequestDto;
import org.genemania.dto.RemoveAttributeGroupEngineResponseDto;
import org.genemania.engine.cache.DataCache;
import org.genemania.engine.core.data.AttributeData;
import org.genemania.engine.core.data.AttributeGroups;
import org.genemania.exception.ApplicationException;

public class RemoveAttributeGroup {

    DataCache cache;
    RemoveAttributeGroupEngineRequestDto request;
    public RemoveAttributeGroup(DataCache cache,
            RemoveAttributeGroupEngineRequestDto request) {
        super();
        this.cache = cache;
        this.request = request;
    }
  
    public RemoveAttributeGroupEngineResponseDto process() throws ApplicationException {
        if (request.getNamespace() == null) {
            throw new ApplicationException("no namepspace specified");
        }
        
        if (request.getOrganismId() == 0) {
            throw new ApplicationException("no organism id specified, but don't know how to list all organisms. yet.");
        }
         
        if (request.getAttributeGroupId() > 0) {
            throw new ApplicationException("can not remove core attribute group");
        }

        
        AttributeGroups groups = cache.getAttributeGroups(request.getNamespace(), request.getOrganismId());
        if (groups.getAttributeGroups().containsKey(request.getAttributeGroupId())) {
            groups.getAttributeGroups().remove(request.getAttributeGroupId());
            cache.putAttributeGroups(groups);
            cache.removeData(new AttributeData(request.getNamespace(), request.getOrganismId(), request.getAttributeGroupId()));            
        }
        
        return new RemoveAttributeGroupEngineResponseDto();
    }
}
