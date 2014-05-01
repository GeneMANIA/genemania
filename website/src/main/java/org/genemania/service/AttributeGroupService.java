package org.genemania.service;

import java.util.Collection;

import org.genemania.domain.AttributeGroup;
import org.genemania.exception.DataStoreException;

public interface AttributeGroupService {

	// TODO shouldn't require organism
	public AttributeGroup findAttributeGroup(long organismId, long groupId)
			throws DataStoreException;

	public Collection<AttributeGroup> findAttributeGroups(long organismId)
			throws DataStoreException;

	public Collection<AttributeGroup> findAttributeGroups(long organismId,
			Long[] groupIds) throws DataStoreException;

	public Collection<AttributeGroup> findDefaultAttributeGroups(long organismId)
			throws DataStoreException;
}
