package com.tencent.tsf.femas.config.exception;

public class FemasConfigException extends RuntimeException {

    public FemasConfigException(String message) {
        super(message);
    }

    public FemasConfigException(String message, Throwable cause) {
        super(message, cause);
    }
}
