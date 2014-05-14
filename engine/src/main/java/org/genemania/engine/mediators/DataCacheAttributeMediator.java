package org.genemania.engine.mediators;

import org.genemania.domain.Attribute;
import org.genemania.domain.AttributeGroup;
import org.genemania.engine.cache.DataCache;
import org.genemania.engine.core.data.AttributeGroups;
import org.genemania.engine.core.data.Data;
import org.genemania.exception.ApplicationException;
import org.genemania.exception.DataStoreException;
import org.genemania.mediator.AttributeMediator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DataCacheAttributeMediator implements AttributeMediator {
    private DataCache cache;

    public DataCacheAttributeMediator(DataCache cache) {
        this.cache = cache;
    }

    @Override
    public Attribute findAttribute(long organism, long attributeId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isValidAttribute(long organismId, long attributeId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Attribute> findAttributesByGroup(long organismId, long attributeGroupId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<AttributeGroup> findAttributeGroupsByOrganism(long organismId) {

        AttributeGroups attributeGroups = null;
        try {
            attributeGroups = cache.getAttributeGroups(Data.CORE, organismId);
        }
        catch (ApplicationException e) {
            return null;
        }

        HashMap<Long, ArrayList<Long>> ids = attributeGroups.getAttributeGroups();

        ArrayList<AttributeGroup> result = new ArrayList<AttributeGroup>();
        for (long groupId: ids.keySet()) {
            AttributeGroup g = new AttributeGroup();
            g.setId(groupId);
            result.add(g);
        }

        return result;
    }

    @Override
    public AttributeGroup findAttributeGroup(long organismId, long attributeGroupId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List hqlSearch(String queryString) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
