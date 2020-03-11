package com.adaptris.jmx.remote.jms;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

class JmsMBeanServerConnectionListener extends MBeanServerConnectionProxy implements JmsMBeanServerConnection, ExceptionListener {

  private transient Session jmsSession;
  private transient Map<String, ServerListenerEntry> notificationListeners = new ConcurrentHashMap();
  private transient JmsJmxConnectionFactory factory;
  private transient Object lock = new Object();

  public JmsMBeanServerConnectionListener(MBeanServerConnection connection, JmsJmxConnectionFactory factory) throws JMSException {
    super(connection);
    this.factory = factory;
    initConnection();
  }

  private void initConnection() throws JMSException {
    log.trace("JmsMBeanServerConnectionListener : Initialising Connection");
    synchronized (lock) {
      Connection jmsC = factory.createConnection();
      jmsC.setExceptionListener(this);
      jmsC.start();
      jmsSession = jmsC.createSession(false, Session.AUTO_ACKNOWLEDGE);
    }
  }

  private void removeAllListeners() {
    log.trace("Removing All Listeners during JMS Failure");
    synchronized (notificationListeners) {
      for (Map.Entry<String, ServerListenerEntry> entry : notificationListeners.entrySet()) {
        ServerListenerEntry l = entry.getValue();
        removeNotificationListenerQuietly(l);
        l.close();
      }
      notificationListeners.clear();
    }
    log.trace("All Listeners removed");
  }

  @Override
  public void addJmsNotificationListener(String listenerId, ObjectName name, Destination replyToDestination) {
    doTraceLogging("addJmsNotificationListener", new String[]
    {
        "String", "ObjectName", "Destination"
    }, new Object[]
    {
        listenerId, name, replyToDestination
    });
    try {
      synchronized (lock) {
        ServerListenerEntry info = new ServerListenerEntry(listenerId, name, replyToDestination, jmsSession);
        notificationListeners.put(listenerId, info);
        connection.addNotificationListener(name, info, null, null);
      }
    }
    catch (JMSException e) {
      waitQuietly(2);
      doTraceLogging("Retrying addJmsNotificationListener", new String[]
      {
          "String", "ObjectName", "Destination"
      }, new Object[]
      {
          listenerId, name, replyToDestination
      });
      addJmsNotificationListener(listenerId, name, replyToDestination);
    }
    catch (Exception e) {
      log.error("Failed to addNotificationListener ", e);
    }
  }

  private void waitQuietly(long seconds) {
    try {
      TimeUnit.SECONDS.sleep(seconds);
    }
    catch (InterruptedException e) {

    }
  }

  @Override
  public void removeJmsNotificationListener(String listenerId) {
    ServerListenerEntry info = notificationListeners.remove(listenerId);
    if (info != null) {
      doTraceLogging("removeJmsNotificationListener", new String[]
      {
          "String"
      }, new Object[]
      {
          listenerId
      });
      info.close();
      removeNotificationListenerQuietly(info);
    }
  }

  private void removeNotificationListenerQuietly(ServerListenerEntry entry) {
    try {
      if (entry != null) {
        connection.removeNotificationListener(entry.getObjectName(), entry);
      }
    }
    catch (Exception e) {
    }
  }

  @Override
  public void onException(JMSException arg0) {
    try {
      removeAllListeners();
      initConnection();
    }
    catch (JMSException e) {
      throw new RuntimeException(e);
    }

  }
}