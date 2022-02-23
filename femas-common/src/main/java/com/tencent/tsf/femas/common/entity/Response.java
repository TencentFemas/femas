package com.tencent.tsf.femas.common.entity;

import java.io.Serializable;

/**
 * RPC Response class
 */
public final class Response implements Serializable {

    private static final long serialVersionUID = -4364536436151723421L;


    private ErrorStatus errorStatus;

    /**
     * 原始错误
     */
    private Throwable error;

    /**
     * 业务返回或者业务异常
     */
    private Object appResponse;

    /**
     * Gets app response.
     *
     * @return the app response
     */
    public Object getAppResponse() {
        return appResponse;
    }

    /**
     * Sets app response.
     *
     * @param response the response
     */
    public void setAppResponse(Object response) {
        appResponse = response;
    }

    /**
     * Is error boolean.
     *
     * @return the boolean
     */
    public ErrorStatus getErrorStatus() {
        return errorStatus;
    }

    /**
     * Sets error msg.
     *
     * @param errorStatus the error
     */
    public void setErrorStatus(ErrorStatus errorStatus) {
        this.errorStatus = errorStatus;
    }

    /**
     * Gets error msg.
     *
     * @return the error msg
     */

    public boolean hasError() {
        return error != null;
    }

    public Throwable getError() {
        return error;
    }

    public void setError(Throwable error) {
        this.error = error;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(128);
        sb.append("Response[");

        if (errorStatus != null) {
            sb.append("rpc error=").append(errorStatus.getMessage()).append(", ");
        }

        if (appResponse != null) {
            sb.append("appResponse=").append(appResponse).append("]");
        }

        return sb.toString();
    }
}