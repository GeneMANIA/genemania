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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

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

@SuppressWarnings("deprecation")
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
	
	private Map<Long, Organism> organismMap = new WeakHashMap<>(12);
	
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
			
			var methods = ClassLoader.class.getDeclaredMethods();
			
			for (var m : methods) {
				if (m.getName().equals("defineClass"))
					m.setAccessible(true);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    protected void search(String queryString, Collector results) {
        var parser = new QueryParser(Version.LUCENE_29, DEFAULT_FIELD, analyzer);
        
        try {
            var query = parser.parse(queryString);
            searcher.search(query, results);
        } catch (ParseException e) {
            log(e);
        } catch (IOException e) {
            log(e);
        }
    }

    protected TopDocs search(String queryString, int limit) {
        var parser = new QueryParser(Version.LUCENE_29, DEFAULT_FIELD, analyzer);
        
        try {
            var query = parser.parse(queryString);
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

    protected Node createNode(long id, long organismId) {
		var result = new Node[1];
		
		search(String.format("+%s:\"%d\" +%s:\"%d\"", NODE_ID, id, NODE_ORGANISM_ID, organismId), new AbstractCollector() {
			@Override
			public void handleHit(int doc) {
				try {
					var document = searcher.doc(doc);
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

	protected Node createNode(Document document, long organismId) {
		return new NodeProxy(organismId, document);
	}

	protected GeneData createGeneData(long id) {
		var result = new GeneData[1];
		
		search(String.format("%s:\"%d\"", GENEDATA_ID, id), new AbstractCollector() {
			@Override
			public void handleHit(int doc) {
				try {
					var document = searcher.doc(doc);
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
		var data = new GeneDataProxy(document);

		return data;
	}

	private GeneNamingSource createNamingSource(long id) {
		var result = new GeneNamingSource[1];

		search(String.format("%s:\"%d\"", NAMINGSOURCE_ID, id), new AbstractCollector() {
			@Override
			public void handleHit(int doc) {
				try {
					var document = searcher.doc(doc);
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
		var source = new GeneNamingSource();
		
		source.setId(Long.parseLong(document.get(NAMINGSOURCE_ID)));
		source.setName(document.get(NAMINGSOURCE_NAME));
		source.setRank(Byte.parseByte(document.get(NAMINGSOURCE_RANK)));
		
		var namingSource = document.get(NAMINGSOURCE_SHORT_NAME);

		if (namingSource == null)
			namingSource = "";

		source.setShortName(namingSource);

		return source;
	}

	protected GeneNamingSource createNamingSource(String namingSourceName) {
		var result = new GeneNamingSource[1];

		search(String.format("%s:\"%s\"", NAMINGSOURCE_NAME, QueryParser.escape(namingSourceName)), new AbstractCollector() {
			@Override
			public void handleHit(int doc) {
				try {
					var document = searcher.doc(doc);
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
		var result = new Gene[1];
		
		search(String.format("+%s:\"%d\" +%s:\"%s\"", GENE_ORGANISM_ID, organismId, GENE_SYMBOL, QueryParser.escape(geneSymbol)), new AbstractCollector() {
			@Override
			public void handleHit(int doc) {
				try {
					var document = searcher.doc(doc);
					result[0] = createGene(document, null, null);
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
		var result = new Gene[1];

		search(String.format("%s:\"%s\"", GENE_SYMBOL, QueryParser.escape(geneSymbol)), new AbstractCollector() {
			@Override
			public void handleHit(int doc) {
				try {
					var document = searcher.doc(doc);
					result[0] = createGene(document, null, null);
				} catch (CorruptIndexException e) {
					log(e);
				} catch (IOException e) {
					log(e);
				}
			}
		});

		return result[0];
	}

	protected Gene createGene(Document document, Node node, Organism organism) {
		long organismId = organism != null ? organism.getId() : Long.parseLong(document.get(GENE_ORGANISM_ID));

		return node != null ? new GeneProxy(organismId, node, document) : new GeneProxy(organismId, document);
	}
	
	protected Organism getOrganism(long organismId) {
		var organism = organismMap.get(organismId);
		
		if (organism == null)
			organism = createOrganism(organismId);
		
		return organism;
	}

	private Organism createOrganism(long organismId) {
		var result = new Organism[1];

		search(String.format("%s:\"%d\"", ORGANISM_ID, organismId), new AbstractCollector() {
			@Override
			public void handleHit(int doc) {
				try {
					var document = searcher.doc(doc);
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

	private Organism createOrganism(Document document) {
		var organism = new OrganismProxy(document);
		
		organismMap.put(organism.getId(), organism);
		
		return organism;
	}

	protected Ontology createOntology(long ontologyId) {
		var result = new Ontology[1];

		search(String.format("%s:\"%d\"", ONTOLOGY_ID, ontologyId), new AbstractCollector() {
			@Override
			public void handleHit(int doc) {
				try {
					var document = searcher.doc(doc);
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

	protected Ontology createOntology(Document document) {
		var ontology = new Ontology();
		
		long ontologyId = Long.parseLong(document.get(ONTOLOGY_ID));
		ontology.setId(ontologyId);
		ontology.setName(document.get(ONTOLOGY_NAME));
		ontology.setCategories(createOntologyCategories(ontologyId));
		
		return ontology;
	}

	private OntologyCategory createOntologyCategory(Document document) {
		var category = new OntologyCategory();
		category.setId(Long.parseLong(document.get(ONTOLOGYCATEGORY_ID)));
		category.setName(document.get(ONTOLOGYCATEGORY_NAME));
		category.setDescription(document.get(ONTOLOGYCATEGORY_DESCRIPTION));
		
		return category;
	}

	protected Collection<OntologyCategory> createOntologyCategories(long ontologyId) {
		var result = new HashSet<OntologyCategory>();
		
		search(String.format("%s:\"%d\"", ONTOLOGYCATEGORY_ONTOLOGY_ID, ontologyId), new AbstractCollector() {
			@Override
			public void handleHit(int doc) {
				try {
					var document = searcher.doc(doc);
					result.add(createOntologyCategory(document));
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
		var result = new OntologyCategory[1];
		
		search(String.format("%s:\"%d\"", ONTOLOGYCATEGORY_ID, categoryId), new AbstractCollector() {
			@Override
			public void handleHit(int doc) {
				try {
					var document = searcher.doc(doc);
					result[0] = createOntologyCategory(document);
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
		var result = new OntologyCategory[1];
		var query = new TermQuery(new Term(ONTOLOGYCATEGORY_NAME, name));
		
		try {
			searcher.search(query, new AbstractCollector() {
				@Override
				public void handleHit(int doc) {
					try {
						var document = searcher.doc(doc);
						result[0] = createOntologyCategory(document);
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
		var result = new HashSet<InteractionNetworkGroup>();

		search(String.format("%s:\"%d\"", GROUP_ORGANISM_ID, organismId), new AbstractCollector() {
			@Override
			public void handleHit(int doc) {
				try {
					var document = searcher.doc(doc);
					result.add(createNetworkGroup(document));
				} catch (CorruptIndexException e) {
					log(e);
				} catch (IOException e) {
					log(e);
				}
			}
		});

		return result;
	}

	public InteractionNetworkGroup createNetworkGroup(Document document) {
		long groupId = Long.parseLong(document.get(GROUP_ID));

		var group = new InteractionNetworkGroup();
		group.setId(groupId);
		group.setName(document.get(GROUP_NAME));
		var code = document.get(GROUP_CODE);

		if (code == null)
			code = document.get(GROUP_DESCRIPTION);

		group.setCode(code);
		group.setDescription(document.get(GROUP_DESCRIPTION));
		group.setInteractionNetworks(createNetworks(groupId));

		return group;
	}

	protected List<Gene> createGenes(long organismId, List<String> geneSymbols) {
		var query = new BooleanQuery();

		try {
			for (var symbol : geneSymbols) {
				var phrase = createPhraseQuery(GENE_SYMBOL, symbol);
				query.add(phrase, Occur.SHOULD);
			}

			var query2 = new BooleanQuery();
			query2.add(query, Occur.MUST);
			query2.add(new TermQuery(new Term(GENE_ORGANISM_ID, String.valueOf(organismId))), Occur.MUST);
			
			return createGenes(query2, null, getOrganism(organismId));
		} catch (IOException e) {
			log(e);
		}

		return Collections.emptyList();
	}

	protected List<Gene> createGenes(long organismId) {
		var query = new TermQuery(new Term(GENE_ORGANISM_ID, String.valueOf(organismId)));

		return createGenes(query, null, getOrganism(organismId));
	}

	private Collection<Gene> createDefaultSelectedGenes(Organism organism) {
		var query = new BooleanQuery();
		query.add(new TermQuery(new Term(GENE_ORGANISM_ID, String.valueOf(organism.getId()))), Occur.MUST);
		query.add(new TermQuery(new Term(GENE_DEFAULT_SELECTED, String.valueOf(true))), Occur.MUST);

		return createGenes(query, null, organism);
	}

	protected List<Gene> createGenes(Query query, Node node, Organism organism) {
		var result = new ArrayList<Gene>();

		try {
			searcher.search(query, new AbstractCollector() {
				@Override
				public void handleHit(int doc) {
					try {
						result.add(createGene(searcher.doc(doc), node, organism));
					} catch (IOException e) {
						log(e);
					}
				}
			});
		} catch (IOException e) {
			log(e);
		}

		return result;
	}

	protected PhraseQuery createPhraseQuery(String field, String phrase) throws IOException {
		var stream = analyze(phrase);
		stream.reset();

		var query = new PhraseQuery();

		while (stream.incrementToken()) {
			var term = stream.getAttribute(TermAttribute.class);
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
		var result = new ArrayList<InteractionNetwork>();

		search(String.format("%s:\"%d\"", NETWORK_GROUP_ID, groupId), new AbstractCollector() {
			@Override
			public void handleHit(int doc) {
				try {
					var document = searcher.doc(doc);
					result.add(createNetwork(document));
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
		var network = new InteractionNetwork();

		long networkId = Long.parseLong(document.get(NETWORK_ID));
		network.setId(networkId);
		network.setName(document.get(NETWORK_NAME));
		network.setDescription(document.get(NETWORK_DESCRIPTION));
		Set<Interaction> interactions = Collections.emptySet();
		network.setInteractions(interactions);
		network.setMetadata(createNetworkMetadata(Long.parseLong(document.get(NETWORK_METADATA_ID))));
		network.setTags(createTags(networkId));
		network.setDefaultSelected(Boolean.parseBoolean(document.get(NETWORK_DEFAULT_SELECTED)));

		return network;
	}

	private Collection<Tag> createTags(long networkId) {
		var tags = new ArrayList<Tag>();

		search(String.format("%s:\"%d\"", NETWORKTAGASSOC_NETWORK_ID, networkId), new AbstractCollector() {
			@Override
			public void handleHit(int doc) {
				try {
					var document = searcher.doc(doc);
					long tagId = Long.parseLong(document.get(NETWORKTAGASSOC_TAG_ID));
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
		var result = new Tag[1];

		search(String.format("%s:\"%d\"", TAG_ID, tagId), new AbstractCollector() {
			@Override
			public void handleHit(int doc) {
				try {
					var document = searcher.doc(doc);
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
		var tag = new Tag();
		tag.setId(Long.parseLong(document.get(TAG_ID)));
		tag.setName(document.get(TAG_NAME));

		return tag;
	}

	protected NetworkMetadata createNetworkMetadata(long metadataId) {
		var result = new NetworkMetadata[1];
		
		search(String.format("%s:\"%d\"", NETWORKMETADATA_ID, metadataId), new AbstractCollector() {
			@Override
			public void handleHit(int doc) {
				try {
					var document = searcher.doc(doc);
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
    	var data = new NetworkMetadata();
        
    	data.setId(Long.parseLong(document.get(NETWORKMETADATA_ID)));
        data.setSource(document.get(NETWORKMETADATA_SOURCE));
        data.setReference(document.get(NETWORKMETADATA_REFERENCE));
        data.setPubmedId(document.get(NETWORKMETADATA_PUBMED_ID));
        data.setAuthors(document.get(NETWORKMETADATA_AUTHORS));
        data.setPublicationName(document.get(NETWORKMETADATA_PUBLICATION_NAME));
        data.setYearPublished(document.get(NETWORKMETADATA_YEAR_PUBLISHED));
        data.setProcessingDescription(document.get(NETWORKMETADATA_PROCESSING_DESC));
        data.setNetworkType(document.get(NETWORKMETADATA_NETWORK_TYPE));
        data.setAlias(document.get(NETWORKMETADATA_ALIAS));
        data.setInteractionCount(Long.parseLong(document.get(NETWORKMETADATA_INTERACTION_COUNT)));
        data.setDynamicRange(document.get(NETWORKMETADATA_DYNAMIC_RANGE));
        data.setEdgeWeightDistribution(document.get(NETWORKMETADATA_EDGE_WEIGHT_DIST));
        data.setAccessStats(Long.parseLong(document.get(NETWORKMETADATA_ACCESS_STATS)));
        data.setComment(document.get(NETWORKMETADATA_COMMENT));
        data.setOther(document.get(NETWORKMETADATA_OTHER));
        data.setTitle(document.get(NETWORKMETADATA_TITLE));
        data.setUrl(document.get(NETWORKMETADATA_URL));
        data.setSourceUrl(document.get(NETWORKMETADATA_SOURCE_URL));
        
		return data;
	}

	protected InteractionNetwork createNetwork(long networkId) {
		var result = new InteractionNetwork[1];

		search(String.format("%s:\"%d\"", NETWORK_ID, networkId), new AbstractCollector() {
			@Override
			public void handleHit(int doc) {
				try {
					var document = searcher.doc(doc);
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
		var result = new InteractionNetworkGroup[1];

		search(String.format("+%s:\"%d\" +%s:\"%s\"", GROUP_ORGANISM_ID, organismId, GROUP_NAME, QueryParser.escape(groupName)), new AbstractCollector() {
			@Override
			public void handleHit(int doc) {
				try {
					var document = searcher.doc(doc);
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
		var result = new InteractionNetworkGroup[1];

		search(String.format("+%s:\"%d\"", GROUP_ID, groupId), new AbstractCollector() {
			@Override
			public void handleHit(int doc) {
				try {
					var document = searcher.doc(doc);
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
		var date = new Date[1];
		
		search(String.format("%s:%s", TYPE, STATISTICS), new AbstractCollector() {
			@Override
			public void handleHit(int doc) {
				try {
					var document = searcher.doc(doc);
					String dateString = document.get(STATISTICS_BUILD_DATE);
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

		if (date[0] == null)
			date[0] = new Date();

		var statistics = new Statistics();
		statistics.setDate(date[0]);
		statistics.setGenes(count("type:node"));
		statistics.setNetworks(count("type:network"));
		statistics.setOrganisms(count("type:organism"));
		statistics.setInteractions(sum(String.format("%s:%s", TYPE, NETWORKMETADATA), NETWORKMETADATA_INTERACTION_COUNT));
		
		return statistics;
	}

	private long count(String query) {
		var result = new Long[] { 0L };

		search(query, new AbstractCollector() {
			@Override
			public void handleHit(int doc) {
				result[0] += 1;
			}
		});

		return result[0];
	}

	private long sum(String query, String field) {
		var result = new Long[] { 0L };

		search(query, new AbstractCollector() {
			@Override
			public void handleHit(int doc) {
				try {
					var document = searcher.doc(doc);
					var value = document.get(field);

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
			try (var directory = FSDirectory.open(path)) {
				return IndexReader.indexExists(directory);
			}
		} catch (IOException e) {
			return false;
		}
	}
	
	// ====[ Proxies for Lazy Loading (because of circular dependencies) ]==============================================
	
	private class OrganismProxy extends Organism {

		private static final long serialVersionUID = 1101664334832757016L;
		
		private final long ontologyId;

		private OrganismProxy(Document document) {
			id = Long.parseLong(document.get(ORGANISM_ID));
			name = document.get(ORGANISM_NAME);
			description = document.get(ORGANISM_DESCRIPTION);
			alias = document.get(ORGANISM_ALIAS);
			taxonomyId = Long.parseLong(document.get(ORGANISM_TAXONOMY_ID));
			ontologyId = Long.parseLong(document.get(ORGANISM_ONTOLOGY_ID));
		}
		
		@Override
		public Ontology getOntology() {
			if (ontology == null && ontologyId != -1) {
				ontology = createOntology(ontologyId);
			}
			
			return ontology;
		}
		
		@Override
		public Collection<Gene> getDefaultGenes() {
			if (defaultGenes == null) {
				defaultGenes = createDefaultSelectedGenes(this);
			}
			
			return defaultGenes;
		}
		
		@Override
		public Collection<InteractionNetworkGroup> getInteractionNetworkGroups() {
			if (interactionNetworkGroups == null) {
				interactionNetworkGroups = createNetworkGroups(id);
			}
			
			return interactionNetworkGroups;
		}
	}
	
	private class NodeProxy extends Node {
		
		private static final long serialVersionUID = 1548481382163136316L;
		
		private final long organismId;
		private final Document document;
		
		private NodeProxy(long organismId, Document document) {
			this.organismId = organismId;
			this.document = document;
			
			id = Long.parseLong(document.get(NODE_ID));
			name = document.get(NODE_NAME);
		}
		
		@Override
		public Collection<Gene> getGenes() {
			if (genes == null) {
				var query = new BooleanQuery();
				query.add(new TermQuery(new Term(GENE_ORGANISM_ID, String.valueOf(organismId))), Occur.MUST);
				query.add(new TermQuery(new Term(GENE_NODE_ID, String.valueOf(id))), Occur.MUST);
				genes = createGenes(query, this, getOrganism(organismId));
			}
				
			return genes;
		}
		
		@Override
		public GeneData getGeneData() {
			if (geneData == null) {
				geneData = createGeneData(Long.parseLong(document.get(NODE_GENEDATA_ID)));
			}
				
			return geneData;
		}
	}
	
	private class GeneProxy extends Gene {
		
		private static final long serialVersionUID = -1302165245706925466L;

		private Node node;
		
		private final long organismId;
		
		private final long nodeId;
		private final long namingSourceId;
		
		private GeneProxy(long organismId, Document document) {
			this.organismId = organismId;
			
			id = Long.parseLong(document.get(GENE_ID));
			symbol = document.get(GENE_SYMBOL);
			defaultSelected = Boolean.parseBoolean(document.get(GENE_DEFAULT_SELECTED));
			nodeId = Long.parseLong(document.get(GENE_NODE_ID));
			namingSourceId = Long.parseLong(document.get(GENE_NAMINGSOURCE_ID));
		}
		
		private GeneProxy(long organismId, Node node, Document document) {
			this(organismId, document);
			
			if (node == null || node.getId() != nodeId)
				throw new IllegalArgumentException("Invalid Node object");
			
			this.node = node;
		}
		
		@Override
		public Node getNode() {
			if (node == null) {
				node = createNode(nodeId, organismId);
			}
				
			return node;
		}
		
		@Override
		public GeneNamingSource getNamingSource() {
			if (namingSource == null) {
				namingSource = createNamingSource(namingSourceId);
			}
				
			return namingSource;
		}
		
		@Override
		public Organism getOrganism() {
			if (organism == null) {
				organism = LuceneMediator.this.getOrganism(organismId);
			}
			
			return organism;
		}
	}
	
	private class GeneDataProxy extends GeneData {
		
		private static final long serialVersionUID = 3788456533570151808L;
		
		private final String namingSourceId;
		
		private GeneDataProxy(Document document) {
			namingSourceId = document.get(GENEDATA_NAMINGSOURCE_ID);
			
			id = Long.parseLong(document.get(GENEDATA_ID));
			description = document.get(GENEDATA_DESCRIPTION);
			externalId = document.get(GENEDATA_EXTERNAL_ID);
		}

		@Override
		public GeneNamingSource getLinkoutSource() {
			if (linkoutSource == null && namingSourceId != null) {
				linkoutSource = createNamingSource(Long.parseLong(namingSourceId));
			}
			
			return linkoutSource;
		}
	}
}
