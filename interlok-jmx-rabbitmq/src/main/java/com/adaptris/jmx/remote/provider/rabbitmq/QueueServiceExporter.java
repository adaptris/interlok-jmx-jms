package com.adaptris.jmx.remote.provider.rabbitmq;

import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.Session;

import com.adaptris.jmx.remote.jms.ExtendedJmsInvokerServiceExporter;
import com.rabbitmq.jms.admin.RMQDestination;

class QueueServiceExporter extends ExtendedJmsInvokerServiceExporter<Queue> {

  @Override
  protected Queue createReplyTo(String name, Session session) throws JMSException {
    // Always a temporary queue.
    // Mark the resources as delcared
    RMQDestination d = new RMQDestination(name, true, true);
    d.setDeclared(true);
    return d;
  }

}
