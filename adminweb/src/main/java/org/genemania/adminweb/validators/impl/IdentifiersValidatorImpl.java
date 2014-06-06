package org.genemania.adminweb.validators.impl;

import org.genemania.adminweb.dao.DatamartDb;
import org.genemania.adminweb.dataset.DataSetContext;
import org.genemania.adminweb.entity.DataFile;
import org.genemania.adminweb.entity.Identifiers;
import org.genemania.adminweb.exception.DatamartException;
import org.genemania.adminweb.service.FileStorageService;
import org.genemania.adminweb.service.MappingService;
import org.genemania.adminweb.validators.Validator;
import org.genemania.adminweb.validators.stats.IdentifierValidationStats;
import org.genemania.adminweb.validators.stats.NetworkValidationStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.SQLException;
import java.util.UUID;

/**
 * validation on a single identifiers file
 */
public class IdentifiersValidatorImpl implements Validator {
    final Logger logger = LoggerFactory.getLogger(IdentifiersValidatorImpl.class);

    DatamartDb dmdb;
    MappingService mappingService;
    FileStorageService fileStorageService;
    DataSetContext context;
    Identifiers identifiers;

    public IdentifiersValidatorImpl(DatamartDb dmdb, MappingService mappingService,
                                    FileStorageService fileStorageService,
                                    DataSetContext context, Identifiers identifiers) {

        this.dmdb = dmdb;
        this.mappingService = mappingService;
        this.fileStorageService = fileStorageService;
        this.context = context;
        this.identifiers = identifiers;
    }

    // TODO: generalize return type. not using here for now
    @Override
    public NetworkValidationStats validate() throws DatamartException {
        String path = getIdentifiers().getDataFile().getFilename();
        File file = getFileStorageService().getFile(path);
        IdentifierValidationStats stats = new IdentifierValidationStats();
        stats.setStatus("ERROR");

        try {
            process(file, stats);
        }
        catch (DatamartException e) {

            throw new DatamartException("failed to validate file", e);
        }
        finally {
            try {
                updateProcessingDetails(stats);
            }
            catch (SQLException e2) {
                logger.warn("failed to record validation details", e2);
            }
        }

        return null;
    }

    private void process(File file, IdentifierValidationStats stats)
            throws DatamartException {

        IdentVac vac = null;

        try {
            vac = new IdentVac(UUID.randomUUID().toString());
            vac.process(file.getAbsolutePath(), "\t");

            // hmm, maybe shoulda passed the stats into the vac?
            // but i likes the vac being independent
            stats.setNumRecordsRead(vac.numRecordsRead);
            stats.setNumDups(vac.numDups);
            stats.setNumMissing(vac.numMissing);
            stats.setNumIds(vac.numIds);
            stats.setNumSymbols(vac.numSymbols);
            stats.setNumSources(vac.numSources);
            stats.setSourceCounts(vac.sourceCounts);
            stats.setStatus("OK");

        } catch (ClassNotFoundException e) {
            throw new DatamartException("failed to validate file", e);
        } catch (SQLException e) {
            throw new DatamartException("failed to validate file", e);
        }
        finally {
            try {
                if (vac != null) vac.close();
            }
            catch (SQLException e2) {
                logger.warn("error closing resources", e2);
            }
        }

    }

    private void updateProcessingDetails(IdentifierValidationStats validationStats)
            throws SQLException {

        // convert to json string
        String details = mappingService.map(validationStats);
        logger.info("identifier validation details: " + details);

        // set it. save it.
        DataFile dataFile = getIdentifiers().getDataFile();
        dataFile.setProcessingDetails(details);
        dmdb.getDataFileDao().update(dataFile);
    }

    public DatamartDb getDmdb() {
        return dmdb;
    }

    public void setDmdb(DatamartDb dmdb) {
        this.dmdb = dmdb;
    }

    public MappingService getMappingService() {
        return mappingService;
    }

    public void setMappingService(MappingService mappingService) {
        this.mappingService = mappingService;
    }

    public FileStorageService getFileStorageService() {
        return fileStorageService;
    }

    public void setFileStorageService(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    public DataSetContext getContext() {
        return context;
    }

    public void setContext(DataSetContext context) {
        this.context = context;
    }

    public Identifiers getIdentifiers() {
        return identifiers;
    }

    public void setIdentifiers(Identifiers identifiers) {
        this.identifiers = identifiers;
    }
}
