package org.genemania.adminweb.service.impl;

import java.io.File;
import java.io.InputStream;
import java.sql.SQLException;

import org.genemania.adminweb.dao.DatamartDb;
import org.genemania.adminweb.dataset.DataSetContext;
import org.genemania.adminweb.entity.DataFile;
import org.genemania.adminweb.entity.Identifiers;
import org.genemania.adminweb.entity.Organism;
import org.genemania.adminweb.exception.DatamartException;
import org.genemania.adminweb.service.DataSetManagerService;
import org.genemania.adminweb.service.IdentifiersService;
import org.genemania.adminweb.service.ValidationService;
import org.genemania.adminweb.web.service.UploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IdentifiersServiceImpl implements IdentifiersService {
    final Logger logger = LoggerFactory.getLogger(IdentifiersServiceImpl.class);

    @Autowired
    private DatamartDb dmdb;

    @Autowired
    private UploadService uploadService;

    @Autowired
    private ValidationService validationService;

    @Autowired
    private DataSetManagerService dataSetManagerService;

    @Override
    public Identifiers addIdentifiers(int organismId,
            String originalFilename, InputStream inputStream) {

        logger.info("adding identifiers from " + originalFilename);

        Identifiers identifiers = null;

        try {

            Organism organism = dmdb.getOrganismDao().queryForId(organismId);

            if (organism == null) {
                logger.error("undefined parameters");
            }
            else {
                // store the data file
                DataFile dataFile = uploadService.addDataFile(organismId, originalFilename, inputStream);
                identifiers = new Identifiers();
                identifiers.setOrganism(organism);
                identifiers.setDataFile(dataFile);
                dmdb.getIdentifiersDao().create(identifiers);

                // cleanup working data cache
                deleteDataSet(organismId);

                // validate
                validationService.validateIdentifiers(identifiers);
            }
        }
        catch (SQLException e) {
            logger.error("failed to add network", e);
        } catch (DatamartException e) {
            logger.error("failed to add network file", e);
        }

        // TODO: need an error handling convention
        return identifiers;
    }

    @Override
    public Identifiers replaceIdentifiers(int organismId, int identifiersId,
            String originalFilename, InputStream inputStream) {
        logger.info("replacing identifiers");

        Identifiers identifiers = null;
        try {
            Organism organism = dmdb.getOrganismDao().queryForId(organismId);
            identifiers = dmdb.getIdentifiersDao().queryForId(identifiersId);

            if (organism == null || identifiers == null) {
                logger.error("undefined parameters");
            }
            else {
                DataFile dataFile = uploadService.addDataFile(organismId, originalFilename, inputStream);

                identifiers.setDataFile(dataFile);
                dmdb.getIdentifiersDao().update(identifiers);
                // TODO: delete old file, or move to trash?

                // cleanup working data cache
                deleteDataSet(organismId);

                // validate
                validationService.validateIdentifiers(identifiers);
             }
        }
        catch (SQLException e) {
            logger.error("failed to replace identifiers", e);
        } catch (DatamartException e) {
            logger.error("failed to replace identifiers file", e);
        }

        return identifiers;
    }

    @Override
    public void deleteIdentifiers(int organismId, int identifiersId) throws DatamartException {
        logger.info("deleting identifiers " + identifiersId);

        try {
            Organism organism = dmdb.getOrganismDao().queryForId(organismId);
            Identifiers identifiers = dmdb.getIdentifiersDao().queryForId(identifiersId);
            if (organism.getId() != identifiers.getOrganism().getId()) {
                throw new DatamartException("inconsistent organism/network id's");
            }

            // TODO: logging, checking, mark as trashed instead of really deleting?
            dmdb.getIdentifiersDao().delete(identifiers);
        }
        catch (SQLException e) {
            throw new DatamartException("failed to delete identifiers", e);
        }
    }

    @Override
    public File getIdentifiersFile(Identifiers identifiers)
            throws DatamartException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * if the identifiers change, than everything that depends on them,
     * networks, attributes etc is subject to change. so just delete
     * the computed cache so it will get rebuilt on next use.
     */
    private void deleteDataSet(int organismId) throws DatamartException {
        logger.debug("cleaning up build files for organism " + organismId);
        DataSetContext context = dataSetManagerService.getContext(organismId);
        dataSetManagerService.delete(context);
    }
}
