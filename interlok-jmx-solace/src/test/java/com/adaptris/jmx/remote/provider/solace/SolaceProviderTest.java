package com.adaptris.jmx.remote.provider.solace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import com.adaptris.jmx.remote.BaseCase;
import com.adaptris.jmx.remote.SimpleManagementBean;
import com.adaptris.jmx.remote.SimpleManagementBeanMBean;
import com.adaptris.jmx.remote.jms.JmsJmxConnectionFactory;
import com.adaptris.jmx.remote.jms.JmsJmxConnectionFactoryImpl;

public class SolaceProviderTest extends BaseCase {

  private static final String KEY_TESTS_ENABLED = "solace.enabled";
  private static final String KEY_QUEUE_JMX_URL = "solace.queue.JmxServiceURL";
  private static final String KEY_TOPIC_JMX_URL = "solace.topic.JmxServiceURL";
  private static final String KEY_BROKER_USER = "solace.brokerUser";
  private static final String KEY_BROKER_PASSWORD = "solace.brokerPassword";
  private static final String KEY_BROKER_URL = "solace.broker";
  private static final String KEY_BACKUP_BROKER_URL = "solace.backupBrokers";
  private static final String KEY_MESSAGE_VPN = "solace.messageVPN";

  private static boolean testsEnabled() {
    return Boolean.valueOf(PROPERTIES.getProperty(KEY_TESTS_ENABLED, "false")).booleanValue();
  }

  @Test
  public void testFactory_AdditionalQueryOnURL_Stripped() throws Exception {
    assumeTrue(testsEnabled());

    JMXServiceURL jmxServiceUrl = new JMXServiceURL(PROPERTIES.getProperty(KEY_QUEUE_JMX_URL) + "&abcde=def");
    try {
      SolaceJmsConnectionFactory fact = new SolaceJmsConnectionFactory(new HashMap<String, Object>(), jmxServiceUrl);
      String url = JmsJmxConnectionFactoryImpl.removeQuery(new URI(fact.getBrokerURL())).toString();
      assertEquals(PROPERTIES.getProperty(KEY_BROKER_URL), url);
    } finally {
    }
  }

  @Test
  public void testMBean_UseQueues() throws Exception {
    assumeTrue(testsEnabled());

    JMXConnectorServer jmxServer = null;
    JMXConnector jmxClient = null;
    JMXServiceURL jmxServiceUrl = new JMXServiceURL(PROPERTIES.getProperty(KEY_QUEUE_JMX_URL));
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
    assumeTrue(testsEnabled());

    JMXConnectorServer jmxServer = null;
    JMXConnector jmxClient = null;
    JMXServiceURL jmxServiceUrl = new JMXServiceURL(PROPERTIES.getProperty(KEY_TOPIC_JMX_URL));
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
  public void testMBean_WithClientId() throws Exception {
    assumeTrue(testsEnabled());
    JMXConnectorServer jmxServer = null;
    JMXConnector jmxClient = null;
    JMXServiceURL jmxServiceUrl = new JMXServiceURL(PROPERTIES.getProperty(KEY_TOPIC_JMX_URL));
    ObjectName objName = createObjectName("JmxJms:name=" + getName());
    try {
      SimpleManagementBean realBean = createAndRegisterBean(getName(), objName);
      Map<String, Object> environment = createEnvironment();
      environment.put(JmsJmxConnectionFactory.ATTR_CLIENT_ID, UUID.randomUUID().toString());
      jmxServer = JMXConnectorServerFactory.newJMXConnectorServer(jmxServiceUrl, environment, null);
      mbeanServer.registerMBean(jmxServer, connectorServerObjectName);
      jmxServer.start();
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
  public void testMBean_WithBackupBroker() throws Exception {
    assumeTrue(testsEnabled());
    JMXConnectorServer jmxServer = null;
    JMXConnector jmxClient = null;
    JMXServiceURL jmxServiceUrl = new JMXServiceURL(PROPERTIES.getProperty(KEY_TOPIC_JMX_URL));
    ObjectName objName = createObjectName("JmxJms:name=" + getName());
    try {
      SimpleManagementBean realBean = createAndRegisterBean(getName(), objName);
      Map<String, Object> environment = createEnvironment();
      environment.put(ProviderAttributes.ATTR_BACKUP_BROKER_URLS, PROPERTIES.getProperty(KEY_BACKUP_BROKER_URL));
      jmxServer = JMXConnectorServerFactory.newJMXConnectorServer(jmxServiceUrl, environment, null);
      mbeanServer.registerMBean(jmxServer, connectorServerObjectName);
      jmxServer.start();
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
  public void testMBean_WithJmxConnectorCredentialsAndMessageVPN() throws Exception {
    assumeTrue(testsEnabled());
    JMXConnectorServer jmxServer = null;
    JMXConnector jmxClient = null;
    JMXServiceURL jmxServiceUrl = new JMXServiceURL(PROPERTIES.getProperty(KEY_TOPIC_JMX_URL));
    ObjectName objName = createObjectName("JmxJms:name=" + getName());
    try {
      SimpleManagementBean realBean = createAndRegisterBean(getName(), objName);
      Map<String, Object> environment = createEnvironment();
      environment.put(ProviderAttributes.ATTR_MESSAGE_VPN, PROPERTIES.getProperty(KEY_MESSAGE_VPN));
      environment.put(JMXConnector.CREDENTIALS,
          new String[] { PROPERTIES.getProperty(KEY_BROKER_USER, PROPERTIES.getProperty(KEY_BROKER_USER)),
              PROPERTIES.getProperty(KEY_BROKER_PASSWORD, PROPERTIES.getProperty(KEY_BROKER_PASSWORD)) });
      jmxServer = JMXConnectorServerFactory.newJMXConnectorServer(jmxServiceUrl, environment, null);
      mbeanServer.registerMBean(jmxServer, connectorServerObjectName);
      jmxServer.start();
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
  public void testFactory_CreateConnection() throws Exception {
    assumeTrue(testsEnabled());
    JMXServiceURL jmxServiceUrl = new JMXServiceURL(PROPERTIES.getProperty(KEY_TOPIC_JMX_URL));
    SolaceJmsConnectionFactory fact = new SolaceJmsConnectionFactory(new HashMap<String, Object>(), jmxServiceUrl);
    try {
      assertNotNull(fact.createConnection());
      assertNotNull(fact.createConnection("guest", "guest"));
    } finally {
      fact.destroy();
    }
  }

  @Override
  public Map<String, Object> createEnvironment() {
    Map<String, Object> environment = super.createEnvironment();
    environment.put(JmsJmxConnectionFactory.ATTR_BROKER_USERNAME,
        PROPERTIES.getProperty(KEY_BROKER_USER, ProviderAttributes.ENV_DEFAULT_SOLACE_USERNAME));
    environment.put(JmsJmxConnectionFactory.ATTR_BROKER_PASSWORD,
        PROPERTIES.getProperty(KEY_BROKER_PASSWORD, ProviderAttributes.ENV_DEFAULT_SOLACE_PASSWORD));
    environment.put(ProviderAttributes.ATTR_MESSAGE_VPN,
        PROPERTIES.getProperty(KEY_MESSAGE_VPN, ProviderAttributes.ENV_DEFAULT_MESSAGE_VPN));
    return environment;
  }

}
