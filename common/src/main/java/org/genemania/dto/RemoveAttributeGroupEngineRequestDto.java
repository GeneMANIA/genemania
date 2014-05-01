package org.genemania.dto;

import java.io.Serializable;

public class RemoveAttributeGroupEngineRequestDto implements Serializable {
    private static final long serialVersionUID = 5851527990552112077L;
    
    long organismId;
    String namespace;
    long attributeGroupId;
        
    public RemoveAttributeGroupEngineRequestDto() {}
    
    public RemoveAttributeGroupEngineRequestDto(long organismId, String namespace,
            long attributeGroupId) {
        this.organismId = organismId;
        this.namespace = namespace;
        this.attributeGroupId = attributeGroupId;
    }

    public long getOrganismId() {
        return organismId;
    }
    
    public void setOrganismId(long organismId) {
        this.organismId = organismId;
    }
    
    public String getNamespace() {
        return namespace;
    }
    
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
    
    public long getAttributeGroupId() {
        return attributeGroupId;
    }
    
    public void setAttributeGroupId(long attributeGroupId) {
        this.attributeGroupId = attributeGroupId;
    }
}
