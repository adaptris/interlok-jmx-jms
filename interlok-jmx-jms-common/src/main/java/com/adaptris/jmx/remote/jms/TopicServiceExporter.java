package com.adaptris.jmx.remote.jms;

import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.Topic;

class TopicServiceExporter extends ExtendedJmsInvokerServiceExporter<Topic> {

  @Override
  protected Topic createReplyTo(String name, Session session) throws JMSException {
    return session.createTopic(name);
  }

}
