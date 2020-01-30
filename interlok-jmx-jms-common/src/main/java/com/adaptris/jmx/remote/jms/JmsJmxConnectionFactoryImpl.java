package com.adaptris.jmx.remote.jms;

import static com.adaptris.jmx.remote.jms.JmsHelper.closeQuietly;
import static com.adaptris.jmx.remote.jms.JmsHelper.deleteQuietly;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.jms.Connection;
import javax.jms.ConnectionConsumer;
import javax.jms.ConnectionFactory;
import javax.jms.ConnectionMetaData;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.ServerSessionPool;
import javax.jms.Session;
import javax.jms.TemporaryQueue;
import javax.jms.TemporaryTopic;
import javax.jms.Topic;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXServiceURL;

import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for {@link JmsJmxConnectionFactory} instances.
 * 
 * @author lchan
 * 
 */
public abstract class JmsJmxConnectionFactoryImpl implements JmsJmxConnectionFactory {

  protected enum DestinationFactory {
    Topic() {
      @Override
      public Destination create(JmsJmxConnectionFactoryImpl f, String ref) throws JMSException {
        return f.createTopicDestination(ref);
      }
    },
    Queue() {
      @Override
      public Destination create(JmsJmxConnectionFactoryImpl f, String ref) throws JMSException {
        return f.createQueueDestination(ref);
      }
    };
    public abstract Destination create(JmsJmxConnectionFactoryImpl f, String ref) throws JMSException;
  }

  protected static final Logger log = LoggerFactory.getLogger(JmsJmxConnectionFactory.class);

  protected transient Map<String, ?> initialEnvironment;
  protected transient Map<String, String> jmsEnvironment;

  protected transient JmxJmsConnection defaultConnection = null;
  protected transient DestinationFactory destinationFactory;
  protected transient Object lock = new Object();

  private transient JMXServiceURL serviceUrl;
  private transient Set<Connection> openedConnections = new HashSet<Connection>();
  private transient Set<TemporaryQueue> tempQueues = new HashSet<TemporaryQueue>();
  private transient Set<TemporaryTopic> tempTopics = new HashSet<TemporaryTopic>();

  protected JmsJmxConnectionFactoryImpl(Map<String, ?> env, JMXServiceURL url) throws IOException {
    validateProtocol(url.getProtocol());
    initialEnvironment = env;
    serviceUrl = url;
    jmsEnvironment = buildJmsEnvironment(initialEnvironment, createDefaultEnvironment());
    try {
      URI brokerUri = getBrokerURI(serviceUrl);
      jmsEnvironment.putAll(parseParameters(brokerUri));
      destinationFactory = DestinationFactory.valueOf(jmsEnvironment.get(ATTR_DESTINATION_TYPE));
      if (initialEnvironment.containsKey(JMXConnector.CREDENTIALS)) {
        String[] credentials = (String[]) initialEnvironment.get(JMXConnector.CREDENTIALS);
        jmsEnvironment.put(ATTR_BROKER_USERNAME, credentials[0]);
        jmsEnvironment.put(ATTR_BROKER_PASSWORD, credentials[1]);
      }
    }
    catch (URISyntaxException e) {
      throw new IOException(e);
    }
  }

  /**
   * Validate the protocol
   * 
   * @param protocol the protocol from the {@link JMXServiceURL}
   * @throws MalformedURLException if the protocol is invalid
   */
  protected void validateProtocol(String protocol) throws MalformedURLException {
    if (!validProtocols().contains(protocol)) {
      throw new MalformedURLException("Wrong protocol " + protocol + " for provider");
    }
  }

  protected abstract Collection<String> validProtocols();

  protected final Queue createQueueDestination(String qName) throws JMSException {
    return defaultConnection.currentSession().createQueue(qName);
  }

  protected final Topic createTopicDestination(String tName) throws JMSException {
    return defaultConnection.currentSession().createTopic(tName);
  }

  @Override
  public Destination createTemporaryDestination(Session s) throws JMSException {
    Destination destination = null;
    if (destinationFactory.equals(DestinationFactory.Topic)) {
      TemporaryTopic t = s.createTemporaryTopic();
      addTemporaryTopic(t);
      destination = t;
    }
    else {
      TemporaryQueue q = s.createTemporaryQueue();
      addTemporaryQueue(q);
      destination = q;
    }
    return destination;
  }

