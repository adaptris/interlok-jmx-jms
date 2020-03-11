package com.adaptris.jmx.remote.jms;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import javax.management.ListenerNotFoundException;
import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXServiceURL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.listener.SimpleMessageListenerContainer;

/**
 * Implementation of {@link JMXConnectorServer} that provides JMX connectivity via JMS.
 * <p>
 * As this implementation uses a JMS Provider to provide connectivity, clients aren't directly connected to this server; requests
 * automagically appear on the given {@link javax.jms.Destination} that this is registered against. As a result, you will find that
 * the standard {@link #getConnectionIds()} may return you an empty array as the corresponding
 * {@link #connectionClosed(String, String, Object)}, {@link #connectionOpened(String, String, Object)},
 * {@link #connectionFailed(String, String, Object)} methods are never called.
 * </p>
 *
 * @author lchan
 *
 */
public class JmsJmxConnectorServer extends JMXConnectorServer {
  private static enum ConnectionState {
    FRESH, STARTED, STOPPED;
  }

  private static final Logger log = LoggerFactory.getLogger(JmsJmxConnectorServer.class);
  private JMXServiceURL url;
  private final Map env;
  private String destinationName;
  private ConnectionState connectionState = ConnectionState.FRESH;
  private SimpleMessageListenerContainer listener;
  private JmsMBeanServerConnectionListener jmsServerConnection;
  private JmsJmxConnectionFactory factory;

  /**
   * Constructor.
   *
   * @param url the JMXServiceURL.
   * @param environment the initial environment for {@link #getAttributes()}
   * @param server the {@link MBeanServer} that we should be attached to.
   * @param factory the {@link JmsJmxConnectionFactory} that will used to connection to the JMS Provider.
   * @throws IOException wrapping any other exception.
   */
  public JmsJmxConnectorServer(JMXServiceURL url, Map environment, MBeanServer server, JmsJmxConnectionFactory factory) throws IOException {
    super(server);
    this.url = url;
    env = environment;
    this.factory = factory;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void start() throws IOException {
    if (connectionState == ConnectionState.STOPPED) {
      throw new IOException("start() after close() is not valid");
    }
    if (connectionState != ConnectionState.STARTED) {
      try {
        listener = new SimpleMessageListenerContainer();
        ExtendedJmsInvokerServiceExporter service = factory.createServiceExporter();
        jmsServerConnection = new JmsMBeanServerConnectionListener(getMBeanServer(), factory);
        service.setServiceInterface(JmsMBeanServerConnection.class);
        service.setService(jmsServerConnection);
        service.setMessageConverter(new XStreamMessageConverter());

        listener.setDestination(factory.getTargetDestination());
        listener.setMessageListener(service);
        listener.setConnectionFactory(factory);

        service.afterPropertiesSet();
        listener.start();
        connectionState = ConnectionState.STARTED;
      }
      catch (Exception e) {
        throw new IOException(e);
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void stop() throws IOException {
    if (connectionState != ConnectionState.STOPPED) {
      stop(listener);
      factory.destroy();
      connectionState = ConnectionState.STOPPED;
    }
  }

  private static void stop(SimpleMessageListenerContainer container) throws IOException {
    try {
      if (container != null) {
        container.stop();
      }
    }
    catch (org.springframework.jms.JmsException e) {
      throw new IOException(e);
    }
  }

  /**
   * {@inheritDoc}
   * <p>
   * Note that this will throw an UnsupportedOperationException
   * </p>
   */
  @Override
  public JMXConnector toJMXConnector(Map<String, ?> env) throws IOException {
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isActive() {
    return connectionState == ConnectionState.STARTED;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public JMXServiceURL getAddress() {
    return url;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Map getAttributes() {
    return Collections.unmodifiableMap(env);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void sendNotification(Notification notification) {
    log.trace("sendNotification [" + notification.getMessage() + "]");
    super.sendNotification(notification);
    log.trace("Notification Sent");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback)
      throws IllegalArgumentException {
    log.trace("Adding [" + listener + "][" + filter + "][" + handback + "]");
    super.addNotificationListener(listener, filter, handback);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void removeNotificationListener(NotificationListener listener) throws ListenerNotFoundException {
    log.trace("Removing [" + listener + "]");
    super.removeNotificationListener(listener);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void removeNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback)
      throws ListenerNotFoundException {
    log.trace("Removing [" + listener + "][" + filter + "][" + handback + "]");
    super.removeNotificationListener(listener, filter, handback);
  }
}