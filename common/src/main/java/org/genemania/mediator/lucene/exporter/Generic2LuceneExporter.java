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

package org.genemania.mediator.lucene.exporter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.DateTools.Resolution;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.genemania.configobj.ConfigObj;
import org.genemania.configobj.Section;
import org.genemania.domain.Organism;
import org.genemania.mediator.lucene.LuceneMediator;

public class Generic2LuceneExporter {
	public static final int NETWORK_ID = 0;
	public static final int NETWORK_NAME = 1;
	public static final int NETWORK_METADATA_ID = 2;
	public static final int NETWORK_DESCRIPTION = 3;
	public static final int NETWORK_DEFAULT_SELECTED = 4;
	public static final int NETWORK_GROUP_ID = 5;

	private Map<String, String> networkGroupColours;
	private String genericDbPath;
	private String indexPath;
	private String basePath;
	private String profileName;
	private ConfigObj config;

	public Generic2LuceneExporter() {
		networkGroupColours = Collections.emptyMap();
	}

	public void close() {
	}
	
	static String join(String connector, String[] items) {
		StringBuilder builder = new StringBuilder();
		for (String item : items) {
			if (builder.length() > 0) {
				builder.append(connector);
			}
			builder.append(item);
		}
		return builder.toString();
	}
	
	public static void main(String[] args) throws Exception {
		if (args.length < 3) {
			System.out.println("Usage:");
			System.out.printf("%s <db-config.cfg> <raw-data-path> <colours.txt> [<profile> [<index-path>]]\n", Generic2LuceneExporter.class.getName());
			System.out.println();
			return;
		}
		
		String configurationPath = args[0];
		String basePath = args[1];
		String colourConfigPath = args[2];
		String profileName = null;
		String indexPath = "lucene_index";

		if (args.length >= 4) {
			profileName = args[3];

			// set to default profile (spelled null) if given "none" or "null" as profile name
			if ("none".equalsIgnoreCase(profileName) || "null".equalsIgnoreCase(profileName)) {
				profileName = null;
			}
		}

		if (args.length == 5) {
			indexPath = args[4];
		}

		final Map<String, String> colours = loadColours(colourConfigPath);
		
		ConfigObj config = new ConfigObj(new FileReader(configurationPath));
		String genericDbPath = join(File.separator, new String[] { basePath, config.getSection("FileLocations").getEntry("generic_db_dir") });
		
		final Generic2LuceneExporter exporter = new Generic2LuceneExporter();
		exporter.setNetworkGroupColours(colours);
		exporter.setBasePath(basePath);
		exporter.setGenericDbPath(genericDbPath);
		exporter.setProfileName(profileName);
		exporter.setConfig(config);
		exporter.setIndexPath(indexPath);
		exporter.export();
	}

