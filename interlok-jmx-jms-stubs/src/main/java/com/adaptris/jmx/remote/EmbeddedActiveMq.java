package com.adaptris.jmx.remote;


import java.net.URI;
import java.util.UUID;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.store.memory.MemoryPersistenceAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmbeddedActiveMq {

  private static final String AMQP_URL_PREFIX = "amqp://localhost:";
  private static final String OPENWIRE_URL_PREFIX = "tcp://localhost:";
  private static final String DEFAULT_URL_SUFFIX = "?maximumConnections=1000&wireFormat.maxInactivityDuration=0";

  private static Logger log = LoggerFactory.getLogger(EmbeddedActiveMq.class);
  private BrokerService broker = null;
  private Integer amqpPort;
  private Integer openwirePort;
  private String amqpConnectorURI;
  private String openwireConnectorURI;;
  private String brokerName;

  static {
    // @see http://activemq.apache.org/objectmessage.html
    System.setProperty("org.apache.activemq.SERIALIZABLE_PACKAGES", "*");
  }

  public EmbeddedActiveMq() throws Exception {
    brokerName = createSafeUniqueId();
    amqpPort = PortManager.nextUnusedPort(5672);
    openwirePort = PortManager.nextUnusedPort(61616);
    openwireConnectorURI = OPENWIRE_URL_PREFIX + openwirePort + DEFAULT_URL_SUFFIX;
    amqpConnectorURI = AMQP_URL_PREFIX + amqpPort + DEFAULT_URL_SUFFIX;
  }

  public String getName() {
    return brokerName;
  }

  public void start() throws Exception {
    broker = createBroker();
    broker.start();
    while (!broker.isStarted()) {
      Thread.sleep(100);
    }
  }

  public BrokerService createBroker() throws Exception {
    BrokerService br = new BrokerService();
    br.setBrokerName(brokerName);
    br.addConnector(new URI(amqpConnectorURI));
    br.addConnector(new URI(openwireConnectorURI));
    br.setUseJmx(false);
    br.setDeleteAllMessagesOnStartup(true);
    br.setPersistent(false);
    br.setPersistenceAdapter(new MemoryPersistenceAdapter());
    return br;
  }

  public void destroy() throws Exception {
    new Thread(new Runnable() {

      @Override
      public void run() {
        try {
          stop();
          PortManager.release(amqpPort);
          PortManager.release(openwirePort);
        }
        catch (Exception e) {

        }
      }
    }).start();
  }

  public void stop() throws Exception {
    if (broker != null) {
      broker.stop();
      broker.waitUntilStopped();
    }
  }

  public String getBrokerUrl() {
    return OPENWIRE_URL_PREFIX + openwirePort;
  }

  public String getAmqpBrokerUrl() {
    return AMQP_URL_PREFIX + amqpPort;
  }


  public static String createSafeUniqueId() {
    return UUID.randomUUID().toString().replaceAll(":", "").replaceAll("-", "");
  }
}
