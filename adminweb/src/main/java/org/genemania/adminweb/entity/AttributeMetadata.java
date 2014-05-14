package org.genemania.adminweb.entity;

import org.genemania.adminweb.dao.impl.AttributeMetadataDaoImpl;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "ATTRIBUTE_METADATA", daoClass = AttributeMetadataDaoImpl.class)
public class AttributeMetadata {

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(columnName = "DATA_FILE_ID", canBeNull = true, foreign = true)
    private DataFile dataFile;

    @DatabaseField(columnName = "LINKOUT_URL", canBeNull = true)
    private String linkoutUrl;

    @DatabaseField(columnName = "LINKOUT_LABEL", canBeNull = true)
    private String linkoutLabel;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public DataFile getDataFile() {
        return dataFile;
    }

    public void setDataFile(DataFile dataFile) {
        this.dataFile = dataFile;
    }

}
