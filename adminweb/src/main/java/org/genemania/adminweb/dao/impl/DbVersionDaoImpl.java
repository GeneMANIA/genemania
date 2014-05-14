package org.genemania.adminweb.dao.impl;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;
import org.genemania.adminweb.dao.DbVersionDao;
import org.genemania.adminweb.entity.DbVersion;

import java.sql.SQLException;

public class DbVersionDaoImpl extends BaseDaoImpl<DbVersion, Integer> implements DbVersionDao {

    public DbVersionDaoImpl(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, DbVersion.class);
    }
}
