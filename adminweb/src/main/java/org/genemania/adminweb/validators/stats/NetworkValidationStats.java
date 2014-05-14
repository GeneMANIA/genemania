package org.genemania.adminweb.validators.stats;

import java.util.Date;
import java.util.List;

import org.genemania.type.DataLayout;

/*
 * information we record about the parsing
 * and processing of a given data file to
 * a network.
 */
public class NetworkValidationStats {

    String status;

    // for networks
    String processingDescription;
    long interactionCount;
    long normInteractionCount;
    long duplicateInteractions;
    List<String> invalidInteractions;
    long invalidCount;
    DataLayout dataLayout;
    Date processingDate;

    // attributes
    long numAttributes;
    long numAssociations;
    long numGenes;

    // linkout sample - attributes for now
    String sampleAccession;
    String sampleName;

    public String getProcessingDescription() {
        return processingDescription;
    }
    public void setProcessingDescription(String processingDescription) {
        this.processingDescription = processingDescription;
    }

    public long getInteractionCount() {
        return interactionCount;
    }
    public void setInteractionCount(long interactionCount) {
        this.interactionCount = interactionCount;
    }
    public long getNormInteractionCount() {
        return normInteractionCount;
    }
    public void setNormInteractionCount(long normInteractionCount) {
        this.normInteractionCount = normInteractionCount;
    }
    public long getDuplicateInteractions() {
        return duplicateInteractions;
    }
    public void setDuplicateInteractions(long duplicateInteractions) {
        this.duplicateInteractions = duplicateInteractions;
    }
    public List<String> getInvalidInteractions() {
        return invalidInteractions;
    }
    public void setInvalidInteractions(List<String> invalidInteractions) {
        this.invalidInteractions = invalidInteractions;
    }
    public Date getProcessingDate() {
        return processingDate;
    }
    public void setProcessingDate(Date processingDate) {
        this.processingDate = processingDate;
    }
    public void setDataLayout(DataLayout dataLayout) {
        this.dataLayout = dataLayout;
    }
    public DataLayout getDataLayout() {
        return dataLayout;
    }
    public long getInvalidCount() {
        return invalidCount;
    }
    public void setInvalidCount(long invalidCount) {
        this.invalidCount = invalidCount;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public long getNumAttributes() {
        return numAttributes;
    }
    public void setNumAttributes(long numAttributes) {
        this.numAttributes = numAttributes;
    }
    public long getNumAssociations() {
        return numAssociations;
    }
    public void setNumAssociations(long numAssociations) {
        this.numAssociations = numAssociations;
    }
    public long getNumGenes() {
        return numGenes;
    }
    public void setNumGenes(long numGenes) {
        this.numGenes = numGenes;
    }
    public String getSampleAccession() {
        return sampleAccession;
    }
    public void setSampleAccession(String sampleAccession) {
        this.sampleAccession = sampleAccession;
    }
    public String getSampleName() {
        return sampleName;
    }
    public void setSampleName(String sampleName) {
        this.sampleName = sampleName;
    }
}
