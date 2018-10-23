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

package org.genemania.plugin.apps;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.genemania.domain.Gene;
import org.genemania.domain.GeneNamingSource;
import org.genemania.domain.InteractionNetwork;
import org.genemania.domain.Node;
import org.genemania.domain.Organism;
import org.genemania.dto.AttributeDto;
import org.genemania.dto.EnrichmentEngineRequestDto;
import org.genemania.dto.EnrichmentEngineResponseDto;
import org.genemania.dto.InteractionDto;
import org.genemania.dto.InteractionVisitor;
import org.genemania.dto.NetworkCombinationRequestDto;
import org.genemania.dto.NetworkDto;
import org.genemania.dto.NodeDto;
import org.genemania.dto.RelatedGenesEngineRequestDto;
import org.genemania.dto.RelatedGenesEngineResponseDto;
import org.genemania.engine.Constants;
import org.genemania.engine.Constants.ScoringMethod;
import org.genemania.engine.Mania2;
import org.genemania.engine.actions.CombineNetworks;
import org.genemania.engine.actions.FindRelated;
import org.genemania.engine.cache.DataCache;
import org.genemania.engine.cache.MemObjectCache;
import org.genemania.engine.cache.SynchronizedObjectCache;
import org.genemania.engine.core.MatrixUtils;
import org.genemania.engine.core.data.NodeIds;
import org.genemania.engine.core.integration.Feature;
import org.genemania.engine.core.integration.FeatureWeightMap;
import org.genemania.engine.core.mania.CoreMania;
import org.genemania.engine.labels.LabelVectorGenerator;
import org.genemania.engine.matricks.SymMatrix;
import org.genemania.exception.ApplicationException;
import org.genemania.exception.DataStoreException;
import org.genemania.mediator.GeneMediator;
import org.genemania.mediator.NodeMediator;
import org.genemania.plugin.GeneMania;
import org.genemania.plugin.NetworkUtils;
import org.genemania.plugin.controllers.IGeneProvider;
import org.genemania.plugin.controllers.RankedGeneProviderWithUniprotHack;
import org.genemania.plugin.data.DataSet;
import org.genemania.plugin.data.DataSetManager;
import org.genemania.plugin.formatters.FlatReportOutputFormatter;
import org.genemania.plugin.formatters.GeneListOutputFormatter;
import org.genemania.plugin.formatters.GeneScoresOutputFormatter;
import org.genemania.plugin.formatters.IOutputFormatter;
import org.genemania.plugin.formatters.XmlReportOutputFormatter;
import org.genemania.plugin.model.SearchResult;
import org.genemania.plugin.model.ViewState;
import org.genemania.plugin.model.impl.ViewStateImpl;
import org.genemania.plugin.parsers.IQueryParser;
import org.genemania.plugin.parsers.Query;
import org.genemania.plugin.parsers.TabDelimitedQueryParser;
import org.genemania.type.CombiningMethod;
import org.genemania.util.NullProgressReporter;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.xml.sax.SAXException;

import no.uib.cipr.matrix.Vector;

public class QueryRunner extends AbstractPluginDataApp {
	
	private static final int MIN_CATEGORIES = 10;

	private static final double Q_VALUE_THRESHOLD = 0.1;

	@Option(name = "--in", usage = "input format (one of: flat); defaults to \"flat\"")
	private String fInputFormat;
	
	@Option(name = "--out", usage = "output format (one of: genes, flat, xml, scores); defaults to \"genes\"")
	private String fOutputFormat;
	
	@Option(name = "--results", usage = "directory where results should be stored")
	private String fResultsPath = "."; //$NON-NLS-1$
	
	@Option(name = "--list-networks", usage = "list available networks for given organism")
	private String fListNetworksFor;
	
	@Option(name = "--list-genes", usage = "list recognized gene symbols for given organism")
	private String fListGenesFor;
	
	@Option(name = "--scoring-method", usage = "gene scoring method (one of: discriminant, z); defaults to \"discriminant\"")
	private String fScoringMethod = "discriminant"; //$NON-NLS-1$
	
	@Option(name = "--ids", usage = "comma-separated list gene identifier types to use in the output")
	private String fIds;
	
	private Mania2 fMania;
	private IQueryParser fQueryParser;
	private File fOutputDirectory;
	private QueryHandler fQueryHandler;

