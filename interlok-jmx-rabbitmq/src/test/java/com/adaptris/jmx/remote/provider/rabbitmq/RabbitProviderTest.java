package com.adaptris.jmx.remote.provider.rabbitmq;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.HashMap;
import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXServiceURL;
import org.apache.commons.io.IOUtils;
import org.junit.Assume;
import org.junit.Test;
import com.adaptris.jmx.remote.BaseCase;
import com.adaptris.jmx.remote.SimpleManagementBean;
import com.adaptris.jmx.remote.SimpleManagementBeanMBean;

@SuppressWarnings("deprecation")
public class RabbitProviderTest extends BaseCase {

  private static final String KEY_TESTS_ENABLED = "rabbitmq.enabled";
  private static final String KEY_QUEUE_JMX_URL = "rabbitmq.queue.JmxServiceURL";
  private static final String KEY_TOPIC_JMX_URL = "rabbitmq.topic.JmxServiceURL";
  private static final String KEY_BROKER_URL = "rabbitmq.broker";

  public RabbitProviderTest() {}

  private static boolean testsEnabled() {
    return Boolean.valueOf(PROPERTIES.getProperty(KEY_TESTS_ENABLED, "false")).booleanValue();
  }

  @Test
  public void testMBean_NeverUsesQueues() throws Exception {
    Assume.assumeTrue(testsEnabled());

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
    Assume.assumeTrue(testsEnabled());

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
  public void testFactory_AdditionalQueryOnURL_Removed() throws Exception {
    Assume.assumeTrue(testsEnabled());

    JMXServiceURL jmxServiceUrl = new JMXServiceURL(PROPERTIES.getProperty(KEY_QUEUE_JMX_URL));
    try {
      RabbitConnectionFactory fact = new RabbitConnectionFactory(new HashMap<String, Object>(), jmxServiceUrl);
      String url = fact.getBrokerURL();
      assertEquals(PROPERTIES.getProperty(KEY_BROKER_URL), url);
    } finally {
    }

  }

  @Test
  public void testFactory_CreateConnection() throws Exception {
    Assume.assumeTrue(testsEnabled());

    JMXServiceURL jmxServiceUrl = new JMXServiceURL(PROPERTIES.getProperty(KEY_QUEUE_JMX_URL));
    RabbitConnectionFactory fact = new RabbitConnectionFactory(new HashMap<String, Object>(), jmxServiceUrl);
    try {
      assertNotNull(fact.createConnection());
      assertNotNull(fact.createConnection("admin", "admin"));
    } finally {
      fact.destroy();
    }

  }
}
