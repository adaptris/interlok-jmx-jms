package com.adaptris.jmx.remote.provider.activemq;

import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.management.remote.JMXServiceURL;

import org.apache.activemq.ActiveMQConnectionFactory;

import com.adaptris.jmx.remote.jms.JmsJmxConnectionFactoryImpl;

class ActiveMqJmsConnectionFactory extends JmsJmxConnectionFactoryImpl {

  private static final Set<String> STRIP_KEYS = new HashSet<String>(Arrays.asList(ATTR_CLIENT_ID, ATTR_BROKER_PASSWORD,
      ATTR_BROKER_USERNAME, ATTR_CLIENT_ID, ATTR_DESTINATION_TYPE, ATTR_DESTINATION, ATTR_RETRY_INTERVAL_MS, ATTR_TIMEOUT_MS));

  private transient ActiveMQConnectionFactory factory;

  private transient Object lock = new Object();
  private transient String brokerURL;
  
  ActiveMqJmsConnectionFactory(Map<String, ?> env, JMXServiceURL url) throws IOException {
    super(env, url);
    try {
      URI baseURI = getBrokerURI(url);
      String queryString = rebuildQuery(parseParameters(baseURI), STRIP_KEYS);
      if (!isEmpty(queryString)) {
        brokerURL = newURI(baseURI, queryString).toString();
      } else {
        brokerURL = newURI(baseURI, null).toString();
      }
      factory = new ActiveMQConnectionFactory(brokerURL);
      factory.setClientID(jmsEnvironment.get(ATTR_CLIENT_ID));
      factory.setUserName(jmsEnvironment.get(ATTR_BROKER_USERNAME));
      factory.setPassword(jmsEnvironment.get(ATTR_BROKER_PASSWORD));
      trustAll(factory);
    }
    catch (URISyntaxException e) {
      throw new IOException(e);
    }
  }

  @Override
  protected Collection<String> validProtocols() {
    return Arrays.asList("activemq");
  }

  @Override
  public Connection createConnection() throws JMSException {
    synchronized (lock) {
      if (defaultConnection == null) {
        defaultConnection = connect(factory, factory.getBrokerURL());
      }
    }
    return defaultConnection;
  }

  @Override
  public Connection createConnection(String arg0, String arg1) throws JMSException {
    Connection c = factory.createConnection(arg0, arg1);
    addOpenedConnection(c);
    return c;
  }

  String getBrokerURL() {
    return brokerURL;
  }

  // INTERLOK-2433
  private static void trustAll(ActiveMQConnectionFactory factory) {
    try {
      Method method = ActiveMQConnectionFactory.class.getMethod("setTrustAllPackages", boolean.class);
      method.invoke(factory, Boolean.TRUE);
    } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
    }
  }
}
