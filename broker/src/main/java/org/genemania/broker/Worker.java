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
 * Worker: a JMS consumer that mediates engine requests  
 * Created Jul 16, 2009
 * @author Ovi Comes
 */
package org.genemania.broker;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.pool.PooledConnectionFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.genemania.dto.EnrichmentEngineRequestDto;
import org.genemania.dto.EnrichmentEngineResponseDto;
import org.genemania.dto.NetworkDto;
import org.genemania.dto.OntologyCategoryDto;
import org.genemania.dto.RelatedGenesEngineRequestDto;
import org.genemania.dto.RelatedGenesEngineResponseDto;
import org.genemania.dto.UploadNetworkEngineRequestDto;
import org.genemania.dto.UploadNetworkEngineResponseDto;
import org.genemania.engine.IMania;
import org.genemania.engine.Mania2;
import org.genemania.engine.cache.DataCache;
import org.genemania.engine.cache.FileSerializedObjectCache;
import org.genemania.engine.cache.MemObjectCache;
import org.genemania.exception.ApplicationException;
import org.genemania.message.RelatedGenesRequestMessage;
import org.genemania.message.RelatedGenesResponseMessage;
import org.genemania.message.UploadNetworkRequestMessage;
import org.genemania.message.UploadNetworkResponseMessage;
import org.genemania.util.ApplicationConfig;
import org.genemania.util.BrokerUtils;

public class Worker implements MessageListener, ExceptionListener {

	// __[static]______________________________________________________________
	private static Logger LOG = Logger.getLogger(Worker.class);
	
	// __[attributes]__________________________________________________________
	private String appVer;
	private String brokerUrl;
    private Session session;
    private MessageConsumer requestHandler;
    private MessageProducer responseHandler;
    private IMania engine;
    private String mqRequestsQueueName;
    private int processedMessages = 0;
    private TextMessage requestMessage = null;
    private TextMessage responseMessage = null;
    
	// __[constructors]________________________________________________________
	public Worker() {
		config();
	}
	
	// __[public interface]____________________________________________________
	public void start() {
		try {
            String cacheDir = ApplicationConfig.getInstance().getProperty(org.genemania.Constants.CONFIG_PROPERTIES.CACHE_DIR);
            if (StringUtils.isEmpty(cacheDir)) {
    			LOG.error("Missing required parameter: engine cache dir. Exiting...");
            	System.exit(1);
            }

            engine = new Mania2(new DataCache(new MemObjectCache(new FileSerializedObjectCache(cacheDir))));
			// output startup info
			LOG.info("GeneMANIA Worker ver. " + appVer);
            LOG.info("Engine ver. " + engine.getVersion() + ", cache: " + cacheDir);
            startNewConnection();
		} catch (JMSException e) {
        	LOG.error("Worker startup error", e);
        }
	}
	
	// __[interface implementation]____________________________________________
	public synchronized void onMessage(Message msg) {
        if (msg instanceof TextMessage) {
        	String responseBody = "";
			try {
				// extract message data
				Queue queue = (Queue)msg.getJMSDestination();
				requestMessage = (TextMessage)msg;
            	LOG.debug("new " + msg.getJMSType() + " message received on queue " + queue.getQueueName() + "[correlation id: " + msg.getJMSCorrelationID() + "]");            	
				responseMessage = session.createTextMessage();
				responseMessage.setJMSDestination(requestMessage.getJMSReplyTo());
				responseMessage.setJMSDeliveryMode(DeliveryMode.PERSISTENT);
				responseMessage.setJMSCorrelationID(msg.getJMSCorrelationID());
				// invoke engine
				if(queue.getQueueName().equalsIgnoreCase(mqRequestsQueueName)) {
					if(MessageType.RELATED_GENES.equals(MessageType.fromCode(msg.getJMSType()))) {
						RelatedGenesRequestMessage data = RelatedGenesRequestMessage.fromXml(requestMessage.getText());
			        	RelatedGenesResponseMessage response = getRelatedGenes(data);
			        	responseBody = response.toXml();
					} else if (MessageType.TEXT2NETWORK.equals(MessageType.fromCode(msg.getJMSType()))) {
						UploadNetworkRequestMessage data = UploadNetworkRequestMessage.fromXml(requestMessage.getText());
						UploadNetworkResponseMessage response = uploadNetwork(data);
			        	responseBody = response.toXml();
					} else if (MessageType.PROFILE2NETWORK.equals(MessageType.fromCode(msg.getJMSType()))) {
		            	LOG.warn("invoking engine.profile2network: not implemented");
					} else {
			        	LOG.warn("Unknown jms type: " + msg.getJMSType());
					}
				}
				processedMessages++;
			} catch (JMSException e) {
				LOG.error(e);
				try {
		        	responseBody = buildErrorMessage(e.getMessage(), MessageType.fromCode(msg.getJMSType()));
				} catch (JMSException x) {
					LOG.error(x);
				}
			} finally {
				if((requestMessage != null) && (responseMessage != null)) {
	            	try {
	            		if(StringUtils.isNotEmpty(responseBody)) {
	            			responseMessage.setText(responseBody);
	            			LOG.debug("Responding to " + responseMessage.getJMSDestination() + ", msg id " + responseMessage.getJMSCorrelationID() + ", response body size " + (int)responseBody.length());
	            			responseHandler.send(responseMessage.getJMSDestination(), responseMessage);
	            		} else {
	    		        	responseBody = buildErrorMessage("Empty response body detected", MessageType.fromCode(msg.getJMSType()));
	            		}
					} catch (JMSException e) {
						LOG.error("JMS Exception: " + e.getMessage());
            			try {
        		        	responseBody = buildErrorMessage(e.getMessage(), MessageType.fromCode(msg.getJMSType()));
							responseHandler.send(responseMessage);
						} catch (JMSException e1) {
							LOG.error("JMS Exception", e1);
						}
					}            	
				} else {
					if(requestMessage == null) {
						LOG.error("request message is null");
					}
					if(responseMessage == null) {					
						LOG.error("response message is null");
					}
				}
			}
        } else {
        	LOG.warn("Unknown message type: " + msg);
        }
		LOG.info("successfully processed messages: " + processedMessages);
	}

