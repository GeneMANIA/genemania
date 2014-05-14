
package org.genemania.adminweb.service.impl;

import java.io.File;
import java.io.InputStream;
import java.sql.SQLException;

import org.genemania.adminweb.dao.DatamartDb;
import org.genemania.adminweb.entity.AttributeMetadata;
import org.genemania.adminweb.entity.DataFile;
import org.genemania.adminweb.entity.Group;
import org.genemania.adminweb.entity.Network;
import org.genemania.adminweb.entity.Organism;
import org.genemania.adminweb.exception.DatamartException;
import org.genemania.adminweb.service.FileStorageService;
import org.genemania.adminweb.service.MetadataService;
import org.genemania.adminweb.service.NetworkService;
import org.genemania.adminweb.service.ResourceNamingService;
import org.genemania.adminweb.web.service.UploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NetworkServiceImpl implements NetworkService {
    final Logger logger = LoggerFactory.getLogger(NetworkServiceImpl.class);

    @Autowired
    private DatamartDb dmdb;

    @Autowired
    private MetadataService metadataService;

    @Autowired
    UploadService uploadService;

    @Autowired
    private ResourceNamingService resourceNamingService;

    @Autowired
    private FileStorageService fileStorageService;

    @Override
    public Network addNetwork(int organismId, int groupId, String originalFilename, InputStream inputStream) {
        logger.info("adding network from " + originalFilename);
        Network network = null;

        try {

            Organism organism = dmdb.getOrganismDao().queryForId(organismId);
            Group group = dmdb.getGroupDao().queryForId(groupId);

            if (organism == null || group == null) {
                logger.error("undefined network/group parameters");
            }
            else {
                DataFile dataFile = uploadService.addDataFile(organismId, originalFilename, inputStream);

                network = new Network();
                network.setName("");
                network.setDataFile(dataFile);
                network.setDescription("");
                network.setOrganism(organism);
                network.setGroup(group);
                network.setFormat(null);
                network.setComment("");
                network.setType(determineNetworkType(group));
                network.setStatus(Network.STATUS_ACTIVE);

                dmdb.getNetworkDao().create(network);
            }
        }
        catch (SQLException e) {
            logger.error("failed to add network", e);
        } catch (DatamartException e) {
            logger.error("failed to add network file", e);
        }

        return network;
    }


    // for now we just decide based on group name
    // maybe someday we'll make an explicit control
    // at the network level, or have multiple attribute
    // groupings.
    String determineNetworkType(Group group) {
        if ("Attributes".equalsIgnoreCase(group.getName())) {
            return Network.TYPE_ATTRIBUTE;
        }
        else {
            return Network.TYPE_NETWORK;
        }
    }

    // TODO: field validation, eg pubmed is a number
    @Override
    public String updateNetwork(int organismId, int networkId, String field,
            String value) throws DatamartException {

            logger.info(String.format("updating organism %d network %d field '%s' to '%s'",
                    organismId, networkId, field, value));
            try {
                Network network = dmdb.getNetworkDao().queryForId(networkId);

                if (field.equals("description")) {
                    network.setDescription(value);
                }
                else if (field.equals("comment")) {
                    network.setComment(value);
                }
                else if (field.equals("name")) {
                    network.setName(value);
                }
                else if (field.equals("pubmedId")) {
                    long pubmedId = Long.parseLong(value);
                    refreshPubmed(network, pubmedId);
                }
                else {
                    logger.error(String.format("unknown network field: '%s'", field));
                    value = null;
                }

                dmdb.getNetworkDao().update(network);
            }
            catch (SQLException e) {
                logger.error("failed to update network", e);
                value = null;
            }

            return value;
    }

    @Override
    public Network replaceNetwork(int organismId, int networkId, String originalFilename, InputStream inputStream) {
        logger.info("replacing network for organism {} network {}", organismId, networkId);

        Network network = null;
        try {

            Organism organism = dmdb.getOrganismDao().queryForId(organismId);
            network = dmdb.getNetworkDao().queryForId(networkId);

            if (organism == null || network == null) {
                logger.error("undefined network  parameters");
            }
            else {
                DataFile dataFile = uploadService.addDataFile(organismId, originalFilename, inputStream);

                network.setDataFile(dataFile);
                dmdb.getNetworkDao().update(network);

                // TODO: remove old file, or move to 'trash'?
            }
        }
        catch (SQLException e) {
            logger.error("failed to replace network", e);
        } catch (DatamartException e) {
            logger.error("failed to replace network file", e);
        }

        return network;
    }

    @Override
    public void deleteNetwork(int organismId, int networkId) throws DatamartException {
        logger.info("deleting network");
        try {
            Network network = dmdb.getNetworkDao().queryForId(networkId);
            // TODO: logging, checking, mark as trashed instead of really deleting?
            // fileStorageService.remove(network.getFilename());
            dmdb.getNetworkDao().delete(network);
        }
        catch (SQLException e) {
            logger.error("failed to replace network", e);
        }
    }

    @Override
    public void deleteAttributeMetadata(long organismId, long networkId)
            throws DatamartException {
        try {
            Network network = dmdb.getNetworkDao().queryForId((int)networkId);
            AttributeMetadata md = network.getAttributeMetadata();
            dmdb.getAttributeMetadataDao().refresh(md);

            md.setDataFile(null);
            // TODO: trash?
            dmdb.getAttributeMetadataDao().update(md);

        } catch (SQLException e) {
            throw new DatamartException("error deleting network file", e);
        }
    }

    /*
     * pubmed magic
     */
    @Override
    public Network updateNetworkMetadata(int organismId, int networkId) {
        logger.info("updating metadata for network {}", networkId);

        Network network = null;
        try {
            network = dmdb.getNetworkDao().queryForId(networkId);
            metadataService.updateMetadata(network);
            dmdb.getNetworkDao().update(network);
        }
        catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (DatamartException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return network;
    }

    /*
     * should users use this or drop down to the db layer?
     */
    @Override
    public Network getNetwork(int organismId, int networkId) throws DatamartException {
        Network network = null;
        try {
            // TODO: organism id validation
            network = dmdb.getNetworkDao().queryForId(networkId);
        }
        catch (SQLException e) {
            throw new DatamartException("failed to get network", e);
        }

        return network;
    }

    /*
     * fetch pubmed info if the id has changed, or we don't have
     * pubmed info already (e.g. previous retrieval failed for some reason
     */
    @Override
    public void refreshPubmed(Network network, long pubmedId) throws DatamartException {
        if (network.getPubmedId() != pubmedId || network.getExtra() == null || network.getExtra().trim() == "") {
            network.setPubmedId(pubmedId);
            metadataService.updateMetadata(network);
        }
    }

    @Override
    public Network replaceAttributeMetadata(int organismId, int networkId,
            String originalFilename, InputStream inputStream) {
        logger.info("replacing attribute metadata for organism {} network {}", organismId, networkId);

        Network network = null;
        try {

            Organism organism = dmdb.getOrganismDao().queryForId(organismId);
            network = dmdb.getNetworkDao().queryForId(networkId);

            if (organism == null || network == null) {
                logger.error("undefined network  parameters");
            }
            else {

                DataFile dataFile = uploadService.addDataFile(organismId, originalFilename, inputStream);

                AttributeMetadata md = network.getAttributeMetadata();
                if (md == null) {
                    md = new AttributeMetadata();
                    md.setDataFile(dataFile);
                    dmdb.getAttributeMetadataDao().create(md);
                    network.setAttributeMetadata(md);
                    dmdb.getNetworkDao().update(network);
                }
                else {
                    dmdb.getAttributeMetadataDao().refresh(md);
                    md.setDataFile(dataFile);
                    dmdb.getAttributeMetadataDao().update(md);;
                }

                // TODO: remove old file, or move to 'trash'?
            }
        }
        catch (SQLException e) {
            logger.error("failed to replace network", e);
        }
        catch (DatamartException e) {
            logger.error("failed to replace network file", e);
        }

        return network;
    }

    @Override
    public File getNetworkFile(Network network) throws DatamartException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public File getMetadataFile(Network network) throws DatamartException {
        // TODO Auto-generated method stub
        return null;
    }



}
