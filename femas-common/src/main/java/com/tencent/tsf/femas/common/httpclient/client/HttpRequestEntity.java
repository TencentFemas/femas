package com.tencent.tsf.femas.common.httpclient.client;


import static com.tencent.tsf.femas.common.util.HttpHeaderKeys.ACCEPT_CHARSET;
import static com.tencent.tsf.femas.common.util.HttpHeaderKeys.CONTENT_TYPE;
import static com.tencent.tsf.femas.common.util.HttpHeaderKeys.DEFAULT_ENCODE;

import com.tencent.tsf.femas.common.util.HttpElement;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

/**
 * @Author leoziltong
 * @Description //TODO
 * @Date: 2021/3/30 14:45
 * @Version 1.0
 */
public class HttpRequestEntity {

    private String url;

    private String httpMethod;

    private Map<String, String> headers;

    private Map<String, Object> params;

    private Object body;

    private HttpClientConfig httpClientConfig;

    //for get and default config
    public HttpRequestEntity(String url, String httpMethod, Map<String, String> headers, Map<String, Object> params) {
        this(url, httpMethod, headers, null, params, null);
    }

    //for post and default config
    public HttpRequestEntity(String url, String httpMethod, Map<String, String> headers, Object body) {
        this(url, httpMethod, headers, null, null, body);
    }

    public HttpRequestEntity(String url, String httpMethod, Map<String, String> headers, Map<String, Object> params,
            Object body) {
        this(url, httpMethod, headers, null, params, body);

    }

    public HttpRequestEntity(String url, String httpMethod, Map<String, String> headers,
            HttpClientConfig httpClientConfig, Map<String, Object> params, Object body) {
        this.url = url;
        this.httpMethod = httpMethod;
        this.headers = headers;
        this.httpClientConfig = httpClientConfig;
        this.params = params;
        this.body = body;
    }

    public void addContentType(String contentType) {
        if (contentType == null) {
            contentType = HttpElement.MediaType.APPLICATION_JSON;
        }
        this.headers.put(CONTENT_TYPE, contentType);
    }

    public String getCharset() {
        String acceptCharset = headers.get(ACCEPT_CHARSET);
        if (acceptCharset == null) {
            String contentType = headers.get(CONTENT_TYPE);
            acceptCharset = StringUtils.isNotBlank(contentType) ? analysisCharset(contentType) : DEFAULT_ENCODE;
        }
        return acceptCharset;
    }

    private String analysisCharset(String contentType) {
        String[] values = contentType.split(";");
        String charset = DEFAULT_ENCODE;
        if (values.length == 0) {
            return charset;
        }
        for (String value : values) {
            if (value.startsWith("charset=")) {
                charset = value.substring("charset=".length());
            }
        }
        return charset;
    }

    //encode url
    public String encodeQueryUrl() {
        StringBuilder urlBuilder = new StringBuilder();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            try {
                if (null != entry.getValue()) {
                    urlBuilder.append(entry.getKey()).append("=")
                            .append(URLEncoder.encode(String.valueOf(entry.getValue()), "UTF-8"))
                            .append("&");
                }
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }
        dealStringTail(urlBuilder, "&");
        return urlBuilder.toString();
    }

    //处理尾字符，避免重复append造成字符串错误
    void dealStringTail(StringBuilder var, final String tail) {
        if (var.toString().endsWith(tail)) {
            var.deleteCharAt(var.length() - 1);
        }
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public HttpClientConfig getHttpClientConfig() {
        return httpClientConfig;
    }

    public void setHttpClientConfig(HttpClientConfig httpClientConfig) {
        this.httpClientConfig = httpClientConfig;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }

}
