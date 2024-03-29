package com.adaptris.jmx.remote.jms;

import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TemporaryQueue;
import javax.jms.TemporaryTopic;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.adaptris.jmx.remote.EmbeddedActiveMq;

public class JmsHelperTest {
  protected EmbeddedActiveMq broker;

  @BeforeEach
  public void setUp() throws Exception {
    broker = new EmbeddedActiveMq();
    broker.start();
  }

  @AfterEach
  public void tearDown() throws Exception {
    broker.destroy();
  }

  @Test
  public void testCloseConnection() throws Exception {
    ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(broker.getBrokerUrl());
    ActiveMQConnection connection = (ActiveMQConnection) factory.createConnection();
    JmsHelper.closeQuietly(connection);
    JmsHelper.closeQuietly((ActiveMQConnection) null);
  }

  @Test
  public void testCloseConnection_StopFirst() throws Exception {
    ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(broker.getBrokerUrl());
    ActiveMQConnection connection = (ActiveMQConnection) factory.createConnection();
    JmsHelper.closeQuietly(connection, true);
    JmsHelper.closeQuietly((ActiveMQConnection) null, true);

  }

  @Test
  public void testCloseSession() throws Exception {
    ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(broker.getBrokerUrl());
    ActiveMQConnection connection = (ActiveMQConnection) factory.createConnection();
    Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    JmsHelper.closeQuietly(session);
    JmsHelper.closeQuietly((Session) null);
    JmsHelper.closeQuietly(connection);
  }

  @Test
  public void testCloseMessageProducer() throws Exception {
    ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(broker.getBrokerUrl());
    ActiveMQConnection connection = (ActiveMQConnection) factory.createConnection();
    Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    TemporaryTopic t = session.createTemporaryTopic();
    MessageProducer producer = session.createProducer(t);
    JmsHelper.closeQuietly(producer);
    JmsHelper.closeQuietly((MessageProducer) null);
    JmsHelper.deleteQuietly(t);
    JmsHelper.closeQuietly(connection);
  }

  @Test
  public void testCloseMessageConsumer() throws Exception {
    ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(broker.getBrokerUrl());
    ActiveMQConnection connection = (ActiveMQConnection) factory.createConnection();
    Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    TemporaryTopic t = session.createTemporaryTopic();
    MessageConsumer consumer = session.createConsumer(t);
    JmsHelper.closeQuietly(consumer);
    JmsHelper.closeQuietly((MessageConsumer) null);
    JmsHelper.deleteQuietly(t);
    JmsHelper.closeQuietly(connection);
  }

  @Test
  public void testDeleteQuietly() throws Exception {
    ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(broker.getBrokerUrl());
    ActiveMQConnection connection = (ActiveMQConnection) factory.createConnection();
    Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    JmsHelper.deleteQuietly(session.createTemporaryQueue());
    JmsHelper.deleteQuietly(session.createTemporaryTopic());
    JmsHelper.deleteQuietly((TemporaryQueue) null);
    JmsHelper.deleteQuietly((TemporaryTopic) null);
  }

}