    public void export() throws Exception {
        final ExportProfile profile = createExportProfile(basePath, profileName);
        Analyzer analyzer = createAnalyzer();

        try {
			final Map<String, Long> namingSourceIds = new HashMap<String, Long>();

			File indexFile = new File(makeIndexPath("base"));
			FSDirectory directory = FSDirectory.open(indexFile);
			final IndexWriter indexWriter = new IndexWriter(directory, analyzer, true, MaxFieldLength.UNLIMITED);
			processFile(genericDbPath, "GENE_NAMING_SOURCES.txt", new FileHandler() {
				@Override
                public boolean process(String line) throws IOException {
					String[] parts = line.split("\t", -1);
					exportNamingSource(indexWriter, parts);
					namingSourceIds.put(parts[1], Long.parseLong(parts[0]));
					return true;
				}
			});
			
			processFile(genericDbPath, "TAGS.txt", new FileHandler() {
				@Override
                public boolean process(String line) throws IOException {
					String[] parts = line.split("\t", -1);
					exportTag(indexWriter, parts);
					return true;
				}
			});
			
			processFile(genericDbPath, "ONTOLOGIES.txt", new FileHandler() {
				@Override
                public boolean process(String line) throws IOException {
					String[] parts = line.split("\t", -1);
					exportOntologies(indexWriter, parts);
					return true;
				}
			});

			processFile(genericDbPath, "ONTOLOGY_CATEGORIES.txt", new FileHandler() {
				@Override
                public boolean process(String line) throws IOException {
					String[] parts = line.split("\t", -1);
					exportOntologyCategories(indexWriter, parts);
					return true;
				}
			});

			exportStatistics(indexWriter);
			indexWriter.close();
			
			String[] organisms = config.getSection("Organisms").getEntry("organisms").split("\\s*,\\s*");
			for (final String organismId : organisms) {
				Section organismSection = config.getSection(organismId);
				final String shortName = organismSection.getEntry("short_name");
				System.out.println(shortName);

				RAMDirectory ramDirectory = new RAMDirectory();
				final IndexWriter writer = new IndexWriter(ramDirectory, analyzer, true, MaxFieldLength.UNLIMITED);
				final Organism organism = new Organism();
				processFile(genericDbPath, "ORGANISMS.txt", new FileHandler() {
					@Override
                    public boolean process(String line) throws IOException {
						String[] parts = line.split("\t", -1);
						if (parts[1].equals(shortName)) {
							exportOrganism(writer, parts);
							populateOrganism(organism, parts);
							return false;
						}
						return true;
					}
				});

				final Long entrezNamingSourceId = namingSourceIds.get("Entrez Gene ID");
				final Map<Long, String> externalIds = new HashMap<Long, String>();
				final Map<Long, Long> externalNamingSourceIds = new HashMap<Long, Long>();

				final Set<Long> nodes = new HashSet<Long>();
				processFile(genericDbPath, "GENES.txt", new FileHandler() {
					@Override
                    public boolean process(String line) throws IOException {
						String[] parts = line.split("\t", -1);
						long organismId = Long.parseLong(parts[5]);
						if (organismId == organism.getId()) {
							exportGene(writer, parts);
							long nodeId = Long.parseLong(parts[4]);
							nodes.add(nodeId);
							try{	
								long namingSourceId = Long.parseLong(parts[3]);
								if (namingSourceId == entrezNamingSourceId) {
									externalIds.put(nodeId, parts[1]);
									externalNamingSourceIds.put(nodeId, namingSourceId);
								}		
							} catch(NumberFormatException e){
								System.out.println("Number format error:"+parts[3]+",Nodeid:" +parts[4]+  ",Organismid:" + parts[5]);
								return true;
							}
						}
						return true;
					}
				});

				final Map<Long, Long> geneDataToNodeIds = new HashMap<Long, Long>();
				processFile(genericDbPath, "NODES.txt", new FileHandler() {
					@Override
                    public boolean process(String line) throws IOException {
						String[] parts = line.split("\t", -1);
						long nodeId = Long.parseLong(parts[0]);
						if (nodes.contains(nodeId)) {
							exportNode(writer, parts, String.valueOf(organism.getId()));
							geneDataToNodeIds.put(Long.parseLong(parts[2]), nodeId);
						}
						return true;
					}
				});

				processFile(genericDbPath, "GENE_DATA.txt", new FileHandler() {
					@Override
                    public boolean process(String line) throws IOException {
						String[] parts = line.split("\t", -1);
						long geneDataId = Long.parseLong(parts[0]);
						Long nodeId = geneDataToNodeIds.get(geneDataId);
						if (nodeId != null) {
							String externalId = externalIds.get(nodeId);
							long namingSourceId = -1;
							if (externalId != null) {
								namingSourceId = externalNamingSourceIds.get(nodeId);
							}
							exportGeneData(writer, parts, externalId, namingSourceId);
						}
						return true;
					}
				});

				final Set<Long> groups = new HashSet<Long>();
				processFile(genericDbPath, "NETWORK_GROUPS.txt", new FileHandler() {
					@Override
                    public boolean process(String line) throws IOException {
						String[] parts = line.split("\t", -1);
						long organismId = Long.parseLong(parts[4]);
						if (organismId == organism.getId()) {
							exportGroup(writer, parts);
							groups.add(Long.parseLong(parts[0]));
						}
						return true;
					}
				});

				final Set<Long> metadata = new HashSet<Long>();
				final Set<Long> networks = new HashSet<Long>();
				processFile(genericDbPath, "NETWORKS.txt", new FileHandler() {
					@Override
                    public boolean process(String line) throws IOException {
						String[] parts = line.split("\t", -1);
						long groupId = Long.parseLong(parts[5]);
						long networkId = Long.parseLong(parts[0]);
						if (groups.contains(groupId) && profile.includesNetwork(parts)) {
							exportNetwork(writer, parts);
							long metadataId = Long.parseLong(parts[2]);
							metadata.add(metadataId);

							networks.add(networkId);
						}
						return true;
					}
				});

				processFile(genericDbPath, "NETWORK_METADATA.txt", new FileHandler() {
					@Override
                    public boolean process(String line) throws IOException {
						String[] parts = line.split("\t", -1);
						long metadataId = Long.parseLong(parts[0]);
						if (metadata.contains(metadataId)) {
							exportNetworkMetadata(writer, parts);
						}
						return true;
					}
				});

				processFile(genericDbPath, "NETWORK_TAG_ASSOC.txt", new FileHandler() {
					@Override
                    public boolean process(String line) throws IOException {
						String[] parts = line.split("\t", -1);
						long networkId = Long.parseLong(parts[1]);
						if (networks.contains(networkId)) {
							exportNetworkTagAssoc(writer, parts);
						}
						return true;
					}
				});

                final Set<Long> attribute_groups = new HashSet<Long>();
				processFile(genericDbPath, "ATTRIBUTE_GROUPS.txt", new FileHandler() {
                    @Override
                    public boolean process(String line) throws IOException {
                        String[] parts = line.split("\t", -1);
                        long organismId = Long.parseLong(parts[1]);
                        if (organismId == organism.getId()) {
                            exportAttributeGroup(writer, parts);
                            long group_id = Long.parseLong(parts[0]);
                            attribute_groups.add(group_id);
                        }
                        return true;
                    }
				});

                final Set<Long> attributes = new HashSet<Long>();
                processFile(genericDbPath, "ATTRIBUTES.txt", new FileHandler() {
                    @Override
                    public boolean process(String line) throws IOException {
                        String[] parts = line.split("\t", -1);
                        long organismId = Long.parseLong(parts[1]);
                        long group_id = Long.parseLong(parts[2]);
                        if (organismId == organism.getId() && attribute_groups.contains(group_id)) {
                            exportAttribute(writer, parts);
                            long attribute_id = Long.parseLong(parts[0]);
                            attributes.add(attribute_id);
                        }
                        return true;
                    }
                });

                writer.close();

				String gmOrganismId = organismSection.getEntry("gm_organism_id");
				File organismFile = new File(makeIndexPath(String.format("%s", gmOrganismId)));
				FSDirectory fileDirectory = FSDirectory.open(organismFile);
				IndexWriter organismWriter = new IndexWriter(fileDirectory, analyzer, true, MaxFieldLength.UNLIMITED);
				IndexReader reader = IndexReader.open(ramDirectory);
				organismWriter.addIndexes(new IndexReader[] { reader });
				organismWriter.close();
				fileDirectory.close();
				ramDirectory.close();

				Properties properties = new Properties();
				properties.put("short_name", shortName);
				properties.put("common_name", organismSection.getEntry("common_name"));
				properties.put("organism_id", gmOrganismId);

				String propertyPath = String.format("%s%smetadata.xml", gmOrganismId, File.separator);
				FileOutputStream out = new FileOutputStream(makeIndexPath(propertyPath));
				try {
					properties.storeToXML(out, null, "UTF-8");
				} finally {
					out.close();
				}
			}
		} finally {
			close();
		}
	}

