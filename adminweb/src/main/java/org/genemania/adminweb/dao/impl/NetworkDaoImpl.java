package org.genemania.adminweb.dao.impl;

import java.sql.SQLException;
import java.util.List;

import org.genemania.adminweb.dao.NetworkDao;
import org.genemania.adminweb.entity.Group;
import org.genemania.adminweb.entity.Network;
import org.genemania.adminweb.entity.Organism;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;

public class NetworkDaoImpl extends BaseDaoImpl <Network, Integer> implements NetworkDao {
    final Logger logger = LoggerFactory.getLogger(NetworkDaoImpl.class);

    public NetworkDaoImpl(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, Network.class);
    }

    @Override
    public List<Network> getNetworks(Organism organism) {
        try {
            QueryBuilder<Network, Integer> queryBuilder = queryBuilder();
            queryBuilder.where().eq(Network.ORGANISM_ID_FIELD, organism.getId());
            List<Network> result = queryBuilder.query();
            return result;
        }
        catch (SQLException e) {
            System.out.println(e);
            return null;
        }
     }

    @Override
    public List<Network> getNetworks(Organism organism, Group group) {
        try {
            QueryBuilder<Network, Integer> queryBuilder = queryBuilder();
            queryBuilder.where()
            .eq(Network.ORGANISM_ID_FIELD, organism.getId())
            .and()
            .eq(Network.GROUP_ID_FIELD, group.getId());

            List<Network> result = queryBuilder.query();
            return result;
        }
        catch (SQLException e) {
            logger.error("failed to get networks", e);
            return null;
        }
    }

    /*
     * we don't require that network names are unique at the database level (they
     * can be disambiguated during a processing step). so the result is a list.
     *
     * (non-Javadoc)
     * @see org.genemania.adminweb.dao.NetworkDao#getNetworks(org.genemania.adminweb.entity.Organism, java.lang.String)
     */
    @Override
    public List<Network> getNetworks(Organism organism, String name) {
        try {
            QueryBuilder<Network, Integer> queryBuilder = queryBuilder();
            queryBuilder.where()
            .eq(Network.ORGANISM_ID_FIELD, organism.getId())
            .and()
            .eq(Network.NAME_FIELD, name);

            List<Network> result = queryBuilder.query();
            return result;
        }
        catch (SQLException e) {
            logger.error("failed to get networks", e);
            return null;
        }
    }
}
