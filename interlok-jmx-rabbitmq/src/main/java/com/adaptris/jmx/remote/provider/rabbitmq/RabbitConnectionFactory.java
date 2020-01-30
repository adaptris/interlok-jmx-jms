package com.adaptris.jmx.remote.provider.rabbitmq;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.management.remote.JMXServiceURL;

import com.adaptris.jmx.remote.jms.ExtendedJmsInvokerServiceExporter;
import com.adaptris.jmx.remote.jms.JmsJmxConnectionFactoryImpl;
import com.rabbitmq.jms.admin.RMQConnectionFactory;

class RabbitConnectionFactory extends JmsJmxConnectionFactoryImpl {

  private transient RMQConnectionFactory factory;
  private transient Object lock = new Object();
  private transient String brokerURL;

  RabbitConnectionFactory(Map<String, ?> env, JMXServiceURL url) throws IOException {
    super(env, url);
    try {
      brokerURL = removeQuery(getBrokerURI(url)).toString();
      factory = new RMQConnectionFactory();
      factory.setUri(brokerURL);

    }
    catch (Exception e) {
      throw new IOException(e);
    }
    // Force it to topic only.
    destinationFactory = DestinationFactory.Topic;
    jmsEnvironment.put(ATTR_DESTINATION_TYPE, DestinationFactory.Topic.name());
  }

  @Override
  protected Collection<String> validProtocols() {
    return Arrays.asList("rabbitmq");
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

  @Override
  public ExtendedJmsInvokerServiceExporter createServiceExporter() throws JMSException {
    // if (destinationFactory == DestinationFactory.Queue) {
    // return new QueueServiceExporter();
    // }
    // else {
      return new TopicServiceExporter();
    // }
  }

  // @Override
  // protected Queue createQueueDestination(String qName) throws JMSException {
  // return new RMQDestination(qName, true, false);
  // }
  //
  // @Override
  // protected Topic createTopicDestination(String tName) throws JMSException {
  // return new RMQDestination(tName, false, false);
  // }

  String getBrokerURL() {
    return brokerURL;
  }
}
