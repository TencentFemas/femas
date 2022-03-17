package com.tencent.tsf.femas.common.httpclient.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tencent.tsf.femas.common.exception.FemasRuntimeException;
import com.tencent.tsf.femas.common.httpclient.FemasApacheClientHttpResponse;
import com.tencent.tsf.femas.common.httpclient.HttpClientResponse;
import com.tencent.tsf.femas.common.util.HttpElement;
import com.tencent.tsf.femas.common.util.HttpResult;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.tencent.tsf.femas.common.util.HttpHeaderKeys.CONTENT_TYPE;


/**
 * @Author leoziltong
 * @Description //TODO
 * @Date: 2021/3/30 11:21
 * @Version 1.0
 */
public class FemasApacheHttpRestClient extends AbstractHttpClient {

    static final  ObjectMapper mapper = new ObjectMapper();
    private static final  Logger logger = LoggerFactory.getLogger(FemasApacheHttpRestClient.class);
    protected final CloseableHttpClient client;

    public FemasApacheHttpRestClient(CloseableHttpClient client) {
        this.client = client;
    }

    private static void replaceDefaultConfig(HttpRequestBase requestBase, HttpClientConfig httpClientConfig) {
        if (httpClientConfig == null) {
            return;
        }
        if (httpClientConfig != null) {
            requestBase.setConfig(RequestConfig.custom()
                    .setConnectTimeout(httpClientConfig.getConTimeOutMillis())
                    .setSocketTimeout(httpClientConfig.getReadTimeOutMillis()).build());
        }
    }

    public static void initRequestEntity(HttpRequestBase requestBase, HttpRequestEntity requestHttpEntity)
            throws Exception {
        Object body = requestHttpEntity.getBody();
        Map<String, String> header = requestHttpEntity.getHeaders();
        if (body == null) {
            return;
        }
        if (requestBase instanceof HttpEntityEnclosingRequest) {
            HttpEntityEnclosingRequest request = (HttpEntityEnclosingRequest) requestBase;

            ContentType contentType = ContentType.parse(header.get(CONTENT_TYPE));
            HttpEntity entity;
            if (body instanceof byte[]) {
                entity = new ByteArrayEntity((byte[]) body, contentType);
            } else {
                entity = new StringEntity(body instanceof String ? (String) body : mapper.writeValueAsString(body),
                        contentType);
            }
            request.setEntity(entity);
        }
    }

    public static void initRequestFromEntity(HttpRequestBase requestBase, Map<String, String> body, String charset)
            throws Exception {
        if (body == null || body.isEmpty()) {
            return;
        }
        List<NameValuePair> params = new ArrayList<NameValuePair>(body.size());
        for (Map.Entry<String, String> entry : body.entrySet()) {
            params.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }
        if (requestBase instanceof HttpEntityEnclosingRequest) {
            HttpEntityEnclosingRequest request = (HttpEntityEnclosingRequest) requestBase;
            HttpEntity entity = new UrlEncodedFormEntity(params, charset);
            request.setEntity(entity);
        }
    }

    public <T> HttpResult<T> get(String url, Map<String, String> header, Map<String, Object> query) throws Exception {
        return execute(new HttpRequestEntity(url, HttpElement.HttpMethod.GET, header, query), null);
    }

    public <T> HttpResult<T> get(String url, Map<String, String> header, Map<String, Object> query, String wrapper)
            throws Exception {
        return execute(new HttpRequestEntity(url, HttpElement.HttpMethod.GET, header, query), wrapper);
    }

    public <T> HttpResult<T> delete(String url, Map<String, String> header, Map<String, Object> query)
            throws Exception {
        return execute(new HttpRequestEntity(url, HttpElement.HttpMethod.DELETE, header, query), null);
    }

    public <T> HttpResult<T> put(String url, Map<String, String> header, Map<String, Object> query) throws Exception {
        return execute(new HttpRequestEntity(url, HttpElement.HttpMethod.PUT, header, query), null);
    }

    public <T> HttpResult<T> post(String url, Map<String, String> header, Map<String, Object> query, Object body)
            throws Exception {
        return execute(new HttpRequestEntity(url, HttpElement.HttpMethod.POST, header, query, body), null);
    }

