package com.adaptris.jmx.remote.jms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.Collections;

import javax.management.ListenerNotFoundException;
import javax.management.Notification;
import javax.management.remote.JMXConnectionNotification;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXServiceURL;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import com.adaptris.jmx.remote.SimpleNotificationListener;

public class JmsJmxConnectorClientTest extends ActiveMqBaseCase {

  @Test
  public void testConnect() throws Exception {
    JMXConnectorServer jmxServer = null;
    JMXConnector jmxClient = null;
    JMXServiceURL jmxServiceUrl = new JMXServiceURL(JMX_URL_PREFIX + broker.getBrokerUrl() + JMX_URL_SUFFIX_QUEUE);
    createObjectName("JmxJms:name=" + getName());
    try {
      jmxServer = createAndStart(jmxServiceUrl);
      jmxClient = JMXConnectorFactory.newJMXConnector(jmxServiceUrl, createEnvironment());
      jmxClient.connect();
      assertNotNull(jmxClient.getConnectionId());
      String connectionId = jmxClient.getConnectionId();
      assertTrue(connectionId.startsWith("activemq://" + broker.getBrokerUrl()));
    } finally {
      closeQuietly(jmxServer);
      IOUtils.closeQuietly(jmxClient);
    }
  }

  @Test
  public void testConnect_WithMap() throws Exception {
    JMXConnectorServer jmxServer = null;
    JMXConnector jmxClient = null;
    JMXServiceURL jmxServiceUrl = new JMXServiceURL(JMX_URL_PREFIX + broker.getBrokerUrl() + JMX_URL_SUFFIX_QUEUE);
    createObjectName("JmxJms:name=" + getName());
    try {
      jmxServer = createAndStart(jmxServiceUrl);
      jmxClient = JMXConnectorFactory.newJMXConnector(jmxServiceUrl, createEnvironment());
      jmxClient.connect(Collections.emptyMap());
      assertNotNull(jmxClient.getConnectionId());
      assertTrue(jmxClient.getConnectionId().startsWith("activemq://" + broker.getBrokerUrl()));
    } finally {
      closeQuietly(jmxServer);
      IOUtils.closeQuietly(jmxClient);
    }
  }

  @Test
  public void testConnect_AlreadyConnected() throws Exception {
    JMXConnectorServer jmxServer = null;
    JMXConnector jmxClient = null;
    JMXServiceURL jmxServiceUrl = new JMXServiceURL(JMX_URL_PREFIX + broker.getBrokerUrl() + JMX_URL_SUFFIX_QUEUE);
    createObjectName("JmxJms:name=" + getName());
    try {
      jmxServer = createAndStart(jmxServiceUrl);
      jmxClient = JMXConnectorFactory.newJMXConnector(jmxServiceUrl, createEnvironment());
      SimpleNotificationListener listener = new SimpleNotificationListener();
      jmxClient.addConnectionNotificationListener(listener, null, null);
      jmxClient.connect();
      jmxClient.connect(); // No problems
      // Only 1 notification though; as we won't connect again.
      assertEquals(1, listener.getNotifications().size());
    } finally {
      closeQuietly(jmxServer);
      IOUtils.closeQuietly(jmxClient);
    }
  }

  @Test
  public void testConnect_AlreadyClosed() throws Exception {
    JMXConnectorServer jmxServer = null;
    JMXConnector jmxClient = null;
    JMXServiceURL jmxServiceUrl = new JMXServiceURL(JMX_URL_PREFIX + broker.getBrokerUrl() + JMX_URL_SUFFIX_QUEUE);
    createObjectName("JmxJms:name=" + getName());
    try {
      jmxServer = createAndStart(jmxServiceUrl);
      jmxClient = JMXConnectorFactory.newJMXConnector(jmxServiceUrl, createEnvironment());
      SimpleNotificationListener listener = new SimpleNotificationListener();
      jmxClient.addConnectionNotificationListener(listener, null, null);
      jmxClient.connect();
      jmxClient.close();
      assertEquals(2, listener.getNotifications().size());
      try {
        jmxClient.connect(); // This is apparently mean to throw an IOException according to the javadocs.
        fail();
      } catch (IOException expected) {
        assertEquals("connect() impossible after close", expected.getMessage());
      }
      assertEquals(2, listener.getNotifications().size());
    } finally {
      closeQuietly(jmxServer);
      IOUtils.closeQuietly(jmxClient);
    }
  }

