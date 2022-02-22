package com.tencent.tsf.femas.common.httpclient.wrapper;

import com.tencent.tsf.femas.common.httpclient.HttpClientResponse;
import com.tencent.tsf.femas.common.util.HttpResult;
import com.tencent.tsf.femas.common.util.IOTinyUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.http.HttpStatus;

/**
 * @Author leoziltong
 * @Description //TODO
 * @Date: 2021/3/29 21:21
 * @Version 1.0
 */
public interface ResultWrapper<T> {

    default HttpResult<T> wrapper(HttpClientResponse response) throws Exception {
        if (HttpStatus.SC_OK != NumberUtils.toInt(response.getStatusCode())) {
            return errorResult(response);
        }
        return dealWithResponse(response);
    }

    HttpResult<T> dealWithResponse(HttpClientResponse response) throws Exception;

    /**
     * key是实现类名称，包含包路径，在注册结果包装器时，取这里的key
     *
     * @return com.XXX.Name
     */
    default String genKey() {
        return this.getClass().getName();
    }

    default HttpResult<T> errorResult(HttpClientResponse response) throws Exception {
        String message = IOTinyUtils.toString(response.getBody(), response.getCharset());
        return new HttpResult<T>(response.getHeaders(), response.getStatusCode(), null, message);
    }

}
