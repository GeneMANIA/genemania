package org.genemania.adminweb.validators.impl;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;
import org.genemania.adminweb.dao.DatamartDb;
import org.genemania.adminweb.dataset.DataSetContext;
import org.genemania.adminweb.entity.Functions;
import org.genemania.adminweb.exception.DatamartException;
import org.genemania.adminweb.service.FileStorageService;
import org.genemania.adminweb.service.MappingService;
import org.genemania.adminweb.validators.Validator;
import org.genemania.adminweb.validators.stats.NetworkValidationStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FunctionsValidator implements Validator {
    final Logger logger = LoggerFactory.getLogger(FunctionsValidator.class);

    DatamartDb dmdb;
    MappingService mappingService;
    FileStorageService fileStorageService;
    DataSetContext context;
    Functions functions;

    private int organismId;

    public FunctionsValidator(DatamartDb dmdb, MappingService mappingService,
            FileStorageService fileStorageService, DataSetContext context,
            Functions functions) {
        this.dmdb = dmdb;
        this.mappingService = mappingService;
        this.fileStorageService = fileStorageService;
        this.context = context;
        this.functions = functions;

        this.organismId = functions.getDataFile().getOrganism().getId();
    }

    @Override
    public NetworkValidationStats validate() throws DatamartException {
        NetworkValidationStats validationStats = null;

        try {
            validationStats = process();
        }
        catch (Exception e) {
            throw new DatamartException("network validation failed", e);
        }
        finally {
            // try to record as much as we can on network processing, to help user
            // sort out what's wrong
            if (functions != null) {
                try {
                    updateProcessingDetails(validationStats);
                } catch (SQLException e) {
                    logger.error("failed to update processing details", e);
                }
            }
        }

        return validationStats;
    }

    public NetworkValidationStats process() throws Exception {
        String path = functions.getDataFile().getFilename();
        organismId = functions.getDataFile().getOrganism().getId();

        File file = getFileStorageService().getFile(path);
        AttributeParser parser = new AttributeParser(getContext(), organismId);
        parser.parse(file);

        NetworkValidationStats validationStats = parser.getValidationStats();
        validationStats.setProcessingDescription("Attributes");
        validationStats.setStatus("OK");
        return validationStats;
    }

    private void updateProcessingDetails(NetworkValidationStats validationStats) throws SQLException {

        // TODO: hide jackson behind mapper service
        // TODO: trim length without breaking serialization, probably limit # of identifiers
        // stored
        ObjectMapper mapper = new ObjectMapper();
        String details = "";

        // just take 50 idents max for now
        List<String> invalids = validationStats.getInvalidInteractions();

        if (invalids != null && invalids.size() > 50) {
            validationStats.setInvalidInteractions(invalids.subList(0, 49));
        }
        details = mappingService.map(validationStats);

        logger.info("processing details: " + details);
        functions.getDataFile().setProcessingDetails(details);

        dmdb.getDataFileDao().update(functions.getDataFile());
    }

    public DatamartDb getDmdb() {
        return dmdb;
    }

    public FileStorageService getFileStorageService() {
        return fileStorageService;
    }

    public DataSetContext getContext() {
        return context;
    }
}
