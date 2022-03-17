package com.tencent.tsf.femas.common.httpclient.client;

import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @Author leoziltong
 * @Description //TODO
 * @Date: 2021/3/30 11:21
 * @Version 1.0
 */
public class LongPullingApacheHttpRestClient extends FemasApacheHttpRestClient {

    private static final  Logger logger = LoggerFactory.getLogger(LongPullingApacheHttpRestClient.class);

    public LongPullingApacheHttpRestClient(CloseableHttpClient client) {
        super(client);
    }

}
