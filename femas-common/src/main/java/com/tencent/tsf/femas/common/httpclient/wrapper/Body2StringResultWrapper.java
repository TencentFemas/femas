package com.tencent.tsf.femas.common.httpclient.wrapper;

import com.tencent.tsf.femas.common.httpclient.HttpClientResponse;
import com.tencent.tsf.femas.common.util.HttpResult;
import com.tencent.tsf.femas.common.util.IOTinyUtils;

/**
 * @Author leoziltong
 * @Description //TODO
 * @Date: 2021/3/31 14:52
 * @Version 1.0
 */
public class Body2StringResultWrapper implements ResultWrapper<String> {

    @Override
    public HttpResult<String> dealWithResponse(HttpClientResponse response) throws Exception {
        String stringBody = IOTinyUtils.toString(response.getBody(), response.getCharset());
        return new HttpResult<String>(response.getHeaders(), response.getStatusCode(), stringBody, null);
    }

}
