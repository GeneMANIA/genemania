package org.genemania.adminweb.web.service.impl;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.genemania.adminweb.dao.DatamartDb;
import org.genemania.adminweb.entity.AttributeMetadata;
import org.genemania.adminweb.entity.Functions;
import org.genemania.adminweb.entity.Group;
import org.genemania.adminweb.entity.Identifiers;
import org.genemania.adminweb.entity.Network;
import org.genemania.adminweb.entity.Organism;
import org.genemania.adminweb.service.MappingService;
import org.genemania.adminweb.service.impl.PubmedServiceImpl.PubmedInfo;
import org.genemania.adminweb.validators.stats.AttributeMetadataValidationStats;
import org.genemania.adminweb.validators.stats.IdentifierValidationStats;
import org.genemania.adminweb.validators.stats.NetworkValidationStats;
import org.genemania.adminweb.web.model.AttributesFolderTN;
import org.genemania.adminweb.web.model.FunctionsFolderTN;
import org.genemania.adminweb.web.model.FunctionsTN;
import org.genemania.adminweb.web.model.GroupFolderTN;
import org.genemania.adminweb.web.model.IdentifiersFolderTN;
import org.genemania.adminweb.web.model.IdentifiersTN;
import org.genemania.adminweb.web.model.NetworkTN;
import org.genemania.adminweb.web.model.NetworksFolderTN;
import org.genemania.adminweb.web.model.TreeNode;
import org.genemania.adminweb.web.service.TreeBuilderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
// TODO: exception handling is wrong, the interface methods should throw an
// exception if they fail to retrieve data

@Component
public class TreeBuilderServiceImpl implements TreeBuilderService {
    final Logger logger = LoggerFactory.getLogger(TreeBuilderServiceImpl.class);

    @Autowired
    private DatamartDb dmdb;

    @Autowired
    MappingService mappingService;

    // not thread safe, see helper
    SimpleDateFormat dateFormatter = new SimpleDateFormat("MMM d, yyyy h:mm:ss a z");

    /*
     * return a "Networks" container node, with child nodes for each network group,
     * each of those containing children for each network belonging to the organism.
     *
     * (non-Javadoc)
     * @see org.genemania.adminweb.web.service.NetworkService#getGroupedNetworkTree(int)
     */
    @Override
    public TreeNode getGroupedNetworkTree(int organismId) {
        List<TreeNode> children = new ArrayList<TreeNode>();

        try {
            // get all groups, including those that have no networks for
            // the given organism, since we want to be able to display
            // these in the tree

            Organism organism = dmdb.getOrganismDao().queryForId(organismId);
            List<Group> groups = dmdb.getGroupDao().queryForAll();
            // alphabetical, but 'other' at end
            Collections.sort(groups, new Comparator<Group>() {
                @Override
                public int compare(Group group1, Group group2) {
                    String name1 = group1.getName();
                    String name2 = group2.getName();
                    if (name1.equalsIgnoreCase("OTHER") && !name2.equalsIgnoreCase("OTHER")) {
                        return +1;
                    }
                    else if (!name1.equalsIgnoreCase("OTHER") && name2.equalsIgnoreCase("OTHER")) {
                        return -1;
                    }
                    else {
                        return name1.compareTo(name2);
                    }
                }
            });

            for (Group group: groups) {
                List<Network> networks = dmdb.getNetworkDao().getNetworks(organism, group);
                // order by network name, ignoring case. better not be null!
                Collections.sort(networks, new Comparator<Network>() {
                    @Override
                    public int compare(Network d1, Network d2) {
                        return (d1.getName().toLowerCase().compareTo(d2.getName().toLowerCase()));
                    }
                });

                TreeNode groupNode = getGroupTN(organismId, group);
                for (Network network: networks) {
                    TreeNode child = getNetworkTN(network);
                    groupNode.addChild(child);
                }
                children.add(groupNode);
            }
        }
        catch (SQLException e) {
            logger.error("failed to get networks", e);
        }

        TreeNode node = new NetworksFolderTN("Networks");
        node.addChildren(children);
        return node;
    }

