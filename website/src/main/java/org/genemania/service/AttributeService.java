package org.genemania.service;

import org.genemania.domain.Attribute;
import org.genemania.exception.DataStoreException;

/** 
 * Access to attribute domain data
 */
public interface AttributeService {

	public Attribute findAttribute(long organismId, long attributeId) throws DataStoreException;
	
}
