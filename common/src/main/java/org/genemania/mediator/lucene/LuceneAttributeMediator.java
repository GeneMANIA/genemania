package org.genemania.mediator.lucene;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.search.Searcher;
import org.genemania.domain.Attribute;
import org.genemania.domain.AttributeGroup;
import org.genemania.mediator.AttributeMediator;

/*
 * should say something about uniqueness of ids
 */
public class LuceneAttributeMediator extends LuceneMediator implements
        AttributeMediator {
    
    public LuceneAttributeMediator(Searcher searcher, Analyzer analyzer) {
        super(searcher, analyzer);
    }
    
    @Override
    public List hqlSearch(String queryString) {
        return null;
    }

    @Override
    public Attribute findAttribute(long organismId, long attributeId) {

        final Attribute[] attribute = new Attribute[1];
        
        search(String.format("+%s:\"%d\" +%s:\"%d\"", LuceneMediator.ATTRIBUTE_ORGANISM_ID, organismId, LuceneMediator.ATTRIBUTE_ID, attributeId), new AbstractCollector() {
            @Override
            public void handleHit(int doc) {
                try {
                    Document document = searcher.doc(doc);
                    attribute[0] = createAttribute(document);
                }
                catch (CorruptIndexException e) {
                    log(e);
                }
                catch (IOException e) {
                    log(e);
                }
            }
        });
        
        return attribute[0];        
    }

    protected Attribute createAttribute(Document document) {
        Attribute attribute = new Attribute();
        attribute.setId(Long.parseLong(document.get(LuceneMediator.ATTRIBUTE_ID)));
        attribute.setName(document.get(LuceneMediator.ATTRIBUTE_NAME));        
        attribute.setExternalId(document.get(LuceneMediator.ATTRIBUTE_EXTERNAL_ID));
        attribute.setDescription(document.get(LuceneMediator.ATTRIBUTE_DESCRIPTION));
        return attribute;
    }
    
    /*
     * validate an attribute
     * 
     */
    @Override
    public boolean isValidAttribute(long organismId, long attributeId) {
        final Long[] groupId = new Long[1];
        search(String.format("+%s:\"%d\"", LuceneMediator.ATTRIBUTE_ID, attributeId), new AbstractCollector() {
            @Override
            public void handleHit(int doc) {
                try {
                    Document document = searcher.doc(doc);
                    groupId[0] = Long.parseLong(document.get(LuceneMediator.ATTRIBUTE_ID));
                    
                }
                catch (CorruptIndexException e) {
                    log(e);
                }
                catch (IOException e) {
                    log(e);
                }
            }
        });
        
        if (groupId[0] == null) {
            return false;
        }

        final Long[] organism = new Long[1];
        search(String.format("+%s:\"%d\"", LuceneMediator.ATTRIBUTEGROUP_ID, groupId[0]), new AbstractCollector() {
            @Override
            public void handleHit(int doc) {
                try {
                    Document document = searcher.doc(doc);
                    organism[0] = Long.parseLong(document.get(LuceneMediator.ATTRIBUTEGROUP_ORGANISM_ID));
                } catch (CorruptIndexException e) {
                    log(e);
                } catch (IOException e) {
                    log(e);
                }
            }
        });
        
        if (organism[0] == null) {
            return false;
        }
        
        return organism[0].equals(organismId);
    }

    @Override
    public List<Attribute> findAttributesByGroup(long organismId,
            long attributeGroupId) {
        final List<Attribute> result = new ArrayList<Attribute>();

        search(String.format("+%s:\"%d\" +%s:\"%d\"", LuceneMediator.ATTRIBUTE_ORGANISM_ID, organismId, LuceneMediator.ATTRIBUTE_GROUP_ID, attributeGroupId), new AbstractCollector() {        
            @Override
            public void handleHit(int doc) throws IOException {
                try {
                    Document document = searcher.doc(doc);
                    result.add(createAttribute(document));
                } 
                catch (CorruptIndexException e) {
                    log(e);
                } 
                catch (IOException e) {
                    log(e);
                }
            }


        });
        return result;
    }

    @Override
    public List<AttributeGroup> findAttributeGroupsByOrganism(long organismId) {
        
        final List<AttributeGroup> result = new ArrayList<AttributeGroup>();
        search(String.format("+%s:\"%d\"", LuceneMediator.ATTRIBUTEGROUP_ORGANISM_ID, organismId), new AbstractCollector() {        
            @Override
            public void handleHit(int doc) throws IOException {
                try {
                    Document document = searcher.doc(doc);
                    result.add(createAttributeGroup(document));
                } 
                catch (CorruptIndexException e) {
                    log(e);
                } 
                catch (IOException e) {
                    log(e);
                }
            }


        });
        return result;
    }

    @Override
    public AttributeGroup findAttributeGroup(long organismId,
            long attributeGroupId) {
        final AttributeGroup[] attributeGroup = new AttributeGroup[1];

        search(String.format("+%s:\"%d\" +%s:\"%d\"", LuceneMediator.ATTRIBUTEGROUP_ID, attributeGroupId, LuceneMediator.ATTRIBUTEGROUP_ORGANISM_ID, organismId), new AbstractCollector() {
            public void handleHit(int doc) {
                try {   
                    Document document = searcher.doc(doc);
                    attributeGroup[0] = createAttributeGroup(document);
                }
                catch (CorruptIndexException e) {
                    log(e);
                }
                catch (IOException e) {
                    log(e);
                }
            }

        });

        return attributeGroup[0];
    }
    
    protected AttributeGroup createAttributeGroup(Document document) {
        AttributeGroup attributeGroup = new AttributeGroup();
        attributeGroup.setId(Long.parseLong(document.get(LuceneMediator.ATTRIBUTEGROUP_ID)));
        attributeGroup.setName(document.get(LuceneMediator.ATTRIBUTEGROUP_NAME));
        attributeGroup.setCode(document.get(LuceneMediator.ATTRIBUTEGROUP_CODE));
        attributeGroup.setDescription(document.get(LuceneMediator.ATTRIBUTEGROUP_DESCRIPTION));
        attributeGroup.setDefaultSelected(Boolean.parseBoolean(document.get(LuceneMediator.ATTRIBUTEGROUP_DEFAULT_SELECTED)));
        attributeGroup.setLinkoutLabel(document.get(LuceneMediator.ATTRIBUTEGROUP_LINKOUT_LABEL));
        attributeGroup.setLinkoutUrl(document.get(LuceneMediator.ATTRIBUTEGROUP_LINKOUT_URL));
        attributeGroup.setPublicationName(document.get(LuceneMediator.ATTRIBUTEGROUP_PUBLICATION_NAME));
        attributeGroup.setPublicationUrl(document.get(LuceneMediator.ATTRIBUTEGROUP_PUBLICATION_URL));
        return attributeGroup;
    }


}
