package org.genemania.dao;

import java.util.List;

import org.genemania.domain.AttributeGroup;

public interface AttributeGroupDao {
    List<AttributeGroup> findAttributeGroupsByOrganism(long organismId);
}
