package com.tencent.tsf.femas.governance.loadbalance.exception;

public class FemasNoAvailableInstanceException extends RuntimeException {

    public FemasNoAvailableInstanceException() {
        super();
    }

    public FemasNoAvailableInstanceException(String message) {
        super(message);
    }

    public FemasNoAvailableInstanceException(String message, Throwable cause) {
        super(message, cause);
    }
}
