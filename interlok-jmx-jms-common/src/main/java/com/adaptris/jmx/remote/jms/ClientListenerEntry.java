package com.adaptris.jmx.remote.jms;

import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;

class ClientListenerEntry {
  private String id;
  private NotificationListener listener;
  private NotificationFilter filter;
  private Object handback;
  private ObjectName objectName;

  public ClientListenerEntry(String id, ObjectName objectName, NotificationListener listener, NotificationFilter filter,
                             Object handback) {
    this.id = id;
    this.objectName = objectName;
    this.listener = listener;
    this.filter = filter;
    this.handback = handback;
  }

  public NotificationFilter getFilter() {
    return filter;
  }

  public Object getHandback() {
    return handback;
  }

  public String getId() {
    return id;
  }

  public NotificationListener getListener() {
    return listener;
  }

  public ObjectName getObjectName() {
    return objectName;
  }

}