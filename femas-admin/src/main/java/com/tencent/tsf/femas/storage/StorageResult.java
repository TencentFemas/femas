package com.tencent.tsf.femas.storage;

import java.io.Serializable;

/**
 * @Author leoziltong
 * @Description //TODO
 * @Date: 2021/4/19 16:58
 * @Version 1.0
 */
public class StorageResult<T> implements Serializable {

    public static String EXCEPTION_ERROR = "500";
    public static String ERROR = "1";
    public static String SUCCESS = "0";

    private String status;
    private String error;
    private T data;

    public StorageResult() {
    }

    public StorageResult(String status, String error) {
        this.status = status;
        this.error = error;
    }

    public StorageResult(String status, T data) {
        this.status = status;
        this.data = data;
    }

    public static <T> ResultBuilder<T> builder() {
        return new ResultBuilder<T>();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("StorageResult{");
        sb.append("status='").append(status).append('\'');
        sb.append(", error='").append(error).append('\'');
        sb.append(", data='").append(data).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public static final class ResultBuilder<T> {

        private String status;
        private String error;
        private T data;

        private ResultBuilder() {
        }

        public ResultBuilder<T> status(String status) {
            this.status = status;
            return this;
        }

        public ResultBuilder<T> error(String error) {
            this.error = error;
            return this;
        }

        public ResultBuilder<T> data(T data) {
            this.data = data;
            return this;
        }

        public StorageResult<T> build() {
            StorageResult<T> restResult = new StorageResult<T>();
            restResult.setStatus(status);
            restResult.setError(error);
            restResult.setData(data);
            return restResult;
        }
    }

}
