package com.adaptris.jmx.remote.provider.rabbitmq;

import jakarta.jms.JMSException;
import jakarta.jms.Session;
import jakarta.jms.Topic;

import com.adaptris.jmx.remote.jms.ExtendedJmsInvokerServiceExporter;
import com.rabbitmq.jms.admin.RMQDestination;

public class TopicServiceExporter extends ExtendedJmsInvokerServiceExporter<Topic> {

  @Override
  protected Topic createReplyTo(String name, Session session) throws JMSException {
    // Always a temporary topic
    // Mark it as declared
    RMQDestination d = new RMQDestination(name, false, true);
    d.setDeclared(true);
    return d;
  }

}
