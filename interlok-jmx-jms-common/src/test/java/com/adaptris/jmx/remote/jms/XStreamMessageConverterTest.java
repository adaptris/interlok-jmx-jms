package com.adaptris.jmx.remote.jms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.junit.Test;
import org.springframework.jms.support.converter.MessageConversionException;

public class XStreamMessageConverterTest {

  private static final String OBJ_XML = "<?xml version=\"1.0\" ?><com.adaptris.jmx.remote.jms.XStreamMessageConverterTest_-BeanObject><field>field</field></com.adaptris.jmx.remote.jms.XStreamMessageConverterTest_-BeanObject>";

  @Test
  public void toMessage() throws JMSException, MessageConversionException {
    TextMessage message = mock(TextMessage.class);
    Session session = mock(Session.class);
    when(session.createTextMessage()).thenReturn(message);
    BeanObject obj = new BeanObject("field");

    Message returnedMessage = new XStreamMessageConverter().toMessage(obj, session);

    assertEquals(message, returnedMessage);
    verify(message).setText(OBJ_XML);
    verify(session).createTextMessage();
  }

  @Test
  public void fromMessage() throws JMSException, MessageConversionException {
    TextMessage message = mock(TextMessage.class);
    when(message.getText()).thenReturn(OBJ_XML);

    Object obj = new XStreamMessageConverter().fromMessage(message);

    assertTrue(obj instanceof BeanObject);
    assertEquals("field", ((BeanObject) obj).getField());
  }

  @Test
  public void fromMessageEmptyContent() throws JMSException, MessageConversionException {
    TextMessage message = mock(TextMessage.class);
    when(message.getText()).thenReturn("");

    Object obj = new XStreamMessageConverter().fromMessage(message);

    assertTrue(obj instanceof TextMessage);
    assertEquals(message, obj);
  }

  @Test
  public void fromMessageNotTextMessage() throws JMSException, MessageConversionException {
    Message message = mock(Message.class);

    Object obj = new XStreamMessageConverter().fromMessage(message);

    assertTrue(obj instanceof Message);
    assertEquals(message, obj);
  }

  public static class BeanObject {
    private String field;

    public BeanObject(String field) {
      this.field = field;
    }

    public String getField() {
      return field;
    }

    public void setField(String field) {
      this.field = field;
    }

  }

}
