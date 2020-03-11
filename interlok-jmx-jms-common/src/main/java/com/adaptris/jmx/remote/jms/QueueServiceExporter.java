package com.adaptris.jmx.remote.jms;

import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.Session;

class QueueServiceExporter extends ExtendedJmsInvokerServiceExporter<Queue> {

  @Override
  protected Queue createReplyTo(String name, Session session) throws JMSException {
    return session.createQueue(name);
  }
}
