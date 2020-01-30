package com.adaptris.jmx.remote.jms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.Notification;
import javax.management.ObjectName;
import javax.management.StandardEmitterMBean;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import com.adaptris.jmx.remote.SimpleManagementBean;
import com.adaptris.jmx.remote.SimpleManagementBeanMBean;
import com.adaptris.jmx.remote.SimpleNotificationBean;
import com.adaptris.jmx.remote.SimpleNotificationBeanMBean;
import com.adaptris.jmx.remote.SimpleNotificationListener;

@SuppressWarnings("deprecation")
public class JmsJmxConnectorServerTest extends ActiveMqBaseCase {

  public JmsJmxConnectorServerTest() {
  }

  @Test
  public void testGetJMXServiceURL() throws Exception {
    JMXConnectorServer jmxServer = null;
    JMXServiceURL jmxServiceUrl = new JMXServiceURL(JMX_URL_PREFIX + broker.getBrokerUrl() + JMX_URL_SUFFIX_TOPIC);
    try {
      jmxServer = createAndRegister(jmxServiceUrl);
      assertEquals(jmxServiceUrl, jmxServer.getAddress());
    }
    finally {
      closeQuietly(jmxServer);
    }
  }

  @Test
  public void testGetAttributes() throws Exception {
    JMXConnectorServer jmxServer = null;
    JMXServiceURL jmxServiceUrl = new JMXServiceURL(JMX_URL_PREFIX + broker.getBrokerUrl() + JMX_URL_SUFFIX_TOPIC);
    try {
      jmxServer = createAndRegister(jmxServiceUrl);
      Map attr = jmxServer.getAttributes();
      assertEquals("com.adaptris.jmx.remote.provider", attr.get(JMXConnectorServerFactory.PROTOCOL_PROVIDER_PACKAGES).toString());
    }
    finally {
      closeQuietly(jmxServer);
    }
  }

  @Test
  public void testStart() throws Exception {
    JMXConnectorServer jmxServer = null;
    JMXServiceURL jmxServiceUrl = new JMXServiceURL(JMX_URL_PREFIX + broker.getBrokerUrl() + JMX_URL_SUFFIX_TOPIC);
    try {
      jmxServer = createAndRegister(jmxServiceUrl);
      jmxServer.start();
      assertTrue(jmxServer.isActive());
    }
    finally {
      closeQuietly(jmxServer);
    }
  }

  @Test
  public void testStart_AlreadyStarted() throws Exception {
    JMXConnectorServer jmxServer = null;
    JMXServiceURL jmxServiceUrl = new JMXServiceURL(JMX_URL_PREFIX + broker.getBrokerUrl() + JMX_URL_SUFFIX_TOPIC);
    try {
      jmxServer = createAndRegister(jmxServiceUrl);
      jmxServer.start();
      jmxServer.start();
      assertTrue(jmxServer.isActive());
    }
    finally {
      closeQuietly(jmxServer);
    }
  }

  @Test
  public void testStart_AlreadyStopped() throws Exception {
    JMXConnectorServer jmxServer = null;
    JMXServiceURL jmxServiceUrl = new JMXServiceURL(JMX_URL_PREFIX + broker.getBrokerUrl() + JMX_URL_SUFFIX_TOPIC);
    try {
      jmxServer = createAndStart(jmxServiceUrl);
      jmxServer.stop();
      try {
        jmxServer.start();
        fail();
      }
      catch (IOException expected) {
        assertEquals("start() after close() is not valid", expected.getMessage());
      }
      assertFalse(jmxServer.isActive());

    }
    finally {
      closeQuietly(jmxServer);
    }
  }

  @Test
  public void testStop_AlreadyStopped() throws Exception {
    JMXConnectorServer jmxServer = null;
    JMXServiceURL jmxServiceUrl = new JMXServiceURL(JMX_URL_PREFIX + broker.getBrokerUrl() + JMX_URL_SUFFIX_TOPIC);
    try {
      jmxServer = createAndStart(jmxServiceUrl);
      jmxServer.stop();
      jmxServer.stop();
      assertFalse(jmxServer.isActive());
    }
    finally {
      closeQuietly(jmxServer);
    }
  }

