package com.adaptris.jmx.remote.jms;

import static org.apache.commons.lang3.StringUtils.isEmpty;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.jms.support.converter.MessageConverter;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.reflection.SunUnsafeReflectionProvider;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import com.thoughtworks.xstream.security.AnyTypePermission;

public class XStreamMessageConverter implements MessageConverter {

  private transient XStream xstream;

  public XStreamMessageConverter() {
    // Use Unsafe because ObjectName doesn't have a no-arg constructor.
    xstream = new XStream(new SunUnsafeReflectionProvider(), new StaxDriver());
    // will stop the sytem .err nonense.
    xstream.addPermission(AnyTypePermission.ANY);
  }

  @Override
  public Message toMessage(Object object, Session session) throws JMSException, MessageConversionException {
    TextMessage msg = session.createTextMessage();
    msg.setText(xstream.toXML(object));
    return msg;
  }

  /**
   * This implementation deserialize a TextMessage xml content to an object.
   *
   * @return the deserialized object or a plain Message object in case of an unknown message type or empty content.
   */
  @Override
  public Object fromMessage(Message message) throws JMSException, MessageConversionException {
    if (message instanceof TextMessage) {
      String contents = ((TextMessage) message).getText();
      if (!isEmpty(contents)) {
        return xstream.fromXML(contents);
      }
    }
    return message;
  }

}