    public String makeIndexPath(String path) {
        if (indexPath != null && !indexPath.equals("")) {
            return indexPath + File.separator + path;
        }
        else {
            return path;
        }
    }

	private static ExportProfile createExportProfile(String basePath, String name) throws IOException {
		if (name == null) {
			return DefaultExportProfile.instance();
		}

		File file = new File(name);
		String profile = file.getName();
		if ("all".equals(profile)) {
			return DefaultExportProfile.instance();
		}
		if ("core".equals(profile)) {
			return new CoreExportProfile(basePath);
		}

		return new CustomExportProfile(name);
	}

	private void exportStatistics(IndexWriter writer) throws IOException {
		Document doc = new Document();
		Date date = new Date();
		doc.add(new Field(LuceneMediator.TYPE, LuceneMediator.STATISTICS, Store.YES, Index.ANALYZED));
		doc.add(new Field(LuceneMediator.STATISTICS_BUILD_DATE, DateTools.dateToString(date, Resolution.DAY), Store.YES, Index.ANALYZED));
		writer.addDocument(doc);
	}

	protected void exportOntologyCategories(IndexWriter writer, String[] parts) throws IOException {
		Document doc = new Document();
		doc.add(new Field(LuceneMediator.TYPE, LuceneMediator.ONTOLOGY, Store.YES, Index.ANALYZED));
		doc.add(new Field(LuceneMediator.ONTOLOGYCATEGORY_ID, parts[0], Store.YES, Index.ANALYZED));
		doc.add(new Field(LuceneMediator.ONTOLOGYCATEGORY_ONTOLOGY_ID, parts[1], Store.YES, Index.ANALYZED));
		doc.add(new Field(LuceneMediator.ONTOLOGYCATEGORY_NAME, parts[2], Store.YES, Index.ANALYZED));
		doc.add(new Field(LuceneMediator.ONTOLOGYCATEGORY_DESCRIPTION, parts[3], Store.YES, Index.ANALYZED));
		writer.addDocument(doc);
	}

