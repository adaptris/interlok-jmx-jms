/**
 * JMX Remote provider for Solace.
 * <p>
 * Various URL query parameters control the behaviour of the the JMS Connection; which are described in the table below. Each of these
 * properties can also be provided in the initial set of attributes that is passed in
 * {@link javax.management.remote.JMXConnectorServerFactory#newJMXConnectorServer(javax.management.remote.JMXServiceURL,java.util.Map,javax.management.MBeanServer)
 * JMXConnectorServerFactory#newJMXConnectorServer} or
 * {@link javax.management.remote.JMXConnectorFactory#newJMXConnector(javax.management.remote.JMXServiceURL,java.util.Map)
 * JMXConnectorFactory#newJMXConnector()}. All keys are case sensitive and if specified on the URL, will be stripped before being passed to
 * {@code
 com.solacesystems.jms.SolConnectionFactory}
 * </p>
 * <table border="1">
 * <thead>
 * <th>Attribute/Parameter</th>
 * <th>Description</th> </thead>
 * <tr>
 * <td>{@link com.adaptris.jmx.remote.jms.JmsJmxConnectionFactory#ATTR_DESTINATION_TYPE jmx.type}</td>
 * <td>The destination type (i.e. <code>Topic</code> or <code>Queue</code>; case-sensitive); defaults to Topic.</td>
 * </tr>
 * <tr>
 * <td>{@link com.adaptris.jmx.remote.jms.JmsJmxConnectionFactory#ATTR_DESTINATION jmx.destination}</td>
 * <td>The name of a Topic or Queue; if not assigned, then a unique one will be created to avoid exceptions; this is, though, pointless from
 * a usability perspective</td>
 * </tr>
 * <tr>
 * <td>{@link com.adaptris.jmx.remote.jms.JmsJmxConnectionFactory#ATTR_BROKER_USERNAME jmx.brokerUser}</td>
 * <td>The username to connect to the broker (if required); defaults to 'default'.</td>
 * </tr>
 * <tr>
 * <td>{@link com.adaptris.jmx.remote.jms.JmsJmxConnectionFactory#ATTR_BROKER_PASSWORD jmx.brokerPassword}</td>
 * <td>The password to connect to the broker (if required); defaults to ''.</td>
 * </tr>
 * <tr>
 * <td>{@link com.adaptris.jmx.remote.jms.JmsJmxConnectionFactory#ATTR_TIMEOUT_MS jmx.timeout}</td>
 * <td>The timeout in milliseconds for a client to wait for a reply after sending a request; defaults to 60000.</td>
 * </tr>
 * <tr>
 * <td>{@link com.adaptris.jmx.remote.jms.JmsJmxConnectionFactory#ATTR_CLIENT_ID jmx.clientid}</td>
 * <td>The client ID to be associated with the underlying {@code
     com.solacesystems.jms.SolConnectionFactory} if desired; defaults to null.</td>
 * </tr>
 * <tr>
 * <td>{@link com.adaptris.jmx.remote.provider.solace.ProviderAttributes#ATTR_MESSAGE_VPN jmx.messageVPN}</td>
 * <td>The message VPN to use when connecting to router; defaults to default.</td>
 * </tr>
 * <tr>
 * <td>{@link com.adaptris.jmx.remote.provider.solace.ProviderAttributes#ATTR_BACKUP_BROKER_URLS jmx.backupBrokers}</td>
 * <td>A comma separated list of additional brokers that should serve as a list of backup brokers. Note that doing this as part of the URL
 * will make it hard to read, so that should be discouraged from maintainability point of view, it will be best to add the backup brokers as
 * part of the initial environment</td>
 * </tr>
 * </table>
 * <p>
 * You can mix and match the environment with the URL, the URL will take precedence, apart from in the case where
 * {@link javax.management.remote.JMXConnector#CREDENTIALS} exists in the initial set of attributes, that will always replace the brokerUser
 * and brokerPassword.
 * </p>
 * <ul>
 * <li>service:jmx:solace:///smf://localhost:55555?jmx.type=Topic&amp;jmx.destination=jmxTopic</li>
 * <li>service:jmx:solace:///smf://localhost:55555?jmx.type=Queue&amp;jmx.destination=SampleQ1</li>
 * <li>service:jmx:solace:///smf://localhost:55555?jmx.type=Topic&amp;jmx.destination=jmxTopic&amp;jmx.brokerUser=Administrator&amp;jmx.brokerPassword=Administrator</li>
 * <li>service:jmx:solace:///smf://localhost:55555?jmx.type=Topic&amp;jmx.destination=jmxTopic&amp;jmx.messageVPN=vpn1</li>
 * <li>service:jmx:solace:///smf://localhost:55555?jmx.type=Topic&amp;jmx.destination=jmxTopic&amp;jmx.backupBrokers=smf%3a%2f%2flocalhost%3a55556%2Csmf%3a%2f%2flocalhost%3a55557</li>
 * </ul>
 * <p>
 * Or if you want to have things in the initial environment, then you could have something like
 * </p>
 *
 * <pre>
 * JMXServiceURL url = new JMXServiceURL("service:jmx:solace://tcp://localhost:55555?jmx.destination=SampleQ1");
 * Map env = new HashMap();
 * env.put(JmsJmxConnectionFactory.ATTR_DESTINATION_TYPE, "Queue");
 * env.put(JmsJmxConnectionFactory.ATTR_BROKER_USERNAME, "MyUserName");
 * env.put(JmsJmxConnectionFactory.ATTR_BROKER_PASSWORD, "MyPassword");
 * env.put(ProviderAttributes.ATTR_MESSAGE_VPN, "default");
 * env.put(ProviderAttributes.ATTR_BACKUP_BROKER_URLS, "smf://backupBroker1:55555,smf://backupBroker2:55555");
 * JMXConnector jmxClient = JMXConnectorFactory.newJMXConnector(url, env);
 * jmxClient.connect();
 * </pre>
 *
 *
 */
package com.adaptris.jmx.remote.provider.solace;
