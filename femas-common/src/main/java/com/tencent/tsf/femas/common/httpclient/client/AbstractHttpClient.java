package com.tencent.tsf.femas.common.httpclient.client;

import com.tencent.tsf.femas.common.exception.FemasRuntimeException;
import com.tencent.tsf.femas.common.httpclient.HttpClientResponse;
import com.tencent.tsf.femas.common.httpclient.wrapper.ResultWrapper;
import com.tencent.tsf.femas.common.util.HttpResult;
import com.tencent.tsf.femas.common.util.StringUtils;
import java.io.Closeable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author leoziltong
 * @Description //TODO
 * @Date: 2021/3/30 10:39
 * @Version 1.0
 */
public abstract class AbstractHttpClient implements FemasHttpClient, Closeable {

    private final static Logger logger = LoggerFactory.getLogger(AbstractHttpClient.class);

    private final Map<String, ResultWrapper> resultWrapperHashMap = new ConcurrentHashMap<>(5);

    @Override
    public void registerWrapper(String key, ResultWrapper wrapper) {
        resultWrapperHashMap.putIfAbsent(key, wrapper);
    }

    @Override
    public ResultWrapper getOneResultWrapper(String key) {
        //默认处理方式
        if (StringUtils.isEmpty(key)) {
            return getDefaultWrapper();
        }
        return resultWrapperHashMap.getOrDefault(key, getDefaultWrapper());
    }

    public <T> HttpResult<T> execute(HttpRequestEntity requestEntity, String wrapperType) {
        String url = requestEntity.getUrl();
        if (requestEntity.getParams() != null && !requestEntity.getParams().isEmpty()) {
            url = url.concat("?").concat(requestEntity.encodeQueryUrl());
        }
        requestEntity.setUrl(url);
        if (logger.isDebugEnabled()) {
            logger.debug("HTTP method: {}, url: {}, body: {}", requestEntity.getHttpMethod(), url,
                    requestEntity.getBody());
        }
        ResultWrapper<T> responseHandler = getOneResultWrapper(wrapperType);
        HttpClientResponse response = null;
        try {
            response = callServer(requestEntity);
            return responseHandler.wrapper(response);
        } catch (Exception e) {
            logger.error("wrapper result error", e);
            throw new FemasRuntimeException(e);
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    public abstract <T> HttpResult<T> get(String url, Map<String, String> header, Map<String, Object> query)
            throws Exception;

    public abstract <T> HttpResult<T> get(String url, Map<String, String> header, Map<String, Object> query,
            String wrapper) throws Exception;

    public abstract <T> HttpResult<T> delete(String url, Map<String, String> header, Map<String, Object> query)
            throws Exception;

    public abstract <T> HttpResult<T> put(String url, Map<String, String> header, Map<String, Object> query)
            throws Exception;

    public abstract <T> HttpResult<T> post(String url, Map<String, String> header, Map<String, Object> query,
            Object body)
            throws Exception;

    public abstract <T> HttpResult<T> post(String url, Map<String, String> header, Map<String, Object> query,
            Object body, String wrapper)
            throws Exception;

    public abstract <T> HttpResult<T> postJson(String url, Map<String, String> header, Map<String, Object> query,
            Object body, String wrapper)
            throws Exception;

    public abstract <T> HttpResult<T> postJson(String url, Map<String, String> header, Object body, String wrapper)
            throws Exception;

    public abstract <T> HttpResult<T> postForm(String url, Map<String, String> header, Map<String, Object> query,
            Map<String, String> body,
            String wrapper) throws Exception;

    public abstract <T> HttpResult<T> postForm(String url, Map<String, String> header, Map<String, String> body,
            String wrapper)
            throws Exception;

    public abstract <T> HttpResult<T> request(String url, HttpClientConfig config, Map<String, String> header,
            Map<String, Object> query,
            Object body, String httpMethod, String wrapper) throws Exception;

    public abstract HttpClientResponse callServer(HttpRequestEntity entity);

}
