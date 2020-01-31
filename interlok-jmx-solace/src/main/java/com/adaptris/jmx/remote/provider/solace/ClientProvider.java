package com.adaptris.jmx.remote.provider.solace;

import com.adaptris.jmx.remote.jms.JmsJmxConnectorClient;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorProvider;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.util.Map;


public class ClientProvider implements JMXConnectorProvider {

  @Override
  public JMXConnector newJMXConnector(JMXServiceURL serviceURL, Map<String, ?> environment) throws IOException {
    return new JmsJmxConnectorClient(serviceURL, environment, new SolaceJmsConnectionFactory(environment, serviceURL));
  }

}
