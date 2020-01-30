/**
 * <p>
 * JMX Remote provider for RabbitMQ via {@code com.rabbitmq.jms:rabbitmq-jms} and {@code com.rabbitmq:amqp-client}.
 * </p>
 * <p>
 * Note that <strong>Queues</strong> are not supported in this implementation, as in certain situations the implementation fails to
 * get exclusive access to a Queue. Since RabbitMQ doesn't appear to support{@code JMSReplyTo} in its JMS implementation we pass the
 * {@code JMSReplyTo} as a string property, and attempt to create a JMS Destination from the name. This doesn't work reliably enough
 * for with queues us to offer that as a messaging option
 * </p>
 * <p>
 * Also be aware that a number of queue/exchange bindings will be created within RabbitMQ (you can see this in the admin interface).
 * This number will grow as you make more JMX requests over RabbitMQ; they are only cleared when the connection is closed. If you
 * are using jconsole (or similar); or leave the Interlok UI attached to an instance (either by leaving a browser open on the
 * dashboard/widgets page), then this value may grow past your broker limit. <strong>For this reason we don't recommend you use it
 * in production.</strong>
 * </p>
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
 * <td>{@link com.adaptris.jmx.remote.jms.JmsJmxConnectionFactory#ATTR_DESTINATION jmx.destination}</td>
 * <td>The name of a Topic; if not assigned, then a unique one will be created to avoid exceptions; this is, though, pointless from
 * a usability perspective.</td>
 * </tr>
 * </table>
 *
 */
package com.adaptris.jmx.remote.provider.rabbitmq;
