package org.genemania.adminweb.dao.impl;

import java.sql.SQLException;
import java.util.List;

import org.genemania.adminweb.dao.IdentifiersDao;
import org.genemania.adminweb.entity.Identifiers;
import org.genemania.adminweb.entity.Organism;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;

public class IdentifiersDaoImpl extends BaseDaoImpl<Identifiers, Integer> implements IdentifiersDao {
    final Logger logger = LoggerFactory.getLogger(IdentifiersDaoImpl.class);

    public IdentifiersDaoImpl(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, Identifiers.class);
    }

    @Override
    public List<Identifiers> getIdentifiers(Organism organism) {
        try {
            QueryBuilder<Identifiers, Integer> queryBuilder = queryBuilder();
            queryBuilder.where()
            .eq(Identifiers.ORGANISM_ID_FIELD, organism.getId());

            List<Identifiers> result = queryBuilder.query();
            return result;
        }
        catch (SQLException e) {
            logger.error("failed to get identifiers", e);
            return null;
        }
    }
}
