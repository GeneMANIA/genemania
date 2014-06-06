package org.genemania.adminweb.web.model;

import org.genemania.adminweb.validators.stats.IdentifierValidationStats;

public class IdentifiersTN extends TreeNode {
    public static final String NODETYPE = "identifiers";

    private int id;
    private String filename;
    private int fileId;
    private int organismId;
    private String date;
    private IdentifierValidationStats stats;

    public IdentifiersTN(String title) {
        super(title);
        setType(NODETYPE);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

	public int getOrganismId() {
		return organismId;
	}

	public void setOrganismId(int organismId) {
		this.organismId = organismId;
	}

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public String getKey() {
        return String.format("o=%d:i=%d", getOrganismId(), getId());
    }

    public int getFileId() {
        return fileId;
    }

    public void setFileId(int fileId) {
        this.fileId = fileId;
    }

    public IdentifierValidationStats getStats() {
        return stats;
    }

    public void setStats(IdentifierValidationStats stats) {
        this.stats = stats;
    }
}