	private DataCache fCache;

	private NetworkUtils fNetworkUtils;
	
	private static Object fJobMutex = new Object();

	private void initialize() throws ApplicationException, DataStoreException {
		try {
			DataSetManager manager = createDataSetManager();
			fNetworkUtils = new NetworkUtils();
			
			fData = manager.open(new File(fDataPath));
			fCache = new DataCache(new SynchronizedObjectCache(new MemObjectCache(fData.getObjectCache(NullProgressReporter.instance(), false))));
			fMania = new Mania2(fCache);

			if (fInputFormat == null) {
				fQueryParser = new TabDelimitedQueryParser(fData);
			} else if ("flat".equals(fInputFormat)) { //$NON-NLS-1$
				fQueryParser = new TabDelimitedQueryParser(fData);
			} else {
				throw new ApplicationException(String.format("Unrecognized input format: %s", fInputFormat)); //$NON-NLS-1$
			}
			
			IGeneProvider geneProvider = parseIdTypes(fIds);
			
			if (fOutputFormat == null) {
				fQueryHandler = new DefaultQueryHandler(new GeneListOutputFormatter(geneProvider));
			} else if ("genes".equals(fOutputFormat)) { //$NON-NLS-1$
				fQueryHandler = new DefaultQueryHandler(new GeneListOutputFormatter(geneProvider));
			} else if ("scores".equals(fOutputFormat)) { //$NON-NLS-1$
				fQueryHandler = new DefaultQueryHandler(new GeneScoresOutputFormatter(geneProvider));
			} else if ("flat".equals(fOutputFormat)) { //$NON-NLS-1$
				fQueryHandler = new DefaultQueryHandler(new FlatReportOutputFormatter(fData, geneProvider));
			} else if ("xml".equals(fOutputFormat)) { //$NON-NLS-1$
				fQueryHandler = new DefaultQueryHandler(new XmlReportOutputFormatter(fData, geneProvider));
			} else if ("combined-network".equals(fOutputFormat)) { //$NON-NLS-1$
				fQueryHandler = new CombineNetworksQueryHandler(geneProvider);
			} else {
				throw new ApplicationException(String.format("Unrecognized output format: %s", fOutputFormat)); //$NON-NLS-1$
			}
			
			if (fThreads < 1) {
				fThreads = 1;
			}
			
			fOutputDirectory = new File(fResultsPath);
			if (!fOutputDirectory.exists()) {
				throw new ApplicationException(String.format("Output directory doesn't exist: %s", fResultsPath)); //$NON-NLS-1$
			}
			if (!fOutputDirectory.isDirectory()) {
				throw new ApplicationException(String.format("Output directory isn't a directory: %s", fResultsPath)); //$NON-NLS-1$
			}
		} catch (SAXException e) {
			throw new ApplicationException(e);
		}
	}
	
	private IGeneProvider parseIdTypes(String idList) {
		if (idList == null) {
			List<GeneNamingSource> userPreferences = Collections.emptyList();
			return new RankedGeneProviderWithUniprotHack(fData.getAllNamingSources(), userPreferences);
		}
		String[] items = idList.split(","); //$NON-NLS-1$
		GeneMediator mediator = fData.getMediatorProvider().getGeneMediator();
		List<GeneNamingSource> userPreferences = new ArrayList<GeneNamingSource>();
		for (String item : items) {
			String name = item.trim();
			GeneNamingSource source = findSourceByName(mediator, name);
			if (source != null) {
				userPreferences.add(source);
			}
		}
		return new RankedGeneProviderWithUniprotHack(fData.getAllNamingSources(), userPreferences);
	}

	private GeneNamingSource findSourceByName(GeneMediator mediator, String name) {
		// Hack to support differentiating between Uniprot accessions and
		// entry names.
		if (RankedGeneProviderWithUniprotHack.UNIPROT_AC.equals(name)) {
			GeneNamingSource prototype = mediator.findNamingSourceByName(RankedGeneProviderWithUniprotHack.UNIPROT_ID);
			if (prototype == null) {
				return null;
			}
			GeneNamingSource source = new GeneNamingSource();
			source.setId(prototype.getId());
			source.setName(RankedGeneProviderWithUniprotHack.UNIPROT_AC);
			source.setRank(prototype.getRank());
			source.setShortName(prototype.getShortName());
			return source;
		} else {
			return mediator.findNamingSourceByName(name);
		}
	}

