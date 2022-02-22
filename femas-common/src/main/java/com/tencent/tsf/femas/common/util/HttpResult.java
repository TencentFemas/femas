package com.tencent.tsf.femas.common.util;


import java.util.Map;

/**
 * @Author leoziltong
 * @Description //TODO
 * @Date: 2021/3/29 21:33
 * @Version 1.0
 */
public class HttpResult<T> extends Result<T> {

    private Map<String, String> headers;

    public HttpResult() {
    }

    public HttpResult(Map<String, String> headers, String code, T data, String message) {
        super(code, message, data);
        this.headers = headers;
    }

    public Map<String, String> getHeader() {
        return headers;
    }

    public void setHeader(Map<String, String> header) {
        this.headers = header;
    }

}
