package org.genemania.adminweb.entity;

import org.genemania.adminweb.dao.impl.OrganismDaoImpl;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "ORGANISMS", daoClass = OrganismDaoImpl.class)
public class Organism {

    @DatabaseField(generatedId = true)
    private int id;
    
    @DatabaseField(columnName = "NAME", canBeNull = false)
    private String name;
        
    @DatabaseField(columnName = "CODE", canBeNull = false, unique = true)
    private String code;
    
    public Organism() {}

    public Organism(String name, String code) {
        this.name = name;
        this.code = code;
    }

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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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
        Organism other = (Organism) obj;
        if (id != other.id)
            return false;
        return true;
    }
}
