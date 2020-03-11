/**
 * JMX Remote provider for AMQP 1.0 via <a href="http://qpid.apache.org/">org.apache.qpid:qpid-jms-client</a>.
 * <p>
 * Various URL query parameters control the behaviour of the the JMS Connection; which are described in the table below. Each of
 * these properties can also be provided in the initial set of attributes that is passed in
 * {@link javax.management.remote.JMXConnectorServerFactory#newJMXConnectorServer(javax.management.remote.JMXServiceURL,java.util.Map,javax.management.MBeanServer)
 * JMXConnectorServerFactory#newJMXConnectorServer} or
 * {@link javax.management.remote.JMXConnectorFactory#newJMXConnector(javax.management.remote.JMXServiceURL,java.util.Map)
 * JMXConnectorFactory#newJMXConnector()}. All keys are case sensitive
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
 * <td>The username to connect to the broker (if required); This is likely to be redundant; as you can often configure the username
 * directly on the URL.</td>
 * </tr>
 * <tr>
 * <td>{@link com.adaptris.jmx.remote.jms.JmsJmxConnectionFactory#ATTR_BROKER_PASSWORD jmx.brokerPassword}</td>
 * <td>The password to connect to the broker (if required);This is likely to be redundant; as you can often configure the password
 * directly on the URL.</td>
 * </tr>
 * </table>
 * <p>
 * You can mix and match the environment with the URL which will be stripped of any known parameters before being passed through to
 * {@link org.apache.qpid.jms.JmsConnectionFactory}. If either
 * {@link com.adaptris.jmx.remote.jms.JmsJmxConnectionFactory#ATTR_BROKER_USERNAME jmx.brokerUser} or
 * {@link com.adaptris.jmx.remote.jms.JmsJmxConnectionFactory#ATTR_BROKER_PASSWORD jmx.brokerPassword} are specified (via URL,
 * environment or {@link javax.management.remote.JMXConnector#CREDENTIALS}), then this will cause
 * {@code ConnectionFactory.createConnection(String, String)} to be used when creating the connection.
 * </p>
 * <ul>
 * <li>{@code service:jmx:amqp:///amqp://localhost:5672?jms.prefetchPolicy.all=100&jmx.type=Topic&jmx.destination=jmxTopic} will
 * result in {@code amqp://localhost:5672?jms.prefetchPolicy.all=100} being passed into the
 * {@link org.apache.qpid.jms.JmsConnectionFactory}</li>
 * </ul>
 * *
 */
package com.adaptris.jmx.remote.provider.amqp;
