package com.adaptris.jmx.remote.provider.solace;


import com.adaptris.jmx.remote.jms.JmsJmxConnectionFactoryImpl;
import com.solacesystems.jms.SolConnectionFactory;
import com.solacesystems.jms.SolJmsUtility;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

class SolaceJmsConnectionFactory  extends JmsJmxConnectionFactoryImpl implements ProviderAttributes {

  private transient SolConnectionFactory factory;
  private transient Object lock = new Object();
  private transient String brokerURL;

  protected SolaceJmsConnectionFactory(Map<String, ?> env, JMXServiceURL url) throws IOException {
    super(env, url);
    try {
      brokerURL = removeQuery(getBrokerURI(url)).toString();
      factory = SolJmsUtility.createConnectionFactory();
      if (jmsEnvironment.containsKey(ATTR_BACKUP_BROKER_URLS)) {
        factory.setHost(brokerURL + "," + jmsEnvironment.get(ATTR_BACKUP_BROKER_URLS));
      }
      else {
        factory.setHost(getBrokerURL());
      }
      if (jmsEnvironment.containsKey(ATTR_CLIENT_ID)) {
        factory.setClientID(jmsEnvironment.get(ATTR_CLIENT_ID));
      }
      factory.setUsername(jmsEnvironment.get(ATTR_BROKER_USERNAME));
      factory.setPassword(jmsEnvironment.get(ATTR_BROKER_PASSWORD));
      factory.setVPN(jmsEnvironment.get(ATTR_MESSAGE_VPN));
    } catch (Exception e) {
      throw new IOException(e);
    }
  }

  @Override
  protected Collection<String> validProtocols() {
    return Arrays.asList("solace");
  }

  @Override
  public Connection createConnection() throws JMSException {
    synchronized (lock) {
      if (defaultConnection == null) {
        defaultConnection = connect(factory, factory.getHost());
      }
    }
    return defaultConnection;
  }

  @Override
  public Connection createConnection(String userName, String password) throws JMSException {
    Connection c = factory.createConnection(userName, password);
    addOpenedConnection(c);
    return c;
  }

  @Override
  protected Map<String, String> createDefaultEnvironment() {
    Map<String, String> env = super.createDefaultEnvironment();
    env.put(ATTR_MESSAGE_VPN, ENV_DEFAULT_MESSAGE_VPN);
    env.put(ATTR_BROKER_USERNAME, ENV_DEFAULT_SOLACE_USERNAME);
    env.put(ATTR_BROKER_PASSWORD, ENV_DEFAULT_SOLACE_PASSWORD);
    return env;
  }

  public String getBrokerURL() {
    return brokerURL;
  }
}
