package org.genemania.adminweb.dao.impl;

import java.sql.SQLException;

import org.genemania.adminweb.dao.FormatDao;
import org.genemania.adminweb.entity.Format;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;

public class FormatDaoImpl extends BaseDaoImpl <Format, Integer> implements FormatDao {

    public FormatDaoImpl (ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, Format.class);
    }
}
