package com.adaptris.jmx.remote.jms;

import javax.jms.JMSException;

import org.apache.commons.lang.WordUtils;

/**
 * Factory class for creating {@link JmsInvokerProxy} instances.
 *
 * @author lchan
 *
 */
public abstract class JmsInvokerFactory {

  public static enum Factory {
    Queue {
      @Override
      JmsInvokerProxy<?> createInvoker(JmsJmxConnectionFactory factory) throws JMSException {
        return new JmsQueueInvokerProxy(factory, factory.getTargetDestination(), JmsMBeanServerConnection.class);
      }

      @Override
      ExtendedJmsInvokerServiceExporter<?> createServiceExporter() {
        return new QueueServiceExporter();
      }
    },
    Topic {
      @Override
      JmsInvokerProxy<?> createInvoker(JmsJmxConnectionFactory factory) throws JMSException {
        return new JmsTopicInvokerProxy(factory, factory.getTargetDestination(), JmsMBeanServerConnection.class);
      }

      @Override
      ExtendedJmsInvokerServiceExporter<?> createServiceExporter() {
        return new TopicServiceExporter();
      }
    };

    abstract JmsInvokerProxy<?> createInvoker(JmsJmxConnectionFactory factory) throws JMSException;

    abstract ExtendedJmsInvokerServiceExporter<?> createServiceExporter();

  }

  public static final String TYPE_QUEUE = Factory.Queue.name();
  public static final String TYPE_TOPIC = Factory.Topic.name();

  /**
   * Convenience method for creating a {@link JmsInvokerProxy} instance.
   *
   * @param factory
   *          the {@link JmsJmxConnectionFactory} instance.
   * @param type
   *          the type of {@link JmsInvokerProxy}
   * @return a {@link JmsInvokerProxy} instance.
   * @throws JMSException
   *           if there was a problem with JMS.
   * @see #TYPE_QUEUE
   * @see #TYPE_TOPIC
   * @see #createInvoker(JmsJmxConnectionFactory, Factory)
   */
  public static JmsInvokerProxy<?> createInvoker(JmsJmxConnectionFactory factory, String type) throws JMSException {
    return createInvoker(factory, Factory.valueOf(WordUtils.capitalizeFully(type)));
  }

  public static JmsInvokerProxy<?> createInvoker(JmsJmxConnectionFactory factory, Factory type) throws JMSException {
    return type.createInvoker(factory);
  }

  /**
   * Convenience method for creating a {@link ExtendedJmsInvokerServiceExporter} instance.
   *
   * @param type
   *          the type of {@link ExtendedJmsInvokerServiceExporter}
   * @return a {@link ExtendedJmsInvokerServiceExporter} instance.
   * @throws JMSException
   *           if there was a problem with JMS.
   * @see #TYPE_QUEUE
   * @see #TYPE_TOPIC
   * @see #createServiceExporter(Factory)
   */
  public static ExtendedJmsInvokerServiceExporter<?> createServiceExporter(String type) throws JMSException {
    return createServiceExporter(Factory.valueOf(WordUtils.capitalizeFully(type)));
  }

  public static ExtendedJmsInvokerServiceExporter<?> createServiceExporter(Factory type) throws JMSException {
    return type.createServiceExporter();
  }

}
