package com.adaptris.jmx.remote.jms;

import javax.jms.Destination;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

/**
 * Interface with additional methods for supporting {@link MBeanServerConnection} over JMS.
 * 
 * @author lchan
 * 
 */
public interface JmsMBeanServerConnection extends MBeanServerConnection {

  /**
   * Add a notification listener for a given MBean
   *
   * @param listenerId the uniqueId for the listener.
   * @param name The name of the MBean on which a listener has been added.
   * @param replyToDestination the JMS Destination where notifications will be sent.
   * @see #removeJmsNotificationListener(String)
   */
  void addJmsNotificationListener(String listenerId, ObjectName name, Destination replyToDestination);

  /**
   * Remove a notification listener.
   *
   * @param listenerId the listenerId.
   */
  void removeJmsNotificationListener(String listenerId);

}