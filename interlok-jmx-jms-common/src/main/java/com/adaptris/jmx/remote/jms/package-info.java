/**
 * Base package for handling remote JMX via JMS.
 * <p>
 * The underlying framework for handling remote JMX invocation is handled by Spring Remoting (and its dependencies); particularly
 * {@link org.springframework.jms.remoting.JmsInvokerServiceExporter}. This package simply wraps the Spring remoting functionality
 * and implements the required interfaces from {@code javax.management.remote} and performs operation in a simple sequential
 * fashion.
 * </p>
 * <p>
 * Although the classes contained in this package implement the core functionality required to handle remote JMX over JMS, there are
 * some contracts that they will not fulfil. The most important one is the behaviour of
 * {@link javax.management.remote.JMXConnectorServer#getConnectionIds()} and
 * {@link javax.management.remote.JMXConnector#getConnectionId()}. It uses a JMS provider to provide connectivity so clients aren't
 * directly connected to our {@link com.adaptris.jmx.remote.jms.JmsJmxConnectorServer} implementation, requests just appear on a
 * given {@link javax.jms.Destination} and the class attempts to service them. As a result, you will find that the
 * {@link javax.management.remote.JMXConnectorServer#getConnectionIds()} will return an empty string, and although
 * {@link javax.management.remote.JMXConnector#getConnectionId()} will return a conventional connection ID, it is only consistent
 * for the lifetime of the underlying JMS connection and cannot be cross-referenced with anything.
 * </p>
 * <p>
 * Communication via both {@link javax.jms.Queue} and {@link javax.jms.Topic} are supported unless other specified in their
 * respective implementations, each request will create a temporary destination matching the target destination type.
 * {@link javax.management.Notification Notifications} are received and handled via a separate temporary destination.
 * </p>
 */
package com.adaptris.jmx.remote.jms;
