package org.genemania.dao.impl;

import org.genemania.connector.LuceneConnector;
import org.genemania.dao.AttributeDao;
import org.genemania.domain.Attribute;
import org.genemania.exception.DataStoreException;

import com.googlecode.ehcache.annotations.Cacheable;

public class LuceneAttributeDao implements AttributeDao {
    private LuceneConnector connector;

    public LuceneAttributeDao() {
        connector = LuceneConnector.getInstance();
    }

    @Override
    @Cacheable(cacheName = "attributeCache")
    public Attribute findAttribute(long organismId, long attributeId) throws DataStoreException {
        return connector.findAttribute(organismId, attributeId);
    }

    @Override
    @Cacheable(cacheName = "attributeIsValidCache")
    public boolean isValidAttribute(long organismId, long attributeId)
            throws DataStoreException {
        return connector.isValidAttribute(organismId, attributeId);
    }
 
}