    /*
     * in the database, groups are shared between organisms, for
     * the user interface we still add an organism id to the corresponding
     * group node, for convenience.
     */
    @Override
    public GroupFolderTN getGroupTN(int organismId, Group group) {
        GroupFolderTN node = new GroupFolderTN(group.getName());
        node.setId(group.getId());
        node.setOrganismId(organismId);
        return node;
    }

    @Override
    public NetworkTN getNetworkTN(Network network) {
        NetworkTN node = new NetworkTN(network.getName());

        try {
            populateBasicNetworkFields(network, node);
        }
        catch (SQLException e) {
            logger.error("failed to get network", e);
        }

        if (network.getType().equals("ATTRIBUTE")) {
            try {
                populateAttributeFields(network, node);
            }
            catch (SQLException e) {
                logger.error("failed to get attribute metadata");
            }
        }

        return node;
    }

    void populateAttributeFields(Network network, NetworkTN node) throws SQLException {
        node.setAttributeNetwork(true);

        AttributeMetadata md = network.getAttributeMetadata();
        if (md == null) {
            return;
        }
        dmdb.getAttributeMetadataDao().refresh(md);
        node.setLinkoutLabel(md.getLinkoutLabel());
        node.setLinkoutUrl(md.getLinkoutUrl());
        dmdb.getDataFileDao().refresh(md.getDataFile());

        if (md.getDataFile() != null) {
            node.setMetadataFilename(md.getDataFile().getOriginalFilename());
            node.setMetadataFileId(md.getDataFile().getId());

            if (md.getDataFile().getProcessingDetails() != null) {
                AttributeMetadataValidationStats details = (AttributeMetadataValidationStats) mappingService.unmap(md.getDataFile().getProcessingDetails(),
                        AttributeMetadataValidationStats.class);
                node.setMetadataProcessingDetails(details);
            }
        }
    }

