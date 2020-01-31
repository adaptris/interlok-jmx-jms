package com.adaptris.jmx.remote.jms;

import static com.adaptris.jmx.remote.jms.JmsHelper.closeQuietly;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ServerListenerEntry implements NotificationListener {
  private static final Logger log = LoggerFactory.getLogger("com.adaptris.jmx.remote.jmx.NotificationListener");
  private Destination replyTo;
  private Session session;
  private MessageProducer producer;
  private ObjectName objectName;
  private String id;

  ServerListenerEntry(String id, ObjectName objectName, Destination replyTo, Session session) throws JMSException {
    this.id = id;
    this.objectName = objectName;
    this.replyTo = replyTo;
    this.session = session;
    producer = session.createProducer(replyTo);
  }

  @Override
  public void handleNotification(Notification notification, Object handback) {
    try {
      log.trace("Handling notification from [{}]", notification.getSource());
      ObjectMessage msg = session.createObjectMessage(notification);
      producer.send(msg);
      log.trace("Sent notification to [{}", replyTo);
    }
    catch (Exception e) {
      // eat the exception because notifications aren't that important...
      // log.error("Failed to handle notification: " + notification, e);
    }
  }

  public void close() {
    closeQuietly(producer);
  }

  public ObjectName getObjectName() {
    return objectName;
  }
}