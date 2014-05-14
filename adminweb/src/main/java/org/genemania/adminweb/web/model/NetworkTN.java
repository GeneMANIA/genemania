package org.genemania.adminweb.web.model;

import org.genemania.adminweb.service.impl.PubmedServiceImpl.PubmedInfo;
import org.genemania.adminweb.validators.stats.AttributeMetadataValidationStats;
import org.genemania.adminweb.validators.stats.NetworkValidationStats;


/*
 * tree node representing a network
 */
public class NetworkTN extends TreeNode {
    public static final String NODETYPE = "network";

    private int id;
    private String filename;
    private int fileId;
    private String pubmedId;
    private String description;
    private int organismId;
    private String comment;
    private boolean isDefaultSelected = false;
    private boolean isRestrictedLicense = false;
    private boolean isEnabled;
    private String date;
    private PubmedInfo extra; // TODO: should be a more generic metadata structure

    // used by attributes
    private boolean isAttributeNetwork = false;
    private String linkoutUrl;
    private String linkoutLabel;
    private String metadataFilename;
    private int metadataFileId;

    // hold misc fields related to data validation
    private NetworkValidationStats processingDetails;
    private AttributeMetadataValidationStats metadataProcessingDetails;

    public NetworkTN(String title) {
        super(title);
        setType(NODETYPE);
    }

    public String getPubmedId() {
        return pubmedId;
    }

    public void setPubmedId(String pubmedId) {
        this.pubmedId = pubmedId;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOrganismId() {
        return organismId;
    }

    public void setOrganismId(int organismId) {
        this.organismId = organismId;
    }

    public boolean isRestrictedLicense() {
        return isRestrictedLicense;
    }

    public void setRestrictedLicense(boolean isRestrictedLicense) {
        this.isRestrictedLicense = isRestrictedLicense;
    }

    public boolean isDefaultSelected() {
        return isDefaultSelected;
    }

    public void setDefaultSelected(boolean isDefaultSelected) {
        this.isDefaultSelected = isDefaultSelected;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String getKey() {
        return String.format("o=%d:n=%d", getOrganismId(), getId());
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public NetworkValidationStats getProcessingDetails() {
        return processingDetails;
    }

    public void setProcessingDetails(NetworkValidationStats processingDetails) {
        this.processingDetails = processingDetails;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public PubmedInfo getExtra() {
        return extra;
    }

    public void setExtra(PubmedInfo extra) {
        this.extra = extra;
    }

    public boolean isAttributeNetwork() {
        return isAttributeNetwork;
    }

    public void setAttributeNetwork(boolean isAttributeNetwork) {
        this.isAttributeNetwork = isAttributeNetwork;
    }

    public String getLinkoutUrl() {
        return linkoutUrl;
    }

    public void setLinkoutUrl(String linkoutUrl) {
        this.linkoutUrl = linkoutUrl;
    }

    public String getLinkoutLabel() {
        return linkoutLabel;
    }

    public void setLinkoutLabel(String linkoutLabel) {
        this.linkoutLabel = linkoutLabel;
    }

    public String getMetadataFilename() {
        return metadataFilename;
    }

    public void setMetadataFilename(String metadataFilename) {
        this.metadataFilename = metadataFilename;
    }
    public AttributeMetadataValidationStats getMetadataProcessingDetails() {
        return metadataProcessingDetails;
    }

    public void setMetadataProcessingDetails(
            AttributeMetadataValidationStats metadataProcessingDetails) {
        this.metadataProcessingDetails = metadataProcessingDetails;
    }

    public int getFileId() {
        return fileId;
    }

    public void setFileId(int fileId) {
        this.fileId = fileId;
    }

    public int getMetadataFileId() {
        return metadataFileId;
    }

    public void setMetadataFileId(int metadataFileId) {
        this.metadataFileId = metadataFileId;
    }
}