    void populateBasicNetworkFields(Network network, NetworkTN node) throws SQLException {
        node.setDescription(network.getDescription());
        node.setPubmedId("" + network.getPubmedId());
        dmdb.getDataFileDao().refresh(network.getDataFile());
        node.setFilename(network.getDataFile().getOriginalFilename());
        node.setFileId(network.getDataFile().getId());
        node.setId(network.getId());
        node.setOrganismId(network.getOrganism().getId());

        if (network.getComment() != null) {
            node.setComment(network.getComment());
        }
        else {
            node.setComment("");
        }

        node.setDefaultSelected(network.isDefault());
        node.setRestrictedLicense(network.isRestrictedLicense());
        node.setEnabled(network.isEnabled());

        if (network.getDataFile().getUploadDate() != null) {
            node.setDate(formatDate(network.getDataFile().getUploadDate()));
        }
        else {
            node.setDate("unknown");
        }

        NetworkValidationStats details = null;
        if (network.getDataFile().getProcessingDetails() != null) {

            // TODO: hide jackson dependency behind a service
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            try {
                details = mapper.readValue(network.getDataFile().getProcessingDetails(), NetworkValidationStats.class);
            } catch (JsonParseException e) {
                logger.warn("failed to convert processing details to json", e);
            } catch (JsonMappingException e) {
                logger.warn("failed to convert processing details to json", e);
            } catch (IOException e) {
                logger.warn("failed to convert processing details to json", e);
            }
        }
        node.setProcessingDetails(details);

        // TODO: pubmedinfo defined in wrong place
        PubmedInfo extra = null;
        if (network.getExtra() != null && !network.getExtra().trim().equalsIgnoreCase("")) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                extra = mapper.readValue(network.getExtra(), PubmedInfo.class);
            } catch (JsonParseException e) {
                logger.warn("failed to convert pubmed info to json", e);
            } catch (JsonMappingException e) {
                logger.warn("failed to convert pubmed info to json", e);
            } catch (IOException e) {
                logger.warn("failed to convert pubmed info to json", e);
            }
        }

        node.setExtra(extra);
    }

    @Override
    public IdentifiersFolderTN getIdentifiersFolderTN(int organismId) {
        IdentifiersFolderTN node = new IdentifiersFolderTN("Identifiers");
        node.setOrganismId(organismId);

        try {
            Organism organism = dmdb.getOrganismDao().queryForId(organismId);
            List<Identifiers> identifiersList = dmdb.getIdentifiersDao().getIdentifiers(organism);

            for (Identifiers identifiers: identifiersList) {
                node.addChild(getIdentifiersTN(identifiers));
            }

        }
        catch (SQLException e) {
            logger.error("failed to get identifiers", e);
        }
        return node;
    }

    @Override
    public IdentifiersTN getIdentifiersTN(Identifiers identifiers) {
        try {
            dmdb.getDataFileDao().refresh(identifiers.getDataFile());
        }
        catch (SQLException e) {
            logger.error("failed to get identifiers");
        }

        IdentifiersTN node = new IdentifiersTN(identifiers.getDataFile().getOriginalFilename());
        node.setId(identifiers.getId());
        node.setFilename(identifiers.getDataFile().getOriginalFilename()); // UI doesn't need to know about internal storage filenames
        node.setFileId(identifiers.getDataFile().getId());
        node.setOrganismId(identifiers.getOrganism().getId());

        String processingDetails = identifiers.getDataFile().getProcessingDetails();
        if(processingDetails != null && processingDetails.trim() != null) {
            IdentifierValidationStats stats = (IdentifierValidationStats) mappingService.unmap(processingDetails,
                    IdentifierValidationStats.class);
            node.setStats(stats);
        }

        if (identifiers.getDataFile().getUploadDate() != null) {
            node.setDate(formatDate(identifiers.getDataFile().getUploadDate()));
        }
        else {
            node.setDate("unknown");
        }

        return node;
    }

    @Override
    public AttributesFolderTN getAttributesFolderTN(int organismId) {
        return new AttributesFolderTN("Attributes");
    }

    @Override
    public FunctionsFolderTN getFunctionsFolderTN(int organismId) {
        FunctionsFolderTN node = new FunctionsFolderTN("Functions");
        node.setOrganismId(organismId);

        try {
            Organism organism = dmdb.getOrganismDao().queryForId(organismId);

            // all orms seem to suck in ease of use of the relational model
            // need to figure out how to to a simple join. just query for
            // everything and filter cause its easier and i'm lazy but evil/inefficient
            List<Functions> functionsList = dmdb.getFunctionsDao().queryForAll();

            for (Functions functions: functionsList) {
                dmdb.getDataFileDao().refresh(functions.getDataFile());
                if (functions.getDataFile().getOrganism().getId() == organismId) {
                    node.addChild(getFunctionsTN(functions));
                }
            }
        }
        catch (SQLException e) {
            logger.error("failed to get functions", e);
        }
        return node;
    }

    @Override
    public List<TreeNode> getOrganismDataTree(int organismId) {
        List<TreeNode> tree = new ArrayList<TreeNode>();
        tree.add(getIdentifiersFolderTN(organismId));
        tree.add(getGroupedNetworkTree(organismId));
//        tree.add(getAttributesFolderTN(organismId));
        tree.add(getFunctionsFolderTN(organismId));

        return tree;
    }

    public
    String formatDate(Date date) {
        synchronized(dateFormatter) {
            return dateFormatter.format(date);
        }
    }

    @Override
    public FunctionsTN getFunctionsTN(Functions functions) {
        FunctionsTN node = new FunctionsTN(functions.getDataFile().getOriginalFilename());
        node.setId(functions.getId());
        node.setFilename(functions.getDataFile().getOriginalFilename());
        node.setOrganismId(functions.getDataFile().getOrganism().getId());
        node.setDate(formatDate(functions.getDataFile().getUploadDate()));
        node.setComment(functions.getComment());
        node.setFileId(functions.getDataFile().getId());
        node.setUsage(functions.getFunctionType());
        NetworkValidationStats details = (NetworkValidationStats) mappingService.unmap(functions.getDataFile().getProcessingDetails(),
                NetworkValidationStats.class);
        node.setProcessingDetails(details);

        return node;
    }
}
