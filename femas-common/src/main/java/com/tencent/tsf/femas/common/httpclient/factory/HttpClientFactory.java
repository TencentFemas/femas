package com.tencent.tsf.femas.common.httpclient.factory;

import com.tencent.tsf.femas.common.httpclient.client.AbstractHttpClient;


/**
 * @Author leoziltong
 * @Description //TODO
 * @Date: 2021/3/29 11:03
 * @Version 1.0
 */
public interface HttpClientFactory {

    AbstractHttpClient createHttpClient();

}
