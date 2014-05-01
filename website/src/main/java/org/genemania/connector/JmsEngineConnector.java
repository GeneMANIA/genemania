/**
 * This file is part of GeneMANIA.
 * Copyright (C) 2010 University of Toronto.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

/**
 * JmsEngineConnector: a JMS implementation of the contract interface to the engine 
 * Created Jul 15, 2009
 * @author Ovi Comes
 */
package org.genemania.connector;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.genemania.Constants;
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
import org.genemania.exception.ApplicationException;
import org.genemania.exception.DataStoreException;
import org.genemania.message.RelatedGenesRequestMessage;
import org.genemania.message.RelatedGenesResponseMessage;
import org.genemania.message.UploadNetworkRequestMessage;
import org.genemania.message.UploadNetworkResponseMessage;
import org.genemania.util.ApplicationConfig;
import org.genemania.util.BrokerUtils;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Component;

import com.googlecode.ehcache.annotations.Cacheable;
import com.googlecode.ehcache.annotations.KeyGenerator;

@Component
public class JmsEngineConnector implements EngineConnector {

	// __[static]______________________________________________________________
	private static Logger LOG = Logger.getLogger(JmsEngineConnector.class);
	private static int sentMessages = 0;
	private static int receivedMessages = 0;
	private static int processedMessages = 0;
	private static int errors = 0;
	private static int appErrors = 0;

	// __[attributes]__________________________________________________________
	private JmsTemplate jmsTemplate;
	private Queue requestQueue;
	private Queue replyQueue;
	private Queue statsQueue;

	//__[ injected properties ]________________________________________________
	
	private NetworkDao networkDao;
	private NodeDao nodeDao;
	private OrganismDao organismDao;

	//__[ injected properties setters/getters ]________________________________
	
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
	
	// __[constructors]________________________________________________________
	public JmsEngineConnector() {
	}

	// __[accessors]___________________________________________________________

	public void setConnectionFactory(ConnectionFactory cf) {
		this.jmsTemplate = new JmsTemplate(cf);
		int timeout = Integer
				.parseInt(ApplicationConfig
						.getInstance()
						.getProperty(
								org.genemania.broker.Constants.CONFIG_PROPERTIES.CLIENT_TIMEOUT));
		this.jmsTemplate.setReceiveTimeout(1000 * timeout);
	}

	public void setRequestQueue(Queue requestsQueue) {
		this.requestQueue = requestsQueue;
	}

	public JmsTemplate getJmsTemplate() {
		return jmsTemplate;
	}

	public void setJmsTemplate(JmsTemplate jmsTemplate) {
		this.jmsTemplate = jmsTemplate;
	}

	public Queue getRequestQueue() {
		return requestQueue;
	}

	public Queue getReplyQueue() {
		return replyQueue;
	}

	public void setReplyQueue(Queue replyQueue) {
		this.replyQueue = replyQueue;
	}

	public Queue getStatsQueue() {
		return statsQueue;
	}

	public void setStatsQueue(Queue statsQueue) {
		this.statsQueue = statsQueue;
	}

