package org.genemania.dao;

import org.genemania.domain.Attribute;
import org.genemania.exception.DataStoreException;

public interface AttributeDao {
    Attribute findAttribute(long organismId, long attributeId) throws DataStoreException;
    boolean isValidAttribute(long organismId, long attributeId) throws DataStoreException;
}
