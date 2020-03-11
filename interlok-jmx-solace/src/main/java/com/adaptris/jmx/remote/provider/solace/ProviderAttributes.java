package com.adaptris.jmx.remote.provider.solace;


import javax.management.remote.JMXServiceURL;

public interface ProviderAttributes {

  /**
   * Key  that specifies the message VPN to be associated the underlying
   * {@link com.solacesystems.jms.SolConnectionFactory} .
   * @see #ENV_DEFAULT_MESSAGE_VPN
   */
  String ATTR_MESSAGE_VPN = "jmx.messageVPN";

  /**
   * Key in the initial attributes that specifies any additional broker urls.
   * <p>
   * The value in the initial attributes can by overriden by specifying this as part of the query portion of the
   * {@link JMXServiceURL}.
   * </p>
   */
  String ATTR_BACKUP_BROKER_URLS = "jmx.backupBrokers";

  /**
   * The default message VPN
   *
   */
  String ENV_DEFAULT_MESSAGE_VPN = "default";

  /**
   * The default Solace Username
   *
   */
  String ENV_DEFAULT_SOLACE_USERNAME = "default";
  /**
   * The default Solace Password.
   */
  String ENV_DEFAULT_SOLACE_PASSWORD = "";
}
