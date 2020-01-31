package com.adaptris.jmx.remote;

import java.util.UUID;

import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.ObjectName;

public class SimpleManagementBean implements SimpleManagementBeanMBean, MBeanRegistration {

  private String name;
  private int count = 0;

  public SimpleManagementBean() {
    super();
    name = createSafeUniqueId();
  }

  public SimpleManagementBean(String name) {
    this();
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public int getCurrentCount() {
    return count;
  }

  @Override
  public void incrementCount(int i) {
    count += i;
  }

  @Override
  public ObjectName preRegister(MBeanServer server, ObjectName name) throws Exception {
    if (name == null) {
      return ObjectName.getInstance("JmxJms:name=" + getName());
    }
    return name;
  }

  @Override
  public void postRegister(Boolean registrationDone) {
  }

  @Override
  public void preDeregister() throws Exception {
  }

  @Override
  public void postDeregister() {
  }

  private static String createSafeUniqueId() {
    return UUID.randomUUID().toString().replaceAll(":", "").replaceAll("-", "");
  }
}
