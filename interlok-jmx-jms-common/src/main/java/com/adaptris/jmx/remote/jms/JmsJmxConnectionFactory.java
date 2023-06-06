package com.adaptris.jmx.remote.jms;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXServiceURL;

/**
 * Interface that defines additional functionality that is required to support JMX over JMS.
 *
 * @author lchan
 *
 */
public interface JmsJmxConnectionFactory extends ConnectionFactory {

  /**
   * Key in the initial attributes that specifies the destination.
   * <p>
   * The value in the initial attributes can by overriden by specifying this as part of the query portion of the {@link JMXServiceURL}.
   * </p>
   */
  String ATTR_DESTINATION = "jmx.destination";

  /**
   * Key in the initial attributes that specifies the destination type (either "Topic" or "Queue")
   * <p>
   * The value in the initial attributes can by overriden by specifying this as part of the query portion of the {@link JMXServiceURL}.
   * </p>
   */
  String ATTR_DESTINATION_TYPE = "jmx.type";
  /**
   * Key in the initial attributes that specifies the timeout value for operations in milliseconds.
   * <p>
   * The value in the initial attributes can by overriden by specifying this as part of the query portion of the {@link JMXServiceURL}.
   * </p>
   *
   * @see #DEFAULT_TIMEOUT_MS
   */
  String ATTR_TIMEOUT_MS = "jmx.timeout";
  /**
   * Key in the initial attributes that specifies the clientid to be associated the underlying {@link javax.jms.ConnectionFactory}.
   * <p>
   * The value in the initial attributes can by overriden by specifying this as part of the query portion of the {@link JMXServiceURL}.
   * </p>
   */
  String ATTR_CLIENT_ID = "jmx.clientid";
  /**
   * Key in the initial attributes that specifies the broker user to be associated the underlying {@link javax.jms.ConnectionFactory} .
   * <p>
   * The value in the initial attributes can by overriden by specifying this as part of the query portion of the {@link JMXServiceURL}.
   * Where appropriate then the values stored against the standard key {@link JMXConnector#CREDENTIALS} will supersede both this key and any
   * query parameter.
   * </p>
   */
  String ATTR_BROKER_USERNAME = "jmx.brokerUser";
  /**
   * Key in the initial attributes that specifies the broker password to be associated the underlying {@link javax.jms.ConnectionFactory} .
   * <p>
   * The value in the initial attributes can by overriden by specifying this as part of the query portion of the {@link JMXServiceURL}.
   * Where appropriate then the values stored against the standard key {@link JMXConnector#CREDENTIALS} will supersede both this key and any
   * query parameter.
   * </p>
   */
  String ATTR_BROKER_PASSWORD = "jmx.brokerPassword";

  /**
   * Key in the initial attributes that controls the interval (ms) between attempts to connect to the given broker.
   * <p>
   * This value comes into play when the JMS broker is unavailable or an exception has occured with the connection. The underlying
   * {@link JMXConnector} or {@link JMXConnectorServer} will not process any requests until a connection succeeds.
   * </p>
   *
   * @see #DEFAULT_RETRY_INTERVAL_MS
   */
  String ATTR_RETRY_INTERVAL_MS = "jmx.retryInterval";

  /**
   * The default value associated with {@link #ATTR_RETRY_INTERVAL_MS} which is {@value} .
   *
   */
  long DEFAULT_RETRY_INTERVAL_MS = 30000L;
  /**
   * The default value associated with {@link #ATTR_TIMEOUT_MS} which is {@value} .
   *
   */
  long DEFAULT_TIMEOUT_MS = 60000L;

  /**
   * Get the target dstination where you want to send requests and other things to.
   *
   * @return the destination.
   * @throws JMSException
   */
  Destination getTargetDestination() throws JMSException;

  /**
   * Create the invoker proxy.
   *
   * @return a {@link JmsInvokerProxy} instance.
   * @throws JMSException
   *           if there was an problem creating the invoker proxy.
   */
  JmsInvokerProxy<?> createInvokerProxy() throws JMSException;

  /**
   * Create the invoker proxy.
   *
   * @return a {@link ExtendedJmsInvokerServiceExporter} instance.
   * @throws JMSException
   *           if there was an problem creating the ServiceExport proxy.
   */
  ExtendedJmsInvokerServiceExporter<?> createServiceExporter() throws JMSException;

  /**
   * Create a temporary destination (either a queue or a topic).
   *
   * @param s
   *          the session
   * @return a {@link javax.jms.TemporaryQueue} or {@link javax.jms.TemporaryTopic}.
   * @throws JMSException
   *           if there was an error creating the temporary destination.
   */
  Destination createTemporaryDestination(Session s) throws JMSException;

  /**
   * Destroy any resources that may need destroying.
   *
   */
  void destroy();
}
