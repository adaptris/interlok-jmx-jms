package com.adaptris.jmx.remote.jms;

import static com.adaptris.jmx.remote.jms.JmsHelper.closeQuietly;

import java.util.concurrent.TimeUnit;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageFormatException;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jms.remoting.JmsInvokerClientInterceptor;
import org.springframework.jms.remoting.JmsInvokerProxyFactoryBean;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.remoting.RemoteAccessException;
import org.springframework.remoting.RemoteInvocationFailureException;
import org.springframework.remoting.support.DefaultRemoteInvocationFactory;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.RemoteInvocationFactory;
import org.springframework.remoting.support.RemoteInvocationResult;
import org.springframework.util.ClassUtils;

/**
 * {@link org.aopalliance.intercept.MethodInterceptor} for accessing a JMS-based remote service.
 *
 * <p>
 * Serializes remote invocation objects and deserializes remote invocation result objects. Uses Java serialization just like RMI,
 * but with the JMS provider as communication infrastructure.
 * </p>
 * <p>
 * This class is effectively a copy of {@link JmsInvokerProxyFactoryBean} and {@link JmsInvokerClientInterceptor} with the Queue
 * support abstracted as JMS1.1 destination. Concrete sub-classes will allow you to optionally a {@link javax.jms.Queue} or
 * {@link javax.jms.Topic} as your JMS delivery mechanism.
 * </p>
 */
