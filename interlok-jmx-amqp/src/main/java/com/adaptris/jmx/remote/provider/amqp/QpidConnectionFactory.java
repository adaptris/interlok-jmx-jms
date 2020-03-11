package com.adaptris.jmx.remote.provider.amqp;

import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.io.IOException;
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

import org.apache.qpid.jms.JmsConnectionFactory;

import com.adaptris.jmx.remote.jms.JmsJmxConnectionFactory;
import com.adaptris.jmx.remote.jms.JmsJmxConnectionFactoryImpl;

class QpidConnectionFactory extends JmsJmxConnectionFactoryImpl {

  private static final Set<String> STRIP_KEYS = new HashSet<String>(Arrays.asList(ATTR_DESTINATION_TYPE, ATTR_DESTINATION,
      ATTR_BROKER_USERNAME, ATTR_BROKER_PASSWORD, ATTR_RETRY_INTERVAL_MS, ATTR_TIMEOUT_MS));

  private transient JmsConnectionFactory factory;
  private transient Object lock = new Object();
  private transient String brokerURL;
  private transient boolean useCredentials;

  QpidConnectionFactory(Map<String, ?> env, JMXServiceURL url) throws IOException {
    super(env, url);
    try {
      URI baseURI = getBrokerURI(url);
      String queryString = rebuildQuery(parseParameters(baseURI), STRIP_KEYS);
      if (!isEmpty(queryString)) {
        brokerURL = newURI(baseURI, queryString).toString();
      }
      else {
        brokerURL = newURI(baseURI, null).toString();
      }
      factory = new JmsConnectionFactory(brokerURL);
      useCredentials = jmsEnvironment.containsKey(JmsJmxConnectionFactory.ATTR_BROKER_USERNAME);
    }
    catch (URISyntaxException e) {
      throw new IOException(e);
    }
  }
  @Override
  protected Collection<String> validProtocols() {
    return Arrays.asList("amqp");
  }

  @Override
  public Connection createConnection() throws JMSException {
    synchronized (lock) {
      if (defaultConnection == null) {
        try {
          defaultConnection = connect(factory, maskUserInfo(new URI(brokerURL)).toString());
        }
        catch (URISyntaxException e) {
          throw wrapJmsException(e);
        }
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


  // @Override
  // protected Queue createQueueDestination(String qName) throws JMSException {
  // return new org.apache.qpid.jms.JmsQueue(qName);
  // }
  //
  // @Override
  // protected Topic createTopicDestination(String tName) throws JMSException {
  // return new org.apache.qpid.jms.JmsTopic(tName);
  // }

  String getBrokerURL() {
    return brokerURL;
  }
}
