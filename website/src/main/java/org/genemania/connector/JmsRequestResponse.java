package org.genemania.connector;

import org.apache.log4j.Logger;
import org.springframework.jms.core.SessionCallback;
import org.springframework.jms.support.JmsUtils;
import org.springframework.jms.support.destination.DestinationResolver;

import javax.jms.*;
import java.util.UUID;

/*
 * generic synchronous request/response with plain string messages, based on
 *
 *   http://codedependents.com/2010/03/04/synchronous-request-response-with-activemq-and-spring/
 *
 * returns null if specified timeout is exceeded.
 */
public class JmsRequestResponse implements SessionCallback<String> {
    private static final Logger LOG = Logger.getLogger(JmsRequestResponse.class);

    private final String msg;
    private final String msgType;
    private final DestinationResolver destinationResolver;
    private final String requestQueueName;
    private final String responseQueueName;
    private final long timeoutMillis;
    private final long messageExpirationMillis;

    public JmsRequestResponse(final String msg, String msgType, String requestQueueName,
                              String responseQueueName, final DestinationResolver destinationResolver,
                              long timeoutMillis, long messageExpirationMillis) {
        this.msg = msg;
        this.msgType = msgType;
        this.requestQueueName = requestQueueName;
        this.responseQueueName = responseQueueName;
        this.destinationResolver = destinationResolver;
        this.timeoutMillis = timeoutMillis;
        this.messageExpirationMillis = messageExpirationMillis;
    }

    public String doInJms(final Session session) throws JMSException {
        MessageConsumer consumer = null;
        MessageProducer producer = null;
        final String correlationId = UUID.randomUUID().toString();
        LOG.debug("preparing jms request with correlationId: " + correlationId);

        try {
            final Destination requestQueue =
                    destinationResolver.resolveDestinationName(session, requestQueueName, false);
            final Destination replyQueue =
                    destinationResolver.resolveDestinationName(session, responseQueueName, false);

            // setup to receive reply by selecting the correlation id
            consumer = session.createConsumer(replyQueue, "JMSCorrelationID = '" + correlationId + "'");

            // construct & send request
            final TextMessage textMessage = session.createTextMessage(msg);
            textMessage.setJMSCorrelationID(correlationId);
            textMessage.setJMSReplyTo(replyQueue);
            textMessage.setJMSType(msgType);
            textMessage.setJMSDeliveryMode(DeliveryMode.PERSISTENT);

            LOG.debug("sending request message for " + correlationId);
            producer = session.createProducer(requestQueue);
            producer.setTimeToLive(messageExpirationMillis);
            producer.send(requestQueue, textMessage);

            // block on reply for specified timeout
            LOG.debug("waiting for response message for " + correlationId);
            TextMessage response = (TextMessage) consumer.receive(timeoutMillis);

            if (response != null) {
                return response.getText();
            }
            else { // we timed out
                return null;
            }

        }
        finally {
            LOG.debug("completed jms request with correlationId: " + correlationId);
            JmsUtils.closeMessageConsumer(consumer);
            JmsUtils.closeMessageProducer(producer);
        }
    }
}
