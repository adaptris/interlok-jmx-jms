package com.adaptris.jmx.remote;

import java.util.ArrayList;
import java.util.List;

import javax.management.ListenerNotFoundException;
import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleNotificationBean extends NotificationBroadcasterSupport implements SimpleNotificationBeanMBean {

  private String name;
  private transient Logger log = LoggerFactory.getLogger(this.getClass());
  private transient List<NotificationListener> listeners = new ArrayList<>();

  public SimpleNotificationBean(String name) {
    super(new MBeanNotificationInfo(new String[] { name }, Notification.class.getName(), "Notification about " + name));
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void addNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback)
      throws IllegalArgumentException {
    listeners.add(listener);
    super.addNotificationListener(listener, filter, handback);
  }

  @Override
  public void removeNotificationListener(NotificationListener listener) throws ListenerNotFoundException {
    super.removeNotificationListener(listener);
  }

  @Override
  public void removeNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback)
      throws ListenerNotFoundException {
    super.removeNotificationListener(listener, filter, handback);
  }

  public List<NotificationListener> getListeners() {
    return new ArrayList<>(listeners);
  }

  @Override
  public void sendNotification(Notification notification) {
    log.trace("sendNotification [" + notification.getMessage() + "]");
    super.sendNotification(notification);
    log.trace("Notification Sent");
  }

}
