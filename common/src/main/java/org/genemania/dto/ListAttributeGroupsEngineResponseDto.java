package org.genemania.dto;

import java.io.Serializable;
import java.util.Collection;

public class ListAttributeGroupsEngineResponseDto implements Serializable {
    private static final long serialVersionUID = 8322959282292573547L;
    Collection<Long> attributeGroupIds;
    
    public Collection<Long> getAttributeGroupIds() {
        return attributeGroupIds;
    }
    public void setAttributeGroupIds(Collection<Long> attributeGroupIds) {
        this.attributeGroupIds = attributeGroupIds;
    }
}