  @Test
  public void testNotificationListener_AddRemove_Topics() throws Exception {
    JMXConnectorServer jmxServer = null;
    JMXServiceURL jmxServiceUrl = new JMXServiceURL(JMX_URL_PREFIX + broker.getBrokerUrl() + JMX_URL_SUFFIX_TOPIC);
    ObjectName objName1 = createObjectName("JmxJms:name=" + getName() + "1");
    ObjectName objName2 = createObjectName("JmxJms:name=" + getName() + "2");
    JMXConnector jmxClient = null;

    try {
      jmxServer = createAndStart(jmxServiceUrl);
      jmxClient = createAndConnect(jmxServiceUrl);
      MBeanServerConnection serverConnection = jmxClient.getMBeanServerConnection();
      SimpleNotificationListener listener1 = new SimpleNotificationListener();
      SimpleNotificationListener listener2 = new SimpleNotificationListener();
      SimpleNotificationListener listener3 = new SimpleNotificationListener();
      SimpleNotificationListener listener4 = new SimpleNotificationListener();
      mbeanServer.registerMBean(listener3, objName1);
      mbeanServer.registerMBean(listener4, objName2);
      serverConnection.addNotificationListener(connectorServerObjectName, listener1, null, null);
      serverConnection.addNotificationListener(connectorServerObjectName, listener2, null, null);
      serverConnection.addNotificationListener(connectorServerObjectName, objName1, null, null);
      serverConnection.addNotificationListener(connectorServerObjectName, objName2, null, null);
      serverConnection.removeNotificationListener(connectorServerObjectName, listener1);
      serverConnection.removeNotificationListener(connectorServerObjectName, listener2, null, null);
      serverConnection.removeNotificationListener(connectorServerObjectName, objName1);
      serverConnection.removeNotificationListener(connectorServerObjectName, objName2, null, null);
      try {
        serverConnection.removeNotificationListener(connectorServerObjectName, listener1, null, null);
        fail("Successfully removed a listener, when it doesn't exist");
      }
      catch (ListenerNotFoundException expected) {

      }
    }
    finally {
      closeQuietly(jmxServer);
      IOUtils.closeQuietly(jmxClient);
    }
  }

  @Test
  public void testNotificationListener_GetsNotifications() throws Exception {
    JMXConnectorServer jmxServer = null;
    JMXServiceURL jmxServiceUrl = new JMXServiceURL(JMX_URL_PREFIX + broker.getBrokerUrl() + JMX_URL_SUFFIX_QUEUE);
    ObjectName objName = createObjectName("JmxJms:name=" + getName());
    JMXConnector jmxClient = null;

    try {
      jmxServer = createAndStart(jmxServiceUrl);

      jmxClient = createAndConnect(jmxServiceUrl);
      SimpleNotificationBean simpleBean = new SimpleNotificationBean(getName());
      StandardEmitterMBean emitter = new StandardEmitterMBean(simpleBean, SimpleNotificationBeanMBean.class, simpleBean);
      mbeanServer.registerMBean(emitter, objName);

      MBeanServerConnection serverConnection = jmxClient.getMBeanServerConnection();
      SimpleNotificationListener listener = new SimpleNotificationListener();
      serverConnection.addNotificationListener(objName, listener, null, null);
      assertEquals(1, simpleBean.getListeners().size());
      Notification n = new Notification(getName(), objName, 1, "Here's a notification");
      emitter.sendNotification(n);
      listener.waitForMessages(1);
      assertEquals(1, listener.getNotifications().size());
      serverConnection.removeNotificationListener(objName, listener);
    }
    finally {
      IOUtils.closeQuietly(jmxClient);
      closeQuietly(jmxServer);

    }
  }

