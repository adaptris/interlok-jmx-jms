package com.adaptris.jmx.remote.jms;

import static org.apache.commons.lang3.StringUtils.isEmpty;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.remoting.JmsInvokerServiceExporter;
import org.springframework.jms.support.JmsUtils;
import org.springframework.remoting.support.RemoteInvocationResult;

/**
 * Overrides {@code JmsInvokerServiceExporter.writeRemoteInvocationResult} so we can derive the replyTo from a StringProperty.
 */
public abstract class ExtendedJmsInvokerServiceExporter<S extends Destination> extends JmsInvokerServiceExporter {
  public static final String FALLBACK_REPLY_TO_KEY = "JmsInvokerReplyTo";

  protected transient final Logger log = LoggerFactory.getLogger("com.adaptris.jmx.remote.jms.JmsInvoker");

  @Override
  protected void writeRemoteInvocationResult(Message requestMessage, Session session, RemoteInvocationResult result) throws JMSException {

    Message response = createResponseMessage(requestMessage, session, result);
    MessageProducer producer = session.createProducer(getReplyTo(requestMessage, session));
    try {
      producer.send(response);
    } finally {
      JmsUtils.closeMessageProducer(producer);
    }
  }

  protected Destination getReplyTo(Message request, Session session) throws JMSException {
    Destination result = request.getJMSReplyTo();
    if (result == null) {
      log.trace("Null Reply To, attempting to use fallback key [{}]", FALLBACK_REPLY_TO_KEY);
      String name = request.getStringProperty(FALLBACK_REPLY_TO_KEY);
      if (!isEmpty(name)) {
        log.trace("Found fallback value [{}]", name);
        result = createReplyTo(name, session);
      }
    }
    return result;
  }

  protected abstract S createReplyTo(String name, Session session) throws JMSException;

}