// Yes it's a copy of JmsInvokerProxyFactoryBean + JmsInvokerClientInterceptor from Spring-JMS; but that uses QUEUES, not TOPICS...
abstract class JmsInvokerProxy<S extends Destination> implements MethodInterceptor, InitializingBean, FactoryBean<Object>,
    BeanClassLoaderAware {


  // need to have a fake category, as the classes are obfuscated.
  protected transient final Logger log = LoggerFactory.getLogger("com.adaptris.jmx.remote.jms.JmsInvoker");
  protected JmsJmxConnectionFactory connectionFactory;
  private Destination jmsDestination;
  private RemoteInvocationFactory remoteInvocationFactory = new DefaultRemoteInvocationFactory();
  private MessageConverter messageConverter = new XStreamMessageConverter();
  private long receiveTimeout = TimeUnit.SECONDS.toMillis(60L);
  private Class serviceInterface;
  private ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();
  private Object serviceProxy;

  protected JmsInvokerProxy(JmsJmxConnectionFactory cf, Destination d) {
    connectionFactory = cf;
    jmsDestination = d;
  }

  protected JmsInvokerProxy(JmsJmxConnectionFactory cf, Destination d, Class serviceInterface) {
    this(cf, d);
    setServiceInterface(serviceInterface);
  }

  protected ConnectionFactory getConnectionFactory() {
    return connectionFactory;
  }

  /**
   * Set the timeout to use for receiving the response message for a request (in milliseconds).
   *
   * @param receiveTimeout the timeout in milliseconds (default is 60seconds).
   * @see javax.jms.MessageConsumer#receive(long)
   * @see javax.jms.MessageConsumer#receive()
   */
  public void setReceiveTimeout(long receiveTimeout) {
    this.receiveTimeout = receiveTimeout;
  }

  /**
   * Return the timeout to use for receiving the response message for a request (in milliseconds).
   */
  protected long getReceiveTimeout() {
    return receiveTimeout;
  }

  @Override
  public void afterPropertiesSet() {
    // if (getConnectionFactory() == null) {
    // throw new IllegalArgumentException("Property 'connectionFactory' is required");
    // }
    // if (jmsDestination == null) {
    // throw new IllegalArgumentException("'destination' is required");
    // }
    // if (serviceInterface == null) {
    // throw new IllegalArgumentException("Property 'serviceInterface' is required");
    // }
    serviceProxy = new ProxyFactory(serviceInterface, this).getProxy(beanClassLoader);
  }

  @Override
  public Object invoke(MethodInvocation methodInvocation) throws Throwable {
    if (AopUtils.isToStringMethod(methodInvocation.getMethod())) {
      return "JMS invoker proxy for queue [" + jmsDestination + "]";
    }
    RemoteInvocation invocation = createRemoteInvocation(methodInvocation);
    RemoteInvocationResult result;
    try {
      result = executeRequest(invocation);
    }
    catch (JMSException ex) {
      throw convertJmsInvokerAccessException(ex);
    }
    try {
      return recreateRemoteInvocationResult(result);
    }
    catch (Throwable ex) {
      if (result.hasInvocationTargetException()) {
        throw ex;
      }
      else {
        throw new RemoteInvocationFailureException("Invocation of method [" + methodInvocation.getMethod()
            + "] failed in JMS invoker remote service at queue [" + jmsDestination + "]", ex);
      }
    }
  }

  protected RemoteInvocation createRemoteInvocation(MethodInvocation methodInvocation) {
    return remoteInvocationFactory.createRemoteInvocation(methodInvocation);
  }

  protected RemoteInvocationResult executeRequest(RemoteInvocation invocation) throws JMSException {
    logRemoteInvocation(invocation);
    Connection con = createConnection();
    Session session = null;
    try {
      session = createSession(con);
      Destination queueToUse = jmsDestination;
      Message requestMessage = createRequestMessage(session, invocation);
      con.start();
      Message responseMessage = doExecuteRequest(session, queueToUse, requestMessage);
      return extractInvocationResult(responseMessage);
    }
    finally {
      closeQuietly(session);
      closeQuietly(con, true);
    }
  }

  private void logRemoteInvocation(RemoteInvocation invocation) {
    if (log.isTraceEnabled()) {
      Object[] args = invocation.getArguments();
      StringBuilder logging = new StringBuilder("RemoteInvocation: ");
      logging.append(invocation.getMethodName());
      logging.append("(");
      for (int i = 0; i < args.length; i++) {
        logging.append("arg" + i);
        logging.append("=[{}],");
      }
      logging.deleteCharAt(logging.length() - 1);
      logging.append(")");
      log.trace(logging.toString(), args);
    }
  }

  protected Connection createConnection() throws JMSException {
    return getConnectionFactory().createConnection();
  }

  protected Session createSession(Connection con) throws JMSException {
    return con.createSession(false, Session.AUTO_ACKNOWLEDGE);
  }

  protected Message createRequestMessage(Session session, RemoteInvocation invocation) throws JMSException {
    return messageConverter.toMessage(invocation, session);
  }

  protected Message doExecuteRequest(Session session, Destination dest, Message requestMessage) throws JMSException {
    S replyToDestination = null;
    MessageProducer producer = null;
    MessageConsumer consumer = null;
    try {
      replyToDestination = createTemporaryDestination(session);
      addReplyTo(requestMessage, replyToDestination);
      producer = session.createProducer(dest);
      consumer = session.createConsumer(replyToDestination);
      requestMessage.setJMSReplyTo(replyToDestination);
      producer.send(requestMessage);
      long timeout = getReceiveTimeout();
      return timeout > 0 ? consumer.receive(timeout) : consumer.receive();
    }
    finally {
      closeQuietly(consumer);
      closeQuietly(producer);
      deleteTemporaryDestination(replyToDestination);
    }
  }

  protected abstract S createTemporaryDestination(Session s) throws JMSException;

  protected abstract void deleteTemporaryDestination(S tmpDest) throws JMSException;

  protected abstract void addReplyTo(Message requestMessage, S tmpDest) throws JMSException;

  protected RemoteInvocationResult extractInvocationResult(Message responseMessage) throws JMSException {
    Object content = messageConverter.fromMessage(responseMessage);
    if (content instanceof RemoteInvocationResult) {
      return (RemoteInvocationResult) content;
    }
    return onInvalidResponse(responseMessage);
  }

  protected RemoteInvocationResult onInvalidResponse(Message responseMessage) throws JMSException {
    throw new MessageFormatException("Invalid response message: " + responseMessage);
  }

  protected Object recreateRemoteInvocationResult(RemoteInvocationResult result) throws Throwable {
    return result.recreate();
  }

  protected RemoteAccessException convertJmsInvokerAccessException(JMSException ex) {
    log.error("Encountered a JMS Exception : " + ex.getMessage(), ex);
    throw new RemoteAccessException("Could not access JMS Destination [" + jmsDestination + "]", ex);
  }

  /**
   * Set the interface that the proxy must implement.
   *
   * @param serviceInterface the interface that the proxy must implement
   * @throws IllegalArgumentException if the supplied <code>serviceInterface</code> is <code>null</code>, or if the supplied
   *           <code>serviceInterface</code> is not an interface type
   */
  public void setServiceInterface(Class serviceInterface) {
    if (serviceInterface == null || !serviceInterface.isInterface()) {
      throw new IllegalArgumentException("'serviceInterface' must be an interface");
    }
    this.serviceInterface = serviceInterface;
  }

  @Override
  public void setBeanClassLoader(ClassLoader classLoader) {
    beanClassLoader = classLoader;
  }

  @Override
  public Object getObject() {
    return serviceProxy;
  }

  @Override
  public Class<?> getObjectType() {
    return serviceInterface;
  }

  @Override
  public boolean isSingleton() {
    return true;
  }

}