package org.genemania.adminweb.dao.impl;

import java.sql.SQLException;

import org.genemania.adminweb.dao.DataFileDao;
import org.genemania.adminweb.entity.DataFile;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;

public class DataFileDaoImpl extends BaseDaoImpl <DataFile, Integer> implements DataFileDao {

    public DataFileDaoImpl (ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, DataFile.class);
    }
}