  @Test
  public void testNotificationListener_NotSerializable() throws Exception {
    JMXConnectorServer jmxServer = null;
    JMXServiceURL jmxServiceUrl = new JMXServiceURL(JMX_URL_PREFIX + broker.getBrokerUrl() + JMX_URL_SUFFIX_QUEUE);
    ObjectName objName = createObjectName("JmxJms:name=" + getName());
    JMXConnector jmxClient = null;

    try {
      jmxServer = createAndStart(jmxServiceUrl);

      jmxClient = createAndConnect(jmxServiceUrl);
      SimpleNotificationBean simpleBean = new SimpleNotificationBean(getName());
      StandardEmitterMBean emitter = new StandardEmitterMBean(simpleBean, SimpleNotificationBeanMBean.class, simpleBean);
      mbeanServer.registerMBean(emitter, objName);

      MBeanServerConnection serverConnection = jmxClient.getMBeanServerConnection();
      SimpleNotificationListener listener = new SimpleNotificationListener();
      serverConnection.addNotificationListener(objName, listener, null, null);
      assertEquals(1, simpleBean.getListeners().size());
      Notification n = new Notification(getName(), new Object(), 1, "Here's a notification");
      emitter.sendNotification(n);
      Thread.sleep(250);
      assertEquals(0, listener.getNotifications().size());
      serverConnection.removeNotificationListener(objName, listener);

    }
    finally {
      IOUtils.closeQuietly(jmxClient);
      closeQuietly(jmxServer);

    }
  }

  @Test
  public void testNotificationListener_AddRemove_Queues() throws Exception {
    JMXConnectorServer jmxServer = null;
    JMXServiceURL jmxServiceUrl = new JMXServiceURL(JMX_URL_PREFIX + broker.getBrokerUrl() + JMX_URL_SUFFIX_QUEUE);
    ObjectName objName = createObjectName("JmxJms:name=" + getName());
    JMXConnector jmxClient = null;

    try {
      jmxServer = createAndStart(jmxServiceUrl);

      jmxClient = createAndConnect(jmxServiceUrl);
      MBeanServerConnection serverConnection = jmxClient.getMBeanServerConnection();
      SimpleNotificationListener listener1 = new SimpleNotificationListener();
      SimpleNotificationListener listener2 = new SimpleNotificationListener();
      serverConnection.addNotificationListener(connectorServerObjectName, listener1, null, null);
      serverConnection.addNotificationListener(connectorServerObjectName, listener2, null, null);
      serverConnection.removeNotificationListener(connectorServerObjectName, listener1);
      serverConnection.removeNotificationListener(connectorServerObjectName, listener2, null, null);
      try {
        serverConnection.removeNotificationListener(connectorServerObjectName, listener1, null, null);
        fail("Successfully removed a listener, when it doesn't exist");
      }
      catch (ListenerNotFoundException expected) {

      }
    }
    finally {
      IOUtils.closeQuietly(jmxClient);
      closeQuietly(jmxServer);

    }
  }

  @Test
  public void testServerConnection_QueryNames() throws Exception {
    JMXConnectorServer jmxServer = null;
    JMXServiceURL jmxServiceUrl = new JMXServiceURL(JMX_URL_PREFIX + broker.getBrokerUrl() + JMX_URL_SUFFIX_QUEUE);
    JMXConnector jmxClient = null;

    try {
      jmxServer = createAndStart(jmxServiceUrl);

      jmxClient = createAndConnect(jmxServiceUrl);
      MBeanServerConnection serverConnection = jmxClient.getMBeanServerConnection();
      Set<ObjectName> names = serverConnection.queryNames(null, null);
      for (ObjectName name : names) {
        log.trace("Found [" + name + "]");
      }
    }
    finally {
      IOUtils.closeQuietly(jmxClient);
      closeQuietly(jmxServer);

    }
  }

