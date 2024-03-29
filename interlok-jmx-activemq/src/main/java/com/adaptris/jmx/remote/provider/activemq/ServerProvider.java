package com.adaptris.jmx.remote.provider.activemq;

import java.io.IOException;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerProvider;
import javax.management.remote.JMXServiceURL;

import com.adaptris.jmx.remote.jms.JmsJmxConnectorServer;

public class ServerProvider implements JMXConnectorServerProvider {

  @Override
  public JMXConnectorServer newJMXConnectorServer(JMXServiceURL url, Map<String, ?> environment, MBeanServer server) throws IOException {
    return new JmsJmxConnectorServer(url, environment, server, new ActiveMqJmsConnectionFactory(environment, url));
  }

}
