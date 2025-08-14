package com.adaptris.jmx.remote.jms;

import jakarta.jms.JMSException;
import jakarta.jms.Queue;
import jakarta.jms.Session;

class QueueServiceExporter extends ExtendedJmsInvokerServiceExporter<Queue> {

  @Override
  protected Queue createReplyTo(String name, Session session) throws JMSException {
    return session.createQueue(name);
  }

}
