package org.genemania.domain;

import java.io.Serializable;

public class AttributeGroup implements Serializable {
	private static final long serialVersionUID = 442201345750196970L;

	private long id;
	private String name;
	private String description;
	private boolean defaultSelected;
	private String code;
	private String linkoutLabel;
    private String linkoutUrl; // this is a template used to create per-attribute urls
    private String publicationName;
    private String publicationUrl;	

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

	public boolean isDefaultSelected() {
		return defaultSelected;
	}

	public void setDefaultSelected(boolean defaultSelected) {
		this.defaultSelected = defaultSelected;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
    
	public String getLinkoutLabel() {
        return linkoutLabel;
    }

    public void setLinkoutLabel(String linkoutLabel) {
        this.linkoutLabel = linkoutLabel;
    }

    public String getLinkoutUrl() {
        return linkoutUrl;
    }
    
    public void setLinkoutUrl(String linkoutUrl) {
        this.linkoutUrl = linkoutUrl;
    }
    
    public String getPublicationName() {
        return publicationName;
    }
    
    public void setPublicationName(String publicationName) {
        this.publicationName = publicationName;
    }
    
    public String getPublicationUrl() {
        return publicationUrl;
    }
    
    public void setPublicationUrl(String publicationUrl) {
        this.publicationUrl = publicationUrl;
    }
}
