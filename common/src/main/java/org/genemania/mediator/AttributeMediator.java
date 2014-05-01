package org.genemania.mediator;

import java.util.List;

import org.genemania.domain.Attribute;
import org.genemania.domain.AttributeGroup;

/*
 * access to attribute & attribute group domain objects.
 * 
 * what are the no-result semantics here? exception, null, 
 * empty collection?
 */
public interface AttributeMediator extends BaseMediator {
    Attribute findAttribute(long organism, long attributeId);
    boolean isValidAttribute(long organismId, long attributeId);
    List<Attribute> findAttributesByGroup(long organismId, long attributeGroupId);

    List<AttributeGroup> findAttributeGroupsByOrganism(long organismId);
    AttributeGroup findAttributeGroup(long organismId, long attributeGroupId);
    
    // do we need:
    //
    //    AttributeGroup findAttributeGroupByName(long organismId, String name);
    //    List<AttributeGroup> findDefaultAttributeGroupsForOrganism(long organismId);
    
}
