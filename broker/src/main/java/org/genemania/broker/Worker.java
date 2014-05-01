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

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import javax.jms.*;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.pool.PooledConnection;
import org.apache.activemq.pool.PooledConnectionFactory;
import org.apache.activemq.transport.TransportListener;
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

public class Worker implements MessageListener, ExceptionListener, TransportListener {

    private static Logger LOG = Logger.getLogger(Worker.class);

    // configure message expiration, why are all app specific config constant names in common?
    // putting this one here.
    private static final String CONFIG_MESSAGE_EXPIRATION_MILLIS = "messageExpirationMillis";

    private String appVer;
    private String brokerUrl;
    private Session session;

    private MessageConsumer requestHandler;
    private MessageProducer responseHandler;

    private IMania engine;
    private String cacheDir;
    private String mqRequestsQueueName;
    private long messageExpirationMillis;
    private int processedMessages = 0;

    private long checkisActivePollingIntervalMillis = 60000;
    private boolean active = true;

    public static void main(String[] args) {
        Worker w = new Worker();
        w.start();
    }

    public Worker() {
        config();
    }

    public void start() {
        try {
            if (StringUtils.isEmpty(cacheDir)) {
                LOG.error("Missing required parameter: engine cache dir. Exiting...");
                System.exit(1);
            }

            engine = new Mania2(new DataCache(new MemObjectCache(new FileSerializedObjectCache(cacheDir))));

            // output startup info
            LOG.info("GeneMANIA Worker ver: " + appVer);
            LOG.info("Engine ver: " + engine.getVersion());
            LOG.info("cache dir: " + cacheDir);
            LOG.info("broker URL: " + brokerUrl);
            LOG.info("request Queue Name: " + mqRequestsQueueName);
            LOG.info("messageExpirationMillis: " + messageExpirationMillis);

            startNewConnection();
            waitForExit();
        } catch (JMSException e) {
            LOG.error("Worker startup error", e);
        }
    }

    /*
     * in order to keep the worker alive across jms disconnects, keep the main thread
     * running while polling a status flag. tidy cooperative shutdown would involve
     * setting the flag to false somehow, but currently we just kill workers externally
     */
    public void waitForExit() {
        while (true) {
            try {
                if (!isActive()) {
                    return;
                }
                Thread.sleep(checkisActivePollingIntervalMillis);
            }
            catch (InterruptedException e) {
                // swallow, return to sleep unless
                // active flag was changed to false
            }
        }
    }

    /*
     * implement MessageListener interface, handling requests
     * from website
     */
    @Override
    public synchronized void onMessage(Message msg) {
        if (!(msg instanceof TextMessage)) {
            LOG.warn("Unexpected message instance type: " + msg.getClass().getName());
            return;
        }

        try {
            // extract message data
            Queue queue = (Queue) msg.getJMSDestination();
            TextMessage requestMessage = (TextMessage) msg;

            LOG.debug("new " + msg.getJMSType() + " message received on queue " + queue.getQueueName() +
                    "[correlation id: " + msg.getJMSCorrelationID() + "]");

            // invoke engine
            String responseBody = invokeEngine(requestMessage.getJMSType(), requestMessage.getText());

            // send reply
            LOG.debug("Responding to " + requestMessage.getJMSDestination() + ", msg id " +
                    requestMessage.getJMSCorrelationID() + ", response body size " + responseBody.length());

            TextMessage responseMessage = session.createTextMessage();
            responseMessage.setJMSDestination(requestMessage.getJMSReplyTo());
            responseMessage.setJMSDeliveryMode(DeliveryMode.PERSISTENT);
            responseMessage.setJMSCorrelationID(msg.getJMSCorrelationID());
            responseMessage.setText(responseBody);

            responseHandler.send(responseMessage.getJMSDestination(), responseMessage);

            processedMessages++;
            LOG.info("successfully processed messages: " + processedMessages);
        }
        catch (JMSException e) {
            LOG.error("JMS Exception: ", e);
        }
    }

