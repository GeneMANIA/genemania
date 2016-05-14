package org.genemania.dao.impl;

import java.util.List;
import org.genemania.connector.LuceneConnector;
import org.genemania.dao.AttributeGroupDao;
import org.genemania.domain.AttributeGroup;
import com.googlecode.ehcache.annotations.Cacheable;

public class LuceneAttributeGroupDao implements AttributeGroupDao {
	private LuceneConnector connector;

	public LuceneAttributeGroupDao() {
		connector = LuceneConnector.getInstance();
	}

	@Override
	// @Cacheable(cacheName = "attributeGroupsByOrganismCache")
	public List<AttributeGroup> findAttributeGroupsByOrganism(long organismId) {
	    return connector.findAttributeGroupsByOrganism(organismId);
	}

}
