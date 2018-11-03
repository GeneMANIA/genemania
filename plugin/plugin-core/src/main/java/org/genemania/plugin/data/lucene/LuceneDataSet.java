/**
 * This file is part of GeneMANIA.
 * Copyright (C) 2008-2011 University of Toronto.
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
package org.genemania.plugin.data.lucene;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Searchable;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.genemania.data.classification.IGeneClassifier;
import org.genemania.data.classification.lucene.LuceneGeneClassifier;
import org.genemania.data.normalizer.GeneCompletionProvider2;
import org.genemania.domain.GeneNamingSource;
import org.genemania.domain.InteractionNetwork;
import org.genemania.domain.InteractionNetworkById;
import org.genemania.domain.InteractionNetworkGroup;
import org.genemania.domain.InteractionNetworkGroupById;
import org.genemania.domain.Organism;
import org.genemania.dto.RemoveNetworkEngineRequestDto;
import org.genemania.engine.IMania;
import org.genemania.engine.Mania2;
import org.genemania.engine.cache.DataCache;
import org.genemania.engine.cache.MemObjectCache;
import org.genemania.engine.core.data.Data;
import org.genemania.engine.core.data.NetworkIds;
import org.genemania.exception.ApplicationException;
import org.genemania.exception.DataStoreException;
import org.genemania.mediator.AttributeMediator;
import org.genemania.mediator.GeneMediator;
import org.genemania.mediator.NetworkMediator;
import org.genemania.mediator.NodeMediator;
import org.genemania.mediator.OntologyMediator;
import org.genemania.mediator.OrganismMediator;
import org.genemania.mediator.StatsMediator;
import org.genemania.mediator.lucene.AbstractCollector;
import org.genemania.mediator.lucene.LuceneAttributeMediator;
import org.genemania.mediator.lucene.LuceneGeneMediator;
import org.genemania.mediator.lucene.LuceneMediator;
import org.genemania.mediator.lucene.LuceneNamingSourceMediator;
import org.genemania.mediator.lucene.LuceneNetworkMediator;
import org.genemania.mediator.lucene.LuceneNodeMediator;
import org.genemania.mediator.lucene.LuceneOntologyMediator;
import org.genemania.mediator.lucene.LuceneOrganismMediator;
import org.genemania.mediator.lucene.LuceneStatsMediator;
import org.genemania.plugin.FileUtils;
import org.genemania.plugin.Strings;
import org.genemania.plugin.cytoscape.CytoscapeUtils;
import org.genemania.plugin.data.Colour;
import org.genemania.plugin.data.Configuration;
import org.genemania.plugin.data.DataDescriptor;
import org.genemania.plugin.data.DataSet;
import org.genemania.plugin.data.DataSetManager;
import org.genemania.plugin.data.DefaultConfirmationHandler;
import org.genemania.plugin.data.IConfiguration;
import org.genemania.plugin.data.IConfirmationHandler;
import org.genemania.plugin.data.IMediatorProvider;
import org.genemania.plugin.data.IModelManager;
import org.genemania.plugin.data.IModelWriter;
import org.genemania.plugin.data.Namespace;
import org.genemania.plugin.task.TaskDispatcher;
import org.genemania.plugin.view.util.UiUtils;
import org.genemania.util.ChildProgressReporter;
import org.genemania.util.NullProgressReporter;
import org.genemania.util.ProgressReporter;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class LuceneDataSet extends DataSet {
	
	private static final String METADATA_SHORT_NAME = "short_name"; //$NON-NLS-1$
	private static final String METADATA_COMMON_NAME = "common_name"; //$NON-NLS-1$
	
	private Searcher searcher;
	
	private final Analyzer analyzer;
	private final Queries queries;
	
	// Not needed for headless mode
	private final DataSetManager dataSetManager;
	private final UiUtils uiUtils;
	private final FileUtils fileUtils;
	private final CytoscapeUtils cytoscapeUtils;
	private final TaskDispatcher taskDispatcher;

	public LuceneDataSet(
			File path,
			Node root,
			DataSetManager dataSetManager,
			UiUtils uiUtils,
			FileUtils fileUtils,
			CytoscapeUtils cytoscapeUtils,
			TaskDispatcher taskDispatcher
	) throws SAXException {
		super(path, root);
		setHeadless(uiUtils == null);
		this.dataSetManager = dataSetManager;
		this.uiUtils = uiUtils;
		this.fileUtils = fileUtils;
		this.cytoscapeUtils = cytoscapeUtils;
		this.taskDispatcher = taskDispatcher;
		
		this.queries = new Queries();
		analyzer = LuceneMediator.createDefaultAnalyzer();
		try {
			reload(NullProgressReporter.instance());
		} catch (IOException e) {
			throw new SAXException(e);
		}
	}
	
	public LuceneDataSet(File path, Node root, FileUtils fileUtils, CytoscapeUtils cytoscapeUtils,
			TaskDispatcher taskDispatcher) throws SAXException {
		this(path, root, null, null, fileUtils, cytoscapeUtils, taskDispatcher);
	}
	
	public Searcher getSearcher() {
		return searcher;
	}
	
	private Searcher createSearcher(String indexPath) throws IOException {
		ArrayList<Searcher> searchers = new ArrayList<>();
		
		File indices = new File(indexPath);
		for (File file : indices.listFiles()) {
			try {
				if (!LuceneMediator.indexExists(file)) {
					continue;
				}
				FSDirectory directory = FSDirectory.open(file);
				searchers.add(new IndexSearcher(directory));
			} catch (IOException e) {
				log(e);
			}
		}
		if (searchers.size() == 0) {
			Directory directory = createEmptyIndex();
			searchers.add(new IndexSearcher(directory));
		}
		return new MultiSearcher(searchers.toArray(new Searchable[searchers.size()]));
	}

	private Directory createEmptyIndex() {
		RAMDirectory directory = new RAMDirectory();
		try {
			IndexWriter writer = new IndexWriter(directory, new StandardAnalyzer(Version.LUCENE_29), true, MaxFieldLength.UNLIMITED);
			writer.commit();
			writer.close();
		} catch (CorruptIndexException e) {
		} catch (LockObtainFailedException e) {
		} catch (IOException e) {
		}
		return directory;
	}

	@Override
	public IMediatorProvider getMediatorProvider() {
		return new LuceneMediatorProvider();
	}
	
	class LuceneMediatorProvider implements IMediatorProvider {
		private GeneMediator geneMediator;
		private NetworkMediator networkMediator;
		private NodeMediator nodeMediator;
		private StatsMediator statsMediator;
		private OntologyMediator ontologyMediator;
		private AttributeMediator attributeMediator;
		
		public OrganismMediator getOrganismMediator() {
			return new LuceneOrganismMediator(searcher, analyzer);
		}
		
		public GeneMediator getGeneMediator() {
			synchronized (this) {
				if (geneMediator == null) {
					geneMediator = new LuceneGeneMediator(searcher, analyzer); 
				}
			}
			return geneMediator;
		}

		public NetworkMediator getNetworkMediator() {
			synchronized (this) {
				if (networkMediator == null) {
					networkMediator = new LuceneNetworkMediator(searcher, analyzer); 
				}
			}
			return networkMediator;
		}

		public NodeMediator getNodeMediator() {
			synchronized (this) {
				if (nodeMediator == null) {
					nodeMediator = new LuceneNodeMediator(searcher, analyzer); 
				}
			}
			return nodeMediator;
		}

		public StatsMediator getStatsMediator() {
			synchronized (this) {
				if (statsMediator == null) {
					statsMediator = new LuceneStatsMediator(searcher, analyzer); 
				}
			}
			return statsMediator;
		}

		public OntologyMediator getOntologyMediator() {
			synchronized (this) {
				if (ontologyMediator == null) {
					ontologyMediator = new LuceneOntologyMediator(searcher, analyzer); 
				}
			}
			return ontologyMediator;
		}
		
		@Override
		public AttributeMediator getAttributeMediator() {
			synchronized (this) {
				if (attributeMediator == null) {
					attributeMediator = new LuceneAttributeMediator(searcher, analyzer);
				}
			}
			return attributeMediator;
		}
	}

	@Override
	public IConfiguration getConfiguration() {
		if (isHeadless()) {
			return new Configuration(this);
		} else {
			return new LuceneConfiguration(this, dataSetManager, uiUtils, fileUtils, cytoscapeUtils, taskDispatcher);
		}
	}
	
	public List<DataDescriptor> getInstalledDataDescriptors() {
		List<DataDescriptor> descriptors = new ArrayList<DataDescriptor>();
		String indexPath = getBasePath();
		File indices = new File(indexPath);
		for (File file : indices.listFiles()) {
			if (!file.isDirectory()) {
				continue;
			}
			String name = file.getName();
			if (name.equalsIgnoreCase(BASE)) {
				continue;
			}
			if (name.equalsIgnoreCase(USER)) {
				continue;
			}
			try {
				if (LuceneMediator.indexExists(file)) {
					String id = name;
					String description = null;
					
					File metadataFile = new File(String.format("%s%smetadata.xml", file.getPath(), File.separator)); //$NON-NLS-1$
					Properties metadata = loadMetadata(metadataFile);
					if (metadata != null) {
						String shortName = metadata.getProperty(METADATA_SHORT_NAME);
						String commonName = metadata.getProperty(METADATA_COMMON_NAME);
						description = String.format("%s (%s)", shortName, commonName); //$NON-NLS-1$
					}
					if (id != null) {
						if (description == null) {
							description = id;
						}
						descriptors.add(new DataDescriptor(id, description));
					}
				}
			} catch (IOException e) {
				log(e);
			}
		}
		return descriptors;
	}
	
	Properties loadMetadata(File file) throws IOException {
		if (!file.isFile()) {
			return null;
		}
		Properties properties = new Properties();
		FileInputStream stream = new FileInputStream(file);
		try {
			properties.loadFromXML(stream);
			return properties;
		} finally {
			stream.close();
		}
	}

	public Set<Long> getOrganismsForIndex(String name) throws IOException {
		final Set<Long> organisms = new HashSet<Long>();
		String path = String.format("%s%s%s", getBasePath(), File.separator, name); //$NON-NLS-1$
		FSDirectory directory = FSDirectory.open(new File(path));
		final IndexSearcher searcher = new IndexSearcher(directory);
		try {
			Query query = new TermQuery(new Term(LuceneMediator.TYPE, LuceneMediator.ORGANISM));
			searcher.search(query, new AbstractCollector() {
				@Override
				public void handleHit(int doc) {
					try {
						Document document = searcher.doc(doc);
						long id = Long.parseLong(document.get(LuceneMediator.ORGANISM_ID));
						organisms.add(id);
					} catch (IOException e) {
					}
				}
			});
			return organisms;
		} finally {
			searcher.close();
		}
	}
	
	@Override
	public IModelWriter createModelWriter() throws ApplicationException {
		return new LuceneModelWriter(createIndexWriter());
	}
	
	@Override
	public IModelManager createModelManager(Namespace namespace) throws ApplicationException {
		return new LuceneModelManager(this, createConfirmationHandler(), createModelWriter(), namespace, fileUtils);
	}
	
	private IConfirmationHandler createConfirmationHandler() {
		if (isHeadless()) {
			return DefaultConfirmationHandler.instance();
		} else {
			return new LuceneConfirmationHandler(uiUtils, taskDispatcher);
		}
	}

	private IndexWriter createIndexWriter() throws ApplicationException {
		Analyzer analyzer = LuceneMediator.createDefaultAnalyzer();
		String userIndexPath = getFullPath(DataSet.USER);
		try {
			FSDirectory directory = FSDirectory.open(new File(userIndexPath));
			boolean create = !IndexReader.indexExists(directory);
			return new IndexWriter(directory, analyzer, create, MaxFieldLength.UNLIMITED);
		} catch (IOException e) {
			throw new ApplicationException(e);
		}
	}

	public List<DataDescriptor> getAvailableDataDescriptors() throws IOException {
		List<DataDescriptor> result = new ArrayList<DataDescriptor>();
		String baseUrl = fileUtils.findDataSetBaseUrl(FileUtils.DEFAULT_BASE_URL, getVersion().toString());
		InputStream stream = null;
		
		try {
			
			URL url = new URL(String.format("%s.xml", baseUrl)); //$NON-NLS-1$
			URLConnection connection = fileUtils.getUrlConnection(url);
			stream = connection.getInputStream();
		} catch (MalformedURLException e) {
			log(e);
		} catch (IOException e) {
			throw new IOException(Strings.checkForUpdates_error, e);
		}
		
		try {
			Element root = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stream).getDocumentElement();
			XPath xpath = XPathFactory.newInstance().newXPath();
			NodeList nodes = (NodeList) xpath.evaluate("data", root, XPathConstants.NODESET); //$NON-NLS-1$
			
			for (int i = 0; i < nodes.getLength(); i++) {
				Element node = (Element) nodes.item(i);
				String id = node.getAttribute("path"); //$NON-NLS-1$
				String name = (String) xpath.evaluate("name", node, XPathConstants.STRING);  //$NON-NLS-1$
				String description = (String) xpath.evaluate("description", node, XPathConstants.STRING);  //$NON-NLS-1$
				result.add(new DataDescriptor(id, String.format("%s %s", name, description))); //$NON-NLS-1$
			}
		} catch (IOException e) {
			log(e);
		} catch (SAXException e) {
			log(e);
		} catch (ParserConfigurationException e) {
			log(e);
		} catch (XPathExpressionException e) {
			log(e);
		} finally {
			try {
				if (stream != null) stream.close();
			} catch (Exception e) {}
		}
		
		return result;
	}

	public void deleteIndex(String name) throws ApplicationException {
		try {
			// Clear the cache first
			Set<Long> organisms = getOrganismsForIndex(name);
			File cache = new File(getFullPath(CACHE_PATH));
			deleteCache2(cache, organisms);
			
			for (long organismId : organisms) {
				String indexPath = getFullPath(String.valueOf(organismId));
				FSDirectory directory = FSDirectory.open(new File(indexPath));
				if (IndexReader.indexExists(directory)) {
					fileUtils.delete(new File(indexPath));
				}
			}
		} catch (IOException e) {
			throw new ApplicationException(e);
		} catch (DataStoreException e) {
			throw new ApplicationException(e);
		}
	}

	private void deleteCache2(File root, Set<Long> organisms) throws ApplicationException, DataStoreException {
		// Remove network data
		IMania mania = new Mania2(new DataCache(new MemObjectCache(getObjectCache(NullProgressReporter.instance(), false))));
		
		try {
			for (Long organismId : organisms) {
				RemoveNetworkEngineRequestDto request = new RemoveNetworkEngineRequestDto();
				request.setOrganismId(organismId);
				request.setNamespace(Data.CORE);
				mania.removeUserNetworks(request);
			}
		} catch (ApplicationException e) {
			log(e);
		} 
	}

	public void reload(ProgressReporter progress) throws IOException {
		searcher = createSearcher(getBasePath());
		if (!validateUserNetworks()) {
			// Validation caused some clean up--force reload again.
			searcher = createSearcher(getBasePath());
		}
	}
	
	private boolean validateUserNetworks() {
		boolean requiresReload = false;
		try {
			DataCache cache = new DataCache(new MemObjectCache(getObjectCache(NullProgressReporter.instance(), false)));

			// If we have user network models but no cache file or an
			// inconsistent model, delete the orphaned model
			IModelWriter writer = createModelWriter();
			
			try {
				for (InteractionNetwork network : getUserNetworks()) {
					boolean purge = false;
					InteractionNetworkGroup group = getNetworkGroup(network.getId());
					
					if (group == null) {
						purge = true;
					} else {
						Organism organism = getOrganism(group.getId());
						
						if (organism == null) {
							purge = true;
						} else {
							try {
								cache.getNetwork(USER, organism.getId(), network.getId());
							} catch (ApplicationException e) {
								purge = true;
							}
						}
					}
			
					if (purge) {
						writer.deleteNetwork(network);
						requiresReload = true;
					}
				}
			} finally {
				writer.close();
			}
			
			// If we have cache files that don't correspond to a model,
			// delete the orphaned cache files
			IMania mania = new Mania2(cache);
			for (Organism organism : getMediatorProvider().getOrganismMediator().getAllOrganisms()) {
				NetworkIds networkIds;
				try {
					networkIds = cache.getNetworkIds(USER, organism.getId());
				} catch (ApplicationException e) {
					break;
				}
				for (long networkId : networkIds.getNetworkIds()) {
					try {
						InteractionNetworkGroup group = getNetworkGroup(networkId);
						if (group != null) {
							continue;
						}
						RemoveNetworkEngineRequestDto request = new RemoveNetworkEngineRequestDto();
						request.setNamespace(USER);
						request.setNetworkId(networkId);
						request.setOrganismId(organism.getId());
						mania.removeUserNetworks(request);
					} catch (ApplicationException e) {
						log(e);
					}
				}
			}
		} catch (ApplicationException e) {
			log(e);
		} catch (DataStoreException e) {
			log(e);
		}
		return !requiresReload;
	}

	public void installIndex(final String name, String description, ProgressReporter progress) throws ApplicationException {
		progress.setMaximumProgress(4);
		progress.setStatus(String.format(Strings.installIndex_status, description));
		
		try {
			String baseUrl = fileUtils.findDataSetBaseUrl(FileUtils.DEFAULT_BASE_URL, getVersion().toString());
			URL indexUrl = new URL(String.format("%s/%s.zip", baseUrl, URLEncoder.encode(name, "utf-8"))); //$NON-NLS-1$ //$NON-NLS-2$
			URL cacheUrl = new URL(String.format("%s/%s.cache.zip", baseUrl, URLEncoder.encode(name, "utf-8"))); //$NON-NLS-1$ //$NON-NLS-2$
			
			File indexDestination = new File(getBasePath());
			File cacheDestination = new File(getFullPath(CACHE_PATH));
			
			if (progress.isCanceled()) {
				return;
			}
			ChildProgressReporter childProgress = new ChildProgressReporter(progress);
			File indexZip = fileUtils.download(indexUrl, indexDestination, childProgress);
			childProgress.close();
			if (indexZip == null) {
				return;
			}
			try {
				if (progress.isCanceled()) {
					return;
				}
				childProgress = new ChildProgressReporter(progress);
				fileUtils.unzip(indexZip, indexDestination, childProgress);
				childProgress.close();
			} finally {
				indexZip.delete();
			}
			
			if (progress.isCanceled()) {
				return;
			}
			childProgress = new ChildProgressReporter(progress);
			File cacheZip = fileUtils.download(cacheUrl, indexDestination, childProgress);
			childProgress.close();
			if (cacheZip == null) {
				return;
			}
			try {
				if (progress.isCanceled()) {
					return;
				}
				childProgress = new ChildProgressReporter(progress);
				fileUtils.unzip(cacheZip, cacheDestination, childProgress);
				childProgress.close();
			} finally {
				cacheZip.delete();
			}
		} catch (IOException e) {
			throw new ApplicationException(e);
		}
	}

	@Override
	public GeneCompletionProvider2 getCompletionProvider(Organism organism) {
		return new GeneCompletionProvider2(searcher, analyzer, organism);
	}
	
	@Override
	public Colour getColor(String code) {
		String color = getFieldByValue(LuceneMediator.GROUP_CODE, code, LuceneMediator.GROUP_COLOUR);
		
		return color != null ? new Colour(Integer.parseInt(color, 16)) : null;
	}
	
	@Override
	public InteractionNetworkGroup getNetworkGroup(long networkId) {
		LuceneMediator mediator = new LuceneMediator(searcher, LuceneMediator.createDefaultAnalyzer());
		String value = getFieldById(LuceneMediator.NETWORK_ID, networkId, LuceneMediator.NETWORK_GROUP_ID);
		if (value == null) {
			return null;
		}
		long groupId = Long.parseLong(value);
		Document document = getDocumentById(LuceneMediator.GROUP_ID, groupId);
		if (document == null) {
			return null;
		}
		return mediator.createNetworkGroup(document);
	}

	@Override
	public InteractionNetworkGroupById getNetworkGroupById(long networkId) {
		LuceneMediator mediator = new LuceneMediator(searcher, LuceneMediator.createDefaultAnalyzer());
		String value = getFieldById(LuceneMediator.NETWORK_ID, networkId, LuceneMediator.NETWORK_GROUP_ID);
		if (value == null) {
			return null;
		}
		long groupId = Long.parseLong(value);
		Document document = getDocumentById(LuceneMediator.GROUP_ID, groupId);
		if (document == null) {
			return null;
		}
//		TODO need to add adapter to convert InteractionNetworkGroup to InteractionNetworkGroupById
		return mediator.createNetworkGroupById(document);
	}
	
	@Override
	public Organism getOrganism(long networkGroupId) {
		String value = getFieldById(LuceneMediator.GROUP_ID, networkGroupId, LuceneMediator.GROUP_ORGANISM_ID);
		if (value == null) {
			return null;
		}
		long organismId = Long.parseLong(value);
		OrganismMediator mediator = getMediatorProvider().getOrganismMediator();
		try {
			return mediator.getOrganism(organismId);
		} catch (DataStoreException e) {
			log(e);
			return null;
		}
	}
	
	protected Document getDocumentById(String sourceField, long id) {
		return getDocumentByValue(sourceField, String.valueOf(id));
	}
	
	protected Document getDocumentByValue(String sourceField, String value) {
		Query query = new TermQuery(new Term(sourceField, value));
		try {
			TopDocs docs = searcher.search(query, 1);
			if (docs.totalHits > 0) {
				ScoreDoc scoreDoc = docs.scoreDocs[0];
				Document document = searcher.doc(scoreDoc.doc);
				return document;
			}
		} catch (IOException e) {
			log(e);
		}
		return null;
	}

	protected String getFieldById(String sourceField, long id, String targetField) {
		Document document = getDocumentById(sourceField, id);
		if (document == null) {
			return null;
		}
		return document.get(targetField);
	}
	
	protected String getFieldByValue(String sourceField, String value, String targetField) {
		Document document = getDocumentByValue(sourceField, value);
		if (document == null) {
			return null;
		}
		return document.get(targetField);
	}

	public List<Long> getNodeIds(long organismId) {
		final List<Long> ids = new ArrayList<Long>();
		TermQuery query = new TermQuery(new Term(LuceneMediator.NODE_ORGANISM_ID, String.valueOf(organismId)));
		try {
			searcher.search(query, new AbstractCollector() {
				@Override
				public void handleHit(int doc) {
					try {
						Document document = searcher.doc(doc);
						ids.add(Long.parseLong(document.get(LuceneMediator.NODE_ID)));
					} catch (IOException e) {
						log(e);
					}
				}
			});
		} catch (IOException e) {
			log(e);
		}
		return ids;
	}

	@Override
	public List<GeneNamingSource> getAllNamingSources() {
		LuceneNamingSourceMediator mediator = new LuceneNamingSourceMediator(searcher, analyzer);
		return mediator.getAllNamingSources();
	}

	@Override
	public IGeneClassifier getGeneClassifier() {
		return new LuceneGeneClassifier(searcher, analyzer);
	}

	@Override
	public <T> Long getNextAvailableId(Class<T> modelClass, Namespace namespace) {
		String attribute = Schema.getTypeField(modelClass);
		String id = Schema.getIdField(modelClass);
		if (attribute == null || id == null) {
			return null;
		}
		try {
			if (namespace == Namespace.CORE) {
				return queries.getNextAvailableCoreId(searcher, attribute, id);
			} else {
				return queries.getNextAvailableUserId(searcher, attribute, id);
			}
		} catch (ApplicationException e) {
			log(e);
			return null;
		}
	}
	
	@Override
	public Collection<InteractionNetwork> getUserNetworks() throws ApplicationException {
		return  queries.getUserDefinedNetworks(getSearcher(), getMediatorProvider().getNetworkMediator());
	}

	public void deleteOrganism(Organism organism) throws ApplicationException, DataStoreException {
		Set<Long> organisms = new HashSet<Long>();
		organisms.add(organism.getId());
		File cache = new File(getFullPath(CACHE_PATH));
		deleteCache2(cache, organisms);
		
		IModelWriter writer = createModelWriter();
		try {
			writer.deleteOrganismNodesAndGenes(organism);
		} finally {
			writer.close();
		}
	}
}
