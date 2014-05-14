package org.genemania.adminweb.dao.impl;

import java.sql.SQLException;

import org.genemania.adminweb.dao.OrganismDao;
import org.genemania.adminweb.entity.Organism;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;

public class OrganismDaoImpl extends BaseDaoImpl <Organism, Integer> implements OrganismDao {

    public OrganismDaoImpl(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, Organism.class);
    }
}
