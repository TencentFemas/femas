package com.tencent.tsf.femas.agent.tools;

public class FemasRuntimeException extends RuntimeException {

    protected FemasRuntimeException() {

    }

    public FemasRuntimeException(String message) {
        super(message);
    }

    public FemasRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public FemasRuntimeException(Throwable cause) {
        super(cause);
    }
}