  protected JmxJmsConnection connect(ConnectionFactory factory, String loggingString) {
    JmxJmsConnection result = null;
    log.trace("Attempting connection to {}", loggingString);
    boolean useCredentials = jmsEnvironment.containsKey(JmsJmxConnectionFactory.ATTR_BROKER_USERNAME);
    try {
      boolean firstAttempt = true;
      boolean successful = false;
      do {
        try {
          Connection c = null;
          if (useCredentials) {
            c = factory.createConnection(jmsEnvironment.get(ATTR_BROKER_USERNAME), jmsEnvironment.get(ATTR_BROKER_PASSWORD));
          }
          else {
            c = factory.createConnection();
          }
          result = new JmxJmsConnection(c);
          successful = true;
        }
        catch (JMSException e) {
          successful = false;
          if (firstAttempt) {
            log.trace("Failed to make a connection; waiting to retry", e);
            firstAttempt = false;
          }
          else {
            log.trace("Failed to make a connection; waiting to retry");
          }
          TimeUnit.MILLISECONDS.sleep(NumberUtils.toLong(jmsEnvironment.get(ATTR_RETRY_INTERVAL_MS), DEFAULT_RETRY_INTERVAL_MS));
        }
      }
      while (!successful);
      log.trace("(Re)connected to {}", loggingString);
    }
    catch (InterruptedException e) {
    }
    return result;
  }

  protected Map<String, String> createDefaultEnvironment() {
    Map<String, String> env = new HashMap<String, String>();
    env.put(ATTR_DESTINATION_TYPE, DestinationFactory.Topic.name());
    env.put(ATTR_TIMEOUT_MS, String.valueOf(DEFAULT_TIMEOUT_MS));
    env.put(ATTR_RETRY_INTERVAL_MS, String.valueOf(DEFAULT_RETRY_INTERVAL_MS));
    // If you attempt to connect >1 with the same client-id then ActiveMQ barfs.
    // So, by default we leave the clientID unset.
    env.put(ATTR_DESTINATION, "com.adaptris.jmx.remote.jms." + UUID.randomUUID().toString().replaceAll(":", "").replaceAll("-", ""));
    return env;
  }

  protected Map<String, String> buildJmsEnvironment(Map<String, ?> initial, Map<String, String> defaultEnv) {
    Map<String, String> result = defaultEnv;
    for (Map.Entry<String, ?> e : initial.entrySet()) {
      if (e.getValue() instanceof String) {
        result.put(e.getKey(), (String) e.getValue());
      }
    }
    return result;
  }

  @Override
  public void destroy() {
    synchronized (tempQueues) {
      log.trace("Disposing of {} temporary queues", tempQueues.size());
      for (TemporaryQueue q : tempQueues) {
        deleteQuietly(q);
      }
    }
    synchronized (tempTopics) {
      log.trace("Disposing of {} temporary topics", tempTopics.size());
      for (TemporaryTopic q : tempTopics) {
        deleteQuietly(q);
      }
    }
    synchronized (openedConnections) {
      log.trace("Disposing of {} connections", openedConnections.size());
      for (Connection c : openedConnections) {
        closeQuietly(c, true);
      }
      openedConnections.clear();
    }
    synchronized (lock) {
      if (defaultConnection != null) {
        defaultConnection.destroy();
        defaultConnection = null;
      }
    }
  }

  @Override
  public JmsInvokerProxy createInvokerProxy() throws JMSException {
    JmsInvokerProxy result = JmsInvokerFactory.createInvoker(this, jmsEnvironment.get(ATTR_DESTINATION_TYPE));
    result.setReceiveTimeout(Long.parseLong(jmsEnvironment.get(ATTR_TIMEOUT_MS)));
    result.setBeanClassLoader(getClass().getClassLoader());
    return result;
  }

  @Override
  public ExtendedJmsInvokerServiceExporter createServiceExporter() throws JMSException {
    return JmsInvokerFactory.createServiceExporter(jmsEnvironment.get(ATTR_DESTINATION_TYPE));
  }

  @Override
  public Destination getTargetDestination() throws JMSException {
    createConnection();
    return destinationFactory.create(this, jmsEnvironment.get(ATTR_DESTINATION));
  }

  protected void addOpenedConnection(Connection c) {
    openedConnections.add(c);
  }

  protected void addTemporaryTopic(TemporaryTopic t) {
    tempTopics.add(t);
  }

  protected void addTemporaryQueue(TemporaryQueue t) {
    tempQueues.add(t);
  }

  protected URI getBrokerURI(JMXServiceURL serviceURL) throws URISyntaxException {
    String path = serviceURL.getURLPath();
    while (path.startsWith("/")) {
      path = path.substring(1);
    }
    return new URI(path);
  }

  public static Map<String, String> parseParameters(URI uri) throws URISyntaxException {
    return uri.getQuery() == null ? Collections.EMPTY_MAP : parseQuery(stripPrefix(uri.getQuery(), "?"));
  }