	RelatedGenesEngineRequestDto createRequest(Query query) throws ApplicationException {
		RelatedGenesEngineRequestDto request = new RelatedGenesEngineRequestDto();
		request.setNamespace(GeneMania.DEFAULT_NAMESPACE);
		request.setOrganismId(query.getOrganism().getId());
		request.setInteractionNetworks(collapseNetworks(query.getGroups()));
		request.setAttributeGroups(collapseAttributeGroups(query.getGroups()));
		request.setPositiveNodes(query.getNodes());
		request.setLimitResults(query.getGeneLimit());
		request.setAttributesLimit(query.getAttributeLimit());
		request.setCombiningMethod(query.getCombiningMethod());
		
		if (query.getScoringMethod() != null) {
			request.setScoringMethod(query.getScoringMethod());
		} else {
			request.setScoringMethod(parseScoringMethod());
		}
		return request;
	}
	
	private org.genemania.type.ScoringMethod parseScoringMethod() throws ApplicationException {
		if ("discriminant".equals(fScoringMethod)) { //$NON-NLS-1$
			return org.genemania.type.ScoringMethod.DISCRIMINANT;
		}
		if ("z".equals(fScoringMethod)) { //$NON-NLS-1$
			return org.genemania.type.ScoringMethod.ZSCORE;
		}
		throw new ApplicationException(String.format("Unrecognized scoring method: %s", fScoringMethod)); //$NON-NLS-1$
	}

	RelatedGenesEngineResponseDto runQuery(RelatedGenesEngineRequestDto request) throws DataStoreException {
		try {
			request.setProgressReporter(NullProgressReporter.instance());
			
			RelatedGenesEngineResponseDto result;
			if ("scores".equals(fOutputFormat)) { //$NON-NLS-1$
				result = new FindAllRelated(fCache, request).process();
			} else {
				result = fMania.findRelated(request);
			}
			request.setCombiningMethod(result.getCombiningMethodApplied());
			fNetworkUtils.normalizeNetworkWeights(result);
			return result;
		} catch (ApplicationException e) {
			Logger logger = Logger.getLogger(getClass());
			logger.error("Unexpected error", e); //$NON-NLS-1$
			return null;
		}
	}
	
	private EnrichmentEngineRequestDto createEnrichmentRequest(Query query, RelatedGenesEngineResponseDto response) {
		if (query.getOrganism().getOntology() == null) {
			return null;
		}
		
		EnrichmentEngineRequestDto request = new EnrichmentEngineRequestDto();
		request.setProgressReporter(NullProgressReporter.instance());
		request.setMinCategories(MIN_CATEGORIES);
		request.setqValueThreshold(Q_VALUE_THRESHOLD);
		
		Organism organism = query.getOrganism();
		
		request.setOrganismId(organism.getId());
		request.setOntologyId(organism.getOntology().getId());
		
		Set<Long> nodes = new HashSet<Long>();
 		for (NodeDto node : response.getNodes()) {
			nodes.add(node.getId());
		}
		request.setNodes(nodes);
		return request;
	}

	private SearchResult runAlgorithm(DataSet data, Query query) throws DataStoreException, ApplicationException {
		RelatedGenesEngineRequestDto request = createRequest(query);
		RelatedGenesEngineResponseDto response = runQuery(request);
		EnrichmentEngineRequestDto enrichmentRequest;
		
		if ("scores".equals(fOutputFormat)) //$NON-NLS-1$
			enrichmentRequest = null;
		else
			enrichmentRequest = createEnrichmentRequest(query, response);
		
		EnrichmentEngineResponseDto enrichmentResponse = computeEnrichment(enrichmentRequest);

		List<String> queryGenes = query.getGenes();
		Organism organism = query.getOrganism();
		SearchResult options = fNetworkUtils.createSearchOptions(organism, request, response, enrichmentResponse, data, queryGenes);
		
		return options;
	}

	private EnrichmentEngineResponseDto computeEnrichment(EnrichmentEngineRequestDto request) throws ApplicationException {
		return request == null ? null : fMania.computeEnrichment(request);
	}