  @Test
  public void testServerConnection_Unregister() throws Exception {
    JMXConnectorServer jmxServer = null;
    JMXServiceURL jmxServiceUrl = new JMXServiceURL(JMX_URL_PREFIX + broker.getBrokerUrl() + JMX_URL_SUFFIX_QUEUE);
    ObjectName objName = createObjectName("JmxJms:name=" + getName());
    JMXConnector jmxClient = null;

    try {
      jmxServer = createAndStart(jmxServiceUrl);
      jmxClient = createAndConnect(jmxServiceUrl);
      MBeanServerConnection serverConnection = jmxClient.getMBeanServerConnection();
      SimpleManagementBean bean = createAndRegisterBean(getName(), objName);
      serverConnection.unregisterMBean(objName);
    }
    finally {
      IOUtils.closeQuietly(jmxClient);
      closeQuietly(jmxServer);
    }
  }

  @Test
  public void testServerConnection_QueryMBeans() throws Exception {
    JMXConnectorServer jmxServer = null;
    JMXServiceURL jmxServiceUrl = new JMXServiceURL(JMX_URL_PREFIX + broker.getBrokerUrl() + JMX_URL_SUFFIX_QUEUE);
    JMXConnector jmxClient = null;

    try {
      jmxServer = createAndRegister(jmxServiceUrl);
      jmxServer.start();
      jmxClient = createAndConnect(jmxServiceUrl);
      MBeanServerConnection serverConnection = jmxClient.getMBeanServerConnection();
      assertNotNull(serverConnection.queryMBeans(null, null));
    }
    finally {
      IOUtils.closeQuietly(jmxClient);
      closeQuietly(jmxServer);
    }
  }

  @Test
  public void testServerConnection_CreateMBean() throws Exception {
    JMXConnectorServer jmxServer = null;
    JMXServiceURL jmxServiceUrl = new JMXServiceURL(JMX_URL_PREFIX + broker.getBrokerUrl() + JMX_URL_SUFFIX_QUEUE);
    ObjectName objName = createObjectName("JmxJms:name=" + getName());
    JMXConnector jmxClient = null;

    try {
      jmxServer = createAndRegister(jmxServiceUrl);
      jmxServer.start();
      jmxClient = createAndConnect(jmxServiceUrl);
      MBeanServerConnection serverConnection = jmxClient.getMBeanServerConnection();
      assertNotNull(serverConnection.createMBean(SimpleManagementBean.class.getCanonicalName(), objName));
      serverConnection.unregisterMBean(objName);
      // Don't work which means coverage is going to be SUXORS
      // assertNotNull(serverConnection.createMBean(SimpleManagementBean.class.getCanonicalName(), objName, null));
      // serverConnection.unregisterMBean(objName);
      assertNotNull(serverConnection.createMBean(SimpleManagementBean.class.getCanonicalName(), objName, null, null));
      serverConnection.unregisterMBean(objName);
      // Don't work which means coverage is going to be SUXORS
      // assertNotNull(serverConnection.createMBean(SimpleManagementBean.class.getCanonicalName(), objName, null, null, null));
      // serverConnection.unregisterMBean(objName);
    }
    finally {
      IOUtils.closeQuietly(jmxClient);
      closeQuietly(jmxServer);
    }
  }

  @Test
  public void testServerConnection_getObjectInstance() throws Exception {
    JMXConnectorServer jmxServer = null;
    JMXServiceURL jmxServiceUrl = new JMXServiceURL(JMX_URL_PREFIX + broker.getBrokerUrl() + JMX_URL_SUFFIX_QUEUE);
    ObjectName objName = createObjectName("JmxJms:name=" + getName());
    JMXConnector jmxClient = null;

    try {
      jmxServer = createAndRegister(jmxServiceUrl);
      jmxServer.start();
      jmxClient = createAndConnect(jmxServiceUrl);
      MBeanServerConnection serverConnection = jmxClient.getMBeanServerConnection();
      SimpleManagementBean bean = createAndRegisterBean(getName(), objName);
      assertNotNull(serverConnection.getObjectInstance(objName));
    }
    finally {
      IOUtils.closeQuietly(jmxClient);
      closeQuietly(jmxServer);
    }
  }

