package org.genemania.adminweb.service.impl;

import java.io.IOException;
import java.sql.SQLException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.genemania.adminweb.dao.DatamartDb;
import org.genemania.adminweb.entity.Network;
import org.genemania.adminweb.exception.DatamartException;
import org.genemania.adminweb.service.MetadataService;
import org.genemania.adminweb.service.PubmedService;
import org.genemania.adminweb.service.impl.PubmedServiceImpl.PubmedInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MetadataServiceImpl implements MetadataService {
    final Logger logger = LoggerFactory.getLogger(MetadataServiceImpl.class);

    @Autowired
    PubmedService pubmedService;

    @Autowired
    DatamartDb dmdb;

    @Override
    public void updateMetadata(Network network) throws DatamartException {

        if (network.getPubmedId() == 0) {
            logger.debug("no pubmed id, can't retrieve metadata");
            return;
        }

        logger.debug("refreshing pubmed info");
        PubmedInfo info = pubmedService.getInfo(network.getPubmedId());

        try {
            updateJson(network, info);
        }
        catch (SQLException e) {
            throw new DatamartException("failed to save updated pubmed info", e);
        }

    }

    void updateJson(Network network, PubmedInfo info) throws SQLException {

        if (info == null) {
            network.setExtra(null);
        }
        else {
            // TODO: jackson hiding
            ObjectMapper mapper = new ObjectMapper();
            String extra = "";

            try {
                extra = mapper.writeValueAsString(info);
            }
            catch (JsonMappingException e) {
                logger.warn("failed to encode processing details", e);
            } catch (JsonGenerationException e) {
                logger.warn("failed to encode processing details", e);
            } catch (IOException e) {
                logger.warn("failed to encode processing details", e);
            }

            network.setExtra(extra);
        }
    }
}