	protected void exportOntologies(IndexWriter writer, String[] parts) throws IOException {
		Document doc = new Document();
		doc.add(new Field(LuceneMediator.TYPE, LuceneMediator.ONTOLOGY, Store.YES, Index.ANALYZED));
		doc.add(new Field(LuceneMediator.ONTOLOGY_ID, parts[0], Store.YES, Index.ANALYZED));
		doc.add(new Field(LuceneMediator.ONTOLOGY_NAME, parts[1], Store.YES, Index.ANALYZED));
		writer.addDocument(doc);
	}

	public static Map<String, String> loadColours(String path) throws IOException {
		Map<String, String> colours = new HashMap<String, String>();
		Pattern pattern = Pattern.compile("(.*?)\\s+([A-Fa-f0-9]+)\\s*(//.*)?");

		BufferedReader reader = new BufferedReader(new FileReader(path));
		String line = reader.readLine();
		while (line != null) {
			Matcher matcher = pattern.matcher(line);
			if (!matcher.matches()) {
				continue;
			}
			String colour = matcher.group(2);
			String groupType = matcher.group(1);
			colours.put(groupType, colour);
			line = reader.readLine();
		}
		return colours;
	}

	private static void populateOrganism(Organism organism, String[] parts) {
		organism.setId(Long.parseLong(parts[0]));
		organism.setName(parts[1]);
		organism.setDescription(parts[2]);
	}

