package com.adaptris.jmx.remote.jms;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.management.ListenerNotFoundException;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;

class JmsMBeanServerConnectionClient extends MBeanServerConnectionProxy implements MessageListener, ExceptionListener {
  private Destination replyToDestination;
  private JmsMBeanServerConnection serverConnection;
  private NotificationBroadcasterSupport localNotifier = new NotificationBroadcasterSupport();
  private List<ClientListenerEntry> listeners = new CopyOnWriteArrayList<ClientListenerEntry>();

  private JmsJmxConnectionFactory factory;

  public JmsMBeanServerConnectionClient(JmsMBeanServerConnection connection, JmsJmxConnectionFactory factory) throws JMSException {
    super(connection);
    serverConnection = connection;
    this.factory = factory;
    initConnection();
  }

  private void initConnection() throws JMSException {
    log.trace("JmsMBeanServerConnectionClient : Initialising Connection");
    Connection jmsC = factory.createConnection();
    jmsC.setExceptionListener(this);
    Session jmsSession = jmsC.createSession(false, Session.AUTO_ACKNOWLEDGE);
    jmsC.start();
    replyToDestination = factory.createTemporaryDestination(jmsSession);
    MessageConsumer consumer = jmsSession.createConsumer(replyToDestination);
    consumer.setMessageListener(this);
  }

  private List<ClientListenerEntry> purgeListeners() {
    log.debug("Discarding all notification listeners");
    List<ClientListenerEntry> oldListeners = new ArrayList(listeners);
    listeners = new CopyOnWriteArrayList<ClientListenerEntry>();
    for (ClientListenerEntry entry : oldListeners) {
      try {
        localNotifier.removeNotificationListener(entry.getListener());
      }
      catch (ListenerNotFoundException e) {
      }
    }
    localNotifier = new NotificationBroadcasterSupport();
    return oldListeners;
  }

  private void reconnectListeners(List<ClientListenerEntry> list) {
    log.debug("Adding all previously configured notification listeners");
    for (ClientListenerEntry entry : list) {
      doTraceLogging("ReconnectListeners", new String[]
      {
          "String", "ObjectName"
      }, new Object[]
      {
          entry.getId(), entry.getObjectName()
      });
      listeners.add(entry);
      localNotifier.addNotificationListener(entry.getListener(), entry.getFilter(), entry.getHandback());
      serverConnection.addJmsNotificationListener(entry.getId(), entry.getObjectName(), replyToDestination);
    }
  }

  @Override
  public void addNotificationListener(ObjectName name, NotificationListener listener, NotificationFilter filter, Object handback) {
    doTraceLogging("addNotificationListener", new String[]
    {
        "ObjectName", "NotificationListener", "NotificationFilter", "Object"
    }, new Object[]
    {
       name, listener, filter, handback
    });
    String id = UUID.randomUUID().toString().trim();
    ClientListenerEntry info = new ClientListenerEntry(id, name, listener, filter, handback);
    listeners.add(info);
    localNotifier.addNotificationListener(listener, filter, handback);
    serverConnection.addJmsNotificationListener(id, name, replyToDestination);
  }

  @Override
  public void removeNotificationListener(ObjectName name, NotificationListener listener) throws ListenerNotFoundException {
    doTraceLogging("removeNotificationListener", new String[]
    {
        "ObjectName", "NotificationListener"
    }, new Object[]
    {
        name, listener
    });
    for (Iterator i = listeners.iterator(); i.hasNext();) {
      ClientListenerEntry li = (ClientListenerEntry) i.next();
      if (li.getListener() == listener) {
        listeners.remove(li);
        serverConnection.removeJmsNotificationListener(li.getId());
      }
    }
    localNotifier.removeNotificationListener(listener);
  }

  @Override
  public void removeNotificationListener(ObjectName name, NotificationListener listener, NotificationFilter filter, Object handback)
      throws ListenerNotFoundException {
    doTraceLogging("removeNotificationListener", new String[]
    {
        "ObjectName", "NotificationListener", "NotificationFilter", "Object"
    }, new Object[]
    {
        name, listener, filter, handback
    });
    for (Iterator i = listeners.iterator(); i.hasNext();) {
      ClientListenerEntry li = (ClientListenerEntry) i.next();
      if (li.getObjectName() == name && li.getListener() == listener && li.getFilter() == filter && li.getHandback() == handback) {
        listeners.remove(li);
        serverConnection.removeJmsNotificationListener(li.getId());
      }
    }
    localNotifier.removeNotificationListener(listener, filter, handback);
  }

  @Override
  public void onMessage(Message msg) {
    ObjectMessage objMsg = (ObjectMessage) msg;
    try {
      Notification notification = (Notification) objMsg.getObject();
      localNotifier.sendNotification(notification);
    }
    catch (JMSException jmsEx) {
      log.error("Failed to send Notification", jmsEx);
    }
  }

  @Override
  public void onException(JMSException arg0) {
    try {
      List<ClientListenerEntry> oldListeners = purgeListeners();
      initConnection();
      reconnectListeners(oldListeners);
    }
    catch (JMSException e) {
      throw new RuntimeException(e);
    }
  }
}