  protected static String rebuildQuery(Map<String, String> params, Set<String> ignoreKeys) throws URISyntaxException {
    StringBuilder query = new StringBuilder();
    boolean first = true;
    try {
      for (Map.Entry<String, String> entry : params.entrySet()) {
        if (!ignoreKeys.contains(entry.getKey())) {
          String name = URLEncoder.encode(entry.getKey(), "UTF-8");
          String value = URLEncoder.encode(entry.getValue(), "UTF-8");
          if (!first) {
            query.append("&");
          }
          query.append(name);
          query.append("=");
          query.append(value);
          first = false;
        }
      }
    }
    catch (UnsupportedEncodingException e) {
      throw (URISyntaxException) new URISyntaxException(e.toString(), "Invalid encoding").initCause(e);
    }
    return query.toString();
  }

  private static Map<String, String> parseQuery(String queryString) throws URISyntaxException {
    try {
      Map<String, String> rc = new HashMap<String, String>();
      if (queryString != null) {
        String[] parameters = queryString.split("&");
        for (int i = 0; i < parameters.length; i++) {
          int p = parameters[i].indexOf("=");
          if (p >= 0) {
            String name = URLDecoder.decode(parameters[i].substring(0, p), "UTF-8");
            String value = URLDecoder.decode(parameters[i].substring(p + 1), "UTF-8");
            rc.put(name, value);
          }
          else {
            rc.put(parameters[i], null);
          }
        }
      }
      return rc;
    }
    catch (UnsupportedEncodingException e) {
      throw (URISyntaxException) new URISyntaxException(e.toString(), "Invalid encoding").initCause(e);
    }
  }

  /**
   * Removes any URI query from the given uri
   */
  public static URI removeQuery(URI uri) throws URISyntaxException {
    return newURI(uri, null);
  }


  protected static URI maskUserInfo(URI uri) throws URISyntaxException {
    return new URI(uri.getScheme(), null, uri.getHost(), uri.getPort(), uri.getPath(), uri.getQuery(), uri.getFragment());
  }

  protected static URI newURI(URI uri, String query) throws URISyntaxException {
    return new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), uri.getPath(), query, uri.getFragment());
  }

  protected static JMSException wrapJmsException(Throwable e) {
    if (e instanceof JMSException) {
      return (JMSException) e;
    }
    return (JMSException) new JMSException(e.getMessage()).initCause(e);
  }

  private static String stripPrefix(String value, String prefix) {
    if (value.startsWith(prefix)) {
      return value.substring(prefix.length());
    }
    return value;
  }

  private class NotifyingExceptionListener implements ExceptionListener {

    private Set<ExceptionListener> listeners = new HashSet<ExceptionListener>();

    NotifyingExceptionListener() {
    }

    @Override
    public void onException(JMSException arg0) {
      // First of all destroy references.
      destroy();
      // Now notify all our children.
      for (ExceptionListener el : listeners) {
        el.onException(arg0);
      }
      listeners.clear();
    }

    public void addListener(ExceptionListener listener) {
      listeners.add(listener);
    }
  }

  protected class JmxJmsConnection implements Connection {
    private Connection connection;
    private NotifyingExceptionListener masterExceptionListener = new NotifyingExceptionListener();
    private Session baseSession;

    public JmxJmsConnection(Connection c) throws JMSException {
      connection = c;
      connection.setExceptionListener(masterExceptionListener);
      baseSession = c.createSession(false, Session.AUTO_ACKNOWLEDGE);
    }

    public void destroy() {
      closeQuietly(baseSession);
      closeQuietly(connection, true);
    }

    public Session currentSession() {
      return baseSession;
    }

    @Override
    public void close() throws JMSException {
      // don't bother, it'll be done in destroy();

    }

    @Override
    public ConnectionConsumer createConnectionConsumer(Destination arg0, String arg1, ServerSessionPool arg2, int arg3)
        throws JMSException {
      return connection.createConnectionConsumer(arg0, arg1, arg2, arg3);
    }

    @Override
    public ConnectionConsumer createDurableConnectionConsumer(Topic arg0, String arg1, String arg2, ServerSessionPool arg3, int arg4)
        throws JMSException {
      return connection.createDurableConnectionConsumer(arg0, arg1, arg2, arg3, arg4);
    }

    @Override
    public Session createSession(boolean arg0, int arg1) throws JMSException {
      return connection.createSession(arg0, arg1);
    }

    @Override
    public String getClientID() throws JMSException {
      return connection.getClientID();
    }

    @Override
    public ExceptionListener getExceptionListener() throws JMSException {
      return connection.getExceptionListener();
    }

    @Override
    public ConnectionMetaData getMetaData() throws JMSException {
      return connection.getMetaData();
    }

    @Override
    public void setClientID(String arg0) throws JMSException {
      connection.setClientID(arg0);
    }

    @Override
    public void setExceptionListener(ExceptionListener arg0) throws JMSException {
      // We shall not set you as our exceptionlistener; I shall simply add you to the list.
      masterExceptionListener.addListener(arg0);
    }

    @Override
    public void start() throws JMSException {
      connection.start();
    }

    @Override
    public void stop() throws JMSException {
      // don't bother, it'll be done in destroy();
    }

  }

}
