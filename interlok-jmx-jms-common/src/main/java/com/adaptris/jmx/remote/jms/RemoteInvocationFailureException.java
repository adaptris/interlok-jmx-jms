package com.adaptris.jmx.remote.jms;

public class RemoteInvocationFailureException extends Throwable {
    public RemoteInvocationFailureException(String s, Throwable ex) {
        super(s, ex);
    }
}
