package org.genemania.adminweb.dao.impl;

import java.sql.SQLException;

import org.genemania.adminweb.dao.AttributeMetadataDao;
import org.genemania.adminweb.entity.AttributeMetadata;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;

public class AttributeMetadataDaoImpl extends BaseDaoImpl<AttributeMetadata, Integer> implements AttributeMetadataDao {

    public AttributeMetadataDaoImpl(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, AttributeMetadata.class);
    }
}
