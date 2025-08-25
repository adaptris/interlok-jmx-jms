package com.adaptris.jmx.remote.jms;

import jakarta.jms.JMSException;
import jakarta.jms.Session;
import jakarta.jms.Topic;

class TopicServiceExporter extends ExtendedJmsInvokerServiceExporter<Topic> {

  @Override
  protected Topic createReplyTo(String name, Session session) throws JMSException {
    return session.createTopic(name);
  }

}
