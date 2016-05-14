package org.genemania.connector;

import org.apache.log4j.Logger;
import org.genemania.broker.MessageType;
import org.genemania.dao.NetworkDao;
import org.genemania.dao.NodeDao;
import org.genemania.dao.OrganismDao;
import org.genemania.domain.InteractionNetwork;
import org.genemania.domain.Ontology;
import org.genemania.domain.OntologyCategory;
import org.genemania.domain.Organism;
import org.genemania.dto.RelatedGenesWebRequestDto;
import org.genemania.dto.RelatedGenesWebResponseDto;
import org.genemania.dto.UploadNetworkWebRequestDto;
import org.genemania.dto.UploadNetworkWebResponseDto;
import org.genemania.exception.DataStoreException;
import org.genemania.message.RelatedGenesRequestMessage;
import org.genemania.message.RelatedGenesResponseMessage;
import org.genemania.message.UploadNetworkRequestMessage;
import org.genemania.message.UploadNetworkResponseMessage;
import org.genemania.util.BrokerUtils;
import org.genemania.exception.ApplicationException;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.JmsException;

import org.springframework.stereotype.Component;
import com.googlecode.ehcache.annotations.Cacheable;
import com.googlecode.ehcache.annotations.KeyGenerator;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

@Component
public class JmsEngineConnector implements EngineConnector  {
    private static final Logger LOG = Logger.getLogger(JmsEngineConnector.class);
    public static final int DEFAULT_TIMEOUT_MILLIS = 10*60*1000;
    public static final int DEFAULT_MESSAGE_EXPIRATION_MILLIS = 10*60*1000;

    private JmsTemplate jmsTemplate;
    private String requestQueueName;
    private String responseQueueName;
    private NetworkDao networkDao;
    private NodeDao nodeDao;
    private OrganismDao organismDao;
    private long timeoutMillis = DEFAULT_TIMEOUT_MILLIS;
    private long messageExpirationMillis = DEFAULT_MESSAGE_EXPIRATION_MILLIS;


    public JmsEngineConnector() {}

    @Override
    // @Cacheable(cacheName="searchResultsCache", keyGenerator=@KeyGenerator(name="StringCacheKeyGenerator"))
    public RelatedGenesWebResponseDto getRelatedGenes(RelatedGenesWebRequestDto dto) throws ApplicationException {
        LOG.info("getRelatedGenes request");

        RelatedGenesRequestMessage request = BrokerUtils.dto2msg(dto);
        RelatedGenesWebResponseDto ret;

        JmsRequestResponse sessionCallback = new JmsRequestResponse(request.toXml(),
                MessageType.RELATED_GENES.getCode(), requestQueueName, responseQueueName,
                jmsTemplate.getDestinationResolver(), timeoutMillis, messageExpirationMillis);

        String responseText;
        try {
            responseText = jmsTemplate.execute(sessionCallback, true);
        }
        catch (JmsException e) {
            throw new ApplicationException("JMS error processing get related genes request", e);
        }

        if (responseText == null) {
            throw new ApplicationException("engine request timed out");
        }

        RelatedGenesResponseMessage responseMessage = RelatedGenesResponseMessage.fromXml(responseText);
        if (responseMessage.getErrorCode() == 0) {
            RelatedGenesWebResponseDto hollowResponseDto = BrokerUtils.msg2dto(responseMessage);
            try {
                ret = load(hollowResponseDto);
            }
            catch (DataStoreException e) {
                throw new ApplicationException("data access error processing get related genes request", e);
            }
        }
        else {
            throw new ApplicationException(responseMessage.getErrorMessage(),
                    responseMessage.getErrorCode());
        }

        LOG.info("completed getRelatedGenes request");
        return ret;
    }

