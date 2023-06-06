package com.adaptris.jmx.remote;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.management.Notification;
import javax.management.NotificationListener;

public class SimpleNotificationListener implements NotificationListener, Serializable, SimpleNotificationListenerMBean {
  private static final long MAX_WAIT = 65000;
  private static final int DEFAULT_WAIT_INTERVAL = 100;
  private static final long serialVersionUID = 2013092601L;

  private List<Notification> notifications = new ArrayList<>();

  @Override
  public void handleNotification(Notification notification, Object handback) {
    notifications.add(notification);
  }

  @Override
  public List<Notification> getNotifications() {
    return new ArrayList<>(notifications);
  }

  public void waitForMessages(int count) throws Exception {
    long totalWaitTime = 0;
    while (getNotifications().size() < count && totalWaitTime < MAX_WAIT) {
      Thread.sleep(DEFAULT_WAIT_INTERVAL);
      totalWaitTime += DEFAULT_WAIT_INTERVAL;
    }
  }

}
