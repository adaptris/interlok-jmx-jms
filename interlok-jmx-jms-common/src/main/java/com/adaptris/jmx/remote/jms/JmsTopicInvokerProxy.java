package com.adaptris.jmx.remote.jms;

import static com.adaptris.jmx.remote.jms.JmsHelper.deleteQuietly;

import jakarta.jms.Destination;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.Session;
import jakarta.jms.TemporaryTopic;

/**
 * Concrete implementation of JmsInvokerProxy that uses {@link jakarta.jms.Topic} and {@link TemporaryTopic}.
 *
 */
final class JmsTopicInvokerProxy extends JmsInvokerProxy<TemporaryTopic> {

  public JmsTopicInvokerProxy(JmsJmxConnectionFactory cf, Destination d, Class<?> serviceInterface) {
    super(cf, d, serviceInterface);
  }

  @Override
  protected TemporaryTopic createTemporaryDestination(Session s) throws JMSException {
    return s.createTemporaryTopic();
  }

  @Override
  protected void deleteTemporaryDestination(TemporaryTopic tmpDest) throws JMSException {
    deleteQuietly(tmpDest);
  }

  @Override
  protected void addReplyTo(Message requestMessage, TemporaryTopic tmpDest) throws JMSException {
    requestMessage.setJMSReplyTo(tmpDest);
    requestMessage.setStringProperty(ExtendedJmsInvokerServiceExporter.FALLBACK_REPLY_TO_KEY, tmpDest.getTopicName());
  }

}