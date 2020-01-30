package com.adaptris.jmx.remote.jms;

import org.junit.After;
import org.junit.Before;
import com.adaptris.jmx.remote.BaseCase;

public abstract class ActiveMqBaseCase extends BaseCase {
  public static final String JMX_URL_PREFIX = "service:jmx:activemq:///";
  public static final String JMX_URL_SUFFIX_QUEUE = "?jmx.type=Queue&jmx.destination=Junit_Queue";
  public static final String JMX_URL_SUFFIX_TOPIC = "?jmx.type=Topic&jmx.destination=Junit_Topic";
  protected EmbeddedActiveMq broker;

  public ActiveMqBaseCase() {
  }

  @Before
  public void setUp() throws Exception {
    broker = new EmbeddedActiveMq();
    broker.start();
  }

  @After
  public void tearDown() throws Exception {
    broker.destroy();
  }

}
