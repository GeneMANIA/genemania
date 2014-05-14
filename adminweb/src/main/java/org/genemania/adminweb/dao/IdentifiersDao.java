package org.genemania.adminweb.dao;

import java.util.List;

import org.genemania.adminweb.entity.Identifiers;
import org.genemania.adminweb.entity.Organism;

import com.j256.ormlite.dao.Dao;

public interface IdentifiersDao extends Dao<Identifiers, Integer> {

    public List<Identifiers> getIdentifiers(Organism organism);

}