    @Override
    public UploadNetworkWebResponseDto uploadNetwork(UploadNetworkWebRequestDto dto) throws ApplicationException {
        LOG.info("uploadNetwork request");

        UploadNetworkRequestMessage request = BrokerUtils.dto2msg(dto);
        UploadNetworkWebResponseDto ret;

        JmsRequestResponse sessionCallback = new JmsRequestResponse(request.toXml(),
                MessageType.TEXT2NETWORK.getCode(), requestQueueName, responseQueueName,
                jmsTemplate.getDestinationResolver(), timeoutMillis, messageExpirationMillis);

        String responseText;
        try {
            responseText = jmsTemplate.execute(sessionCallback, true);
        }
        catch (JmsException e) {
            throw new ApplicationException("JMS error processing upload network request", e);
        }

        if (responseText == null) {
            throw new ApplicationException("engine request timed out");
        }

        UploadNetworkResponseMessage responseMessage = UploadNetworkResponseMessage.fromXml(responseText);
        if (responseMessage.getErrorCode() == 0) {
            ret = BrokerUtils.msg2dto(responseMessage);
        }
        else {
            throw new ApplicationException(responseMessage.getErrorMessage(),
                    responseMessage.getErrorCode());
        }

        LOG.info("completed uploadNetwork request");
        return ret;
    }

    private RelatedGenesWebResponseDto load(
            RelatedGenesWebResponseDto hollowResponseDto)
            throws DataStoreException {
        RelatedGenesWebResponseDto ret = hollowResponseDto;
        NetworkDao netDao = getNetworkDao();

        for (InteractionNetwork hollowNetwork : hollowResponseDto.getNetworks()) {
            InteractionNetwork network = netDao.findNetwork(hollowNetwork
                    .getId());

            if (network != null) { // predefined networks
                hollowNetwork.setDefaultSelected(network.isDefaultSelected());
                hollowNetwork.setMetadata(network.getMetadata());
                hollowNetwork.setName(network.getName());
                hollowNetwork.setTags(network.getTags());
            }

        }

        // extract organism id
        long organismId = hollowResponseDto.getOrganismId();
        // load ontology categories
        Organism organism = getOrganismDao().findOrganism(organismId);
        Ontology ontology = organism.getOntology();
        Map<Long, Collection<OntologyCategory>> annotations = hollowResponseDto
                .getAnnotations();
        Iterator<Long> nodeIterator = annotations.keySet().iterator();

        while (nodeIterator.hasNext()) {
            long nodeId = nodeIterator.next();
            Collection<OntologyCategory> categories = annotations.get(nodeId);

            for (OntologyCategory hollowCategory : categories) {
                OntologyCategory category = getOntologyCategory(hollowCategory
                        .getId(), ontology);

                if (category != null) {
                    hollowCategory.setDescription(category.getDescription());
                    hollowCategory.setName(category.getName());
                } else {
                    LOG.warn("Could not load ontology category with id ["
                            + hollowCategory.getId() + "]");
                }
            }
        }
        ret.setCombiningMethod(hollowResponseDto.getCombiningMethod());
        return ret;
    }

    private OntologyCategory getOntologyCategory(long ontologyCategoryId,
                                                 Ontology ontology) {
        OntologyCategory ret = null;
        Collection<OntologyCategory> categories = ontology.getCategories();
        for (OntologyCategory cat : categories) {
            if (cat != null) {
                if (cat.getId() == ontologyCategoryId) {
                    ret = cat;
                    break;
                }
            } else {
                LOG.warn("null ontology category found");
            }
        }
        return ret;
    }

    public NetworkDao getNetworkDao() {
        return this.networkDao;
    }

    public NodeDao getNodeDao() {
        return this.nodeDao;
    }

    public OrganismDao getOrganismDao() {
        return this.organismDao;
    }

    public void setNetworkDao(NetworkDao networkDao) {
        this.networkDao = networkDao;
    }

    public void setNodeDao(NodeDao nodeDao) {
        this.nodeDao = nodeDao;
    }

    public void setOrganismDao(OrganismDao organismDao) {
        this.organismDao = organismDao;
    }

    public JmsTemplate getJmsTemplate() {
        return jmsTemplate;
    }

    public void setJmsTemplate(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public String getRequestQueueName() {
        return requestQueueName;
    }

    public void setRequestQueueName(String requestQueueName) {
        this.requestQueueName = requestQueueName;
    }

    public String getResponseQueueName() {
        return responseQueueName;
    }

    public void setResponseQueueName(String resonseQueueName) {
        this.responseQueueName = resonseQueueName;
    }

    public long getTimeoutMillis() {
        return timeoutMillis;
    }

    public void setTimeoutMillis(long timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
    }

    public long getMessageExpirationMillis() {
        return messageExpirationMillis;
    }

    public void setMessageExpirationMillis(long messageExpirationMillis) {
        this.messageExpirationMillis = messageExpirationMillis;
    }
}
