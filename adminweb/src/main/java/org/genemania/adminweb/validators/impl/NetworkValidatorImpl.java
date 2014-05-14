
package org.genemania.adminweb.validators.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.genemania.data.classification.IGeneClassifier;
import org.genemania.data.classification.lucene.LuceneGeneClassifier;
import org.genemania.data.normalizer.DataFileClassifier;
import org.genemania.data.normalizer.DataImportSettings;
import org.genemania.data.normalizer.DataNormalizer;
import org.genemania.data.normalizer.GeneCompletionProvider2;
import org.genemania.data.normalizer.NormalizationResult;
import org.genemania.data.normalizer.OrganismClassifier;
import org.genemania.adminweb.dao.DatamartDb;
import org.genemania.adminweb.dataset.DataSetContext;
import org.genemania.adminweb.dataset.LuceneDataSet;
import org.genemania.adminweb.entity.Network;
import org.genemania.adminweb.exception.DatamartException;
import org.genemania.adminweb.service.FileStorageService;
import org.genemania.adminweb.service.MappingService;
import org.genemania.adminweb.validators.stats.NetworkValidationStats;
import org.genemania.domain.Organism;
import org.genemania.dto.UploadNetworkEngineRequestDto;
import org.genemania.dto.UploadNetworkEngineResponseDto;
import org.genemania.engine.IMania;
import org.genemania.exception.ApplicationException;
import org.genemania.exception.DataStoreException;
import org.genemania.mediator.impl.CachingGeneMediator;
import org.genemania.type.DataLayout;
import org.genemania.type.NetworkProcessingMethod;
import org.genemania.util.NullProgressReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetworkValidatorImpl extends BaseValidator {

    private int organismId;
    private int networkId;

    private String normalizedData;
    private Set<String> invalidInteractions = new HashSet<String>();
    private NormalizationResult normalizationResult;
    private DataImportSettings dataImportSettings;

    private NetworkValidationStats validationStats = new NetworkValidationStats();

    public NetworkValidatorImpl(DatamartDb dmdb, MappingService mappingService,
            FileStorageService fileStorageService,
            DataSetContext context, Network network) {
        super(dmdb, mappingService, fileStorageService, context, network);

        // for convenience, can remove
        this.organismId = network.getOrganism().getId();
        this.networkId = network.getId();
    }


    final Logger logger = LoggerFactory.getLogger(NetworkValidatorImpl.class);

    // TODO: configuration
    public static final int SPARSIFICATION = 50;

    @Override
    public NetworkValidationStats process() throws Exception { // TODO: narrow. i guess.
        String path = getNetwork().getDataFile().getFilename();
        File file = getFileStorageService().getFile(path);
        parse(file);
        load();

        validationStats.setStatus("OK");
        return validationStats;
    }

    public void load()
            throws IOException, ApplicationException
            {

        // build request
        UploadNetworkEngineRequestDto request = new UploadNetworkEngineRequestDto();
        request.setOrganismId(organismId);

        // load as user network in a "validation" namespace with
        // negative network id. api doesn't support loading core networks
        // this way, but it should. TODO.
        request.setNamespace(VALIDATION_NAMESPACE);
        request.setNetworkId(-networkId);

        request.setSparsification(SPARSIFICATION); // TODO: set from config
        request.setProgressReporter(NullProgressReporter.instance());

        DataLayout dataLayout = dataImportSettings.getDataLayout();
        request.setLayout(dataLayout);
        if (DataLayout.GEO_PROFILE.equals(dataLayout)) {
            request.setLayout(DataLayout.PROFILE);
        }

        request.setMethod(dataImportSettings
                .getProcessingMethod());

        request.setData(new StringReader(normalizedData));

        // TODO: reorganize getting mania
        IMania mania = getMania(getContext());
        UploadNetworkEngineResponseDto response = mania.uploadNetwork(request);



        long interactionCount = response.getNumInteractions();
        long normInteractionCount = normalizationResult
                .getTotalEntries();
        long duplicateInteractions = normInteractionCount - interactionCount;
        long invalidInteractions = normalizationResult
                .getDroppedEntries();


        // log the processing stats
        logger.info(String.format(
                "normalization produced %d total entries, %d dropped entries",
                normalizationResult.getTotalEntries(),
                normalizationResult.getDroppedEntries()));
        logger.info(String.format("resulting network has %d interactions",
                interactionCount));

        if (duplicateInteractions > 0) {
            logger.warn(duplicateInteractions + " duplicated interactions found.");
        }

        // set stats
        validationStats.setInteractionCount(interactionCount);

        Set<String> ints = this.invalidInteractions;
        String[] sortedInts = ints.toArray(new String[0]);
        Arrays.sort(sortedInts);
        List<String> intsList = new LinkedList<String>();
        for (String str : sortedInts) {
            intsList.add(str);
        }
        validationStats.setInvalidInteractions(intsList);
        validationStats.setInvalidCount(intsList.size());
        validationStats.setDataLayout(dataImportSettings.getDataLayout());
        validationStats.setProcessingDescription(getProcessingDescription(dataImportSettings.getDataLayout()));
        Date date = new java.util.Date();
        validationStats.setProcessingDate(date);
            }


    public void parse(File file) throws DatamartException,
    IOException, DataStoreException {

        logger.debug("parsing user data");

        InputStream inputStream = new FileInputStream(file);

        LuceneDataSet luceneDataSet = getLuceneDataSet(getContext());
        Organism organism = luceneDataSet.getOrganismMediator().getOrganism(organismId);

        // analyze input
        DataImportSettings settings = new DataImportSettings();

        DataFileClassifier classifier = new DataFileClassifier();
        int maximumLinesToSample = 1000; // TODO: make configurable somewhere

        logger.debug("classifying data file");
        classifier.classify(settings, inputStream, maximumLinesToSample);

        logger.debug("detecting organism");
        // TODO: we probably want to put some indirection around the lucene
        // dependency here
        IGeneClassifier geneClassifier = new LuceneGeneClassifier(luceneDataSet.getSearcher(),
                luceneDataSet.getAnalyzer());
        OrganismClassifier detector = new OrganismClassifier(geneClassifier);

        inputStream.close();
        inputStream = new FileInputStream(file);
        try {
            detector.classify(settings, new InputStreamReader(inputStream),
                    maximumLinesToSample);
        } catch (ApplicationException e) {
            throw new DatamartException("classification failed", e);
        }
        inputStream.close();

        if (settings.getOrganism() == null) {
            logger
            .warn("organism detection failed, setting to user specified organism");
            settings.setOrganism(organism);
        }

        if (settings.getOrganism().getId() != organism.getId()) {
            logger
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

        logger
        .debug("normalizing data file, classification determined layout was: "
                + settings.getDataLayout());
        StringWriter output = new StringWriter();

        try {
            DataNormalizer normalizer = new DataNormalizer();
            GeneCompletionProvider2 geneProvider = new GeneCompletionProvider2(
                    luceneDataSet.getSearcher(),
                    luceneDataSet.getAnalyzer(),
                    organism,
                    new CachingGeneMediator(luceneDataSet.getGeneMediator()));

            inputStream = new FileInputStream(file);
            Reader input = new InputStreamReader(inputStream);

            normalizationResult = normalizer.normalize(settings, geneProvider,
                    input, output, NullProgressReporter.instance());
        } catch (Exception e) { // something wrong here, can we recover
            // from this? i think we need to raise an
            // application exception with a message for the
            // user
            logger.error("normalization failed", e);
            validationStats.setStatus("Could not normalize data");
            throw new DatamartException("Can not normalise data");
        }
        inputStream.close();

        // check for format. note we have to do this after normalization and not
        // just after classification
        // since normalization can update the layout for eg soft profile's
        if (settings.getDataLayout() == DataLayout.UNKNOWN) {
            validationStats.setStatus("Failed: Unrecognized data layout");
            throw new DatamartException("Unrecognized data layout");
        }

        dataImportSettings = settings;
        normalizedData = output.toString();
        invalidInteractions = normalizationResult.getInvalidSymbols();
        logger.debug("completed parsing user data");
    }
}
