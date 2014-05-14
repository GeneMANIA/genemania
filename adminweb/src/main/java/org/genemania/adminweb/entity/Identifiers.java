package org.genemania.adminweb.entity;

import org.genemania.adminweb.dao.impl.IdentifiersDaoImpl;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "IDENTIFIERS", daoClass = IdentifiersDaoImpl.class)
public class Identifiers {

    public static final String ORGANISM_ID_FIELD = "ORGANISM_ID";
    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(columnName = ORGANISM_ID_FIELD, canBeNull = false, foreign = true)
    private Organism organism;

    @DatabaseField(columnName = "DATA_FILE_ID", canBeNull = true, foreign = true)
    private DataFile dataFile;

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

    public DataFile getDataFile() {
        return dataFile;
    }

    public void setDataFile(DataFile dataFile) {
        this.dataFile = dataFile;
    }
}
