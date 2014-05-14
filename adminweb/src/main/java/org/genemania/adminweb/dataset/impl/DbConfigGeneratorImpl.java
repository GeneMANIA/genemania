package org.genemania.adminweb.dataset.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.genemania.adminweb.dao.DatamartDb;
import org.genemania.adminweb.dataset.DatamartToGenericDb;
import org.genemania.adminweb.dataset.DbConfigGenerator;
import org.genemania.adminweb.entity.Organism;
import org.genemania.adminweb.exception.DatamartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/*
 * produce the build config file db.cfg based on adminweb db.
 */
@Component
public class DbConfigGeneratorImpl implements DbConfigGenerator {

	@Autowired
	DatamartDb dmdb;

	@Override
	public String makeConfig() throws DatamartException {
		StringBuilder builder = new StringBuilder("# Auto generated config\n\n");

		try {
	        List<Organism> organisms = dmdb.getOrganismDao().queryForAll();
	        addOrganisms(builder, organisms);
		}
		catch (SQLException e) {
		    throw new DatamartException("failed to make config", e);
		}

		return builder.toString();
	}

    @Override
    public String makeConfig(long organismId) throws DatamartException {
        StringBuilder builder = new StringBuilder("# Auto generated config\n\n");

        try {
            Organism organism = dmdb.getOrganismDao().queryForId((int)organismId);
            ArrayList<Organism> organisms = new ArrayList<Organism>();
            organisms.add(organism);
            addOrganisms(builder, organisms);
        }
        catch (SQLException e) {
            throw new DatamartException("failed to make config", e);
        }

        return builder.toString();
    }

    void addOrganisms(StringBuilder builder, List<Organism> organisms) {
        addOrganismsList(builder, organisms);

        for (Organism organism: organisms) {
            addOrganismSection(builder, organism);
        }
    }

	void addOrganismSection(StringBuilder builder, Organism organism) {
		builder.append("[" + organism.getCode() + "]\n");
		builder.append("name = " + organism.getName() + "\n");
		builder.append("short_name = " + DatamartToGenericDb.makeShortName(organism.getName()) + "\n");
	    builder.append("short_id = " + organism.getCode() + "\n");
		builder.append("common_name = " + organism.getName() + "\n");
		builder.append("gm_organism_id = " + organism.getId() + "\n");
	}

	void addOrganismsList(StringBuilder builder, List<Organism> organisms) {
	    builder.append("[Organisms]\n");
	    builder.append("organisms = ");
	    for (Organism organism: organisms) {
	        builder.append(organism.getCode() + ",");
	    }
	    builder.append("\n");
	}

	public DatamartDb getDatamartDb() {
		return dmdb;
	}

	public void setDatamartDb(DatamartDb dmdb) {
		this.dmdb = dmdb;
	}
}
