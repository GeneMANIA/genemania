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
package org.genemania.engine.core.data;

import java.util.ArrayList;
import java.util.HashMap;
import org.genemania.exception.ApplicationException;

/* 
 * maintains a relation between attribute group ids, 
 * and the attribute ids belonging to each group.
 */
public class AttributeGroups extends Data {
    private static final long serialVersionUID = 36730522635636429L;

    private HashMap<Long, ArrayList<Long>> attributeGroups;
    private HashMap<Long, Long> reverseMap;
    
    public AttributeGroups(String namespace, long organismId) {
        super(namespace, organismId);
    }
    
    public HashMap<Long, ArrayList<Long>> getAttributeGroups() {
        return attributeGroups;
    }
    public void setAttributeGroups(HashMap<Long, ArrayList<Long>> attributeGroups) {
        this.attributeGroups = attributeGroups;
    }
    
    @Override
    public String [] getKey() {
        return new String [] {getNamespace(), "" + getOrganismId(), "attributeGroups"};
    }
    
    public ArrayList<Long> getAttributesForGroup(long groupId) throws ApplicationException {
        ArrayList<Long> attributes = attributeGroups.get(groupId);
        if (attributes == null) {
            throw new ApplicationException("thre is no attribute group with id: " + groupId);
        }
        
        return attributes;
    }
    
    public long getGroupForAttribute(long attributeId) throws ApplicationException {
        
        if (reverseMap == null) {
            reverseMap = makeReverseMap();
        }

        Long groupId = reverseMap.get(attributeId);
        if (groupId == null) {
            throw new ApplicationException("there is no attribute group for attribute id: " + attributeId);
        }

        return groupId;
    }
    
    public boolean hasAttributeGroup(long groupId) {
        if (attributeGroups.containsKey(groupId) && attributeGroups.get(groupId) != null) {
            return true;
        }
        
        return false;
    }
    
    // TODO: can optimize reverse lookups by maintaining maps
    public int getIndexForAttributeId(long groupId, long attributeId) throws ApplicationException {
        int index = attributeGroups.get(groupId).indexOf(attributeId);
        if (index < 0) {
            throw new ApplicationException(String.format("attribute id %d in group %d not found", attributeId, groupId));
        }
        return index;
    }
    
    public long getAttributeIdForIndex(long groupId, int index) throws ApplicationException {
        return attributeGroups.get(groupId).get(index);        
    }
    
    protected HashMap<Long, Long> makeReverseMap() throws ApplicationException {
        HashMap<Long, Long> map = new HashMap<Long, Long>();
        for (long groupId: attributeGroups.keySet()) {
            ArrayList<Long> attributes = attributeGroups.get(groupId);
            for (long attributeId: attributes) {

                if (map.containsKey(attributeId)) {
                    throw new ApplicationException("key already exists (must be unique!): " + attributeId);
                }

                map.put(attributeId, groupId);
            }

        }
        return map;
    }    
}
