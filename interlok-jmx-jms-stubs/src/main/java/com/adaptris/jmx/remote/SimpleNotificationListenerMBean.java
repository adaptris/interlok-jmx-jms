package com.adaptris.jmx.remote;

import java.util.List;

import javax.management.Notification;

public interface SimpleNotificationListenerMBean {

  List<Notification> getNotifications();

}
