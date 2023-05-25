package com.tencent.tsf.femas.common.httpclient.factory;

/**
 * @author mroccyen
 */
public class ApacheLongPollingHttpClientFactory extends ApacheDefaultHttpClientFactory {

    public ApacheLongPollingHttpClientFactory(int conTimeOutMillis, int readTimeOutMillis) {
        super(500L, Runtime.getRuntime().availableProcessors() * 2, conTimeOutMillis, readTimeOutMillis,
                Runtime.getRuntime().availableProcessors(), 0);

    }

    public ApacheLongPollingHttpClientFactory() {
        super(500L, Runtime.getRuntime().availableProcessors() * 2, Integer.getInteger("", 5000),
                Integer.getInteger("", 90000), Runtime.getRuntime().availableProcessors(), 0);
    }
}
