package org.genemania.adminweb.web.model;

import org.genemania.adminweb.validators.stats.NetworkValidationStats;

public class FunctionsTN extends TreeNode {
    public static final String NODETYPE = "functions_node";

    private int id;
    private int organismId;
    private String filename;
    private String date;
    private String comment;
    private int fileId;
    private String usage;

    private NetworkValidationStats processingDetails;

    public FunctionsTN(String title) {
        super(title);
        setType(NODETYPE);
    }

    @Override
    public String getKey() {
        return String.format("o=%d:f=%d", getOrganismId(), getId());
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

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }


    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public int getFileId() {
        return fileId;
    }

    public void setFileId(int fileId) {
        this.fileId = fileId;
    }



    public NetworkValidationStats getProcessingDetails() {
        return processingDetails;
    }

    public void setProcessingDetails(NetworkValidationStats processingDetails) {
        this.processingDetails = processingDetails;
    }

    public String getUsage() {
        return usage;
    }

    public void setUsage(String usage) {
        this.usage = usage;
    }
}
