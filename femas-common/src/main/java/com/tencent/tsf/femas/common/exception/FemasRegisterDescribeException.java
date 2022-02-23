package com.tencent.tsf.femas.common.exception;

public class FemasRegisterDescribeException extends RuntimeException {


    protected FemasRegisterDescribeException() {

    }

    public FemasRegisterDescribeException(String message) {
        super(message);
    }

    public FemasRegisterDescribeException(String message, Throwable cause) {
        super(message, cause);
    }

    public FemasRegisterDescribeException(Throwable cause) {
        super(cause);
    }
}
