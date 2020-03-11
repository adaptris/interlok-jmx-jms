package com.adaptris.jmx.remote.provider.solace;


import com.adaptris.jmx.remote.jms.JmsJmxConnectorServer;

import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerProvider;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.util.Map;

public class ServerProvider  implements JMXConnectorServerProvider {

  @Override
  public JMXConnectorServer newJMXConnectorServer(JMXServiceURL serviceURL, Map<String, ?> environment, MBeanServer mbeanServer) throws IOException {
    return new JmsJmxConnectorServer(serviceURL, environment, mbeanServer, new SolaceJmsConnectionFactory(environment, serviceURL));
  }
}
