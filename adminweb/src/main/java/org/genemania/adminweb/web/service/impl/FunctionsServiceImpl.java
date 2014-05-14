package org.genemania.adminweb.web.service.impl;

import java.io.InputStream;
import java.sql.SQLException;

import org.genemania.adminweb.dao.DatamartDb;
import org.genemania.adminweb.entity.DataFile;
import org.genemania.adminweb.entity.Functions;
import org.genemania.adminweb.entity.Organism;
import org.genemania.adminweb.exception.DatamartException;
import org.genemania.adminweb.service.ValidationService;
import org.genemania.adminweb.web.service.FunctionsService;
import org.genemania.adminweb.web.service.UploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FunctionsServiceImpl implements FunctionsService {
    final Logger logger = LoggerFactory.getLogger(FunctionsServiceImpl.class);

    @Autowired
    private DatamartDb dmdb;

    @Autowired
    UploadService uploadService;

    @Autowired
    ValidationService validationService;

    @Override
    public Functions addFunctions(int organismId, String originalFilename, InputStream inputStream) {
        logger.info("adding network from " + originalFilename);
        Functions functions = null;

        try {

            Organism organism = dmdb.getOrganismDao().queryForId(organismId);

            if (organism == null) {
                logger.error("undefined network/group parameters");
            }
            else {
                DataFile dataFile = uploadService.addDataFile(organismId, originalFilename, inputStream);
                functions = new Functions();
                functions.setDataFile(dataFile);
                functions.setFunctionType("ENRICHMENT");

                dmdb.getFunctionsDao().create(functions);

                // validate. TODO: make async
                validationService.validateFunctions(functions);
            }
        }
        catch (SQLException e) {
            logger.error("failed to add network", e);
        } catch (DatamartException e) {
            logger.error("failed to add network file", e);
        }

        return functions;
    }

    @Override
    public Functions addFunctionDescriptions(int organismId, int functionsId,
            String originalFilename, InputStream inputStream) {

        Functions functions = null;
        try {

            Organism organism = dmdb.getOrganismDao().queryForId(organismId);
            functions = dmdb.getFunctionsDao().queryForId(functionsId);

            if (organism == null || functions == null) {
                logger.error("invalid parameters");
            }
            else {

                DataFile dataFile = uploadService.addDataFile(organismId, originalFilename, inputStream);
                functions.setDescriptionFile(dataFile);

                dmdb.getFunctionsDao().update(functions);
            }
        }
        catch (SQLException e) {
            logger.error("failed to add network", e);
        } catch (DatamartException e) {
            logger.error("failed to add network file", e);
        }

        return functions;
    }

    @Override
    public Functions replaceFunctions(int organismId, int functionsId,
            String originalFilename, InputStream inputStream) {
        Functions functions = null;
        try {

            Organism organism = dmdb.getOrganismDao().queryForId(organismId);
            functions = dmdb.getFunctionsDao().queryForId(functionsId);

            if (organism == null || functions == null) {
                logger.error("invalid parameters");
            }
            else {
                DataFile oldDataFile = functions.getDataFile();

                DataFile dataFile = uploadService.addDataFile(organismId, originalFilename, inputStream);
                functions.setDataFile(dataFile);
                functions.setFunctionType("ENRICHMENT");

                dmdb.getFunctionsDao().update(functions);

                // todo: delete old file? for now just change status, since
                // deleting data is painful for me
                if (oldDataFile != null) {
                    dmdb.getDataFileDao().refresh(oldDataFile);
                    oldDataFile.setStatus("TO_DELETE");
                    dmdb.getDataFileDao().update(oldDataFile);
                }

                // validate. TODO: make async
                validationService.validateFunctions(functions);
            }
        }
        catch (SQLException e) {
            logger.error("failed to add network", e);
        } catch (DatamartException e) {
            logger.error("failed to add network file", e);
        }

        return functions;
    }
}
