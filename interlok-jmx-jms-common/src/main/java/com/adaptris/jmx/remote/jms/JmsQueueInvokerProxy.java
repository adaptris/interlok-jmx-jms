package com.adaptris.jmx.remote.jms;

import static com.adaptris.jmx.remote.jms.JmsHelper.deleteQuietly;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TemporaryQueue;

/**
 * Concrete implementation of JmsInvokerProxy that uses {@link javax.jms.Queue} and {@link TemporaryQueue}.
 */
final class JmsQueueInvokerProxy extends JmsInvokerProxy<TemporaryQueue> {

  public JmsQueueInvokerProxy(JmsJmxConnectionFactory cf, Destination d, Class serviceInterface) {
    super(cf, d, serviceInterface);
  }

  @Override
  protected TemporaryQueue createTemporaryDestination(Session s) throws JMSException {
    return s.createTemporaryQueue();
  }

  @Override
  protected void deleteTemporaryDestination(TemporaryQueue tmpDest) throws JMSException {
    deleteQuietly(tmpDest);
  }

  @Override
  protected void addReplyTo(Message requestMessage, TemporaryQueue tmpDest) throws JMSException {
    requestMessage.setJMSReplyTo(tmpDest);
    requestMessage.setStringProperty(ExtendedJmsInvokerServiceExporter.FALLBACK_REPLY_TO_KEY, tmpDest.getQueueName());
  }
}