    public <T> HttpResult<T> post(String url, Map<String, String> header, Map<String, Object> query, Object body,
            String wrapper)
            throws Exception {
        return execute(new HttpRequestEntity(url, HttpElement.HttpMethod.POST, header, query, body), wrapper);
    }

    public <T> HttpResult<T> postJson(String url, Map<String, String> header, Map<String, Object> query, Object body,
            String wrapper)
            throws Exception {
        HttpRequestEntity requestHttpEntity = new HttpRequestEntity(url, HttpElement.HttpMethod.POST, header, query,
                body);
        requestHttpEntity.addContentType(HttpElement.MediaType.APPLICATION_JSON);
        return execute(requestHttpEntity, wrapper);
    }

    public <T> HttpResult<T> postJson(String url, Map<String, String> header, Object body, String wrapper)
            throws Exception {
        HttpRequestEntity requestHttpEntity = new HttpRequestEntity(url, HttpElement.HttpMethod.POST, header, body);
        requestHttpEntity.addContentType(HttpElement.MediaType.APPLICATION_JSON);
        return execute(requestHttpEntity, wrapper);
    }

    public <T> HttpResult<T> postForm(String url, Map<String, String> header, Map<String, Object> query,
            Map<String, String> body,
            String wrapper) throws Exception {
        HttpRequestEntity requestHttpEntity = new HttpRequestEntity(url, HttpElement.HttpMethod.POST, header, query,
                body);
        requestHttpEntity.addContentType(HttpElement.MediaType.APPLICATION_FORM_URLENCODED);
        return execute(requestHttpEntity, wrapper);

    }

    public <T> HttpResult<T> postForm(String url, Map<String, String> header, Map<String, String> body, String wrapper)
            throws Exception {
        HttpRequestEntity requestHttpEntity = new HttpRequestEntity(url, HttpElement.HttpMethod.POST, header, body);
        requestHttpEntity.addContentType(HttpElement.MediaType.APPLICATION_FORM_URLENCODED);
        return execute(requestHttpEntity, wrapper);
    }

    @Override
    public <T> HttpResult<T> request(String url, HttpClientConfig config, Map<String, String> header,
            Map<String, Object> query,
            Object body, String httpMethod, String wrapper) throws Exception {
        HttpRequestEntity requestHttpEntity = new HttpRequestEntity(url, httpMethod, header, config, query, body);
        return execute(requestHttpEntity, wrapper);
    }

    @Override
    public HttpClientResponse callServer(HttpRequestEntity entity) {
        try {
            HttpRequestBase request = build(entity);
            CloseableHttpResponse response = client.execute(request);
            return new FemasApacheClientHttpResponse(response);
        } catch (Exception e) {
            logger.error("call server error", e);
            throw new FemasRuntimeException(e);
        }
    }

    private HttpRequestBase build(HttpRequestEntity requestHttpEntity) throws Exception {
        final Map<String, String> headers = requestHttpEntity.getHeaders();
        final HttpRequestBase httpRequestBase = new HttpEntityEnclosingRequestBase() {
            @Override
            public String getMethod() {
                return requestHttpEntity.getHttpMethod();
            }
        };
        httpRequestBase.setURI(URI.create(requestHttpEntity.getUrl()));
        if (headers != null && !headers.isEmpty()) {
            Iterator<Map.Entry<String, String>> iterator = headers.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String> entry = iterator.next();
                httpRequestBase.setHeader(entry.getKey(), entry.getValue());
            }
            if (HttpElement.MediaType.APPLICATION_FORM_URLENCODED.equals(headers.get(CONTENT_TYPE))
                    && requestHttpEntity.getBody() instanceof Map) {
                //x-www-form-urlencoded
                initRequestFromEntity(httpRequestBase, (Map<String, String>) requestHttpEntity.getBody(),
                        requestHttpEntity.getCharset());
            } else {
                //ByteArray
                initRequestEntity(httpRequestBase, requestHttpEntity);
            }
        }
        replaceDefaultConfig(httpRequestBase, requestHttpEntity.getHttpClientConfig());
        return httpRequestBase;
    }

    @Override
    public void close() throws IOException {
        client.close();
    }

}