  @Test
  public void testServerConnection_getMBeanCount() throws Exception {
    JMXConnectorServer jmxServer = null;
    JMXServiceURL jmxServiceUrl = new JMXServiceURL(JMX_URL_PREFIX + broker.getBrokerUrl() + JMX_URL_SUFFIX_QUEUE);
    ObjectName objName = createObjectName("JmxJms:name=" + getName());
    JMXConnector jmxClient = null;

    try {
      jmxServer = createAndRegister(jmxServiceUrl);
      jmxServer.start();
      jmxClient = createAndConnect(jmxServiceUrl);
      MBeanServerConnection serverConnection = jmxClient.getMBeanServerConnection();
      SimpleManagementBean bean = createAndRegisterBean(getName(), objName);
      assertTrue(serverConnection.getMBeanCount() > 0);
    }
    finally {
      IOUtils.closeQuietly(jmxClient);
      closeQuietly(jmxServer);
    }
  }

  @Test
  public void testServerConnection_getAttribute() throws Exception {
    JMXConnectorServer jmxServer = null;
    JMXServiceURL jmxServiceUrl = new JMXServiceURL(JMX_URL_PREFIX + broker.getBrokerUrl() + JMX_URL_SUFFIX_QUEUE);
    ObjectName objName = createObjectName("JmxJms:name=" + getName());
    JMXConnector jmxClient = null;

    try {
      jmxServer = createAndRegister(jmxServiceUrl);
      jmxServer.start();
      jmxClient = createAndConnect(jmxServiceUrl);
      MBeanServerConnection serverConnection = jmxClient.getMBeanServerConnection();
      SimpleManagementBean bean = createAndRegisterBean(getName(), objName);
      try {
        serverConnection.getAttribute(objName, getName());
        fail();
      }
      catch (AttributeNotFoundException expected) {
        // Yeah, it's a delegate method so we don't actually care whether we can find the attribute
        // just that the method is in fact called.
      }
    }
    finally {
      IOUtils.closeQuietly(jmxClient);
      closeQuietly(jmxServer);
    }
  }

  @Test
  public void testServerConnection_getAttributes() throws Exception {
    JMXConnectorServer jmxServer = null;
    JMXServiceURL jmxServiceUrl = new JMXServiceURL(JMX_URL_PREFIX + broker.getBrokerUrl() + JMX_URL_SUFFIX_QUEUE);
    ObjectName objName = createObjectName("JmxJms:name=" + getName());
    JMXConnector jmxClient = null;

    try {
      jmxServer = createAndRegister(jmxServiceUrl);
      jmxServer.start();
      jmxClient = createAndConnect(jmxServiceUrl);
      MBeanServerConnection serverConnection = jmxClient.getMBeanServerConnection();
      SimpleManagementBean bean = createAndRegisterBean(getName(), objName);
      AttributeList list = serverConnection.getAttributes(objName, new String[]
      {
        getName()
      });
      assertEquals(0, list.size());
    }
    finally {
      IOUtils.closeQuietly(jmxClient);
      closeQuietly(jmxServer);
    }
  }

  @Test
  public void testServerConnection_setAttribute() throws Exception {
    JMXConnectorServer jmxServer = null;
    JMXServiceURL jmxServiceUrl = new JMXServiceURL(JMX_URL_PREFIX + broker.getBrokerUrl() + JMX_URL_SUFFIX_QUEUE);
    ObjectName objName = createObjectName("JmxJms:name=" + getName());
    JMXConnector jmxClient = null;

    try {
      jmxServer = createAndRegister(jmxServiceUrl);
      jmxServer.start();
      jmxClient = createAndConnect(jmxServiceUrl);
      MBeanServerConnection serverConnection = jmxClient.getMBeanServerConnection();
      SimpleManagementBean bean = createAndRegisterBean(getName(), objName);
      try {
        serverConnection.setAttribute(objName, new Attribute(getName(), getName()));
        fail();
      }
      catch (AttributeNotFoundException expected) {
        // Yeah, it's a delegate method so we don't actually care whether we can find the attribute
        // just that the method is in fact called.
      }
    }
    finally {
      IOUtils.closeQuietly(jmxClient);
      closeQuietly(jmxServer);
    }
  }