	File getOutputDirectory() {
		return fOutputDirectory;
	}
	
	private void runQuery(String filename, File outputDirectory) throws IOException, DataStoreException, ApplicationException {
		Query query;
		Reader reader = new InputStreamReader(new FileInputStream(filename), "UTF-8"); //$NON-NLS-1$
		
		try {
			query = fQueryParser.parse(reader, new IQueryErrorHandler() {
				public void handleUnrecognizedGene(String gene) {
					System.err.println(String.format("WARNING: Unrecognized gene \"%s\"", gene)); //$NON-NLS-1$
				}
				
				public void handleSynonym(String gene) {
					System.err.println(String.format("WARNING: Synonym \"%s\"", gene)); //$NON-NLS-1$
				}

				public void handleNetwork(InteractionNetwork network) {
					if (fVerbose) {
						System.err.println(String.format("INFO: Using network \"%s\"", network.getName())); //$NON-NLS-1$
					}
				}

				public void warn(String message) {
					System.err.println(String.format("WARNING: %s", message)); //$NON-NLS-1$
				}
				
				public void handleUnrecognizedNetwork(String network) {
					System.err.println(String.format("WARNING: Unrecognized network \"%s\"", network)); //$NON-NLS-1$
				}
			});
		} finally {
			reader.close();
		}

		File sourceFile = new File(filename);
		String baseName = sourceFile.getName();
		
		fQueryHandler.process(query, outputDirectory, baseName);
	}
	
	public void handleArguments() throws InterruptedException, ApplicationException, DataStoreException {
		initialize();

		if (fListNetworksFor != null) {
			printNetworks(fListNetworksFor);
			return;
		}
		
		if (fListGenesFor != null) {
			printGenes(fListGenesFor);
			return;
		}
		
		Logger.getLogger("org.genemania.plugin").setLevel(Level.WARN); //$NON-NLS-1$
		Logger logger = Logger.getLogger("org.genemania"); //$NON-NLS-1$
		logger.setLevel(Level.ERROR);
		
		List<String> arguments = getArguments();
		final Iterator<String> jobQueue = arguments.iterator();
		List<Thread> threads = new ArrayList<Thread>();
		long start = System.currentTimeMillis();
		
		for (int i = 0; i < getThreads(); i++) {
			final int threadId = i + 1;
			
			Thread thread = new Thread(new Runnable() {
				public void run() {
					while (true) {
						String filename;
						synchronized (fJobMutex) {
							if (jobQueue.hasNext()) {
								filename = jobQueue.next();
							} else {
								return;
							}
						}
						System.err.println(String.format("[Thread %d] Processing %s...", threadId, filename)); //$NON-NLS-1$
						try {
							runQuery(filename, getOutputDirectory());
						} catch (IOException e) {
							e.printStackTrace(System.err);
						} catch (DataStoreException e) {
							e.printStackTrace(System.err);
						} catch (ApplicationException e) {
							e.printStackTrace(System.err);
						}
						System.err.println(String.format("[Thread %d] Finished %s", threadId, filename)); //$NON-NLS-1$
					}
				}
			});
			threads.add(thread);
			thread.start();
		}
		
		for (Thread thread : threads) {
			thread.join();
		}
		long duration = System.currentTimeMillis() - start;
		System.err.println(String.format("Performed %d predictions in %.2fs", arguments.size(), duration / 1000.0)); //$NON-NLS-1$
	}

	public static void main(String[] args) throws Exception {
		Logger.getLogger("org.genemania").setLevel(Level.FATAL); //$NON-NLS-1$

		final QueryRunner runner = new QueryRunner();
		CmdLineParser parser = new CmdLineParser(runner);

		try {
			parser.parseArgument(args);
		} catch (CmdLineException e) {
			System.err.println(e.getMessage());
			System.err.println(String.format("\nUsage: %s options query-file-1 [query-file-2...]\n", //$NON-NLS-1$
					QueryRunner.class.getSimpleName()));
			parser.printUsage(System.err);
			
			return;
		}

		runner.handleArguments();
	}
	
	/**
	 * Hack class to support retrieving the scores for every single gene
	 * (related or not) with minimal code duplication.
	 */
	static class FindAllRelated extends FindRelated {
		// Hack: aliases so we can access FindRelated's internals
		private DataCache cache2;
		private RelatedGenesEngineRequestDto request2;

