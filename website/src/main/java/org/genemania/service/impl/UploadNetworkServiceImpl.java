package org.genemania.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.genemania.Constants;
import org.genemania.connector.EngineConnector;
import org.genemania.connector.LuceneConnector;
import org.genemania.dao.NetworkDao;
import org.genemania.dao.OrganismDao;
import org.genemania.data.classification.IGeneClassifier;
import org.genemania.data.classification.lucene.LuceneGeneClassifier;
import org.genemania.data.normalizer.DataFileClassifier;
import org.genemania.data.normalizer.DataImportSettings;
import org.genemania.data.normalizer.DataNormalizer;
import org.genemania.data.normalizer.GeneCompletionProvider2;
import org.genemania.data.normalizer.NormalizationResult;
import org.genemania.data.normalizer.OrganismClassifier;
import org.genemania.domain.Interaction;
import org.genemania.domain.InteractionNetwork;
import org.genemania.domain.NetworkMetadata;
import org.genemania.domain.Organism;
import org.genemania.domain.Tag;
import org.genemania.dto.UploadNetworkWebRequestDto;
import org.genemania.dto.UploadNetworkWebResponseDto;
import org.genemania.exception.ApplicationException;
import org.genemania.exception.DataStoreException;
import org.genemania.exception.SystemException;
import org.genemania.exception.ValidationException;
import org.genemania.mediator.impl.CachingGeneMediator;
import org.genemania.mediator.lucene.LuceneGeneMediator;
import org.genemania.service.NetworkService;
import org.genemania.service.UploadNetworkService;
import org.genemania.type.DataLayout;
import org.genemania.type.NetworkProcessingMethod;
import org.genemania.util.ApplicationConfig;
import org.genemania.util.NullProgressReporter;
import org.genemania.util.UidGenerator;
import org.genemania.util.UserNetworkConstants;
import org.springframework.beans.factory.annotation.Autowired;

public class UploadNetworkServiceImpl implements UploadNetworkService {

	private static Logger LOG = Logger
			.getLogger(UploadNetworkServiceImpl.class);

	@Autowired
	EngineConnector engineConnector;

	@Autowired
	OrganismDao organismDao;

	@Autowired
	NetworkDao networkDao;

	@Autowired
	NetworkService networkService;

	private static class NetworkData {
		// __[attributes]___________________________________________________________
		private String normalizedData;
		private Set<String> invalidInteractions = new HashSet<String>();
		private NormalizationResult normalizationResult;
		private DataImportSettings dataImportSettings;
		private long organismId;
		private long id;
		private String sessionId;
		private String name;

		// __[constructors]_________________________________________________________
		public NetworkData() {
			this.id = UidGenerator.getInstance().getNegativeUid();
		}

		// __[accessors]____________________________________________________________

		public Set<String> getInvalidInteractions() {
			return invalidInteractions;
		}

		public void setInvalidInteractions(Set<String> invalidInteractions) {
			this.invalidInteractions = invalidInteractions;
		}

		public String getNormalizedData() {
			return normalizedData;
		}

		public void setNormalizedData(String normalizedData) {
			this.normalizedData = normalizedData;
		}

		public NormalizationResult getNormalizationResult() {
			return normalizationResult;
		}

		public void setNormalizationResult(
				NormalizationResult normalizationResult) {
			this.normalizationResult = normalizationResult;
		}

		public DataImportSettings getDataImportSettings() {
			return dataImportSettings;
		}

		public void setDataImportSettings(DataImportSettings dataImportSettings) {
			this.dataImportSettings = dataImportSettings;
		}

		public long getOrganismId() {
			return organismId;
		}

		public void setOrganismId(long organismId) {
			this.organismId = organismId;
		}

		public long getId() {
			return id;
		}

		public void setId(long id) {
			this.id = id;
		}

		public String getSessionId() {
			return sessionId;
		}

		public void setSessionId(String sessionId) {
			this.sessionId = sessionId;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

	}

