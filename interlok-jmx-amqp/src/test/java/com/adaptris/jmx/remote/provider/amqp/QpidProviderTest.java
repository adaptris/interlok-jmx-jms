package com.adaptris.jmx.remote.provider.amqp;

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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.adaptris.jmx.remote.BaseCase;
import com.adaptris.jmx.remote.EmbeddedActiveMq;
import com.adaptris.jmx.remote.SimpleManagementBean;
import com.adaptris.jmx.remote.SimpleManagementBeanMBean;

@SuppressWarnings("deprecation")
public class QpidProviderTest extends BaseCase {

  public static final String JMX_URL_PREFIX = "service:jmx:amqp:///";
  public static final String JMX_URL_SUFFIX_QUEUE = "?jmx.type=Queue&jmx.destination=Junit_Queue";
  public static final String JMX_URL_SUFFIX_TOPIC = "?jmx.type=Topic&jmx.destination=Junit_Topic";

  private EmbeddedActiveMq broker;
  private String jmxUrlBase;

  public QpidProviderTest() {}


  @Before
  public void setUp() throws Exception {
    broker = new EmbeddedActiveMq();
    broker.start();
    jmxUrlBase = JMX_URL_PREFIX + broker.getAmqpBrokerUrl();

  }

  @After
  public void tearDown() throws Exception {
    broker.destroy();
  }


  @Test
  public void testMBean_UseQueues() throws Exception {
    JMXConnectorServer jmxServer = null;
    JMXConnector jmxClient = null;
    JMXServiceURL jmxServiceUrl = new JMXServiceURL(jmxUrlBase + JMX_URL_SUFFIX_QUEUE);
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
    JMXServiceURL jmxServiceUrl = new JMXServiceURL(jmxUrlBase + JMX_URL_SUFFIX_TOPIC);
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
    JMXServiceURL jmxServiceUrl = new JMXServiceURL(jmxUrlBase + JMX_URL_SUFFIX_TOPIC + "&jms.prefetchPolicy.all=100");
    try {
      QpidConnectionFactory fact = new QpidConnectionFactory(new HashMap<String, Object>(), jmxServiceUrl);
      String url = fact.getBrokerURL();
      assertEquals(broker.getAmqpBrokerUrl() + "?jms.prefetchPolicy.all=100", url);
    } finally {
    }

  }

  @Test
  public void testFactory_CreateConnection() throws Exception {
    JMXServiceURL jmxServiceUrl = new JMXServiceURL(jmxUrlBase + JMX_URL_SUFFIX_TOPIC);
    QpidConnectionFactory fact = new QpidConnectionFactory(new HashMap<String, Object>(), jmxServiceUrl);
    try {
      assertNotNull(fact.createConnection());
      assertNotNull(fact.createConnection("guest", "guest"));
    } finally {
      fact.destroy();
    }
  }
}
