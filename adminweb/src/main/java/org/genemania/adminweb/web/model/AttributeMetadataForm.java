package org.genemania.adminweb.web.model;

public class AttributeMetadataForm {
    int networkId;
    int organismId;
    String linkoutLabel;
    String linkoutUrl;

    public int getNetworkId() {
        return networkId;
    }
    public void setNetworkId(int networkId) {
        this.networkId = networkId;
    }
    public int getOrganismId() {
        return organismId;
    }
    public void setOrganismId(int organismId) {
        this.organismId = organismId;
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
}