    /*
     * convert given message text to an engine request, execute, and convert response
     * back to text.
     */
    String invokeEngine(String msgType, String messageText) {
        String responseBody;

        if (MessageType.RELATED_GENES.equals(MessageType.fromCode(msgType))) {
            RelatedGenesRequestMessage data = RelatedGenesRequestMessage.fromXml(messageText);
            RelatedGenesResponseMessage response = getRelatedGenes(data);
            responseBody = response.toXml();
        }
        else if (MessageType.TEXT2NETWORK.equals(MessageType.fromCode(msgType))) {
            UploadNetworkRequestMessage data = UploadNetworkRequestMessage.fromXml(messageText);
            UploadNetworkResponseMessage response = uploadNetwork(data);
            responseBody = response.toXml();
        }
        else {
            LOG.warn("Unknown message type: " + msgType);
            responseBody = buildErrorMessage("Unknown message type");
        }

        return responseBody;
    }

    /*
     * implement Exception listener. just log the error. depend on activemq failover transport,
     * which should be specified via brokerUrl, to handle reconnect on jms errors
     */
    @Override
    public synchronized void onException(JMSException e) {
        LOG.error("JMS Exception detected.", e);
    }

    /*
     * load run parameters configured in properties file
     */
    private void config() {
        // read config data
        ApplicationConfig config = ApplicationConfig.getInstance();

        appVer = config.getProperty(Constants.CONFIG_PROPERTIES.APP_VER);
        brokerUrl = config.getProperty(Constants.CONFIG_PROPERTIES.BROKER_URL);
        mqRequestsQueueName = config.getProperty(Constants.CONFIG_PROPERTIES.MQ_REQUESTS_QUEUE_NAME);
        cacheDir = config.getProperty(org.genemania.Constants.CONFIG_PROPERTIES.CACHE_DIR);
        messageExpirationMillis = Integer.parseInt(config.getProperty(CONFIG_MESSAGE_EXPIRATION_MILLIS));

    }

    /*
     * setup request handler and start listening to request queue
     */
    private void startNewConnection() throws JMSException {
        PooledConnectionFactory connectionFactory = new PooledConnectionFactory(brokerUrl);
        Connection connection = connectionFactory.createConnection();

        connection.setExceptionListener(this);

        // TransportListener is just for extra logging to monitor disconnects
        // the casting is ugly no? this can be removed without functionally affecting
        // the worker
        ((ActiveMQConnection)((PooledConnection) connection).getConnection()).addTransportListener(this);

        // setup Consumer to receive requests and Producer to send results
        // the response handler will use a temp queue specified in the request
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        responseHandler = session.createProducer(null);
        responseHandler.setTimeToLive(messageExpirationMillis);

        Queue requestsQueue = session.createQueue(mqRequestsQueueName);
        requestHandler = session.createConsumer(requestsQueue);
        requestHandler.setMessageListener(this);

        // everyone is on stage, we can start the dance
        connection.start();
        LOG.info("Listening to " + requestsQueue.getQueueName());
    }

    /*
     * process the two engine api calls corresponding to the website's get-related-genes request,
     * one to find the related-genes and the second to compute enrichment.
     *
     * TODO: cleanup use of deprecated API's
     */
    private RelatedGenesResponseMessage getRelatedGenes(RelatedGenesRequestMessage requestMessage) {
        RelatedGenesResponseMessage ret = new RelatedGenesResponseMessage();

        try {
            RelatedGenesEngineRequestDto rgRequestDto = BrokerUtils.msg2dto(requestMessage);
            RelatedGenesEngineResponseDto rgResponseDto = engine.findRelated(rgRequestDto);

            printEngineReturn(rgResponseDto);

            EnrichmentEngineRequestDto eRequestDto = BrokerUtils.
                    buildEnrichmentRequestFrom(rgRequestDto, rgResponseDto, requestMessage.getOntologyId());
            EnrichmentEngineResponseDto eResponseDto = null;

            try {
                eResponseDto = engine.computeEnrichment(eRequestDto);
            } catch (Exception e) {
                LOG.error("Failed to compute enrichment", e);
            }

            if (eResponseDto != null) {
                Collection<OntologyCategoryDto> enrichedCategories = eResponseDto.getEnrichedCategories();
                LOG.debug("enriched categories size:" + enrichedCategories.size());
                ret = BrokerUtils.dto2msg(rgResponseDto, eResponseDto);
            }
            else {
                ret = BrokerUtils.dto2msg(rgResponseDto);
                LOG.warn("enriched categories response DTO is null");
            }

            ret.setNodes(rgResponseDto.getNodes());
            ret.setOrganismId(requestMessage.getOrganismId());
            printConverterReturn(ret);
        }
        catch (ApplicationException e) {
            LOG.error("Failed to get related genes: ", e);
            ret.setErrorCode(org.genemania.Constants.ERROR_CODES.APPLICATION_ERROR);
            ret.setErrorMessage(e.getMessage());
        }

        return ret;
    }

