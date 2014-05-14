package org.genemania.adminweb.entity;

import org.genemania.adminweb.dao.impl.FormatDaoImpl;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "FORMATS", daoClass = FormatDaoImpl.class)
public class Format {

    public static final String NAME_FIELD = "NAME";
    public static final String DESCRIPTION_FIELD = "DESCRIPTION";

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(columnName = NAME_FIELD, canBeNull = false)
    private String name;

    @DatabaseField(columnName = DESCRIPTION_FIELD, canBeNull = true, width = 128)
    private String description;

    public Format() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Format other = (Format) obj;
        if (id != other.id)
            return false;
        return true;
    }
}
