package com.adaptris.jmx.remote;

public interface SimpleManagementBeanMBean {

  String getName();

  int getCurrentCount();

  void incrementCount(int i);
}
