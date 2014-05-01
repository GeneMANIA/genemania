package org.genemania.service.impl;

import org.genemania.dao.AttributeDao;
import org.genemania.domain.Attribute;
import org.genemania.exception.DataStoreException;
import org.genemania.service.AttributeService;
import org.springframework.beans.factory.annotation.Autowired;

public class AttributeServiceImpl implements AttributeService {

	@Autowired
	AttributeDao attributeDao;

	public Attribute findAttribute(long organismId, long attributeId) throws DataStoreException {
		return attributeDao.findAttribute(organismId, attributeId);
	}

	public AttributeDao getAttributeDao() {
		return attributeDao;
	}

	public void setAttributeDao(AttributeDao attributeDao) {
		this.attributeDao = attributeDao;
	}

}
