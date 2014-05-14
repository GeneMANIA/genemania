package org.genemania.adminweb.validators.impl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.genemania.adminweb.dao.DatamartDb;
import org.genemania.adminweb.entity.AttributeMetadata;
import org.genemania.adminweb.entity.Network;
import org.genemania.adminweb.exception.DatamartException;
import org.genemania.adminweb.service.FileStorageService;
import org.genemania.adminweb.validators.stats.AttributeMetadataValidationStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVParser;
import au.com.bytecode.opencsv.CSVReader;

public class AttributeMetadataValidator {
    final Logger logger = LoggerFactory.getLogger(AttributeValidatorImpl.class);

    int validCount;

    // to help user decide if the data is loaded correctly
    String sampleAccession;
    String sampleName;

    private Network network;
    private FileStorageService fileStorageService;
    private DatamartDb dmdb;

    public AttributeMetadataValidator(DatamartDb dmdb, FileStorageService fileStorageService,
            Network network) {
        this.dmdb = dmdb;
        this.fileStorageService = fileStorageService;
        this.network = network;
    }

    public AttributeMetadataValidationStats validate() throws DatamartException {
        if (network.getAttributeMetadata() == null) {
            throw new DatamartException("no metadata");
        }

        AttributeMetadataValidationStats stats = null;

        try {
            AttributeMetadata md = network.getAttributeMetadata();
            dmdb.getAttributeMetadataDao().refresh(md);

            String path = md.getDataFile().getFilename();
            if (path == null || path.equals("")) {
                throw new DatamartException("no file");
            }

            File file = fileStorageService.getFile(path);
            parse(file);
            logger.info("# metadata records: " + validCount);

            stats = new AttributeMetadataValidationStats();
            stats.setMetadataRecordCount(validCount);
            if (validCount > 0) {
                stats.setSampleAccession(sampleAccession);
                stats.setSampleName(sampleName);
            }

            updateProcessingDetails(stats, md);
        }
        catch (SQLException e) {
            throw new DatamartException("failed to validate", e);
        }
        catch (IOException e) {
            throw new DatamartException("failed to validate", e);
        }


        return stats;
    }

    private void updateProcessingDetails(AttributeMetadataValidationStats stats, AttributeMetadata md) throws SQLException {
        // TODO: hide jackson behind mapper service
        // TODO: trim length without breaking serialization, probably limit # of identifiers
        // stored
        ObjectMapper mapper = new ObjectMapper();
        String details = "";

        try {
            details = mapper.writeValueAsString(stats);
        }
        catch (JsonMappingException e) {
            logger.warn("failed to encode processing details");
        } catch (JsonGenerationException e) {
            logger.warn("failed to encode processing details");
        } catch (IOException e) {
            logger.warn("failed to encode processing details");
        }

        logger.info("processing details: " + details);
        md.getDataFile().setProcessingDetails(details);

        dmdb.getDataFileDao().update(md.getDataFile());
    }

    /*
     * parse metadata file, containing tab delimited records of the form:
     *
     *   accession_id    name      description
     *
     * or just
     *
     *   accession_id    name
     *
     * ignore lines that don't have at least 2 fields, and ignore extra fields
     * in a line if present
     */
    public void parse(File file) throws IOException {
        CSVReader reader = new CSVReader(new InputStreamReader(
                new BufferedInputStream(new FileInputStream(file)), "UTF8"),
                '\t', CSVParser.NULL_CHARACTER);

        String accession = null, name = null, description = null;
        validCount = 0;
        try {
            String[] line = reader.readNext();
            while (line != null) {

                if (line.length < 2) {
                    continue;
                }
                accession = line[0];
                name = line[1];
                description = "";

                if (line.length >= 3) {
                    description = line[2];
                }

                validCount += 1;

                line = reader.readNext();
            }
        }
        finally {
            reader.close();
        }

        // use the last read record for user example
        sampleAccession = accession;
        sampleName = name;
    }
}
