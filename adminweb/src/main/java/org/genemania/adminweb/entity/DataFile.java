package org.genemania.adminweb.entity;

import java.util.Date;

import org.genemania.adminweb.dao.impl.DataFileDaoImpl;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "DATAFILES", daoClass = DataFileDaoImpl.class)
public class DataFile {


    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(columnName = "ORGANISM_ID", canBeNull = false, foreign = true)
    private Organism organism;

    @DatabaseField(columnName = "FILENAME", canBeNull = true, width=128)
    private String filename;

    @DatabaseField(columnName = "ORIGINAL_FILENAME", canBeNull = true, width=128)
    private String originalFilename;

    // identifier, network etc
    @DatabaseField(columnName = "FILE_TYPE", canBeNull = true, width=16)
    private String fileType;

    @DatabaseField(columnName = "UPLOAD_DATE")
    private Date uploadDate;

    // system status flag, validated, failed validation, etc. TODO define
    @DatabaseField(columnName = "STATUS", canBeNull = true, width=16)
    private String status;

    @DatabaseField(columnName = "PROCESSING_DETAILS", canBeNull = true, width = 4096)
    private String processingDetails;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Organism getOrganism() {
        return organism;
    }

    public void setOrganism(Organism organism) {
        this.organism = organism;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public Date getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(Date uploadDate) {
        this.uploadDate = uploadDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    public String getProcessingDetails() {
        return processingDetails;
    }

    public void setProcessingDetails(String processingDetails) {
        this.processingDetails = processingDetails;
    }

}
