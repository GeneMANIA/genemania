package org.genemania.adminweb.web.model;

/*
 * could we just reuse the networkTN object
 * for the form fields instead of creating
 * yet another thing?
 */
public class FunctionsForm {
    int functionsId;
    int organismId;
    String name;
    String description;
    String comment;
    long pubmedId;
    boolean isDefault;
    boolean isRestrictedLicense;
    boolean isEnabled;
    public String usage;

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
    public int getFunctionsId() {
        return functionsId;
    }
    public void setFunctionsId(int functionsId) {
        this.functionsId = functionsId;
    }
    public int getOrganismId() {
        return organismId;
    }
    public void setOrganismId(int organismId) {
        this.organismId = organismId;
    }
    public String getComment() {
        return comment;
    }
    public void setComment(String comment) {
        this.comment = comment;
    }
    public long getPubmedId() {
        return pubmedId;
    }
    public void setPubmedId(long pubmedId) {
        this.pubmedId = pubmedId;
    }
    public boolean isDefault() {
        return isDefault;
    }
    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }
    public boolean isRestrictedLicense() {
        return isRestrictedLicense;
    }
    public void setRestrictedLicense(boolean isRestrictedLicense) {
        this.isRestrictedLicense = isRestrictedLicense;
    }
    public boolean isEnabled() {
        return isEnabled;
    }
    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }
    public String getUsage() {
        return usage;
    }
    public void setUsage(String usage) {
        this.usage = usage;
    }
}
