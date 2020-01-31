package com.adaptris.jmx.remote.jms;

import static org.apache.commons.lang3.StringUtils.isEmpty;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.jms.support.converter.MessageConverter;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.reflection.SunUnsafeReflectionProvider;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import com.thoughtworks.xstream.security.AnyTypePermission;

public class XStreamMessageConverter implements MessageConverter {

  private transient XStream xstream;
  private transient Logger log = LoggerFactory.getLogger(this.getClass());

  public XStreamMessageConverter() {
    // Use Unsafe because ObjectName doesn't have a no-arg constructor.
    xstream = new XStream(new SunUnsafeReflectionProvider(), new StaxDriver());
    // will stop the sytem .err nonense.
    XStream.setupDefaultSecurity(xstream);
    xstream.addPermission(AnyTypePermission.ANY);
  }

  @Override
  public Message toMessage(Object object, Session session) throws JMSException, MessageConversionException {
    TextMessage msg = session.createTextMessage();
    msg.setText(xstream.toXML(object));
    return msg;
  }

  @Override
  public Object fromMessage(Message message) throws JMSException, MessageConversionException {
    String contents = ( (TextMessage) message).getText();
    if (!isEmpty(contents)) {
      return xstream.fromXML(contents);
    }
    return null;
  }

}
