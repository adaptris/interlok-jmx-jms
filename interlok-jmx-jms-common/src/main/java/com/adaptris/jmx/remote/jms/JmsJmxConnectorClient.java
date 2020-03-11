package com.adaptris.jmx.remote.jms;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import javax.jms.JMSException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.NotificationBroadcasterSupport;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.remote.JMXConnectionNotification;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXServiceURL;
import javax.security.auth.Subject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link JMXConnector} that provides JMX connectivity via JMS.
 *
 * <p>
 * It is expected that this class will be constructed using
 * {@link javax.management.remote.JMXConnectorFactory#newJMXConnector(JMXServiceURL, Map)}; rather than directly via the
 * constructor. As the various provider (e.g. {@link com.adaptris.jmx.remote.provider.activemq.ClientProvider}) will initialise the
 * various connection properties that are required for connecting to the JMS provider, effectively the
 * {@link JmsJmxConnectorClient#connect(Map)} method will not override any properties that have already been set in the initial set
 * of attributes.
 * </p>
 * <p>
 * Note that the {@link #getConnectionId()} returned by this client is auto-generated upon each new connection attempt. It will not
 * correspond to any information that can be retrieved from the corresponding {@link JmsJmxConnectorServer}.
 * </p>
 *
 * @author lchan
 *
 */
public class JmsJmxConnectorClient implements JMXConnector {
  private static final String OPENED_TEXT = "Connection Opened";
  private static final String CLOSED_TEXT = "Connection Closed";

  private static enum ConnectionState {
    FRESH {
      @Override
      String notificationType() {
        return null;
      }

      @Override
      String notificationText() {
        return null;
      }
    },
    CONNECTED {
      @Override
      String notificationType() {
        return JMXConnectionNotification.OPENED;
      }

      @Override
      String notificationText() {
        return OPENED_TEXT;
      }
    },
    CLOSED {
      @Override
      String notificationType() {
        return JMXConnectionNotification.CLOSED;
      }

      @Override
      String notificationText() {
        return CLOSED_TEXT;
      }

    };
    abstract String notificationType();

    abstract String notificationText();
  }

  private static final Logger log = LoggerFactory.getLogger(JMXConnector.class);
  private AtomicLong notificationNumber = new AtomicLong();
  private transient NotificationBroadcasterSupport connectionNotifier = new NotificationBroadcasterSupport();
  private transient Map<String, ?> defaultEnvironment;
  private transient String destinationName;
  private transient JmsInvokerProxy proxy;
  private transient JmsMBeanServerConnectionClient client;
  private transient JmsJmxConnectionFactory factory;
  private transient JMXServiceURL serviceURL;
  private transient ConnectionState connectionState;

  private String connectionId;

  public JmsJmxConnectorClient(JMXServiceURL url, Map<String, ?> env, JmsJmxConnectionFactory factory) throws IOException {
    serviceURL = url;
    defaultEnvironment = env;
    this.factory = factory;
    connectionState = ConnectionState.FRESH;
  }

  /**
   * Establishes the connection to the connector server.
   *
   */
  @Override
  public void connect() throws IOException {
    if (connectionState == ConnectionState.CLOSED) {
      throw new IOException("connect() impossible after close");
    }
    if (connectionState != ConnectionState.CONNECTED) {
      try {
        connectionId = createConnectionId();
        proxy = factory.createInvokerProxy();
        proxy.afterPropertiesSet();
        client = new JmsMBeanServerConnectionClient((JmsMBeanServerConnection) proxy.getObject(), factory);
      }
      // catch (JMSException|URISyntaxException e) {
      catch (JMSException e) {
        throw new IOException(e.getMessage(), e);
      }
      connectionState = ConnectionState.CONNECTED;
      notify(connectionState);
    }
  }

  /**
   * Establishes the connection to the connector server.
   *
   * @param env the properties of the connection. It will be ignored.
   */
  @Override
  public void connect(Map env) throws IOException {
    connect();
  }

  @Override
  public MBeanServerConnection getMBeanServerConnection() {
    return client;
  }

  /**
   * Note that this is unsupported and will throw an {@link UnsupportedOperationException}.
   * 
   */
  @Override
  public MBeanServerConnection getMBeanServerConnection(Subject delegationSubject) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void close() throws IOException {
    if (connectionState != ConnectionState.CLOSED) {
      factory.destroy();
      connectionState = ConnectionState.CLOSED;
      notify(connectionState);
    }
  }

  @Override
  public void addConnectionNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback) {
    connectionNotifier.addNotificationListener(listener, filter, handback);
  }

  @Override
  public void removeConnectionNotificationListener(NotificationListener listener) throws ListenerNotFoundException {
    connectionNotifier.removeNotificationListener(listener);
  }

  @Override
  public void removeConnectionNotificationListener(NotificationListener l, NotificationFilter f, Object handback)
      throws ListenerNotFoundException {
    connectionNotifier.removeNotificationListener(l, f, handback);
  }

  @Override
  public String getConnectionId() {
    return connectionId;
  }

  private void notify(ConnectionState state) {
    if (getConnectionId() != null) {
      connectionNotifier.sendNotification(new JMXConnectionNotification(state.notificationType(), this, getConnectionId(),
          notificationNumber.incrementAndGet(), state.notificationText(), null));
    }
  }

  private String createConnectionId() throws IOException {
    String result = null;
    try {
      String protocol = serviceURL.getProtocol();
      String path = serviceURL.getURLPath();
      while (path.startsWith("/")) {
        path = path.substring(1);
      }
      result = protocol + "://" + JmsJmxConnectionFactoryImpl.removeQuery(new URI(path)) + " " + UUID.randomUUID().toString();
    }
    catch (URISyntaxException e) {
      throw new IOException(e);
    }
    return result;
  }
}