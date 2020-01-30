# interlok-jmx-jms [![GitHub tag](https://img.shields.io/github/tag/adaptris/interlok-jmx-jms.svg)](https://github.com/adaptris/interlok-jmx-jms/tags) [![Build Status](https://travis-ci.com/adaptris/interlok-jmx-jms.svg?branch=develop)](https://travis-ci.com/adaptris/interlok-jmx-jms) [![CircleCI](https://circleci.com/gh/adaptris/interlok-jmx-jms/tree/develop.svg?style=svg)](https://circleci.com/gh/adaptris/interlok-jmx-jms/tree/develop) [![Actions Status](https://github.com/adaptris/interlok-jmx-jms/workflows/Java%20CI/badge.svg)](https://github.com/adaptris/interlok-jmx-jms/actions) [![codecov](https://codecov.io/gh/adaptris/interlok-jmx-jms/branch/develop/graph/badge.svg)](https://codecov.io/gh/adaptris/interlok-jmx-jms) [![Total alerts](https://img.shields.io/lgtm/alerts/g/adaptris/interlok-jmx-jms.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/adaptris/interlok-jmx-jms/alerts/) [![Language grade: Java](https://img.shields.io/lgtm/grade/java/g/adaptris/interlok-jmx-jms.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/adaptris/interlok-jmx-jms/context:java)


This project allows you to switch from the default `jmxmp` JMX implementation (which has vulnerabilities) to one that layers JMX operations over JMS.

## ActiveMQ ##

To switch to using ActiveMQ as the JMX transport mechanism we simply start using `activemq` instead of `jmxmp` as the protocol part of the _JMXServiceURL_ along with a URL that specifies the connection information for the ActiveMQ broker. Configuration for ActiveMQ tends to specified completely via the URL, so anything that would be valid as part of an ActiveMQ URL tends to be valid as part of the _JMXServiceURL_. So our bootstrap.properties might end up looking like this:


```
adapterConfigUrl=file://localhost/./config/adapter.xml
managementComponents=jmx:jetty
jmxserviceurl=service:jmx:activemq:///tcp://localhost:61616?jmx.type=Topic&jmx.destination=jmxTopic
# Not required because everything is in the url.
# jmxserviceurl.env.jmx.type=Topic
# jmxserviceurl.env.jmx.destination=jmxTopic
```

The full list of supported properties that control JMX behaviour are as follows :

| Name | Description |
|----|----|
|jmx.type|The destination type (i.e. _Topic_ or _Queue_; case-sensitive); defaults to Topic. |
|jmx.destination|The name of a Topic or Queue; if not assigned, then a unique one will be created to avoid exceptions; this is, though, pointless from a usability perspective |
|jmx.brokerUser| 	The username to connect to the broker (if required); defaults to null. This may be a redundant; as you can often configure the username directly on the URL|
|jmx.brokerPassword|The password to connect to the broker (if required); defaults to null. This is likely to be redundant; as you can often configure the password directly on the URL.|
|jmx.timeout|The timeout in milliseconds for a client to wait for a reply after sending a request; defaults to 60000.|
|jmx.clientid|The client ID to be associated with the underlying `ActiveMQConnectionFactory` if desired; defaults to null.|


All keys are case sensitive, and if specified in the URL will be stripped before being passed to `ActiveMQConnectionFactory` so, for `service:jmx:activemq:///tcp://localhost:61616?jmx.type=Topic&jmx.destination=jmxTopic`; both _jmx.type_ and _jmx.destination_ will be stripped from the URL (leaving `tcp://localhost:61616`) before being passed to `ActiveMQConnectionFactory`. Each of the properties may also be specified in the initial environment, the URL will take precedence, apart from in the case where `JMXConnector.CREDENTIALS` exists in the initial set of attributes, that will always replace the brokerUser and brokerPassword.

----

## Solace ##

You can switch to using Solace as the JMX transport mechanism using `solace` instead of `jmxmp` as the protocol part of the _JMXServiceURL_ along with a URL that specifies the connection to the Solace broker instance along with some specific environment properties which should be self explanatory. `jmxservice.env.` is the prefix that indicates that this property should be passed through to the initial environment when invoking `JMXConnectorFactory.newJMXConnector()`. This prefix is stripped off before the property is added to the initial environment.

