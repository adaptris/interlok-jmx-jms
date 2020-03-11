package com.adaptris.jmx.remote;

import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class BaseCase {
  protected transient Logger log = LoggerFactory.getLogger(this.getClass());

  public static final Properties PROPERTIES;
  private static final String PROPERTIES_RESOURCE = "unit-tests.properties";

  protected ObjectName connectorServerObjectName;
  protected MBeanServer mbeanServer;
  private Set<ObjectName> toBeUnregistered = new HashSet<ObjectName>();

  static {
    PROPERTIES = new Properties();
    try (InputStream in = BaseCase.class.getClassLoader().getResourceAsStream(PROPERTIES_RESOURCE)) {
      PROPERTIES.load(in);
    }
    catch (Exception e) {
      throw new RuntimeException("cannot locate resource [" + PROPERTIES_RESOURCE + "] on classpath", e);
    }
  }

  @Rule
  public TestName testName = new TestName();

  public BaseCase() {
    super();
  }

  @Before
  public void beforeTests() throws Exception {
    mbeanServer = ManagementFactory.getPlatformMBeanServer();
    connectorServerObjectName = createObjectName("JmxJms:name=JmxConnectorServer");
  }

  @After
  public void afterTests() throws Exception {
    for (ObjectName objName : toBeUnregistered) {
      if (mbeanServer.isRegistered(objName)) {
        mbeanServer.unregisterMBean(objName);
      }
    }
  }

  public String getName() {
    return testName.getMethodName();
  }


  protected SimpleManagementBean createAndRegisterBean(String name, ObjectName objName) throws Exception {
    SimpleManagementBean bean = new SimpleManagementBean(name);
    unregisterLater(objName);
    mbeanServer.registerMBean(bean, objName);
    return bean;
  }

  protected JMXConnectorServer createAndRegister(JMXServiceURL url, Map<String, ?> env) throws Exception {
    JMXConnectorServer jmxServer = JMXConnectorServerFactory.newJMXConnectorServer(url, env, null);
    mbeanServer.registerMBean(jmxServer, connectorServerObjectName);
    return jmxServer;
  }

  protected JMXConnectorServer createAndRegister(JMXServiceURL url) throws Exception {
    return createAndRegister(url, createEnvironment());
  }

  protected JMXConnectorServer createAndStart(JMXServiceURL url, Map<String, ?> env) throws Exception {
    JMXConnectorServer jmxServer = createAndRegister(url, env);
    jmxServer.start();
    return jmxServer;
  }

  protected JMXConnectorServer createAndStart(JMXServiceURL url) throws Exception {
    return createAndStart(url, createEnvironment());
  }

  protected JMXConnector createAndConnect(JMXServiceURL url, Map<String, ?> env) throws Exception {
    JMXConnector jmxClient = JMXConnectorFactory.newJMXConnector(url, env);
    jmxClient.connect();
    return jmxClient;
  }

  protected JMXConnector createAndConnect(JMXServiceURL url) throws Exception {
    return createAndConnect(url, createEnvironment());
  }

  public Map<String, ?> createEnvironment() {
    Map environment = new HashMap();
    environment.put(JMXConnectorServerFactory.PROTOCOL_PROVIDER_PACKAGES, "com.adaptris.jmx.remote.provider");
    return environment;
  }

  protected void closeQuietly(JMXConnectorServer s) {
    try {
      if (s != null) {
        s.stop();
      }
    }
    catch (IOException e) {
    }
  }

  protected void unregisterLater(ObjectName obj) {
    toBeUnregistered.add(obj);
  }

  protected ObjectName createObjectName(String s) throws Exception {
    ObjectName objName = ObjectName.getInstance(s);
    unregisterLater(objName);
    return objName;
  }

}