		public FindAllRelated(DataCache cache, RelatedGenesEngineRequestDto request) {
			super(cache, request);
			this.cache2 = cache;
			this.request2 = request;
		}
		
		@Override
		protected RelatedGenesEngineResponseDto prepareResponse(Vector scores,
				Vector discriminant, FeatureWeightMap featureWeights,
				SymMatrix combinedKernel, double scoreThreshold,
				ScoringMethod scoringMethod, CombiningMethod combiningMethod)
				throws ApplicationException {
	        NodeIds nodeIds = cache2.getNodeIds(request2.getOrganismId());
	        List<Integer> indicesForPositiveNodes = nodeIds.getIndicesForIds(request2.getPositiveNodes());

	        // We want all the scores so we'll override the threshold.
	        scoreThreshold = Double.NEGATIVE_INFINITY;
	        		
	        // for context score we still want the top nodes by discriminant. for other
	        // scoring methods just use the score itself.
	        int [] indices;
	        if (scoringMethod == ScoringMethod.CONTEXT) {
	            indices = MatrixUtils.getIndicesForTopScores(discriminant, indicesForPositiveNodes, nodeIds.getNodeIds().length, scoreThreshold);
	        }
	        else {
	            indices = MatrixUtils.getIndicesForTopScores(scores, indicesForPositiveNodes, nodeIds.getNodeIds().length, scoreThreshold);            
	        }
	        
            // Create a phony network containing all the genes where each edge
            // is just the gene "interacting" with itself.
	        RelatedGenesEngineResponseDto result = new RelatedGenesEngineResponseDto();
	        List<NetworkDto> networks = new ArrayList<NetworkDto>();
	        result.setNetworks(networks);
	        
	        NetworkDto network = new NetworkDto(0, 1);
	        networks.add(network);

	        List<NodeDto> nodes = new ArrayList<NodeDto>();
            for (int index : indices) {
	            long nodeId = nodeIds.getIdForIndex(index);
	
	            double score = scores.get(index);
	
	            NodeDto nodeVO = new NodeDto();
				nodeVO.setId(nodeId);
	            nodeVO.setScore(score);
	            nodes.add(nodeVO);
	            
	            InteractionDto interaction = new InteractionDto(nodeVO, nodeVO, 1);
				network.addInteraction(interaction);
            }
            result.setNodes(nodes);
	        return result;
		}
	}
	
	interface QueryHandler {
		void process(Query query, File outputDirectory, String baseName) throws ApplicationException, DataStoreException, IOException;
	}
	
	class DefaultQueryHandler implements QueryHandler {
		IOutputFormatter fFormatter;
		
		public DefaultQueryHandler(IOutputFormatter formatter) {
			fFormatter = formatter;
		}
		
		@Override
		public void process(Query query, File outputDirectory, String baseName) throws ApplicationException, DataStoreException, IOException {
			SearchResult options = runAlgorithm(fData, query);
			ViewState viewState = new ViewStateImpl(options);
			OutputStream out = new FileOutputStream(String.format("%s%s%s-results.%s", outputDirectory.getPath(), File.separator, baseName, fFormatter.getExtension())); //$NON-NLS-1$
			try {
				fFormatter.format(out, viewState);
			} finally {
				out.close();
			}
		}
	}
	
	@SuppressWarnings("nls")
	class CombineNetworksQueryHandler implements QueryHandler {
		static final int INTERACTIONS_PER_DOT = 100000;
		
		private IGeneProvider fGeneProvider;
		Map<Long, String> fSymbolCache;
		private NodeMediator fNodeMediator;
		
		public CombineNetworksQueryHandler(IGeneProvider geneProvider) {
			fGeneProvider = geneProvider;
			fSymbolCache = new HashMap<Long, String>();
			fNodeMediator = fData.getMediatorProvider().getNodeMediator();
		}

		String findSymbol(long nodeId, long organismId) {
			String symbol = fSymbolCache.get(nodeId);
			if (symbol != null) {
				return symbol;
			}
			
			Node node = fNodeMediator.getNode(nodeId, organismId);
			if (node == null) {
				return null;
			}
			
			Gene gene = fGeneProvider.getGene(node);
			if (gene == null) {
				return null;
			}
			
			symbol = gene.getSymbol();
			fSymbolCache.put(nodeId, symbol);
			return symbol;
		}
		
