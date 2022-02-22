package com.tencent.tsf.femas.common.httpclient.client;


import com.tencent.tsf.femas.common.httpclient.wrapper.Body2StringResultWrapper;
import com.tencent.tsf.femas.common.httpclient.wrapper.ResultWrapper;


/**
 * @Author leoziltong
 * @Description //TODO
 * @Date: 2021/3/29 20:58
 * @Version 1.0
 */
public interface FemasHttpClient {

    void registerWrapper(String var, ResultWrapper wrapper);

    ResultWrapper getOneResultWrapper(String key);

    default ResultWrapper getDefaultWrapper() {
        return new Body2StringResultWrapper();
    }

}
