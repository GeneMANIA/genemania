package org.genemania.dto;

import java.io.Serializable;

public class ListAttributeGroupsEngineRequestDto implements Serializable {
    private static final long serialVersionUID = -5473749309569292121L;

    long organismId;
    String namespace;
    
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
}