	public synchronized void onException(JMSException e) {
        LOG.error("JMS Exception detected.", e);
    }
	
	// __[private helpers]____________________________________________________
    private void config() {
		// read config data
		appVer = ApplicationConfig.getInstance().getProperty(Constants.CONFIG_PROPERTIES.APP_VER);
		String brokerProtocol = ApplicationConfig.getInstance().getProperty(Constants.CONFIG_PROPERTIES.BROKER_PROTOCOL);
		String brokerHost = ApplicationConfig.getInstance().getProperty(Constants.CONFIG_PROPERTIES.BROKER_HOST);
		String brokerPort = ApplicationConfig.getInstance().getProperty(Constants.CONFIG_PROPERTIES.BROKER_PORT);
		String maxInactivityDuration = ApplicationConfig.getInstance().getProperty(Constants.CONFIG_PROPERTIES.MAX_INACTIVITY_DURATION );
		brokerUrl = brokerProtocol + "://" + brokerHost + ":" + brokerPort + "?wireFormat.maxInactivityDuration=" + maxInactivityDuration; 
		mqRequestsQueueName = ApplicationConfig.getInstance().getProperty(Constants.CONFIG_PROPERTIES.MQ_REQUESTS_QUEUE_NAME);
    }
    
    private void startNewConnection() throws JMSException {
		PooledConnectionFactory connectionFactory = new PooledConnectionFactory(brokerUrl);
    	Connection connection = connectionFactory.createConnection();
    	connection.setExceptionListener(this);
    	connection.start();
	    session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
	    responseHandler = session.createProducer(null);
	    Queue requestsQueue = session.createQueue(mqRequestsQueueName);
	    requestHandler = session.createConsumer(requestsQueue);
	    requestHandler.setMessageListener(this);
        LOG.info("Listening to " + requestsQueue.getQueueName());
    }
    
    private RelatedGenesResponseMessage getRelatedGenes(RelatedGenesRequestMessage requestMessage) {
    	RelatedGenesResponseMessage ret = new RelatedGenesResponseMessage();
		try {
			RelatedGenesEngineRequestDto rgRequestDto = BrokerUtils.msg2dto(requestMessage);
			RelatedGenesEngineResponseDto rgResponseDto = engine.findRelated(rgRequestDto);
			printEngineReturn(rgResponseDto);
			EnrichmentEngineRequestDto eRequestDto = BrokerUtils.buildEnrichmentRequestFrom(rgRequestDto, rgResponseDto, requestMessage.getOntologyId());
			EnrichmentEngineResponseDto eResponseDto = null;
			try {
				eResponseDto = engine.computeEnrichment(eRequestDto);
			} catch (Exception e) {
				LOG.error("Failed to compute enrichment", e);
			}
			if(eResponseDto != null) {
				Collection<OntologyCategoryDto> enrichedCategories = eResponseDto.getEnrichedCategories();
				LOG.debug("enriched categories size:" + enrichedCategories.size());
//				for(OntologyCategoryVO category: enrichedCategories) {
//					LOG.debug("category[" + category.getId() + "][qVal=" + category.getqValue() + "][coverage=" + category.getNumAnnotatedInSample() + "/" + category.getNumAnnotatedInTotal() + "]");
//				}
				ret = BrokerUtils.dto2msg(rgResponseDto, eResponseDto);
			} else {
				ret = BrokerUtils.dto2msg(rgResponseDto);
				LOG.warn("enriched categories response DTO is null");
			}
			ret.setNodes(rgResponseDto.getNodes());
			// test setting organism id
			ret.setOrganismId(requestMessage.getOrganismId());
			printConverterReturn(ret);
		} catch (ApplicationException e) {
    		LOG.error("Failed to get related genes: ", e);
    		ret.setErrorCode(org.genemania.Constants.ERROR_CODES.APPLICATION_ERROR);
    		ret.setErrorMessage(e.getMessage());
		}
    	return ret;
	}
	
