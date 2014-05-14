package org.genemania.adminweb.web.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.genemania.adminweb.dao.DatamartDb;
import org.genemania.adminweb.entity.DataFile;
import org.genemania.adminweb.entity.Organism;
import org.genemania.adminweb.exception.DatamartException;
import org.genemania.adminweb.service.FileStorageService;
import org.genemania.adminweb.service.ResourceNamingService;
import org.genemania.adminweb.web.service.UploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/*
 * generic service for handling storage of an uploaded data file of any type.
 */
@Service
public class UploadServiceImpl implements UploadService {
    final Logger logger = LoggerFactory.getLogger(UploadServiceImpl.class);

    @Autowired
    private DatamartDb dmdb;

    @Autowired
    private ResourceNamingService resourceNamingService;

    @Autowired
    private FileStorageService fileStorageService;

    @Override
    public DataFile addDataFile(int organismId, String originalFilename,
            InputStream inputStream) throws DatamartException {
        logger.info("adding data file: " + originalFilename);
        DataFile dataFile = null;

        try {

            Organism organism = dmdb.getOrganismDao().queryForId(organismId);

            if (organism == null) {
                logger.error("undefined network/group parameters");
            }
            else {
                dataFile = new DataFile();
                dataFile.setOriginalFilename(originalFilename);
                dataFile.setFilename(null);
                dataFile.setOrganism(organism);
                dataFile.setUploadDate(new Date());
                dataFile.setStatus("UPLOADING");

                dmdb.getDataFileDao().create(dataFile);
            }

            String filename = resourceNamingService.getName(organism, dataFile);
            File file = fileStorageService.getFile(filename);
            IOUtils.copy(inputStream, new FileOutputStream(file));
            dataFile.setFilename(filename);
            dataFile.setStatus("UPLOADED");
            dmdb.getDataFileDao().update(dataFile);
        }
        catch (SQLException e) {
            logger.error("failed to add network", e);
        } catch (IOException e) {
            logger.error("failed to add network file", e);
        } catch (DatamartException e) {
            logger.error("failed to add network file", e);
        }

        return dataFile;
    }
}