	// __[public helpers]______________________________________________________
	@Cacheable(cacheName="searchResultsCache", keyGenerator=@KeyGenerator(name="StringCacheKeyGenerator"))
	public RelatedGenesWebResponseDto getRelatedGenes(
			final RelatedGenesWebRequestDto dto) throws ApplicationException {
		final String rgcid = String.valueOf(System.currentTimeMillis());
		RelatedGenesWebResponseDto ret = new RelatedGenesWebResponseDto();
		jmsTemplate.send(requestQueue, new MessageCreator() {
			public TextMessage createMessage(Session session)
					throws JMSException {
				LOG.debug("sending GetRelatedGenesMessage request to "
						+ requestQueue.getQueueName());
				RelatedGenesRequestMessage request = BrokerUtils.dto2msg(dto);
				TextMessage ret = session.createTextMessage(request.toXml());
				ret.setJMSDeliveryMode(DeliveryMode.PERSISTENT);
				ret.setJMSType(MessageType.RELATED_GENES.getCode());
				replyQueue = session.createTemporaryQueue();
				ret.setJMSReplyTo(replyQueue);
				ret.setJMSCorrelationID(rgcid);
				LOG.debug("getRelatedGenes waiting for reply on "
						+ replyQueue.getQueueName());
				return ret;
			}
		});
		sentMessages++;
		Message response;
		try {
			response = jmsTemplate.receive(replyQueue);
			receivedMessages++;
			if (response == null) {
				LOG.error("getRelatedGenes JMS response is null");				
			}
			else if (!response.getJMSCorrelationID().equals(rgcid)) {
				LOG.error("JMS response id does not match request, sent " + rgcid + 
						", recieved " + response.getJMSCorrelationID() + ", dropping response.");
			}
			else {
					LOG.debug("getRelatedGenes reply received");
					String responseBody = ((TextMessage) response).getText();
					if (StringUtils.isNotEmpty(responseBody)) {
						RelatedGenesResponseMessage responseMessage = RelatedGenesResponseMessage
								.fromXml(responseBody);
						LOG.debug("finished fromXml");
						LOG.debug("num attributes in response message: " + responseMessage.getAttributes().size());
						if (responseMessage.getErrorCode() == 0) {
							// friendlyPrintNetworks("networks in response message: ",
							// responseMessage.getNetworks());
							// friendlyPrintCategories("categories in response message: ",
							// responseMessage.getAnnotations());
							RelatedGenesWebResponseDto hollowResponseDto = BrokerUtils
									.msg2dto(responseMessage);
							LOG.debug("finished msg2dto");
							ret = load(hollowResponseDto);
							LOG.debug("finished hollowResponseDto");
						} else {
							LOG.error(responseMessage.getErrorMessage());
							appErrors++;
							throw new ApplicationException(responseMessage
									.getErrorMessage(), responseMessage
									.getErrorCode());
						}
					} else {
						LOG.error("getRelatedGenes empty response body");
					}
			}
			processedMessages++;
		} catch (JMSException e) {
			LOG.error("getRelatedGenes JMSException: " + e.getMessage());
			errors++;
			throw new ApplicationException(
					Constants.ERROR_CODES.APPLICATION_ERROR);
		} catch (DataStoreException e) {
			LOG.error("getRelatedGenes DataStoreException: " + e.getMessage());
			errors++;
			throw new ApplicationException(Constants.ERROR_CODES.DATA_ERROR);
		} finally {
			LOG.debug("messages sent/received/processed/errors: "
					+ sentMessages + "/" + receivedMessages + "/"
					+ processedMessages + "/" + errors);
			// updateStats();
		}
		LOG.debug("getRelatedGenes request processing completed");
		return ret;
	}

	public UploadNetworkWebResponseDto uploadNetwork(
			final UploadNetworkWebRequestDto dto) throws ApplicationException {
		final String uncid = String.valueOf(System.currentTimeMillis());
		UploadNetworkWebResponseDto ret = new UploadNetworkWebResponseDto();
		jmsTemplate.send(requestQueue, new MessageCreator() {
			public TextMessage createMessage(Session session)
					throws JMSException {
				LOG.debug("sending UploadnetworkMessage request to "
						+ requestQueue.getQueueName());
				UploadNetworkRequestMessage request = BrokerUtils.dto2msg(dto);
				TextMessage ret = session.createTextMessage(request.toXml());
				ret.setJMSDeliveryMode(DeliveryMode.PERSISTENT);
				ret.setJMSType(MessageType.TEXT2NETWORK.getCode());
				replyQueue = session.createTemporaryQueue();
				ret.setJMSReplyTo(replyQueue);
				ret.setJMSCorrelationID(uncid);
				LOG.debug("uploadNetwork waiting for reply on "
						+ replyQueue.getQueueName());
				sentMessages++;
				return ret;
			}
		});
		Message response;
		try {
			response = jmsTemplate.receive(replyQueue);
			receivedMessages++;
			if (response.getJMSCorrelationID().equals(uncid)) {
				LOG.debug("uploadNetwork reply received");
				
				if (response != null) {
					String responseBody = ((TextMessage) response).getText();
					if (StringUtils.isNotEmpty(responseBody)) {
						UploadNetworkResponseMessage responseMessage = UploadNetworkResponseMessage
								.fromXml(responseBody);
						if (responseMessage.getErrorCode() == 0) {
							ret = BrokerUtils.msg2dto(responseMessage);
						} else {
							throw new ApplicationException(responseMessage
									.getErrorMessage(), responseMessage
									.getErrorCode());
						}
					} else {
						LOG.error("uploadNetwork empty response body");
					}
				} else {
					LOG.error("uploadNetwork JMS response is null");
				}
			}
			processedMessages++;
		} catch (JMSException e) {
			errors++;
			throw new ApplicationException(
					Constants.ERROR_CODES.APPLICATION_ERROR);
		} finally {
			LOG.debug("messages sent/received/processed/appErrors/errors: "
					+ sentMessages + "/" + receivedMessages + "/"
					+ processedMessages + "/" + appErrors + "/" + errors);
			// updateStats();
		}
		LOG.debug("uploadNetwork request processing completed");
		return ret;
	}

	// __[private helpers]_____________________________________________________
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
}