  @Test
  public void testServerConnection_setAttributes() throws Exception {
    JMXConnectorServer jmxServer = null;
    JMXServiceURL jmxServiceUrl = new JMXServiceURL(JMX_URL_PREFIX + broker.getBrokerUrl() + JMX_URL_SUFFIX_QUEUE);
    ObjectName objName = createObjectName("JmxJms:name=" + getName());
    JMXConnector jmxClient = null;

    try {
      jmxServer = createAndRegister(jmxServiceUrl);
      jmxServer.start();
      jmxClient = createAndConnect(jmxServiceUrl);
      MBeanServerConnection serverConnection = jmxClient.getMBeanServerConnection();
      SimpleManagementBean bean = createAndRegisterBean(getName(), objName);
      AttributeList list = new AttributeList();
      list.add(new Attribute(getName(), getName()));
      assertEquals(0, serverConnection.setAttributes(objName, list).size());
    }
    finally {
      IOUtils.closeQuietly(jmxClient);
      closeQuietly(jmxServer);
    }
  }

  @Test
  public void testServerConnection_GetDomains() throws Exception {
    JMXConnectorServer jmxServer = null;
    JMXServiceURL jmxServiceUrl = new JMXServiceURL(JMX_URL_PREFIX + broker.getBrokerUrl() + JMX_URL_SUFFIX_QUEUE);
    ObjectName objName = createObjectName("JmxJms:name=" + getName());
    JMXConnector jmxClient = null;

    try {
      jmxServer = createAndRegister(jmxServiceUrl);
      jmxServer.start();
      jmxClient = createAndConnect(jmxServiceUrl);
      MBeanServerConnection serverConnection = jmxClient.getMBeanServerConnection();
      SimpleManagementBean bean = createAndRegisterBean(getName(), objName);
      assertNotNull(serverConnection.getDefaultDomain());
      assertNotNull(serverConnection.getDomains());
    }
    finally {
      IOUtils.closeQuietly(jmxClient);
      closeQuietly(jmxServer);
    }
  }

  @Test
  public void testServerConnection_GetMBeanInfo() throws Exception {
    JMXConnectorServer jmxServer = null;
    JMXServiceURL jmxServiceUrl = new JMXServiceURL(JMX_URL_PREFIX + broker.getBrokerUrl() + JMX_URL_SUFFIX_QUEUE);
    ObjectName objName = createObjectName("JmxJms:name=" + getName());
    JMXConnector jmxClient = null;

    try {
      jmxServer = createAndRegister(jmxServiceUrl);
      jmxServer.start();
      jmxClient = createAndConnect(jmxServiceUrl);
      MBeanServerConnection serverConnection = jmxClient.getMBeanServerConnection();
      SimpleManagementBean bean = createAndRegisterBean(getName(), objName);
      assertNotNull(serverConnection.getMBeanInfo(objName));
    }
    finally {
      IOUtils.closeQuietly(jmxClient);
      closeQuietly(jmxServer);
    }
  }

  @Test
  public void testServerConnection_IsInstanceOf() throws Exception {
    JMXConnectorServer jmxServer = null;
    JMXServiceURL jmxServiceUrl = new JMXServiceURL(JMX_URL_PREFIX + broker.getBrokerUrl() + JMX_URL_SUFFIX_QUEUE);
    ObjectName objName = createObjectName("JmxJms:name=" + getName());
    JMXConnector jmxClient = null;

    try {
      jmxServer = createAndRegister(jmxServiceUrl);
      jmxServer.start();
      jmxClient = createAndConnect(jmxServiceUrl);
      MBeanServerConnection serverConnection = jmxClient.getMBeanServerConnection();
      SimpleManagementBean bean = createAndRegisterBean(getName(), objName);
      assertTrue(serverConnection.isInstanceOf(objName, SimpleManagementBeanMBean.class.getCanonicalName()));
    }
    finally {
      IOUtils.closeQuietly(jmxClient);
      closeQuietly(jmxServer);
    }
  }
}