  @Test
  public void testClose_AlreadyClosed() throws Exception {
    JMXConnectorServer jmxServer = null;
    JMXConnector jmxClient = null;
    JMXServiceURL jmxServiceUrl = new JMXServiceURL(JMX_URL_PREFIX + broker.getBrokerUrl() + JMX_URL_SUFFIX_QUEUE);
    createObjectName("JmxJms:name=" + getName());
    try {
      jmxServer = createAndStart(jmxServiceUrl);
      jmxClient = JMXConnectorFactory.newJMXConnector(jmxServiceUrl, createEnvironment());
      SimpleNotificationListener listener = new SimpleNotificationListener();
      jmxClient.addConnectionNotificationListener(listener, null, null);
      jmxClient.connect();
      jmxClient.close();
      jmxClient.close();
      assertEquals(2, listener.getNotifications().size());
    } finally {
      closeQuietly(jmxServer);
      IOUtils.closeQuietly(jmxClient);
    }
  }

  @Test
  public void testGetMBeanServerConnection() throws Exception {
    JMXConnectorServer jmxServer = null;
    JMXConnector jmxClient = null;
    JMXServiceURL jmxServiceUrl = new JMXServiceURL(JMX_URL_PREFIX + broker.getBrokerUrl() + JMX_URL_SUFFIX_QUEUE);
    createObjectName("JmxJms:name=" + getName());
    try {
      jmxServer = createAndStart(jmxServiceUrl);
      jmxClient = JMXConnectorFactory.newJMXConnector(jmxServiceUrl, createEnvironment());
      jmxClient.connect();
      assertNotNull(jmxClient.getMBeanServerConnection());
      try {
        jmxClient.getMBeanServerConnection(null);
        fail();
      } catch (UnsupportedOperationException expected) {

      }
    } finally {
      closeQuietly(jmxServer);
      IOUtils.closeQuietly(jmxClient);
    }
  }

  @Test
  public void testNotificationListener_GetsNotifications() throws Exception {
    JMXConnectorServer jmxServer = null;
    JMXConnector jmxClient = null;
    JMXServiceURL jmxServiceUrl = new JMXServiceURL(JMX_URL_PREFIX + broker.getBrokerUrl() + JMX_URL_SUFFIX_QUEUE);
    createObjectName("JmxJms:name=" + getName());
    try {
      jmxServer = createAndStart(jmxServiceUrl);
      jmxClient = JMXConnectorFactory.newJMXConnector(jmxServiceUrl, createEnvironment());
      SimpleNotificationListener listener = new SimpleNotificationListener();
      jmxClient.addConnectionNotificationListener(listener, null, null);
      jmxClient.connect();
      jmxClient.close();
      assertEquals(2, listener.getNotifications().size());
      Notification open = listener.getNotifications().get(0);
      assertEquals("Connection Opened", open.getMessage());
      assertEquals(1L, open.getSequenceNumber());
      assertEquals(JMXConnectionNotification.OPENED, open.getType());
      Notification close = listener.getNotifications().get(1);
      assertEquals("Connection Closed", close.getMessage());
      assertEquals(2L, close.getSequenceNumber());
      assertEquals(JMXConnectionNotification.CLOSED, close.getType());
    } finally {
      closeQuietly(jmxServer);
      IOUtils.closeQuietly(jmxClient);
    }
  }

  @Test
  public void testNotificationListener_AddRemove() throws Exception {
    JMXConnector jmxClient = null;
    JMXServiceURL jmxServiceUrl = new JMXServiceURL(JMX_URL_PREFIX + broker.getBrokerUrl() + JMX_URL_SUFFIX_QUEUE);
    createObjectName("JmxJms:name=" + getName());
    try {
      jmxClient = JMXConnectorFactory.newJMXConnector(jmxServiceUrl, createEnvironment());
      SimpleNotificationListener listener = new SimpleNotificationListener();
      jmxClient.addConnectionNotificationListener(listener, null, null);
      jmxClient.removeConnectionNotificationListener(listener);
      // Should now throw a listener not found Exception
      try {
        jmxClient.removeConnectionNotificationListener(listener, null, null);
        fail("Successfully removed a listener, when it doesn't exist");
      } catch (ListenerNotFoundException expected) {

      }
    } finally {
      IOUtils.closeQuietly(jmxClient);
    }
  }

}
