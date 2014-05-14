package org.genemania.adminweb.dao.impl;

import java.sql.SQLException;

import org.genemania.adminweb.dao.FunctionsDao;
import org.genemania.adminweb.entity.Functions;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;

public class FunctionsDaoImpl extends BaseDaoImpl<Functions, Integer> implements FunctionsDao {
    public FunctionsDaoImpl(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, Functions.class);
    }
}
