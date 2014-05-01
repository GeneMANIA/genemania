package org.genemania.service.impl;

import java.util.Collection;
import java.util.LinkedList;

import org.genemania.dao.AttributeGroupDao;
import org.genemania.domain.AttributeGroup;
import org.genemania.exception.DataStoreException;
import org.genemania.service.AttributeGroupService;
import org.springframework.beans.factory.annotation.Autowired;

public class AttributeGroupServiceImpl implements AttributeGroupService {

	@Autowired
	private AttributeGroupDao attributeGroupDao;

	public Collection<AttributeGroup> findDefaultAttributeGroups(
			long organismId) throws DataStoreException {
		
		Collection<AttributeGroup> allGrs = attributeGroupDao.findAttributeGroupsByOrganism(organismId);
		Collection<AttributeGroup> defGrs = new LinkedList<AttributeGroup>();
		
		for(AttributeGroup gr : allGrs){
			if( gr.isDefaultSelected() ){
				defGrs.add(gr);
			}
		}
		
		return defGrs;
	}

	public AttributeGroup findAttributeGroup(long organismId, long groupId)
			throws DataStoreException {
		for (AttributeGroup g : attributeGroupDao
				.findAttributeGroupsByOrganism(organismId)) {
			if (g.getId() == groupId) {
				return g;
			}
		}

		return null;
	}

	public Collection<AttributeGroup> findAttributeGroups(long organismId,
			Long[] groupIds) throws DataStoreException {

		Collection<AttributeGroup> ret = new LinkedList<AttributeGroup>();

		if (groupIds != null) {
			for (Long id : groupIds) {
				if( id != null ){
					AttributeGroup gr = this.findAttributeGroup(organismId, id);
	
					if (gr != null) {
						ret.add(gr);
					}
				}
			}
		}
	

		return ret;

	}

	public Collection<AttributeGroup> findAttributeGroups(long organismId)
			throws DataStoreException {
		return attributeGroupDao.findAttributeGroupsByOrganism(organismId);
	}

	public AttributeGroupDao getAttributeGroupDao() {
		return attributeGroupDao;
	}

	public void setAttributeGroupDao(AttributeGroupDao attributeGroupDao) {
		this.attributeGroupDao = attributeGroupDao;
	}

}
