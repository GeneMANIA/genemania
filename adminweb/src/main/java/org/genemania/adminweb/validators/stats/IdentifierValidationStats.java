package org.genemania.adminweb.validators.stats;

import java.util.List;

public class IdentifierValidationStats {

    String status;
    int numRecordsRead;
    int numMissing;
    int numDups;
    int numIds;
    int numSymbols;
    int numSources;
    List<Object[]> sourceCounts;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getNumRecordsRead() {
        return numRecordsRead;
    }

    public void setNumRecordsRead(int numRecordsRead) {
        this.numRecordsRead = numRecordsRead;
    }

    public int getNumMissing() {
        return numMissing;
    }

    public void setNumMissing(int numMissing) {
        this.numMissing = numMissing;
    }

    public int getNumDups() {
        return numDups;
    }

    public void setNumDups(int numDups) {
        this.numDups = numDups;
    }

    public int getNumIds() {
        return numIds;
    }

    public void setNumIds(int numIds) {
        this.numIds = numIds;
    }

    public int getNumSymbols() {
        return numSymbols;
    }

    public void setNumSymbols(int numSymbols) {
        this.numSymbols = numSymbols;
    }

    public int getNumSources() {
        return numSources;
    }

    public void setNumSources(int numSources) {
        this.numSources = numSources;
    }

    public List<Object[]> getSourceCounts() {
        return sourceCounts;
    }

    public void setSourceCounts(List<Object[]> sourceCounts) {
        this.sourceCounts = sourceCounts;
    }
}