```
adapterConfigUrl=file://localhost/./config/adapter.xml
managementComponents=jmx:jetty
jmxserviceurl=service:jmx:solace:///smf://localhost:55555
jmxserviceurl.env.jmx.brokerUser=default
jmxserviceurl.env.jmx.type=Topic
jmxserviceurl.env.jmx.destination=jmxTopic
jmxserviceurl.env.jmx.messageVPN=default
```

The various environment properties may also be specified as part of the _JMXServiceURL_, so you could specify `service:jmx:solace:///smf://localhost:55555?jmx.type=Topic&jmx.destination=jmxTopic` which achieves the same thing. For Solace the default username and password defaults to _default_ along with an empty password. The full list of supported properties are

| Name | Description |
|----|----|
|jmx.type|The destination type (i.e. _Topic_ or _Queue_; case-sensitive); defaults to Topic. |
|jmx.destination|The name of a Topic or Queue; if not assigned, then a unique one will be created to avoid exceptions; this is, though, pointless from a usability perspective |
|jmx.brokerUser|The username to connect to the broker (if required); defaults to 'default'.|
|jmx.brokerPassword|The password to connect to the broker (if required); defaults to ''.|
|jmx.timeout|The timeout in milliseconds for a client to wait for a reply after sending a request; defaults to 60000.|
|jmx.clientid|The client ID to be associated with the underlying `SolConnectionFactory` if desired; defaults to null.|
|jmx.messageVPN| The message VPN to use with the underlying `SolConnectionFactory`; defaults to 'default'|
|jmx.backupBrokers|A comma separated list of additional brokers that should serve as a list of backup brokers. Note that doing this as part of the URL will make it hard to read, so that should be discouraged from maintainability point of view, it will be best to add the backup brokers as part of the initial environment|

All properties are case sensitive; you can mix and match the environment with the URL, the URL will take precedence, apart from in the case where `JMXConnector.CREDENTIALS` exists in the initial set of attributes, that will always replace the brokerUser and brokerPassword.

----

## AMQP 1.0 ##

AMQP 1.0 support is provided by the [Apache Qpid][] library. To switch to using AMQP as the JMX transport mechanism we simply start using `amqp` instead of `jmxmp` as the protocol part of the _JMXServiceURL_ along with a URL that specifies the connection information for the AMQP 1.0 broker. Configuration for Qpid tends to specified completely via the URL, so anything that would be valid as part of an URL will be valid as part of the _JMXServiceURL_. So our bootstrap.properties might end up looking like this:

```
adapterConfigUrl=file://localhost/./config/adapter.xml
managementComponents=jmx:jetty
jmxserviceurl=service:jmx:amqp:///amqp://guest:guest@localhost:5672?clientid=test-client&jmx.type=Topic&jmx.destination=jmxTopic
# Not required because everything is in the url.
# jmxserviceurl.env.jmx.type=Topic
# jmxserviceurl.env.jmx.destination=jmxTopic
```

The full list of supported properties that control JMX behaviour are as follows :

| Name | Description |
|----|----|
|jmx.type|The destination type (i.e. _Topic_ or _Queue_; case-sensitive); defaults to Topic. |
|jmx.destination|The name of a Topic or Queue; if not assigned, then a unique one will be created to avoid exceptions; this is, though, pointless from a usability perspective |
|jmx.brokerUser| 	The username to connect to the broker (if required); defaults to null. This may be a redundant; as you can often configure the username directly on the URL|
|jmx.brokerPassword|The password to connect to the broker (if required); defaults to null. This is likely to be redundant; as you can often configure the password directly on the URL.|

All keys are case sensitive, and if specified in the URL will be stripped before being passed to the Qpid ConnectionFactory so, for `service:jmx:amqp:///amqp://guest:guest@localhost:5672?clientid=test-client&jmx.destination=jmxTopic`; then _jmx.destination_ will be stripped from the URL leaving `amqp://guest:guest@localhost:5672?clientid=test-client`. Each of the properties may also be specified in the initial environment as per the other examples, the URL will take precedence, apart from in the case where `JMXConnector.CREDENTIALS` exists in the initial set of attributes, which will always replace the brokerUser and brokerPassword.


[Apache Qpid]: https://qpid.apache.org/
[ActiveMQ]: http://activemq.apache.org/
