package com.adaptris.jmx.remote.provider.activemq;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

import javax.jms.JMSException;
import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.Notification;
import javax.management.ObjectName;
import javax.management.StandardEmitterMBean;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXServiceURL;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import com.adaptris.jmx.remote.SimpleManagementBean;
import com.adaptris.jmx.remote.SimpleManagementBeanMBean;
import com.adaptris.jmx.remote.SimpleNotificationBean;
import com.adaptris.jmx.remote.SimpleNotificationBeanMBean;
import com.adaptris.jmx.remote.SimpleNotificationListener;
import com.adaptris.jmx.remote.jms.ActiveMqBaseCase;
import com.adaptris.jmx.remote.jms.JmsJmxConnectionFactory;

public class ActiveMqProviderTest extends ActiveMqBaseCase {

  private static final String ACTIVEMQ_URL_EXTRA_SUFFIX = "wireFormat.maxInactivityDuration=0";

  @Test
  public void testMBean_UseQueues() throws Exception {
    JMXConnectorServer jmxServer = null;
    JMXConnector jmxClient = null;
    JMXServiceURL jmxServiceUrl = new JMXServiceURL(JMX_URL_PREFIX + broker.getBrokerUrl() + JMX_URL_SUFFIX_QUEUE);
    ObjectName objName = createObjectName("JmxJms:name=" + getName());
    try {
      SimpleManagementBean realBean = createAndRegisterBean(getName(), objName);
      jmxServer = createAndStart(jmxServiceUrl);
      jmxClient = createAndConnect(jmxServiceUrl);

      MBeanServerConnection serverConnection = jmxClient.getMBeanServerConnection();
      assertTrue(serverConnection.isRegistered(objName));
      SimpleManagementBeanMBean remoteBean = JMX.newMBeanProxy(serverConnection, objName, SimpleManagementBeanMBean.class);
      assertEquals(getName(), remoteBean.getName());
      assertEquals(0, remoteBean.getCurrentCount());
      remoteBean.incrementCount(10);
      assertEquals(10, realBean.getCurrentCount());
    } finally {
      closeQuietly(jmxServer);
      IOUtils.closeQuietly(jmxClient);
    }
  }

  @Test
  public void testMBean_UseTopics() throws Exception {
    JMXConnectorServer jmxServer = null;
    JMXConnector jmxClient = null;
    JMXServiceURL jmxServiceUrl = new JMXServiceURL(JMX_URL_PREFIX + broker.getBrokerUrl() + JMX_URL_SUFFIX_TOPIC);
    ObjectName objName = createObjectName("JmxJms:name=" + getName());
    try {
      SimpleManagementBean realBean = createAndRegisterBean(getName(), objName);
      jmxServer = createAndStart(jmxServiceUrl);
      jmxClient = createAndConnect(jmxServiceUrl);

      MBeanServerConnection serverConnection = jmxClient.getMBeanServerConnection();
      assertTrue(serverConnection.isRegistered(objName));
      SimpleManagementBeanMBean remoteBean = JMX.newMBeanProxy(serverConnection, objName, SimpleManagementBeanMBean.class);
      assertEquals(getName(), remoteBean.getName());
      assertEquals(0, remoteBean.getCurrentCount());
      remoteBean.incrementCount(10);
      assertEquals(10, realBean.getCurrentCount());
    } finally {
      closeQuietly(jmxServer);
      IOUtils.closeQuietly(jmxClient);
    }
  }

  @Test
  public void testFactory_AdditionalQueryOnURL_Preserved() throws Exception {
    JMXServiceURL jmxServiceUrl = new JMXServiceURL(
        JMX_URL_PREFIX + broker.getBrokerUrl() + JMX_URL_SUFFIX_QUEUE + "&" + ACTIVEMQ_URL_EXTRA_SUFFIX);
    try {
      ActiveMqJmsConnectionFactory fact = new ActiveMqJmsConnectionFactory(new HashMap<String, Object>(), jmxServiceUrl);
      String url = fact.getBrokerURL();
      assertEquals(broker.getBrokerUrl() + "?" + ACTIVEMQ_URL_EXTRA_SUFFIX, url);
    } finally {
    }
  }

