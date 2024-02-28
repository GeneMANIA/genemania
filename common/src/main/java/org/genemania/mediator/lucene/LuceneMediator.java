/**
 * This file is part of GeneMANIA.
 * Copyright (C) 2010 University of Toronto.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.genemania.mediator.lucene;

import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.genemania.domain.Gene;
import org.genemania.domain.GeneData;
import org.genemania.domain.GeneNamingSource;
import org.genemania.domain.Interaction;
import org.genemania.domain.InteractionNetwork;
import org.genemania.domain.InteractionNetworkGroup;
import org.genemania.domain.NetworkMetadata;
import org.genemania.domain.Node;
import org.genemania.domain.Ontology;
import org.genemania.domain.OntologyCategory;
import org.genemania.domain.Organism;
import org.genemania.domain.Statistics;
import org.genemania.domain.Tag;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.LazyLoader;

public class LuceneMediator {
    private static final String DEFAULT_FIELD                     = "all";
    protected Searcher          searcher;
    protected Analyzer          analyzer;

    public static final String  NODE_ID                           = "node_id";
    public static final String  NODE_NAME                         = "node_name";
    public static final String  NODE_GENEDATA_ID                  = "node_genedata_id";
    public static final String  NODE_ORGANISM_ID                  = "node_organism_id";

    public static final String  GENE_ID                           = "gene_id";
    public static final String  GENE_NODE_ID                      = "node";
    public static final String  GENE_ORGANISM_ID                  = "gene_organism_id";
    public static final String  GENE_SYMBOL                       = "gene";
    public static final String  GENE_NAMINGSOURCE_ID              = "gene_namingsource_id";
    public static final String  GENE_DEFAULT_SELECTED             = "gene_selected";

    public static final String  GROUP_ID                          = "group_id";
    public static final String  GROUP_ORGANISM_ID                 = "group_organism_id";
    public static final String  GROUP_NAME                        = "group_name";
    public static final String  GROUP_CODE                        = "group_code";
    public static final String  GROUP_DESCRIPTION                 = "group_description";
    public static final String  GROUP_COLOUR                      = "group_colour";

    public static final String  GENEDATA_ID                       = "genedata_id";
    public static final String  GENEDATA_DESCRIPTION              = "genedata_desc";
    public static final String  GENEDATA_EXTERNAL_ID              = "genedata_external_id";
    public static final String  GENEDATA_NAMINGSOURCE_ID          = "genedata_namingsource_id";

    public static final String  NAMINGSOURCE_ID                   = "namingsource_id";
    public static final String  NAMINGSOURCE_NAME                 = "namingsource_name";
    public static final String  NAMINGSOURCE_RANK                 = "namingsource_rank";
    public static final String  NAMINGSOURCE_SHORT_NAME           = "namingsource_short_name";

    public static final String  ORGANISM_ID                       = "organism_id";
    public static final String  ORGANISM_NAME                     = "organism_name";
    public static final String  ORGANISM_DESCRIPTION              = "organism_desc";
    public static final String  ORGANISM_ALIAS                    = "organism_alias";
    public static final String  ORGANISM_ONTOLOGY_ID              = "organism_o_id";
    public static final String  ORGANISM_TAXONOMY_ID              = "organism_tax_id";

    public static final String  NETWORK_ID                        = "network_id";
    public static final String  NETWORK_GROUP_ID                  = "network_group_id";
    public static final String  NETWORK_NAME                      = "network_name";
    public static final String  NETWORK_DESCRIPTION               = "network_desc";
    public static final String  NETWORK_DEFAULT_SELECTED          = "network_selected";
    public static final String  NETWORK_METADATA_ID               = "network_metadata_id";

    public static final String  NETWORKMETADATA_ID                = "metadata_id";
    public static final String  NETWORKMETADATA_SOURCE            = "metadata_source";
    public static final String  NETWORKMETADATA_REFERENCE         = "metadata_reference";
    public static final String  NETWORKMETADATA_PUBMED_ID         = "metadata_pubmed_id";
    public static final String  NETWORKMETADATA_AUTHORS           = "metadata_authors";
    public static final String  NETWORKMETADATA_PUBLICATION_NAME  = "metadata_publication";
    public static final String  NETWORKMETADATA_YEAR_PUBLISHED    = "metadata_year";
    public static final String  NETWORKMETADATA_PROCESSING_DESC   = "metadata_processing";
    public static final String  NETWORKMETADATA_NETWORK_TYPE      = "metadata_network";
    public static final String  NETWORKMETADATA_ALIAS             = "metadata_alias";
    public static final String  NETWORKMETADATA_INTERACTION_COUNT = "metadata_interactions";
    public static final String  NETWORKMETADATA_DYNAMIC_RANGE     = "metadata_dynamic_range";
    public static final String  NETWORKMETADATA_EDGE_WEIGHT_DIST  = "metadata_edge_weight_dist";
    public static final String  NETWORKMETADATA_ACCESS_STATS      = "metadata_access_stats";
    public static final String  NETWORKMETADATA_COMMENT           = "metadata_comment";
    public static final String  NETWORKMETADATA_OTHER             = "metadata_other";
    public static final String  NETWORKMETADATA_TITLE             = "metadata_title";
    public static final String  NETWORKMETADATA_URL               = "metadata_url";
    public static final String  NETWORKMETADATA_SOURCE_URL        = "metadata_source_url";

    public static final String  TAG_ID                            = "tag_id";
    public static final String  TAG_NAME                          = "tag_name";

    public static final String  NETWORKTAGASSOC_ID                = "nta_id";
    public static final String  NETWORKTAGASSOC_NETWORK_ID        = "nta_network_id";
    public static final String  NETWORKTAGASSOC_TAG_ID            = "nta_tag_id";

    public static final String  TYPE                              = "type";
    public static final String  ORGANISM                          = "organism";
    public static final String  GROUP                             = "group";
    public static final String  NETWORK                           = "network";
    public static final String  NODE                              = "node";
    public static final String  GENE                              = "gene";
    public static final String  NAMINGSOURCE                      = "namingsource";
    public static final String  GENEDATA                          = "genedata";
    public static final String  NETWORKMETADATA                   = "metadata";
    public static final String  TAG                               = "tag";
    public static final String  NETWORKTAGASSOC                   = "nta";
    public static final String  ONTOLOGY                          = "ontology";
    public static final String  ONTOLOGYCATEGORY                  = "ontologycategory";
	public static final String  STATISTICS                        = "statistics";
	public static final String  ATTRIBUTEGROUP                    = "attributegroup";
	public static final String  ATTRIBUTE                         = "attribute";

    public static final String  ONTOLOGY_ID                       = "o_id";
    public static final String  ONTOLOGY_NAME                     = "o_name";

    public static final String  ONTOLOGYCATEGORY_ID               = "oc_id";
    public static final String  ONTOLOGYCATEGORY_ONTOLOGY_ID      = "oc_o_id";
    public static final String  ONTOLOGYCATEGORY_NAME             = "oc_name";
    public static final String  ONTOLOGYCATEGORY_DESCRIPTION      = "oc_description";

    public static final String  STATISTICS_BUILD_DATE             = "stats_build_date";

    public static final String ATTRIBUTEGROUP_ID                  = "atg_id";
    public static final String ATTRIBUTEGROUP_ORGANISM_ID         = "atg_organism_id";
    public static final String ATTRIBUTEGROUP_NAME                = "atg_name";
    public static final String ATTRIBUTEGROUP_CODE                = "atg_code";
    public static final String ATTRIBUTEGROUP_DESCRIPTION         = "atg_description";
    public static final String ATTRIBUTEGROUP_LINKOUT_LABEL       = "atg_linkout_label";
    public static final String ATTRIBUTEGROUP_LINKOUT_URL         = "atg_linkout_url";
    public static final String ATTRIBUTEGROUP_DEFAULT_SELECTED    = "atg_default_selected";
    public static final String ATTRIBUTEGROUP_PUBLICATION_NAME    = "atg_publication_name";
    public static final String ATTRIBUTEGROUP_PUBLICATION_URL     = "atg_publication_url";
        
    public static final String ATTRIBUTE_ID                       = "at_id";
    public static final String ATTRIBUTE_ORGANISM_ID              = "at_organism_id";
    public static final String ATTRIBUTE_GROUP_ID                 = "at_atg_id";
    public static final String ATTRIBUTE_EXTERNAL_ID              = "at_external_id";
    public static final String ATTRIBUTE_NAME                     = "at_name";
    public static final String ATTRIBUTE_DESCRIPTION              = "at_description";

    public LuceneMediator(Searcher searcher, Analyzer analyzer) {
        this.searcher = searcher;
        this.analyzer = analyzer;
        
        // Hack to hide "illegal reflective access" warnings in Java 9+, caused by cglib.
        // It is based on the fact that it is allowed for code running on classpath (i.e. from the unnamed module)
        // to freely dynamically open packages of any module.
        // It can be done only from the target module itself, or from the unnamed module.
        try {
			if (!this.getClass().getModule().isNamed())
			    Console.class.getModule().addOpens(ClassLoader.class.getPackageName(), this.getClass().getModule());
			
			Method[] methods = ClassLoader.class.getDeclaredMethods();
			
			for (Method m : methods) {
				if (m.getName().equals("defineClass"))
					m.setAccessible(true);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    protected void search(String queryString, Collector results) {
        QueryParser parser = new QueryParser(Version.LUCENE_29, DEFAULT_FIELD, analyzer);
        try {
            Query query = parser.parse(queryString);
            searcher.search(query, results);
        } catch (ParseException e) {
            log(e);
        } catch (IOException e) {
            log(e);
        }
    }

    protected TopDocs search(String queryString, int limit) {
        QueryParser parser = new QueryParser(Version.LUCENE_29, DEFAULT_FIELD, analyzer);
        try {
            Query query = parser.parse(queryString);
            return searcher.search(query, limit);
        } catch (ParseException e) {
            log(e);
        } catch (IOException e) {
            log(e);
        }
        return null;
    }

    protected void log(Throwable t) {
        t.printStackTrace();
    }

    protected Node createNode(final long id, final long organismId) {
        final Node[] result = new Node[1];
        search(String.format("+%s:\"%d\" +%s:\"%d\"", LuceneMediator.NODE_ID, id, LuceneMediator.NODE_ORGANISM_ID, organismId), new AbstractCollector() {
            @Override
            public void handleHit(int doc) {
                try {
                    Document document = searcher.doc(doc);
                    result[0] = createNode(document, organismId);
                } catch (CorruptIndexException e) {
                    log(e);
                } catch (IOException e) {
                    log(e);
                }
            }
        });
        return result[0];
    }

    @SuppressWarnings("unchecked")
    protected Node createNode(final Document document, final long organismId) {
        final Node node = new Node();
        final long nodeId = Long.parseLong(document.get(LuceneMediator.NODE_ID));
        node.setId(nodeId);
        node.setName(document.get(LuceneMediator.NODE_NAME));
        node.setGeneData((GeneData) Enhancer.create(GeneData.class, new LazyLoader() {
            public Object loadObject() throws Exception {
                return createGeneData(Long.parseLong(document.get(LuceneMediator.NODE_GENEDATA_ID)));
            }
        }));
        node.setGenes((Collection<Gene>) Enhancer.create(Collection.class, new LazyLoader() {
            public Object loadObject() throws Exception {
            	BooleanQuery query = new BooleanQuery();
            	query.add(new TermQuery(new Term(LuceneMediator.GENE_ORGANISM_ID, String.valueOf(organismId))), Occur.MUST);
                query.add(new TermQuery(new Term(LuceneMediator.GENE_NODE_ID, String.valueOf(nodeId))), Occur.MUST);
                return createGenes(query, node, null);
            }
        }));
        return node;
    }

    protected GeneData createGeneData(final long id) {
        final GeneData[] result = new GeneData[1];
        search(String.format("%s:\"%d\"", LuceneMediator.GENEDATA_ID, id), new AbstractCollector() {
            @Override
            public void handleHit(int doc) {
                try {
                    Document document = searcher.doc(doc);
                    result[0] = createGeneData(document);
                } catch (CorruptIndexException e) {
                    log(e);
                } catch (IOException e) {
                    log(e);
                }
            }
        });
        return result[0];
    }

    protected GeneData createGeneData(Document document) {
        GeneData data = new GeneData();
        data.setId(Long.parseLong(document.get(LuceneMediator.GENEDATA_ID)));
        data.setDescription(document.get(LuceneMediator.GENEDATA_DESCRIPTION));
        data.setExternalId(document.get(LuceneMediator.GENEDATA_EXTERNAL_ID));

        final String namingSourceId = document.get(LuceneMediator.GENEDATA_NAMINGSOURCE_ID);
        if (namingSourceId != null) {
            data.setLinkoutSource((GeneNamingSource) Enhancer.create(GeneNamingSource.class, new LazyLoader() {
                public Object loadObject() throws Exception {
                    return createNamingSource(Long.parseLong(namingSourceId));
                };
            }));
        }
        return data;
    }

    private GeneNamingSource createNamingSource(long id) {
        final GeneNamingSource[] result = new GeneNamingSource[1];
        search(String.format("%s:\"%d\"", LuceneMediator.NAMINGSOURCE_ID, id), new AbstractCollector() {
            @Override
            public void handleHit(int doc) {
                try {
                    Document document = searcher.doc(doc);
                    result[0] = createNamingSource(document);
                } catch (CorruptIndexException e) {
                    log(e);
                } catch (IOException e) {
                    log(e);
                }
            }
        });
        return result[0];
    }

    protected GeneNamingSource createNamingSource(Document document) {
        GeneNamingSource source = new GeneNamingSource();
        source.setId(Long.parseLong(document.get(LuceneMediator.NAMINGSOURCE_ID)));
        source.setName(document.get(LuceneMediator.NAMINGSOURCE_NAME));
        source.setRank(Byte.parseByte(document.get(LuceneMediator.NAMINGSOURCE_RANK)));
        String namingSource = document.get(LuceneMediator.NAMINGSOURCE_SHORT_NAME);
        if (namingSource == null) {
            namingSource = "";
        }
        source.setShortName(namingSource);
        return source;
    }

    protected GeneNamingSource createNamingSource(String namingSourceName) {
        final GeneNamingSource[] result = new GeneNamingSource[1];
        search(String.format("%s:\"%s\"", LuceneMediator.NAMINGSOURCE_NAME, QueryParser.escape(namingSourceName)), new AbstractCollector() {
            @Override
            public void handleHit(int doc) {
                try {
                    Document document = searcher.doc(doc);
                    result[0] = createNamingSource(document);
                } catch (CorruptIndexException e) {
                    log(e);
                } catch (IOException e) {
                    log(e);
                }
            }
        });
        return result[0];
    }

    protected Gene createGene(long organismId, String geneSymbol) {
        final Gene[] result = new Gene[1];
        search(String.format("+%s:\"%d\" +%s:\"%s\"", LuceneMediator.GENE_ORGANISM_ID, organismId,
                LuceneMediator.GENE_SYMBOL, QueryParser.escape(geneSymbol)), new AbstractCollector() {
            @Override
            public void handleHit(int doc) {
                try {
                    Document document = searcher.doc(doc);
                    result[0] = createGene(document, null, null, null);
                } catch (CorruptIndexException e) {
                    log(e);
                } catch (IOException e) {
                    log(e);
                }
            }
        });
        return result[0];
    }

    protected Gene createGene(String geneSymbol) {
        final Gene[] result = new Gene[1];
        search(String.format("%s:\"%s\"", LuceneMediator.GENE_SYMBOL, QueryParser.escape(geneSymbol)), new AbstractCollector() {
            @Override
            public void handleHit(int doc) {
                try {
                    Document document = searcher.doc(doc);
                    result[0] = createGene(document, null, null, null);
                } catch (CorruptIndexException e) {
                    log(e);
                } catch (IOException e) {
                    log(e);
                }
            }
        });
        return result[0];
    }

    protected Gene createGene(final Document document, Node node, Organism organism, GeneNamingSource namingSource) {
        if (node == null) {
            node = (Node) Enhancer.create(Node.class, new LazyLoader() {
                public Object loadObject() throws Exception {
                	long nodeId = Long.parseLong(document.get(LuceneMediator.GENE_NODE_ID));
                	long organismId = Long.parseLong(document.get(LuceneMediator.GENE_ORGANISM_ID));
                    return createNode(nodeId, organismId);
                }
            });
        }
        if (organism == null) {
            organism = (Organism) Enhancer.create(Organism.class, new LazyLoader() {
                public Object loadObject() throws Exception {
                    return createOrganism(Long.parseLong(document.get(LuceneMediator.GENE_ORGANISM_ID)));
                }
            });
        }
        if (namingSource == null) {
            final long namingSourceId = Long.parseLong(document.get(LuceneMediator.GENE_NAMINGSOURCE_ID));
            namingSource = (GeneNamingSource) Enhancer.create(GeneNamingSource.class, new LazyLoader() {
                public Object loadObject() throws Exception {
                    return createNamingSource(namingSourceId);
                }
            });
        }
        Gene gene = new Gene();
        gene.setId(Long.parseLong(document.get(LuceneMediator.GENE_ID)));
        gene.setSymbol(document.get(LuceneMediator.GENE_SYMBOL));
        gene.setNode(node);
        gene.setOrganism(organism);
        gene.setNamingSource(namingSource);
        gene.setDefaultSelected(Boolean.parseBoolean(document.get(LuceneMediator.GENE_DEFAULT_SELECTED)));
        return gene;
    }

    protected Organism createOrganism(long organismId) {
        final Organism[] result = new Organism[1];
        search(String.format("%s:\"%d\"", LuceneMediator.ORGANISM_ID, organismId), new AbstractCollector() {
            @Override
            public void handleHit(int doc) {
                try {
                    Document document = searcher.doc(doc);
                    result[0] = createOrganism(document);
                } catch (CorruptIndexException e) {
                    log(e);
                } catch (IOException e) {
                    log(e);
                }
            }
        });
        return result[0];
    }

    @SuppressWarnings("unchecked")
    protected Organism createOrganism(final Document document) {
        final long organismId = Long.parseLong(document.get(LuceneMediator.ORGANISM_ID));
        Organism organism = new Organism();
        organism.setId(organismId);
        organism.setName(document.get(LuceneMediator.ORGANISM_NAME));
        organism.setDescription(document.get(LuceneMediator.ORGANISM_DESCRIPTION));
        organism.setInteractionNetworkGroups((Collection<InteractionNetworkGroup>) Enhancer.create(Collection.class, new LazyLoader() {
            public Object loadObject() throws Exception {
                return createNetworkGroups(organismId);
            }
        }));
        organism.setAlias(document.get(LuceneMediator.ORGANISM_ALIAS));
        final long ontologyId = Long.parseLong(document.get(LuceneMediator.ORGANISM_ONTOLOGY_ID));
        if (ontologyId != -1) {
	        organism.setOntology((Ontology) Enhancer.create(Ontology.class, new LazyLoader() {
	            public Object loadObject() throws Exception {
	                return createOntology(ontologyId);
	            }
	        }));
        }
        organism.setTaxonomyId(Long.parseLong(document.get(LuceneMediator.ORGANISM_TAXONOMY_ID)));
        organism.setDefaultGenes((Collection<Gene>) Enhancer.create(Collection.class, new LazyLoader() {
        	public Object loadObject() throws Exception {
        		return createDefaultSelectedGenes(organismId);
        	}
        }));
        return organism;
    }

    protected Ontology createOntology(long ontologyId) {
        final Ontology[] result = new Ontology[1];
        search(String.format("%s:\"%d\"", LuceneMediator.ONTOLOGY_ID, ontologyId), new AbstractCollector() {
            @Override
            public void handleHit(int doc) {
                try {
                    Document document = searcher.doc(doc);
                    result[0] = createOntology(document);
                } catch (CorruptIndexException e) {
                    log(e);
                } catch (IOException e) {
                    log(e);
                }
            }
        });
        return result[0];
    }

    @SuppressWarnings("unchecked")
    protected Ontology createOntology(Document document) {
        Ontology ontology = new Ontology();
        final long ontologyId = Long.parseLong(document.get(LuceneMediator.ONTOLOGY_ID));
        ontology.setId(ontologyId);
        ontology.setName(document.get(LuceneMediator.ONTOLOGY_NAME));
        ontology.setCategories((Collection<OntologyCategory>) Enhancer.create(Collection.class, new LazyLoader() {
            public Object loadObject() throws Exception {
                return createOntologyCategories(ontologyId);
            }
        }));
        return ontology;
    }

    private OntologyCategory createOntologyCategory(Document document) {
        OntologyCategory category = new OntologyCategory();
        category.setId(Long.parseLong(document.get(LuceneMediator.ONTOLOGYCATEGORY_ID)));
        category.setName(document.get(LuceneMediator.ONTOLOGYCATEGORY_NAME));
        category.setDescription(document.get(LuceneMediator.ONTOLOGYCATEGORY_DESCRIPTION));
        return category;
    }

    protected Collection<OntologyCategory> createOntologyCategories(long ontologyId) {
        final Collection<OntologyCategory> result = new HashSet<OntologyCategory>();
        search(String.format("%s:\"%d\"", LuceneMediator.ONTOLOGYCATEGORY_ONTOLOGY_ID, ontologyId), new AbstractCollector() {
            @Override
            public void handleHit(int doc) {
                try {
                    final Document document = searcher.doc(doc);
                    result.add((OntologyCategory) Enhancer.create(OntologyCategory.class, new LazyLoader() {
                        public Object loadObject() throws Exception {
                            return createOntologyCategory(document);
                        }
                    }));
                } catch (CorruptIndexException e) {
                    log(e);
                } catch (IOException e) {
                    log(e);
                }
            }
        });
        return result;
    }

    protected OntologyCategory createOntologyCategory(long categoryId) {
        final OntologyCategory[] result = new OntologyCategory[1];
        search(String.format("%s:\"%d\"", LuceneMediator.ONTOLOGYCATEGORY_ID, categoryId), new AbstractCollector() {
            @Override
            public void handleHit(int doc) {
                try {
                    final Document document = searcher.doc(doc);
                    result[0] = (OntologyCategory) Enhancer.create(OntologyCategory.class, new LazyLoader() {
                        public Object loadObject() throws Exception {
                            return createOntologyCategory(document);
                        }
                    });
                } catch (CorruptIndexException e) {
                    log(e);
                } catch (IOException e) {
                    log(e);
                }
            }
        });
        return result[0];
    }
    
    protected OntologyCategory createOntologyCategory(String name) {
        final OntologyCategory[] result = new OntologyCategory[1];
        Query query = new TermQuery(new Term(ONTOLOGYCATEGORY_NAME, name));
        try {
			searcher.search(query, new AbstractCollector() {
			    @Override
			    public void handleHit(int doc) {
			        try {
			            final Document document = searcher.doc(doc);
			            result[0] = (OntologyCategory) Enhancer.create(OntologyCategory.class, new LazyLoader() {
			                public Object loadObject() throws Exception {
			                    return createOntologyCategory(document);
			                }
			            });
			        } catch (CorruptIndexException e) {
			            log(e);
			        } catch (IOException e) {
			            log(e);
			        }
			    }
			});
		} catch (IOException e) {
			log(e);
		}
        return result[0];
    }

    protected Collection<InteractionNetworkGroup> createNetworkGroups(long organismId) {
        final Set<InteractionNetworkGroup> result = new HashSet<InteractionNetworkGroup>();
        search(String.format("%s:\"%d\"", LuceneMediator.GROUP_ORGANISM_ID, organismId), new AbstractCollector() {
            @Override
            public void handleHit(int doc) {
                try {
                    final Document document = searcher.doc(doc);
                    result.add((InteractionNetworkGroup) Enhancer.create(InteractionNetworkGroup.class,
                            new LazyLoader() {
                                public Object loadObject() throws Exception {
                                    return createNetworkGroup(document);
                                }
                            }));
                } catch (CorruptIndexException e) {
                    log(e);
                } catch (IOException e) {
                    log(e);
                }
            }
        });
        return result;
    }

    @SuppressWarnings("unchecked")
    public InteractionNetworkGroup createNetworkGroup(Document document) {
        final long groupId = Long.parseLong(document.get(LuceneMediator.GROUP_ID));
        InteractionNetworkGroup group = new InteractionNetworkGroup();
        group.setId(groupId);
        group.setName(document.get(LuceneMediator.GROUP_NAME));
        String code = document.get(LuceneMediator.GROUP_CODE);
        if (code == null) {
            code = document.get(LuceneMediator.GROUP_DESCRIPTION);
        }
        group.setCode(code);
        group.setDescription(document.get(LuceneMediator.GROUP_DESCRIPTION));
        group.setInteractionNetworks((Collection<InteractionNetwork>) Enhancer.create(Collection.class, new LazyLoader() {
            public Object loadObject() throws Exception {
                return createNetworks(groupId);
            }
        }));
        return group;
    }

    protected List<Gene> createGenes(long organismId, List<String> geneSymbols) {
        BooleanQuery query = new BooleanQuery();
        try {
            for (String symbol : geneSymbols) {
                PhraseQuery phrase = createPhraseQuery(LuceneMediator.GENE_SYMBOL, symbol);
                query.add(phrase, Occur.SHOULD);
            }
            BooleanQuery query2 = new BooleanQuery();
            query2.add(query, Occur.MUST);
            query2
                    .add(new TermQuery(new Term(LuceneMediator.GENE_ORGANISM_ID, String.valueOf(organismId))),
                            Occur.MUST);
            return createGenes(query2, null, createOrganism(organismId));
        } catch (IOException e) {
            log(e);
        }
        return Collections.emptyList();
    }

    protected List<Gene> createGenes(long organismId) {
        TermQuery query = new TermQuery(new Term(LuceneMediator.GENE_ORGANISM_ID, String.valueOf(organismId)));
        return createGenes(query, null, createOrganism(organismId));
    }

	private Object createDefaultSelectedGenes(long organismId) {
		BooleanQuery query = new BooleanQuery();
        query.add(new TermQuery(new Term(LuceneMediator.GENE_ORGANISM_ID, String.valueOf(organismId))), Occur.MUST);
        query.add(new TermQuery(new Term(LuceneMediator.GENE_DEFAULT_SELECTED, String.valueOf(true))), Occur.MUST);
        return createGenes(query, null, createOrganism(organismId));
	}

	protected List<Gene> createGenes(Query query, final Node node, final Organism organism) {
        final List<Gene> result = new ArrayList<Gene>();
        try {
            searcher.search(query, new AbstractCollector() {
                @Override
                public void handleHit(final int doc) {
                    result.add((Gene) Enhancer.create(Gene.class, new LazyLoader() {
                        public Object loadObject() throws Exception {
                            return createGene(searcher.doc(doc), node, organism, null);
                        }
                    }));
                }
            });
        } catch (IOException e) {
            log(e);
        }
        return result;
    }

    protected PhraseQuery createPhraseQuery(String field, String phrase) throws IOException {
        TokenStream stream = analyze(phrase);
        stream.reset();
        PhraseQuery query = new PhraseQuery();
        while (stream.incrementToken()) {
        	TermAttribute term = stream.getAttribute(TermAttribute.class);
            query.add(new Term(field, term.term()));
        }
        stream.end();
        stream.close();
        return query;
    }

    protected TokenStream analyze(String text) throws IOException {
        return analyzer.reusableTokenStream(DEFAULT_FIELD, new StringReader(text));
    }

    protected List<InteractionNetwork> createNetworks(Long groupId) {
        final List<InteractionNetwork> result = new ArrayList<InteractionNetwork>();
        search(String.format("%s:\"%d\"", LuceneMediator.NETWORK_GROUP_ID, groupId), new AbstractCollector() {
            @Override
            public void handleHit(int doc) {
                try {
                    final Document document = searcher.doc(doc);
                    result.add((InteractionNetwork) Enhancer.create(InteractionNetwork.class, new LazyLoader() {
                        public Object loadObject() throws Exception {
                            return createNetwork(document);
                        }
                    }));
                } catch (CorruptIndexException e) {
                    log(e);
                } catch (IOException e) {
                    log(e);
                }
            }
        });
        return result;
    }

    protected InteractionNetwork createNetwork(Document document) {
        InteractionNetwork network = new InteractionNetwork();
        long networkId = Long.parseLong(document.get(LuceneMediator.NETWORK_ID));
        network.setId(networkId);
        network.setName(document.get(LuceneMediator.NETWORK_NAME));
        network.setDescription(document.get(LuceneMediator.NETWORK_DESCRIPTION));
        Set<Interaction> interactions = Collections.emptySet();
        network.setInteractions(interactions);
        network.setMetadata(createNetworkMetadata(Long.parseLong(document.get(LuceneMediator.NETWORK_METADATA_ID))));
        network.setTags(createTags(networkId));
        network.setDefaultSelected(Boolean.parseBoolean(document.get(LuceneMediator.NETWORK_DEFAULT_SELECTED)));
        return network;
    }

    private Collection<Tag> createTags(long networkId) {
        final List<Tag> tags = new ArrayList<Tag>();
        search(String.format("%s:\"%d\"", LuceneMediator.NETWORKTAGASSOC_NETWORK_ID, networkId), new AbstractCollector() {
            @Override
            public void handleHit(int doc) {
                try {
                    Document document = searcher.doc(doc);
                    long tagId = Long.parseLong(document.get(LuceneMediator.NETWORKTAGASSOC_TAG_ID));
                    tags.add(createTag(tagId));
                } catch (CorruptIndexException e) {
                    log(e);
                } catch (IOException e) {
                    log(e);
                }
            }
        });
        return tags;
    }

    protected Tag createTag(long tagId) {
        final Tag[] result = new Tag[1];
        search(String.format("%s:\"%d\"", LuceneMediator.TAG_ID, tagId), new AbstractCollector() {
            @Override
            public void handleHit(int doc) {
                try {
                    Document document = searcher.doc(doc);
                    result[0] = createTag(document);
                } catch (CorruptIndexException e) {
                    log(e);
                } catch (IOException e) {
                    log(e);
                }
            }
        });
        return result[0];
    }

    protected Tag createTag(Document document) {
        Tag tag = new Tag();
        tag.setId(Long.parseLong(document.get(LuceneMediator.TAG_ID)));
        tag.setName(document.get(LuceneMediator.TAG_NAME));
        return tag;
    }

    protected NetworkMetadata createNetworkMetadata(long metadataId) {
        final NetworkMetadata[] result = new NetworkMetadata[1];
        search(String.format("%s:\"%d\"", LuceneMediator.NETWORKMETADATA_ID, metadataId), new AbstractCollector() {
            @Override
            public void handleHit(int doc) {
                try {
                    Document document = searcher.doc(doc);
                    result[0] = createNetworkMetadata(document);
                } catch (CorruptIndexException e) {
                    log(e);
                } catch (IOException e) {
                    log(e);
                }
            }
        });
        return result[0];
    }

    protected NetworkMetadata createNetworkMetadata(Document document) {
        NetworkMetadata data = new NetworkMetadata();
        data.setId(Long.parseLong(document.get(LuceneMediator.NETWORKMETADATA_ID)));
        data.setSource(document.get(LuceneMediator.NETWORKMETADATA_SOURCE));
        data.setReference(document.get(LuceneMediator.NETWORKMETADATA_REFERENCE));
        data.setPubmedId(document.get(LuceneMediator.NETWORKMETADATA_PUBMED_ID));
        data.setAuthors(document.get(LuceneMediator.NETWORKMETADATA_AUTHORS));
        data.setPublicationName(document.get(LuceneMediator.NETWORKMETADATA_PUBLICATION_NAME));
        data.setYearPublished(document.get(LuceneMediator.NETWORKMETADATA_YEAR_PUBLISHED));
        data.setProcessingDescription(document.get(LuceneMediator.NETWORKMETADATA_PROCESSING_DESC));
        data.setNetworkType(document.get(LuceneMediator.NETWORKMETADATA_NETWORK_TYPE));
        data.setAlias(document.get(LuceneMediator.NETWORKMETADATA_ALIAS));
        data.setInteractionCount(Long.parseLong(document.get(LuceneMediator.NETWORKMETADATA_INTERACTION_COUNT)));
        data.setDynamicRange(document.get(LuceneMediator.NETWORKMETADATA_DYNAMIC_RANGE));
        data.setEdgeWeightDistribution(document.get(LuceneMediator.NETWORKMETADATA_EDGE_WEIGHT_DIST));
        data.setAccessStats(Long.parseLong(document.get(LuceneMediator.NETWORKMETADATA_ACCESS_STATS)));
        data.setComment(document.get(LuceneMediator.NETWORKMETADATA_COMMENT));
        data.setOther(document.get(LuceneMediator.NETWORKMETADATA_OTHER));
        data.setTitle(document.get(LuceneMediator.NETWORKMETADATA_TITLE));
        data.setUrl(document.get(LuceneMediator.NETWORKMETADATA_URL));
        data.setSourceUrl(document.get(LuceneMediator.NETWORKMETADATA_SOURCE_URL));
        return data;
    }

    protected InteractionNetwork createNetwork(long networkId) {
        final InteractionNetwork[] result = new InteractionNetwork[1];
        search(String.format("%s:\"%d\"", LuceneMediator.NETWORK_ID, networkId), new AbstractCollector() {
            @Override
            public void handleHit(int doc) {
                try {
                    Document document = searcher.doc(doc);
                    result[0] = createNetwork(document);
                } catch (CorruptIndexException e) {
                    log(e);
                } catch (IOException e) {
                    log(e);
                }
            }
        });
        return result[0];
    }

    protected InteractionNetworkGroup createNetworkGroup(long organismId, String groupName) {
        final InteractionNetworkGroup[] result = new InteractionNetworkGroup[1];
        search(String.format("+%s:\"%d\" +%s:\"%s\"", LuceneMediator.GROUP_ORGANISM_ID, organismId,
                LuceneMediator.GROUP_NAME, QueryParser.escape(groupName)), new AbstractCollector() {
            @Override
            public void handleHit(int doc) {
                try {
                    Document document = searcher.doc(doc);
                    result[0] = createNetworkGroup(document);
                } catch (CorruptIndexException e) {
                    log(e);
                } catch (IOException e) {
                    log(e);
                }
            }
        });
        return result[0];
    }

    protected InteractionNetworkGroup createNetworkGroup(long groupId) {
        final InteractionNetworkGroup[] result = new InteractionNetworkGroup[1];
        search(String.format("+%s:\"%d\"", LuceneMediator.GROUP_ID, groupId), new AbstractCollector() {
            @Override
            public void handleHit(int doc) {
                try {
                    Document document = searcher.doc(doc);
                    result[0] = createNetworkGroup(document);
                } catch (CorruptIndexException e) {
                    log(e);
                } catch (IOException e) {
                    log(e);
                }
            }
        });
        return result[0];
    }

    protected Statistics createStatistics() {
        final Date[] date = new Date[1];
        search(String.format("%s:%s", LuceneMediator.TYPE, LuceneMediator.STATISTICS), new AbstractCollector() {
            @Override
            public void handleHit(int doc) {
                try {
                    Document document = searcher.doc(doc);
                    String dateString = document.get(LuceneMediator.STATISTICS_BUILD_DATE);
					date[0] = DateTools.stringToDate(dateString);
                } catch (CorruptIndexException e) {
                    log(e);
                } catch (IOException e) {
                    log(e);
				} catch (java.text.ParseException e) {
					log(e);
				}
            }
        });

        if (date[0] == null) {
        	date[0] = new Date();
        }

        Statistics statistics = new Statistics();
        statistics.setDate(date[0]);
        statistics.setGenes(count("type:node"));
        statistics.setNetworks(count("type:network"));
        statistics.setOrganisms(count("type:organism"));
        statistics
                .setInteractions(sum(String.format("%s:%s", TYPE, NETWORKMETADATA), NETWORKMETADATA_INTERACTION_COUNT));
        return statistics;
    }

    private long count(String query) {
        final Long[] result = new Long[] { 0L };
        search(query, new AbstractCollector() {
            @Override
            public void handleHit(int doc) {
                result[0] += 1;
            }
        });
        return result[0];
    }

    private long sum(String query, final String field) {
        final Long[] result = new Long[] { 0L };
        search(query, new AbstractCollector() {
            @Override
            public void handleHit(int doc) {
                try {
                    Document document = searcher.doc(doc);
                    String value = document.get(field);
                    if (value != null) {
                        result[0] += Long.parseLong(value);
                    }
                } catch (IOException e) {
                    log(e);
                }
            }
        });
        return result[0];
    }

    public static final Analyzer createDefaultAnalyzer() {
        return new Analyzer() {
            @Override
            public TokenStream tokenStream(String field, Reader reader) {
                return new LowerCaseFilter(new DefaultTokenStream(reader));
            }
        };
    }
    
    public static boolean indexExists(File path) {
    	try {
    		FSDirectory directory = FSDirectory.open(path);
    		try {
    			return IndexReader.indexExists(directory);
    		} finally {
    			directory.close();
    		}
    	} catch (IOException e) {
    		return false;
    	}
    }
}