    private UploadNetworkResponseMessage uploadNetwork(UploadNetworkRequestMessage requestMessage) {
    	UploadNetworkResponseMessage ret = new UploadNetworkResponseMessage();
		try {
			UploadNetworkEngineRequestDto requestDto = BrokerUtils.msg2dto(requestMessage);
			UploadNetworkEngineResponseDto responseDto = engine.uploadNetwork(requestDto);
			LOG.debug(responseDto.toString());
			ret = BrokerUtils.dto2msg(responseDto);
		} catch (ApplicationException e) {
			LOG.error("Failed to load network", e);
    		ret.setErrorCode(org.genemania.Constants.ERROR_CODES.APPLICATION_ERROR);
    		ret.setErrorMessage(e.getMessage());
		}
    	return ret;
	}
	
	private void printConverterReturn(RelatedGenesResponseMessage msg) {
		StringBuffer buf1 = new StringBuffer("dto2msg returned " + msg.getNetworks().size() + " networks=["); 
		Iterator<NetworkDto> it1 = msg.getNetworks().iterator();
		while(it1.hasNext()) {
			buf1.append(((NetworkDto)it1.next()).getId());
			if(it1.hasNext()) {
				buf1.append(" ");
			}
		}
		buf1.append("]");
		Map<Long, Collection<OntologyCategoryDto>> annotations = msg.getAnnotations();
		buf1.append(" and " + annotations.size() + " annotations.");
//		buf1.append(" and " + annotations.size() + " annotations=[");
//		Iterator<Long> annotationsNodeIterator = annotations.keySet().iterator();
//		while(annotationsNodeIterator.hasNext()) {
//			long nodeId = annotationsNodeIterator.next();
//			Collection<OntologyCategoryVO> categories = annotations.get(nodeId);
//			if(categories.size() > 0) {
//				buf1.append(nodeId + "->[");
//				for(OntologyCategoryVO cat: categories) {
//					buf1.append(cat.toString());
//				}
//				buf1.append("]");
//				if(annotationsNodeIterator.hasNext()) {
//					buf1.append(", ");
//				}
//			}
//		}
//		buf1.append("]");
		LOG.debug(buf1);
	}

	private void printEngineReturn(RelatedGenesEngineResponseDto responseDto) {
		StringBuffer buf = new StringBuffer("engine returned " + responseDto.getNetworks().size() + " networks=["); 
		Iterator<NetworkDto> it = responseDto.getNetworks().iterator();
		while(it.hasNext()) {
			buf.append(((NetworkDto)it.next()).getId());
			if(it.hasNext()) {
				buf.append(" ");
			}
		}
		buf.append("]");
		if (responseDto.getAttributes() != null) {
		    buf.append(" and " + responseDto.getAttributes().size() + " attributes");
		}
		else {
		    buf.append(" and 0 attributes");
		}
		LOG.debug(buf);
	}
	
	private String buildErrorMessage(String errMsg, MessageType msgType) {
		String ret = "";
		LOG.debug("WORKER ERROR: " + errMsg);
		if(MessageType.RELATED_GENES.equals(msgType)) {
        	RelatedGenesResponseMessage response = new RelatedGenesResponseMessage();
        	response.setErrorCode(Constants.ERROR_CODES.ENGINE_ERROR);
        	response.setErrorMessage(errMsg);
        	ret = response.toXml();
		} else if (MessageType.TEXT2NETWORK.equals(msgType)) {
			UploadNetworkResponseMessage response = new UploadNetworkResponseMessage();
        	response.setErrorCode(Constants.ERROR_CODES.ENGINE_ERROR);
        	response.setErrorMessage(errMsg);
        	ret = response.toXml();
		}
		return ret;
	}

	// __[main]________________________________________________________________
	public static void main(String[] args) {
		Worker w = new Worker();
		w.start();
	}
}