	@Override
	public InteractionNetwork upload(String name, InputStream stream,
			long organismId, String sessionId) throws ApplicationException,
			IOException, DataStoreException, ValidationException,
			SystemException {

		NetworkData data = parseNetworkDataFromStream(stream, organismId,
				sessionId);
		NetworkMetadata meta = sendNetworkDataToEngine(data);

		InteractionNetwork network = new InteractionNetwork();
		network.setName(name);
		network.setDefaultSelected(true);
		network.setMetadata(meta);
		network.setId(meta.getId());
		network.setDescription(UserNetworkConstants.NETWORK_DESCRIPTION);
		network.setTags(new LinkedList<Tag>());
		network.setInteractions(new LinkedList<Interaction>());

		if (network.getMetadata().getInteractionCount() > 0) {
			networkService.addUserNetwork(organismId, sessionId, network);
		}

		return network;
	}

	@Override
	public void delete(Integer organismId, Long networkId, String sessionId)
			throws DataStoreException {
		networkService.deleteUserNetwork(organismId, networkId, sessionId);
	}

	private NetworkMetadata sendNetworkDataToEngine(NetworkData network)
			throws ApplicationException, ValidationException, SystemException,
			IOException {
		// init
		NetworkMetadata meta = new NetworkMetadata();

		// build request
		UploadNetworkWebRequestDto requestDto = new UploadNetworkWebRequestDto();
		requestDto.setNamespace(network.getSessionId());
		requestDto.setOrganismId(network.getOrganismId());
		requestDto.setSparsification(Integer.parseInt(ApplicationConfig
				.getInstance().getProperty(
						Constants.CONFIG_PROPERTIES.SPARSIFICATION)));
		requestDto.setData(network.getNormalizedData());
		requestDto.setNetworkId(network.getId());

		DataLayout dataLayout = network.getDataImportSettings().getDataLayout();
		requestDto.setDataLayout(dataLayout);
		if (DataLayout.GEO_PROFILE.equals(dataLayout)) {
			requestDto.setDataLayout(DataLayout.PROFILE);
		}

		requestDto.setProcessingMethod(network.getDataImportSettings()
				.getProcessingMethod());

		// invoke the engine
		UploadNetworkWebResponseDto responseDto = engineConnector
				.uploadNetwork(requestDto);

		long interactionCount = responseDto.getInteractionCount();
		long normInteractionCount = network.getNormalizationResult()
				.getTotalEntries();
		long duplicateInteractions = normInteractionCount - interactionCount;
		long invalidInteractions = network.getNormalizationResult()
				.getDroppedEntries();

		// log the processing stats
		LOG.info(String.format(
				"normalization produced %d total entries, %d dropped entries",
				network.getNormalizationResult().getTotalEntries(), network
						.getNormalizationResult().getDroppedEntries()));
		LOG.info(String.format("resulting network has %d interactions",
				interactionCount));

		if (duplicateInteractions > 0) {
			LOG.warn(duplicateInteractions + " duplicated interactions found.");
		}

		// set metadata
		meta.setId(network.getId());
		meta.setInteractionCount(interactionCount);
		meta.setSource(UserNetworkConstants.SOURCE);
		meta.setNetworkType(UserNetworkConstants.CODE);

		Set<String> ints = network.getInvalidInteractions();
		String[] sortedInts = ints.toArray(new String[0]);
		Arrays.sort(sortedInts);
		List<String> intsList = new LinkedList<String>();
		for (String str : sortedInts) {
			intsList.add(str);
		}
		meta.setInvalidInteractions(intsList);
		meta.setAccessStats(0);
		meta.setAlias("");
		meta.setAuthors("");
		meta.setDynamicRange("");
		meta.setEdgeWeightDistribution("");
		meta.setOther("");
		meta.setProcessingDescription(UserNetworkConstants
				.getProcessingDescription(network.getDataImportSettings().getDataLayout()));
		meta.setPublicationName("");
		meta.setPubmedId("");
		meta.setReference("");
		meta.setSource("");
		meta.setSourceUrl("");
		meta.setTitle("");
		meta.setUrl("");
		meta.setYearPublished("");

		Date date = new java.util.Date();
		meta.setComment("" + new SimpleDateFormat("yyyy-MM-dd").format(date)
				+ " at " + new SimpleDateFormat("HH:mm:ss").format(date));

		return meta;
	}

