package org.genemania.adminweb.entity;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import org.genemania.adminweb.dao.impl.DbVersionDaoImpl;

/**
 * records a version identifier for the database within the database itself,
 * to be used by e.g. migration scripts.
 */
@DatabaseTable(tableName = "DB_VERSION", daoClass = DbVersionDaoImpl.class)
public class DbVersion {

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(columnName = "VERSION_ID", canBeNull = false)
    private String versionId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getVersionId() {
        return versionId;
    }

    public void setVersionId(String versionId) {
        this.versionId = versionId;
    }
}
