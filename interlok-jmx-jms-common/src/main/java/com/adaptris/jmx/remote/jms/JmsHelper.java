package com.adaptris.jmx.remote.jms;

import javax.jms.Connection;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TemporaryQueue;
import javax.jms.TemporaryTopic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class JmsHelper {
  private static final Logger logger = LoggerFactory.getLogger(JmsHelper.class);

  /**
   * Delete a {@link TemporaryQueue} without logging any errors.
   *
   * @param q the queue.
   */
  public static void deleteQuietly(TemporaryQueue q) {
    if (q == null) {
      return;
    }
    try {
      q.delete();
    }
    catch (Exception e) {

    }
  }

  /**
   * Delete a {@link TemporaryTopic} without logging any errors.
   *
   * @param t the topic
   */
  public static void deleteQuietly(TemporaryTopic t) {
    if (t == null) {
      return;
    }
    try {
      t.delete();
    }
    catch (Exception e) {

    }
  }

  /**
   * Close a {@link Connection} without logging any errors or stopping the connection first.
   *
   * @param con the queue.
   * @see #closeQuietly(Connection, boolean)
   */
  public static void closeQuietly(Connection con) {
    closeQuietly(con, false);
  }

  /**
   * Close a {@link Connection} without logging any errors.
   *
   * @param con the queue.
   * @param stopFirst whether or not to stop the connection first.
   */
  public static void closeQuietly(Connection con, boolean stopFirst) {
    if (con == null) {
      return;
    }
    try {
      if (stopFirst) {
        stopQuietly(con);
      }
      con.close();
    }
    catch (Exception ex) {
    }
  }

  private static void stopQuietly(Connection con) {
    try {
      con.stop();
    }
    catch (Exception e) {
    }
  }

  /**
   * Close a {@link Session} without logging any errors.
   *
   * @param session the session.
   */
  public static void closeQuietly(Session session) {
    if (session == null) {
      return;
    }
    try {
      session.close();
    }
    catch (Exception ex) {
    }
  }

  /**
   * Close a {@link MessageProducer} without logging any errors.
   *
   * @param producer the producer.
   */
  public static void closeQuietly(MessageProducer producer) {
    if (producer == null) {
      return;
    }
    try {
      producer.close();
    }
    catch (Exception ex) {
    }
  }

  /**
   * Close a {@link MessageConsumer} without logging any errors.
   *
   * @param consumer the consumer.
   */
  public static void closeQuietly(MessageConsumer consumer) {
    if (consumer == null) {
      return;
    }
    boolean wasInterrupted = Thread.interrupted();
    try {
      consumer.close();
    }
    catch (Exception ex) {
    }
    finally {
      if (wasInterrupted) {
        // Reset the interrupted flag as it was before.
        Thread.currentThread().interrupt();
      }
    }
  }
}
