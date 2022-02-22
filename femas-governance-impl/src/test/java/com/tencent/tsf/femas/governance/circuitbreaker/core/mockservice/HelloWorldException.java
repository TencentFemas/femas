package com.tencent.tsf.femas.governance.circuitbreaker.core.mockservice;

public class HelloWorldException extends RuntimeException {

    public HelloWorldException() {
        super("BAM!");
    }

    public HelloWorldException(String message) {
        super(message);
    }
}