		@Override
		public void process(Query query, File outputDirectory, String baseName) throws ApplicationException, DataStoreException, IOException {
			CoreMania mania = new CoreMania(fCache);
			Organism organism = query.getOrganism();
			final long organismId = organism.getId();
			String namespace;
			if (organismId < 0) {
				namespace = GeneMania.DEFAULT_NAMESPACE;
			} else {
				namespace = "CORE";
			}
			Collection<Collection<Long>> networkIds = collapseNetworks(query.getGroups());
			Collection<Long> attributeGroupIds = collapseAttributeGroups(query.getGroups());
			int attributeLimit = query.getAttributeLimit();
			
	        if (fVerbose) {
	        	System.err.println("Computing weights...");
	        }
			// Compute weights
			Collection<Long> nodes = query.getNodes();
            ArrayList<Long> negativeNodes = new ArrayList<Long>();
            double posLabelValue = 1.0;
			double negLabelValue = -1.0;
			double unLabeledValueProduction = -1.0;
			Vector labels = LabelVectorGenerator.createLabelsFromIds(fCache.getNodeIds(organismId),
                    nodes, negativeNodes, posLabelValue, negLabelValue, unLabeledValueProduction);
			Constants.CombiningMethod combiningMethod = Constants.convertCombiningMethod(query.getCombiningMethod(), nodes.size());
			mania.computeWeights(namespace, organismId, labels, combiningMethod, networkIds, attributeGroupIds, attributeLimit);
			
	        if (fVerbose) {
	        	System.err.println("Computing combined network...");
	        }
			// Extract weights
			FeatureWeightMap featureWeights = mania.getFeatureWeights();
			List<AttributeDto> attributes = new ArrayList<AttributeDto>();
			List<NetworkDto> networks = new ArrayList<NetworkDto>();			
	        for (Entry<Feature, Double> entry: featureWeights.entrySet()) {
	        	Feature feature = entry.getKey();
	            if (featureWeights.get(feature) <= 0d) {
	                continue;
	            }
	            
	            switch (feature.getType()) {
	            case ATTRIBUTE_VECTOR:
					AttributeDto attribute = new AttributeDto();
					attribute.setId(feature.getId());
					attribute.setGroupId(feature.getGroupId());
					attribute.setWeight(entry.getValue());
					attributes.add(attribute);
					break;
	            case SPARSE_MATRIX:
	            	NetworkDto network = new NetworkDto();
	            	network.setId(feature.getId());
	            	network.setWeight(entry.getValue());
	            	networks.add(network);
	            	break;
	            case BIAS:
	            	// Ignore
	            }
			}
			
	        if (fVerbose) {
	        	System.err.print("Writing interactions");
	        }
	        // Generate combined network
			NetworkCombinationRequestDto request = new NetworkCombinationRequestDto();
			request.setOrganismId(organismId);
			request.setNamespace(namespace);
			request.setAttributes(attributes);
			request.setNetworks(networks);
			request.setProgressReporter(NullProgressReporter.instance());
			final PrintWriter writer = new PrintWriter(new BufferedOutputStream(new FileOutputStream(String.format("%s%s%s-results.combined-network.txt", outputDirectory.getPath(), File.separator, baseName))));
			try {
				request.setInteractionVistor(new InteractionVisitor() {
					int counter;
					@Override
					public void visit(long node1, long node2, double weight) {
						if (weight == 0) {
							return;
						}
						if (fVerbose) {
							if (counter % INTERACTIONS_PER_DOT == 0) {
								System.err.print(".");
							}
						}
						String symbol1 = findSymbol(node1, organismId);
						String symbol2 = findSymbol(node2, organismId);
						writer.printf("%s\t%s\t%s\n", symbol1, symbol2, Double.toString(weight));
						counter++;
					}
				});
				CombineNetworks combineNetworks = new CombineNetworks(fCache, request);
				combineNetworks.process();
			} finally {
				writer.close();
			}
	        if (fVerbose) {
	        	System.err.println("\nDone.");
	        }
		}
	}
}
