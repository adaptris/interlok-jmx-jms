package com.adaptris.jmx.remote.jms;

import java.io.IOException;
import java.util.Set;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServerConnection;
import javax.management.NotCompliantMBeanException;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.QueryExp;
import javax.management.ReflectionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class MBeanServerConnectionProxy implements MBeanServerConnection {
  protected transient final Logger log = LoggerFactory.getLogger(JmsMBeanServerConnection.class);

  protected MBeanServerConnection connection;

  public MBeanServerConnectionProxy(MBeanServerConnection connection) {
    this.connection = connection;
  }

  @Override
  public ObjectInstance createMBean(String className, ObjectName name) throws ReflectionException, InstanceAlreadyExistsException,
      MBeanRegistrationException, MBeanException, NotCompliantMBeanException, IOException {
    return connection.createMBean(className, name);
  }

  @Override
  public ObjectInstance createMBean(String className, ObjectName name, ObjectName loaderName) throws ReflectionException,
      InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException,
      InstanceNotFoundException, IOException {
    return connection.createMBean(className, name, loaderName);
  }

  @Override
  public ObjectInstance createMBean(String className, ObjectName name, Object[] params, String[] signature)
      throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException,
      NotCompliantMBeanException, IOException {
    return connection.createMBean(className, name, params, signature);
  }

  @Override
  public ObjectInstance createMBean(String className, ObjectName name, ObjectName loaderName, Object[] params, String[] signature)
      throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException,
      NotCompliantMBeanException, InstanceNotFoundException, IOException {
    return connection.createMBean(className, name, loaderName, params, signature);
  }

  @Override
  public void unregisterMBean(ObjectName name) throws InstanceNotFoundException, MBeanRegistrationException, IOException {
    connection.unregisterMBean(name);

  }

  @Override
  public ObjectInstance getObjectInstance(ObjectName name) throws InstanceNotFoundException, IOException {
    return connection.getObjectInstance(name);
  }

  @Override
  public Set<ObjectInstance> queryMBeans(ObjectName name, QueryExp query) throws IOException {
    return connection.queryMBeans(name, query);
  }

  @Override
  public Set<ObjectName> queryNames(ObjectName name, QueryExp query) throws IOException {
    return connection.queryNames(name, query);
  }

  @Override
  public boolean isRegistered(ObjectName name) throws IOException {
    return connection.isRegistered(name);
  }

  @Override
  public Integer getMBeanCount() throws IOException {
    return connection.getMBeanCount();
  }

  @Override
  public Object getAttribute(ObjectName name, String attribute) throws MBeanException, AttributeNotFoundException,
      InstanceNotFoundException, ReflectionException, IOException {
    return connection.getAttribute(name, attribute);
  }

  @Override
  public AttributeList getAttributes(ObjectName name, String[] attributes) throws InstanceNotFoundException, ReflectionException,
      IOException {
    return connection.getAttributes(name, attributes);
  }

  @Override
  public void setAttribute(ObjectName name, Attribute attribute) throws InstanceNotFoundException, AttributeNotFoundException,
      InvalidAttributeValueException, MBeanException, ReflectionException, IOException {
    connection.setAttribute(name, attribute);
  }

  @Override
  public AttributeList setAttributes(ObjectName name, AttributeList attributes) throws InstanceNotFoundException,
      ReflectionException, IOException {
    return connection.setAttributes(name, attributes);
  }

  @Override
  public Object invoke(ObjectName name, String operationName, Object[] params, String[] signature)
      throws InstanceNotFoundException, MBeanException, ReflectionException, IOException {
    return connection.invoke(name, operationName, params, signature);
  }

  @Override
  public String getDefaultDomain() throws IOException {
    return connection.getDefaultDomain();
  }

  @Override
  public String[] getDomains() throws IOException {
    return connection.getDomains();
  }

  @Override
  public void addNotificationListener(ObjectName name, NotificationListener listener, NotificationFilter filter, Object handback)
      throws InstanceNotFoundException, IOException {
    doTraceLogging("addNotificationListener", new String[]
    {
        "ObjectName", "NotificationListener", "NotificationFilter", "Object"
    }, new Object[]
    {
        name, listener, filter, handback
    });
    connection.addNotificationListener(name, listener, filter, handback);

  }

  @Override
  public void addNotificationListener(ObjectName name, ObjectName listener, NotificationFilter filter, Object handback)
      throws InstanceNotFoundException, IOException {
    doTraceLogging("addNotificationListener", new String[]
    {
        "ObjectName", "ObjectName", "NotificationFilter", "Object"
    }, new Object[]
    {
        name, listener, filter, handback
    });
    connection.addNotificationListener(name, listener, filter, handback);

  }

  @Override
  public void removeNotificationListener(ObjectName name, ObjectName listener) throws InstanceNotFoundException,
      ListenerNotFoundException, IOException {
    doTraceLogging("removeNotificationListener", new String[]
    {
        "ObjectName", "ObjectName"
    }, new Object[]
    {
        name, listener
    });
    connection.removeNotificationListener(name, listener);

  }

  @Override
  public void removeNotificationListener(ObjectName name, ObjectName listener, NotificationFilter filter, Object handback)
      throws InstanceNotFoundException, ListenerNotFoundException, IOException {
    doTraceLogging("removeNotificationListener", new String[]
    {
        "ObjectName", "ObjectName", "NotificationFilter", "Object"
    }, new Object[]
    {
        name, listener, filter, handback
    });
    connection.removeNotificationListener(name, listener, filter, handback);

  }

  @Override
  public void removeNotificationListener(ObjectName name, NotificationListener listener) throws InstanceNotFoundException,
      ListenerNotFoundException, IOException {
    doTraceLogging("removeNotificationListener", new String[]
    {
        "ObjectName", "NotificationListener"
    }, new Object[]
    {
        name, listener
    });
    connection.removeNotificationListener(name, listener);
  }

  @Override
  public void removeNotificationListener(ObjectName name, NotificationListener listener, NotificationFilter filter, Object handback)
      throws InstanceNotFoundException, ListenerNotFoundException, IOException {
    doTraceLogging("removeNotificationListener", new String[]
    {
        "ObjectName", "NotificationListener", "NotificationFilter", "Object"
    }, new Object[]
    {
        name, listener, filter, handback
    });
    connection.removeNotificationListener(name, listener, filter, handback);
  }

  @Override
  public MBeanInfo getMBeanInfo(ObjectName name) throws InstanceNotFoundException, IntrospectionException, ReflectionException,
      IOException {
    return connection.getMBeanInfo(name);
  }

  @Override
  public boolean isInstanceOf(ObjectName name, String className) throws InstanceNotFoundException, IOException {
    return connection.isInstanceOf(name, className);
  }

  protected void doTraceLogging(String func, String[] names, Object[] params) {
    if (log.isTraceEnabled()) {
      StringBuilder format = new StringBuilder(func);
      format.append("(");
      for (String s : names) {
        format.append(s);
        format.append("=[{}],");
      }
      format.deleteCharAt(format.length() - 1);
      format.append(")");
      log.trace(format.toString(), params);
    }
  }
  
}