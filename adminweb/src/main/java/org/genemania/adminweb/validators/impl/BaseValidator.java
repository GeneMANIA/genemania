package org.genemania.adminweb.validators.impl;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.genemania.adminweb.dao.DatamartDb;
import org.genemania.adminweb.dataset.DataSetContext;
import org.genemania.adminweb.dataset.LuceneDataSet;
import org.genemania.adminweb.entity.Format;
import org.genemania.adminweb.entity.Network;
import org.genemania.adminweb.exception.DatamartException;
import org.genemania.adminweb.service.FileStorageService;
import org.genemania.adminweb.service.MappingService;
import org.genemania.adminweb.service.impl.BuildServiceImpl;
import org.genemania.adminweb.validators.Validator;
import org.genemania.adminweb.validators.stats.NetworkValidationStats;
import org.genemania.dto.RemoveNetworkEngineRequestDto;
import org.genemania.dto.RemoveNetworkEngineResponseDto;
import org.genemania.engine.IMania;
import org.genemania.exception.ApplicationException;
import org.genemania.type.DataLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * misc supporting code for performing a validation operation.
 *
 * subclass, and implement process(). callers will call validate() which
 * does some setup/cleanup.
 */
public abstract class BaseValidator implements Validator {
    final Logger logger = LoggerFactory.getLogger(BaseValidator.class);

    public final String VALIDATION_NAMESPACE = "validation";

    DatamartDb dmdb;
    MappingService mappingService;
    FileStorageService fileStorageService;
    DataSetContext context;
    Network network;

    public BaseValidator(DatamartDb dmdb, MappingService mappingService,
            FileStorageService fileStorageService,
            DataSetContext context, Network network) {
        this.dmdb = dmdb;
        this.mappingService = mappingService;
        this.fileStorageService = fileStorageService;
        this.context = context;
        this.network = network;
    }

    public DataSetContext getContext() {
        return context;
    }

    public void setContext(DataSetContext context) {
        this.context = context;
    }

    public abstract NetworkValidationStats process() throws Exception;

    @Override
    public NetworkValidationStats validate() throws DatamartException {
        NetworkValidationStats validationStats = new NetworkValidationStats();
        try {
            dmdb.getDataFileDao().refresh(network.getDataFile());
            cleanupEngineNamespace(context, network.getOrganism().getId());
            validationStats = process();

        } catch (Exception e) {
            throw new DatamartException("network validation failed", e);
        }
        finally {
            // try to record as much as we can on network processing, to help user
            // sort out what's wrong
            if (network != null) {
                try {
                    updateProcessingDetails(validationStats);
                } catch (SQLException e) {
                    logger.error("failed to update processing details", e);
                }
            }
        }

        return validationStats;
    }

    /*
     * we should record network processing stats in the database, for now write
     * only a textual description. TODO fix.
     */
    private void updateProcessingDetails(NetworkValidationStats validationStats) throws SQLException {

        // just take 50 idents max for now
        List<String> invalids = validationStats.getInvalidInteractions();

        if (invalids != null && invalids.size() > 50) {
            validationStats.setInvalidInteractions(invalids.subList(0, 49));
        }

        String details = mappingService.map(validationStats);


        logger.info("processing details: " + details);
        network.getDataFile().setProcessingDetails(details);

        // also set format
        Format format = getFormat(validationStats.getProcessingDescription());
        network.setFormat(format);

        dmdb.getDataFileDao().update(network.getDataFile());
        dmdb.getNetworkDao().update(network);
    }

    public Network getNetwork() {
        return network;
    }

    public DatamartDb getDmdb() {
        return dmdb;
    }

    public void setDmdb(DatamartDb dmdb) {
        this.dmdb = dmdb;
    }

    public FileStorageService getFileStorageService() {
        return fileStorageService;
    }

    private void cleanupEngineNamespace(DataSetContext context, long organismId) throws IOException, ApplicationException {
        RemoveNetworkEngineRequestDto request = new RemoveNetworkEngineRequestDto();
        request.setNamespace(VALIDATION_NAMESPACE);
        request.setOrganismId(organismId);

        IMania mania = getMania(context);
        RemoveNetworkEngineResponseDto response = mania.removeUserNetworks(request);

    }

    protected LuceneDataSet getLuceneDataSet(DataSetContext context) throws IOException {
        LuceneDataSet luceneDataSet = LuceneDataSet.instance(context.getIndexPath());
        return luceneDataSet;
    }

    protected IMania getMania(DataSetContext context) throws IOException {
        return BuildServiceImpl.getMania(context, true);
    }

    /*
     * from website org.genemania.util.UserNetworkConstants, should probably
     * refactor to somewhere in common. TODO.
     */
    public static String getProcessingDescription(DataLayout layout) {
        switch (layout) {
        case BINARY_NETWORK:
            return "Binary network";
        case GEO_PROFILE:
            return "GEO profile";
        case PROFILE:
            return "Profile";
        case SPARSE_PROFILE:
            return "Sparse profile";
        case UNKNOWN:
            return "Uknown format";
        case WEIGHTED_NETWORK:
            return "Weighted network";
        default:
            return "Uknown format";
        }
    }

    /*
     * select the correct format indicator from the database. yes this + the above are
     * pseudo-redundant.
     */
    public Format getFormat(String formatHint) {
        List<Format> formats = null;

        try {
            if (formatHint == null) {
                return null;
            }
            if (formatHint.equals("Profile")) {
                formats = getDmdb().getFormatDao().queryForEq(Format.NAME_FIELD, "profile");
            }
            else if (formatHint.equals("Binary network")) {
                formats = getDmdb().getFormatDao().queryForEq(Format.NAME_FIELD, "network");
            }
            else if (formatHint.equals("Weighted network")) {
                formats = getDmdb().getFormatDao().queryForEq(Format.NAME_FIELD, "network");
            }
            else if (formatHint.equals("Attributes")) {
                formats = getDmdb().getFormatDao().queryForEq(Format.NAME_FIELD, "attributes");
            }
        }
        catch (SQLException e) {
            logger.warn("failed to query for format", e);
        }

        if (formats != null && formats.size() == 1) {
            return formats.get(0);
        }
        else {
            return null;
        }
    }
}
