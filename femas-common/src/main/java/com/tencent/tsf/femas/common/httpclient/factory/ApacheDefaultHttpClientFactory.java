package com.tencent.tsf.femas.common.httpclient.factory;

import com.tencent.tsf.femas.common.httpclient.client.AbstractHttpClient;
import com.tencent.tsf.femas.common.httpclient.client.FemasApacheHttpRestClient;
import com.tencent.tsf.femas.common.httpclient.client.HttpClientConfig;
import java.util.concurrent.TimeUnit;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.RequestContent;

/**
 * @Author leoziltong
 * @Description //TODO
 * @Date: 2021/3/29 17:15
 * @Version 1.0
 */
public class ApacheDefaultHttpClientFactory implements HttpClientFactory {

    private boolean authenticateSslClients = true;

    private long connectionTimeToLive;
    private int maxConnTotal;
    private int conTimeOutMillis;
    private int readTimeOutMillis;
    private int maxConnPerRoute;
    private int maxRedirects;

    public ApacheDefaultHttpClientFactory(int conTimeOutMillis, int readTimeOutMillis) {
        this(500L, Runtime.getRuntime().availableProcessors() * 2, conTimeOutMillis, readTimeOutMillis,
                Runtime.getRuntime().availableProcessors(), 0);

    }

    public ApacheDefaultHttpClientFactory() {
        this(500L, Runtime.getRuntime().availableProcessors() * 2, Integer.getInteger("", 5000),
                Integer.getInteger("", 5000), Runtime.getRuntime().availableProcessors(), 0);
    }

    public ApacheDefaultHttpClientFactory(long connectionTimeToLive, int maxConnTotal, int conTimeOutMillis,
            int readTimeOutMillis, int maxConnPerRoute, int maxRedirects) {
        this.connectionTimeToLive = connectionTimeToLive;
        this.maxConnTotal = maxConnTotal;
        this.conTimeOutMillis = conTimeOutMillis;
        this.readTimeOutMillis = readTimeOutMillis;
        this.maxConnPerRoute = maxConnPerRoute;
        this.maxRedirects = maxRedirects;
    }

    @Override
    public AbstractHttpClient createHttpClient() {
        final HttpClientConfig config = config();
        return new FemasApacheHttpRestClient(HttpClients.custom()
                .addInterceptorLast(new RequestContent(true))
                .setDefaultRequestConfig(buildRequestConfig(config))
                .setUserAgent(config.getUserAgent())
                .setMaxConnTotal(config.getMaxConnTotal())
                .setMaxConnPerRoute(config.getMaxConnPerRoute())
                .setConnectionTimeToLive(config.getConnTimeToLive(),
                        config.getConnTimeToLiveTimeUnit()).build());
    }

    private RequestConfig buildRequestConfig(HttpClientConfig httpClientConfig) {
        return RequestConfig.custom().setConnectTimeout(httpClientConfig.getConTimeOutMillis())
                .setSocketTimeout(httpClientConfig.getReadTimeOutMillis())
                .setConnectionRequestTimeout(httpClientConfig.getConnectionRequestTimeout())
                .setMaxRedirects(httpClientConfig.getMaxRedirects()).build();
    }

    protected HttpClientConfig config() {
        return HttpClientConfig.builder().setConnectionTimeToLive(connectionTimeToLive, TimeUnit.MILLISECONDS)
                .setMaxConnTotal(maxConnTotal)
                .setConTimeOutMillis(conTimeOutMillis)
                .setReadTimeOutMillis(readTimeOutMillis)
                .setMaxConnPerRoute(maxConnPerRoute).setMaxRedirects(maxRedirects).build();
    }

}