    /*
     * process upload network request
     */
    private UploadNetworkResponseMessage uploadNetwork(UploadNetworkRequestMessage requestMessage) {
        UploadNetworkResponseMessage ret = new UploadNetworkResponseMessage();

        try {
            UploadNetworkEngineRequestDto requestDto = BrokerUtils.msg2dto(requestMessage);
            UploadNetworkEngineResponseDto responseDto = engine.uploadNetwork(requestDto);
            LOG.debug(responseDto.toString());
            ret = BrokerUtils.dto2msg(responseDto);
        }
        catch (ApplicationException e) {
            LOG.error("Failed to load network", e);
            ret.setErrorCode(org.genemania.Constants.ERROR_CODES.APPLICATION_ERROR);
            ret.setErrorMessage(e.getMessage());
        }

        return ret;
    }

    private void printConverterReturn(RelatedGenesResponseMessage msg) {
        StringBuffer buf1 = new StringBuffer("dto2msg returned " + msg.getNetworks().size() + " networks=[");
        Iterator<NetworkDto> it1 = msg.getNetworks().iterator();
        while (it1.hasNext()) {
            buf1.append(it1.next().getId());
            if (it1.hasNext()) {
                buf1.append(" ");
            }
        }
        buf1.append("]");
        Map<Long, Collection<OntologyCategoryDto>> annotations = msg.getAnnotations();
        buf1.append(" and " + annotations.size() + " annotations.");
        LOG.debug(buf1);
    }

    private void printEngineReturn(RelatedGenesEngineResponseDto responseDto) {
        StringBuffer buf = new StringBuffer("engine returned " + responseDto.getNetworks().size() + " networks=[");
        Iterator<NetworkDto> it = responseDto.getNetworks().iterator();
        while (it.hasNext()) {
            buf.append(it.next().getId());
            if (it.hasNext()) {
                buf.append(" ");
            }
        }
        buf.append("]");
        if (responseDto.getAttributes() != null) {
            buf.append(" and " + responseDto.getAttributes().size() + " attributes");
        } else {
            buf.append(" and 0 attributes");
        }
        LOG.debug(buf);
    }

    /*
     * error messages when app protocol is broken (shouldn't happen in production)
     */
    private String buildErrorMessage(String errMsg) {
        // should have a generic response message type, reuse related genes
        // response for now
        RelatedGenesResponseMessage response = new RelatedGenesResponseMessage();
        response.setErrorCode(org.genemania.Constants.ERROR_CODES.APPLICATION_ERROR);
        response.setErrorMessage(errMsg);
        return response.toXml();
    }

    public synchronized boolean isActive() {
        return active;
    }

    public synchronized void setActive(boolean active) {
        this.active = active;
    }

    // TransportListener events. intended for monitoring only,
    // don't *do* anything here
    @Override
    public void onCommand(Object o) {
       LOG.trace("Transport event 'Command': " + o) ;
    }

    @Override
    public void onException(IOException e) {
        LOG.warn("Transport event 'Exception'", e);
    }

    @Override
    public void transportInterupted() {
        LOG.info("transport event 'Interrupted'");
    }

    @Override
    public void transportResumed() {
        LOG.info("transport event 'Resumed'");
    }
}

