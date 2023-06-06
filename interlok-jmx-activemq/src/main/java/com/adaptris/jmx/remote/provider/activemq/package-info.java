/**
 * JMX Remote provider for ActiveMQ.
 * <p>
 * Various URL query parameters control the behaviour of the the JMS Connection; which are described in the table below. Each of
 * these properties can also be provided in the initial set of attributes that is passed in
 * {@link javax.management.remote.JMXConnectorServerFactory#newJMXConnectorServer(javax.management.remote.JMXServiceURL,java.util.Map,javax.management.MBeanServer)
 * JMXConnectorServerFactory#newJMXConnectorServer} or
 * {@link javax.management.remote.JMXConnectorFactory#newJMXConnector(javax.management.remote.JMXServiceURL,java.util.Map)
 * JMXConnectorFactory#newJMXConnector()}. All keys are case sensitive, and if specified in the URL will be stripped before being
 * passed to {@link org.apache.activemq.ActiveMQConnectionFactory}
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
 * <td>The name of a Topic or Queue; if not assigned, then a unique one will be created to avoid exceptions; this is, though,
 * pointless from a usability perspective</td>
 * </tr>
 * <tr>
 * <td>{@link com.adaptris.jmx.remote.jms.JmsJmxConnectionFactory#ATTR_BROKER_USERNAME jmx.brokerUser}</td>
 * <td>The username to connect to the broker (if required); defaults to null. This may be a redundant; as you can often configure
 * the username directly on the URL</td>
 * </tr>
 * <tr>
 * <td>{@link com.adaptris.jmx.remote.jms.JmsJmxConnectionFactory#ATTR_BROKER_PASSWORD jmx.brokerPassword}</td>
 * <td>The password to connect to the broker (if required); defaults to null. This is likely to be redundant; as you can often
 * configure the password directly on the URL.</td>
 * </tr>
 * <tr>
 * <td>{@link com.adaptris.jmx.remote.jms.JmsJmxConnectionFactory#ATTR_TIMEOUT_MS jmx.timeout}</td>
 * <td>The timeout in milliseconds for a client to wait for a reply after sending a request; defaults to 60000.</td>
 * </tr>
 * <tr>
 * <td>{@link com.adaptris.jmx.remote.jms.JmsJmxConnectionFactory#ATTR_CLIENT_ID jmx.clientid}</td>
 * <td>The client ID to be associated with the underlying {@link org.apache.activemq.ActiveMQConnectionFactory} if desired. This is
 * likely to be redundant; as you can often configure the clientid directly on the URL anyway.</td>
 * </tr>
 * </table>
 * <p>
 * You can mix and match the environment with the URL, the URL will take precedence, apart from in the case where
 * {@link javax.management.remote.JMXConnector#CREDENTIALS} exists in the initial set of attributes, that will always replace the
 * brokerUser and brokerPassword.
 * </p>
 * <ul>
 * <li>service:jmx:activemq:///tcp://localhost:61616?jmx.type=Queue&amp;jmx.destination=jmxQueue</li>
 * <li>service:jmx:activemq:///tcp://localhost:61616?jmx.type=Topic&amp;jmx.destination=jmxTopic</li>
 * <li>service:jmx:activemq:///tcp://localhost:61616?jmx.type=Topic&amp;jmx.destination=jmxTopic&amp;jmx.brokerUser=myUserName&amp;jmx.brokerPassword=MyPassword</li>
 * </ul>
 * <p>
 * Or if you want to have things in the initial environment, then you could have something like
 * </p>
 * 
 * <pre>
 * JMXServiceURL url = new JMXServiceURL("service:jmx:activemq:///tcp://localhost:61616?jmx.destination=jmxQueue");
 * Map env = new HashMap();
 * env.put(JmsJmxConnectionFactory.ATTR_DESTINATION_TYPE, "Queue");
 * env.put(JmsJmxConnectionFactory.ATTR_BROKER_USERNAME, "MyUserName");
 * env.put(JmsJmxConnectionFactory.ATTR_BROKER_PASSWORD, "MyPassword");
 * JMXConnector jmxClient = JMXConnectorFactory.newJMXConnector(url, env);
 * jmxClient.connect();
 * </pre>
 * <p>
 * Note that if you are using ActiveMQ 5.12 and above then you will need to set the
 * <code>org.apache.activemq.SERIALIZABLE_PACKAGES</code> system property to enable notifications to be sent via ActiveMQ as object
 * message serialization needs to be explicitly enabled. Check <a href="http://activemq.apache.org/objectmessage.html">the
 * documentation</a> for the specifics of the property.
 * </p>
 *
 */
package com.adaptris.jmx.remote.provider.activemq;
