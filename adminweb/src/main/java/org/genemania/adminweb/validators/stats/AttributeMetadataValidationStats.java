package org.genemania.adminweb.validators.stats;

public class AttributeMetadataValidationStats {
    int metadataRecordCount;
    String sampleAccession;
    String sampleName;

    public int getMetadataRecordCount() {
        return metadataRecordCount;
    }
    public void setMetadataRecordCount(int metadataRecordCount) {
        this.metadataRecordCount = metadataRecordCount;
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