  @Test
  public void testFactory_CreateConnection() throws Exception {
    JMXServiceURL jmxServiceUrl = new JMXServiceURL(JMX_URL_PREFIX + broker.getBrokerUrl() + JMX_URL_SUFFIX_TOPIC);
    ActiveMqJmsConnectionFactory fact = new ActiveMqJmsConnectionFactory(new HashMap<String, Object>(), jmxServiceUrl);
    try {
      assertNotNull(fact.createConnection());
      assertNotNull(fact.createConnection("guest", "guest"));
    } finally {
      fact.destroy();
    }
  }

  @Test
  public void testFactory_CreateConnection_WaitForBroker() throws Exception {
    JMXServiceURL jmxServiceUrl = new JMXServiceURL(
        JMX_URL_PREFIX + broker.getBrokerUrl() + JMX_URL_SUFFIX_TOPIC + "&" + JmsJmxConnectionFactory.ATTR_RETRY_INTERVAL_MS + "=100");
    final long maxWait = 3000L;
    final ActiveMqJmsConnectionFactory fact = new ActiveMqJmsConnectionFactory(new HashMap<String, Object>(), jmxServiceUrl);
    try {
      broker.stop();
      log.info(getName() + " Broker Stopped");
      // Create a cyclic barrier to wait for connection operation to finish
      final CyclicBarrier gate = new CyclicBarrier(2);
      Thread initThread = new Thread(() -> {
        try {
          assertNotNull(fact.createConnection());
        } catch (JMSException e) {

        }
        try {
          gate.await(maxWait, TimeUnit.MILLISECONDS);
        } catch (Exception gateException) {
        }
      });
      initThread.start();
      TimeUnit.SECONDS.sleep(1L);
      log.info(getName() + " Broker Restarted");
      broker.start();
      gate.await(maxWait, TimeUnit.MILLISECONDS);
    } finally {
      fact.destroy();
    }
  }

  @Test
  public void testMBeans_NotificationRecoversFromException() throws Exception {
    JMXConnectorServer jmxServer = null;
    JMXServiceURL jmxServiceUrl = new JMXServiceURL(JMX_URL_PREFIX + broker.getBrokerUrl() + JMX_URL_SUFFIX_QUEUE);
    ObjectName objName = createObjectName("JmxJms:name=" + getName());
    JMXConnector jmxClient = null;

    try {
      Map<String, Object> env = createEnvironment();
      env.put(JmsJmxConnectionFactory.ATTR_RETRY_INTERVAL_MS, "100");
      jmxServer = createAndStart(jmxServiceUrl, env);
      jmxClient = createAndConnect(jmxServiceUrl, env);
      SimpleNotificationBean simpleBean = new SimpleNotificationBean(getName());
      StandardEmitterMBean emitter = new StandardEmitterMBean(simpleBean, SimpleNotificationBeanMBean.class, simpleBean);
      mbeanServer.registerMBean(emitter, objName);

      MBeanServerConnection serverConnection = jmxClient.getMBeanServerConnection();
      SimpleNotificationListener listener = new SimpleNotificationListener();
      serverConnection.addNotificationListener(objName, listener, null, null);
      assertEquals(1, simpleBean.getListeners().size());
      Notification n1 = new Notification(getName(), objName, 1, "Here's a notification");
      emitter.sendNotification(n1);
      listener.waitForMessages(1);
      assertEquals(1, listener.getNotifications().size());

      // Now Kill the broker to simulate an exception.
      broker.stop();
      log.info(getName() + " Broker Stopped");
      TimeUnit.SECONDS.sleep(5L);
      broker.start();
      log.info(getName() + " Broker Restarted");
      Notification n2 = new Notification(getName(), objName, 2, "Here's another notification");
      TimeUnit.SECONDS.sleep(5L);
      emitter.sendNotification(n2);
      listener.waitForMessages(2);
      assertEquals(2, listener.getNotifications().size());
    } finally {
      IOUtils.closeQuietly(jmxClient);
      closeQuietly(jmxServer);
    }
  }

}