	private NetworkData parseNetworkDataFromStream(InputStream stream,
			long organismId, String sessionId) throws ApplicationException,
			IOException, DataStoreException {

		LOG.debug("parsing user data");
		Organism organism = organismDao.findOrganism(organismId);

		// analyze input
		DataImportSettings settings = new DataImportSettings();

		DataFileClassifier classifier = new DataFileClassifier();
		int maximumLinesToSample = 100; // TODO: make configurable somewhere

		LOG.debug("classifying data file");
		stream.reset();
		classifier.classify(settings, stream, maximumLinesToSample);

		LOG.debug("detecting organism");
		// TODO: we probably want to put some indirection around the lucene
		// dependency here
		IGeneClassifier geneClassifier = new LuceneGeneClassifier(
				LuceneConnector.getInstance().getSearcher(), LuceneConnector
						.getInstance().getAnalyzer());
		OrganismClassifier detector = new OrganismClassifier(geneClassifier);

		stream.reset();
		detector.classify(settings, new InputStreamReader(stream),
				maximumLinesToSample);
		if (settings.getOrganism() == null) {
			LOG
					.warn("organism detection failed, setting to user specified organism");
			settings.setOrganism(organism);
		}

		if (settings.getOrganism().getId() != organism.getId()) {
			LOG
					.warn(String
							.format(
									"auto-detected the wrong organism in file (detected %s, user specified %s), setting to user specified organism ",
									settings.getOrganism().getId(), organism
											.getId()));
			settings.setOrganism(organism);
		}

		// if we have a binary (two-col) network, the classification
		// system decides to use log-freq processing, but we could
		// also use direct. currently we prefer direct, so force.
		if (settings.getDataLayout() == DataLayout.BINARY_NETWORK
				&& settings.getProcessingMethod() == NetworkProcessingMethod.LOG_FREQUENCY) {
			settings.setProcessingMethod(NetworkProcessingMethod.DIRECT);
		}

		LOG
				.debug("normalizing data file, classification determined layout was: "
						+ settings.getDataLayout());
		StringWriter output = new StringWriter();
		NormalizationResult normalizationResult = null;
		try {
			DataNormalizer normalizer = new DataNormalizer();
			GeneCompletionProvider2 geneProvider = new GeneCompletionProvider2(
					LuceneConnector.getInstance().getSearcher(),
					LuceneConnector.getInstance().getAnalyzer(),
                    organism,
                    new CachingGeneMediator(new LuceneGeneMediator(
                            LuceneConnector.getInstance().getSearcher(),
                            LuceneConnector.getInstance().getAnalyzer())
                    ));

			stream.reset();
			Reader input = new InputStreamReader(stream);

			normalizationResult = normalizer.normalize(settings, geneProvider,
					input, output, NullProgressReporter.instance());
		} catch (Exception e) { // something wrong here, can we recover
			// from this? i think we need to raise an
			// application exception with a message for the
			// user
			LOG.error("normalization failed", e);
			throw new ApplicationException("Can not normalise data",
					Constants.ERROR_CODES.DATA_ERROR);
		}

		// check for format. note we have to do this after normalization and not
		// just after classification
		// since normalization can update the layout for eg soft profile's
		if (settings.getDataLayout() == DataLayout.UNKNOWN) {
			throw new ApplicationException("Unrecognized data layout",
					Constants.ERROR_CODES.UNKNOWN_FILE_FORMAT);
		}

		// populate upload bean
		NetworkData network = new NetworkData();
		network.setDataImportSettings(settings);
		network.setNormalizationResult(normalizationResult);
		network.setNormalizedData(output.toString());
		network.setOrganismId(organismId);
		network.setSessionId(sessionId);
		network.setInvalidInteractions(normalizationResult.getInvalidSymbols());
		LOG.debug("completed parsing user data");
		return network;
	}

	public EngineConnector getEngineConnector() {
		return engineConnector;
	}

	public void setEngineConnector(EngineConnector engineConnector) {
		this.engineConnector = engineConnector;
	}

	public OrganismDao getOrganismDao() {
		return organismDao;
	}

	public void setOrganismDao(OrganismDao organismDao) {
		this.organismDao = organismDao;
	}

	public NetworkDao getNetworkDao() {
		return networkDao;
	}

	public void setNetworkDao(NetworkDao networkDao) {
		this.networkDao = networkDao;
	}

	public NetworkService getNetworkService() {
		return networkService;
	}

	public void setNetworkService(NetworkService networkService) {
		this.networkService = networkService;
	}

}
