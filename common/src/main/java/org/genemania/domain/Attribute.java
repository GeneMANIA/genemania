package org.genemania.domain;

import java.io.Serializable;

public class Attribute implements Serializable {
    private static final long serialVersionUID = -4547610789244414985L;
    
    private long id;
    private String externalId;
    private String name;
    private String description;

    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getExternalId() {
        return externalId;
    }
}
