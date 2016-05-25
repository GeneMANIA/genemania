package org.genemania.dao.impl;

import org.genemania.connector.LuceneConnector;
import org.genemania.dao.AttributeDao;
import org.genemania.domain.Attribute;
import org.genemania.exception.DataStoreException;
import org.springframework.cache.annotation.Cacheable;

public class LuceneAttributeDao implements AttributeDao {
	private LuceneConnector connector;

	public LuceneAttributeDao() {
		connector = LuceneConnector.getInstance();
	}

	@Override
	@Cacheable("attributeCache")
	public Attribute findAttribute(long organismId, long attributeId) throws DataStoreException {
		return connector.findAttribute(organismId, attributeId);
	}

	@Override
	@Cacheable("attributeIsValidCache")
	public boolean isValidAttribute(long organismId, long attributeId) throws DataStoreException {
		return connector.isValidAttribute(organismId, attributeId);
	}

}