package org.genemania.adminweb.entity;

import org.genemania.adminweb.dao.impl.FunctionsDaoImpl;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "FUNCTIONS", daoClass = FunctionsDaoImpl.class)
public class Functions {
    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(columnName = "DATA_FILE_ID", canBeNull = true, foreign = true)
    private DataFile dataFile;

    @DatabaseField(columnName = "DESCRIPTION_FILE_ID", canBeNull= true, foreign = true)
    private DataFile descriptionFile;

    // BP, CC, MF for combining, ENRICHMENT for enrichment analysis
    @DatabaseField(columnName = "FUNCTION_TYPE", canBeNull = false, width = 16)
    private String functionType;

    @DatabaseField(columnName = "COMMENT", canBeNull = true, width = 2048)
    private String comment;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public DataFile getDataFile() {
        return dataFile;
    }

    public void setDataFile(DataFile dataFile) {
        this.dataFile = dataFile;
    }

    public DataFile getDescriptionFile() {
        return descriptionFile;
    }

    public void setDescriptionFile(DataFile descriptionFile) {
        this.descriptionFile = descriptionFile;
    }

    public String getFunctionType() {
        return functionType;
    }

    public void setFunctionType(String functionType) {
        this.functionType = functionType;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
