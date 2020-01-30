package com.adaptris.jmx.remote.jms;

import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TemporaryQueue;
import javax.jms.TemporaryTopic;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public class JmsHelperTest {
  protected EmbeddedActiveMq broker;

  public JmsHelperTest() {
  }

  @BeforeClass
  public void setUp() throws Exception {
    broker = new EmbeddedActiveMq();
    broker.start();
  }

  @AfterClass
  public void tearDown() throws Exception {
    broker.destroy();
  }

  public void testCloseConnection() throws Exception {
    ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(broker.getBrokerUrl());
    ActiveMQConnection connection = (ActiveMQConnection) factory.createConnection();
    JmsHelper.closeQuietly(connection);
    JmsHelper.closeQuietly((ActiveMQConnection) null);
  }

  public void testCloseConnection_StopFirst() throws Exception {
    ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(broker.getBrokerUrl());
    ActiveMQConnection connection = (ActiveMQConnection) factory.createConnection();
    JmsHelper.closeQuietly(connection, true);
    JmsHelper.closeQuietly((ActiveMQConnection) null, true);

  }

  public void testCloseSession() throws Exception {
    ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(broker.getBrokerUrl());
    ActiveMQConnection connection = (ActiveMQConnection) factory.createConnection();
    Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    JmsHelper.closeQuietly(session);
    JmsHelper.closeQuietly((Session) null);
    JmsHelper.closeQuietly(connection);
  }

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
