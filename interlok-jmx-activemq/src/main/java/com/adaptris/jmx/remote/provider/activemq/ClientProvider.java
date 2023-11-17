package com.adaptris.jmx.remote.provider.activemq;

import java.io.IOException;
import java.util.Map;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorProvider;
import javax.management.remote.JMXServiceURL;

import com.adaptris.jmx.remote.jms.JmsJmxConnectorClient;

public class ClientProvider implements JMXConnectorProvider {

  @Override
  public JMXConnector newJMXConnector(JMXServiceURL url, Map<String, ?> environment) throws IOException {
    return new JmsJmxConnectorClient(url, environment, new ActiveMqJmsConnectionFactory(environment, url));
  }

}
