package org.genemania.adminweb.dataset;

public class DataSetContext {
    public String buildIdentifier;  // eg R3B2 or something
    public String basePath;
    public String genericDbPath;
    public String indexPath;
    public String cachePath;

    public String getBuildIdentifier() {
        return buildIdentifier;
    }
    public void setBuildIdentifier(String buildIdentifier) {
        this.buildIdentifier = buildIdentifier;
    }
    public String getBasePath() {
        return basePath;
    }
    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }
    public String getIndexPath() {
        return indexPath;
    }
    public void setIndexPath(String indexPath) {
        this.indexPath = indexPath;
    }
    public String getCachePath() {
        return cachePath;
    }
    public void setCachePath(String cachePath) {
        this.cachePath = cachePath;
    }
    public String getGenericDbPath() {
        return genericDbPath;
    }
    public void setGenericDbPath(String genericDbPath) {
        this.genericDbPath = genericDbPath;
    }
}