	private static void processFile(String basePath, String fileName, FileHandler handler) throws IOException {
		String path = join(File.separator, new String[] { basePath, fileName });
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path), "utf-8"));
		String line = reader.readLine();
		while (line != null) {
			if (!handler.process(line)) {
				break;
			}
			line = reader.readLine();
		}
		reader.close();
	}

	public void setNetworkGroupColours(Map<String, String> colours) {
		networkGroupColours = colours;
	}

	private static Analyzer createAnalyzer() {
		return LuceneMediator.createDefaultAnalyzer();
	}

	public void exportOrganism(IndexWriter writer, String[] parts) throws IOException {
		Document doc = new Document();
		doc.add(new Field(LuceneMediator.TYPE, LuceneMediator.ORGANISM, Store.YES, Index.ANALYZED));
		doc.add(new Field(LuceneMediator.ORGANISM_ID, parts[0], Store.YES, Index.ANALYZED));
		doc.add(new Field(LuceneMediator.ORGANISM_NAME, parts[1], Store.YES, Index.ANALYZED));
		doc.add(new Field(LuceneMediator.ORGANISM_DESCRIPTION, parts[2], Store.YES, Index.ANALYZED));
		doc.add(new Field(LuceneMediator.ORGANISM_ALIAS, parts[3], Store.YES, Index.ANALYZED));
		doc.add(new Field(LuceneMediator.ORGANISM_ONTOLOGY_ID, parts[4], Store.YES, Index.ANALYZED));
		doc.add(new Field(LuceneMediator.ORGANISM_TAXONOMY_ID, parts[5], Store.YES, Index.ANALYZED));
		writer.addDocument(doc);
	}

	public void exportGroup(IndexWriter writer, String[] parts) throws IOException {
		Document doc = new Document();
		String colour = networkGroupColours.get(parts[2]);
		if (colour == null) {
			colour = "000000";
		}
		doc.add(new Field(LuceneMediator.TYPE, LuceneMediator.GROUP, Store.YES, Index.ANALYZED));
		doc.add(new Field(LuceneMediator.GROUP_ID, parts[0], Store.YES, Index.ANALYZED));
		doc.add(new Field(LuceneMediator.GROUP_NAME, parts[1], Store.YES, Index.ANALYZED));
		doc.add(new Field(LuceneMediator.GROUP_CODE, parts[2], Store.YES, Index.ANALYZED));
		doc.add(new Field(LuceneMediator.GROUP_DESCRIPTION, parts[3], Store.YES, Index.ANALYZED));
		doc.add(new Field(LuceneMediator.GROUP_ORGANISM_ID, parts[4], Store.YES, Index.ANALYZED));
		doc.add(new Field(LuceneMediator.GROUP_COLOUR, colour, Store.YES, Index.ANALYZED));
		writer.addDocument(doc);
	}

	public void exportNetwork(IndexWriter writer, String[] parts) throws IOException {
		Document doc = new Document();
		doc.add(new Field(LuceneMediator.TYPE, LuceneMediator.NETWORK, Store.YES, Index.ANALYZED));
		doc.add(new Field(LuceneMediator.NETWORK_ID, parts[NETWORK_ID], Store.YES, Index.ANALYZED));
		doc.add(new Field(LuceneMediator.NETWORK_NAME, parts[NETWORK_NAME], Store.YES, Index.ANALYZED));
		doc.add(new Field(LuceneMediator.NETWORK_METADATA_ID, parts[NETWORK_METADATA_ID], Store.YES, Index.ANALYZED));
		doc.add(new Field(LuceneMediator.NETWORK_DESCRIPTION, parts[NETWORK_DESCRIPTION], Store.YES, Index.ANALYZED));
		doc.add(new Field(LuceneMediator.NETWORK_DEFAULT_SELECTED, parts[NETWORK_DEFAULT_SELECTED].equals("0") ? "false" : "true", Store.YES, Index.ANALYZED));
		doc.add(new Field(LuceneMediator.NETWORK_GROUP_ID, parts[NETWORK_GROUP_ID], Store.YES, Index.ANALYZED));
		writer.addDocument(doc);
	}

	public void exportNetworkMetadata(IndexWriter writer, String[] parts) throws IOException {
		Document doc = new Document();
		doc.add(new Field(LuceneMediator.TYPE, LuceneMediator.NETWORKMETADATA, Store.YES, Index.ANALYZED));
		doc.add(new Field(LuceneMediator.NETWORKMETADATA_ID, parts[0], Store.YES, Index.ANALYZED));
		doc.add(new Field(LuceneMediator.NETWORKMETADATA_SOURCE, parts[1], Store.YES, Index.ANALYZED));
		doc.add(new Field(LuceneMediator.NETWORKMETADATA_REFERENCE, parts[2], Store.YES, Index.ANALYZED));
		doc.add(new Field(LuceneMediator.NETWORKMETADATA_PUBMED_ID, parts[3], Store.YES, Index.ANALYZED));
		doc.add(new Field(LuceneMediator.NETWORKMETADATA_AUTHORS, parts[4], Store.YES, Index.ANALYZED));
		doc.add(new Field(LuceneMediator.NETWORKMETADATA_PUBLICATION_NAME, parts[5], Store.YES, Index.ANALYZED));
		doc.add(new Field(LuceneMediator.NETWORKMETADATA_YEAR_PUBLISHED, parts[6], Store.YES, Index.ANALYZED));
		doc.add(new Field(LuceneMediator.NETWORKMETADATA_PROCESSING_DESC, parts[7], Store.YES, Index.ANALYZED));
		doc.add(new Field(LuceneMediator.NETWORKMETADATA_NETWORK_TYPE, parts[8], Store.YES, Index.ANALYZED));
		doc.add(new Field(LuceneMediator.NETWORKMETADATA_ALIAS, parts[9], Store.YES, Index.ANALYZED));
		doc.add(new Field(LuceneMediator.NETWORKMETADATA_INTERACTION_COUNT, parts[10], Store.YES, Index.ANALYZED));
		doc.add(new Field(LuceneMediator.NETWORKMETADATA_DYNAMIC_RANGE, parts[11], Store.YES, Index.ANALYZED));
		doc.add(new Field(LuceneMediator.NETWORKMETADATA_EDGE_WEIGHT_DIST, parts[12], Store.YES, Index.ANALYZED));
		doc.add(new Field(LuceneMediator.NETWORKMETADATA_ACCESS_STATS, parts[13], Store.YES, Index.ANALYZED));
		doc.add(new Field(LuceneMediator.NETWORKMETADATA_COMMENT, parts[14], Store.YES, Index.ANALYZED));
		if (parts.length > 15) {
			doc.add(new Field(LuceneMediator.NETWORKMETADATA_OTHER, parts[15], Store.YES, Index.ANALYZED));
			doc.add(new Field(LuceneMediator.NETWORKMETADATA_TITLE, parts[16], Store.YES, Index.ANALYZED));
			doc.add(new Field(LuceneMediator.NETWORKMETADATA_URL, parts[17], Store.YES, Index.ANALYZED));
			doc.add(new Field(LuceneMediator.NETWORKMETADATA_SOURCE_URL, parts[18], Store.YES, Index.ANALYZED));
		}
		writer.addDocument(doc);
	}

	public void exportNode(IndexWriter writer, String[] parts, String organismId) throws IOException {
		Document doc = new Document();
		doc.add(new Field(LuceneMediator.TYPE, LuceneMediator.NODE, Store.YES, Index.ANALYZED));
		doc.add(new Field(LuceneMediator.NODE_ID, parts[0], Store.YES, Index.ANALYZED));
		doc.add(new Field(LuceneMediator.NODE_NAME, parts[1], Store.YES, Index.ANALYZED));
		doc.add(new Field(LuceneMediator.NODE_GENEDATA_ID, parts[2], Store.YES, Index.ANALYZED));
		doc.add(new Field(LuceneMediator.NODE_ORGANISM_ID, organismId, Store.NO, Index.ANALYZED));
		writer.addDocument(doc);
	}

	public void exportGene(IndexWriter writer, String[] parts) throws IOException {
		Document doc = new Document();
		doc.add(new Field(LuceneMediator.TYPE, LuceneMediator.GENE, Store.YES, Index.ANALYZED));
		doc.add(new Field(LuceneMediator.GENE_ID, parts[0], Store.YES, Index.ANALYZED));
		doc.add(new Field(LuceneMediator.GENE_SYMBOL, parts[1], Store.YES, Index.ANALYZED));
		doc.add(new Field(LuceneMediator.GENE_NAMINGSOURCE_ID, parts[3], Store.YES, Index.ANALYZED));
		doc.add(new Field(LuceneMediator.GENE_NODE_ID, parts[4], Store.YES, Index.ANALYZED));
		doc.add(new Field(LuceneMediator.GENE_ORGANISM_ID, parts[5], Store.YES, Index.ANALYZED));
		doc.add(new Field(LuceneMediator.GENE_DEFAULT_SELECTED, parts[6].equals("0") ? "false" : "true", Store.YES, Index.ANALYZED));
		writer.addDocument(doc);
	}

	public void exportNamingSource(IndexWriter writer, String[] parts) throws IOException {
		Document doc = new Document();
		doc.add(new Field(LuceneMediator.TYPE, LuceneMediator.NAMINGSOURCE, Store.YES, Index.ANALYZED));
		doc.add(new Field(LuceneMediator.NAMINGSOURCE_ID, parts[0], Store.YES, Index.ANALYZED));
		doc.add(new Field(LuceneMediator.NAMINGSOURCE_NAME, parts[1], Store.YES, Index.ANALYZED));
		doc.add(new Field(LuceneMediator.NAMINGSOURCE_RANK, parts[2], Store.YES, Index.ANALYZED));

		String namingSource = (parts[3].length() > 0) ? parts[3] : "";
		doc.add(new Field(LuceneMediator.NAMINGSOURCE_SHORT_NAME, namingSource, Store.YES, Index.ANALYZED));

		writer.addDocument(doc);
	}

	public void exportGeneData(IndexWriter writer, String[] parts, String externalId, long namingSourceId) throws IOException {
		Document doc = new Document();
		doc.add(new Field(LuceneMediator.TYPE, LuceneMediator.GENEDATA, Store.YES, Index.ANALYZED));
		doc.add(new Field(LuceneMediator.GENEDATA_ID, parts[0], Store.YES, Index.ANALYZED));
		doc.add(new Field(LuceneMediator.GENEDATA_DESCRIPTION, parts[1], Store.YES, Index.ANALYZED));

		if (externalId != null) {
			doc.add(new Field(LuceneMediator.GENEDATA_EXTERNAL_ID, externalId, Store.YES, Index.ANALYZED));
			doc.add(new Field(LuceneMediator.GENEDATA_NAMINGSOURCE_ID, String.valueOf(namingSourceId), Store.YES, Index.ANALYZED));
		}
		writer.addDocument(doc);
	}

	protected void exportTag(IndexWriter writer, String[] parts) throws IOException {
		Document doc = new Document();
		doc.add(new Field(LuceneMediator.TYPE, LuceneMediator.TAG, Store.YES, Index.ANALYZED));
		doc.add(new Field(LuceneMediator.TAG_ID, parts[0], Store.YES, Index.ANALYZED));
		doc.add(new Field(LuceneMediator.TAG_NAME, parts[1], Store.YES, Index.ANALYZED));
		writer.addDocument(doc);
	}

	protected void exportNetworkTagAssoc(IndexWriter writer, String[] parts) throws IOException {
		Document doc = new Document();
		doc.add(new Field(LuceneMediator.TYPE, LuceneMediator.NETWORKTAGASSOC, Store.YES, Index.ANALYZED));
		doc.add(new Field(LuceneMediator.NETWORKTAGASSOC_ID, parts[0], Store.YES, Index.ANALYZED));
		doc.add(new Field(LuceneMediator.NETWORKTAGASSOC_NETWORK_ID, parts[1], Store.YES, Index.ANALYZED));
		doc.add(new Field(LuceneMediator.NETWORKTAGASSOC_TAG_ID, parts[2], Store.YES, Index.ANALYZED));
		writer.addDocument(doc);
	}

	protected void exportAttributeGroup(IndexWriter writer,
			String[] parts)  throws IOException {
		Document doc = new Document();
		doc.add(new Field(LuceneMediator.TYPE, LuceneMediator.ATTRIBUTEGROUP, Store.YES, Index.ANALYZED));
		doc.add(new Field(LuceneMediator.ATTRIBUTEGROUP_ID, parts[0], Store.YES, Index.ANALYZED));
		doc.add(new Field(LuceneMediator.ATTRIBUTEGROUP_ORGANISM_ID, parts[1], Store.YES, Index.ANALYZED));
		doc.add(new Field(LuceneMediator.ATTRIBUTEGROUP_NAME, parts[2], Store.YES, Index.ANALYZED));
		doc.add(new Field(LuceneMediator.ATTRIBUTEGROUP_CODE, parts[3], Store.YES, Index.ANALYZED));
		doc.add(new Field(LuceneMediator.ATTRIBUTEGROUP_DESCRIPTION, parts[4], Store.YES, Index.ANALYZED));
		doc.add(new Field(LuceneMediator.ATTRIBUTEGROUP_LINKOUT_LABEL, parts[5], Store.YES, Index.ANALYZED));
		doc.add(new Field(LuceneMediator.ATTRIBUTEGROUP_LINKOUT_URL, parts[6], Store.YES, Index.ANALYZED));
		doc.add(new Field(LuceneMediator.ATTRIBUTEGROUP_DEFAULT_SELECTED, parts[7], Store.YES, Index.ANALYZED));
		doc.add(new Field(LuceneMediator.ATTRIBUTEGROUP_PUBLICATION_NAME, parts[8], Store.YES, Index.ANALYZED));
		doc.add(new Field(LuceneMediator.ATTRIBUTEGROUP_PUBLICATION_URL, parts[9], Store.YES, Index.ANALYZED));
		writer.addDocument(doc);
	}

	protected void exportAttribute(IndexWriter writer, String[] parts) throws IOException {
		Document doc = new Document();
		doc.add(new Field(LuceneMediator.TYPE, LuceneMediator.ATTRIBUTE, Store.YES, Index.ANALYZED));
		doc.add(new Field(LuceneMediator.ATTRIBUTE_ID, parts[0], Store.YES, Index.ANALYZED));
		doc.add(new Field(LuceneMediator.ATTRIBUTE_ORGANISM_ID, parts[1], Store.YES, Index.ANALYZED));
		doc.add(new Field(LuceneMediator.ATTRIBUTE_GROUP_ID, parts[2], Store.YES, Index.ANALYZED));
		doc.add(new Field(LuceneMediator.ATTRIBUTE_EXTERNAL_ID, parts[3], Store.YES, Index.ANALYZED));
		doc.add(new Field(LuceneMediator.ATTRIBUTE_NAME, parts[4], Store.YES, Index.ANALYZED));
		doc.add(new Field(LuceneMediator.ATTRIBUTE_DESCRIPTION, parts[5], Store.YES, Index.ANALYZED));
		writer.addDocument(doc);
	}

	interface FileHandler {
		boolean process(String line) throws IOException;
	}

	public String getGenericDbPath() {
		return genericDbPath;
	}

	public void setGenericDbPath(String genericDbPath) {
		this.genericDbPath = genericDbPath;
	}

	public String getIndexPath() {
		return indexPath;
	}

	public void setIndexPath(String indexPath) {
		this.indexPath = indexPath;
	}

	public String getBasePath() {
		return basePath;
	}

	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}

	public String getProfileName() {
		return profileName;
	}

	public void setProfileName(String profileName) {
		this.profileName = profileName;
	}

	public ConfigObj getConfig() {
		return config;
	}

	public void setConfig(ConfigObj config) {
		this.config = config;
	}
}
