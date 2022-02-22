package com.tencent.tsf.femas.common.httpclient.client;

import java.util.concurrent.TimeUnit;

/**
 * @Author leoziltong
 * @Description //TODO
 * @Date: 2021/3/29 16:26
 * @Version 1.0
 */
public class HttpClientConfig {

    private final int conTimeOutMillis;

    private final int readTimeOutMillis;

    private final long connTimeToLive;

    private final TimeUnit connTimeToLiveTimeUnit;

    private final int connectionRequestTimeout;

    private final int maxRedirects;

    private final int maxConnTotal;

    private final int maxConnPerRoute;

    private final int ioThreadCount;

    private final String userAgent;

    public HttpClientConfig(int conTimeOutMillis, int readTimeOutMillis, long connTimeToLive, TimeUnit timeUnit,
            int connectionRequestTimeout, int maxRedirects, int maxConnTotal, int maxConnPerRoute, int ioThreadCount,
            String userAgent) {
        this.conTimeOutMillis = conTimeOutMillis;
        this.readTimeOutMillis = readTimeOutMillis;
        this.connTimeToLive = connTimeToLive;
        this.connTimeToLiveTimeUnit = timeUnit;
        this.connectionRequestTimeout = connectionRequestTimeout;
        this.maxRedirects = maxRedirects;
        this.maxConnTotal = maxConnTotal;
        this.maxConnPerRoute = maxConnPerRoute;

        this.ioThreadCount = ioThreadCount;
        this.userAgent = userAgent;
    }

    public static HttpClientConfigBuilder builder() {
        return new HttpClientConfigBuilder();
    }

    public int getConTimeOutMillis() {
        return conTimeOutMillis;
    }

    public int getReadTimeOutMillis() {
        return readTimeOutMillis;
    }

    public long getConnTimeToLive() {
        return connTimeToLive;
    }

    public TimeUnit getConnTimeToLiveTimeUnit() {
        return connTimeToLiveTimeUnit;
    }

    public int getConnectionRequestTimeout() {
        return connectionRequestTimeout;
    }

    public int getMaxRedirects() {
        return maxRedirects;
    }

    public int getMaxConnTotal() {
        return maxConnTotal;
    }

    public int getMaxConnPerRoute() {
        return maxConnPerRoute;
    }

    public int getIoThreadCount() {
        return ioThreadCount;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public static final class HttpClientConfigBuilder {

        private int conTimeOutMillis = -1;

        private int readTimeOutMillis = -1;

        private long connTimeToLive = -1;

        private TimeUnit connTimeToLiveTimeUnit = TimeUnit.MILLISECONDS;

        private int connectionRequestTimeout = -1;

        private int maxRedirects = 50;

        private int maxConnTotal = 0;

        private int maxConnPerRoute = 0;

        private boolean contentCompressionEnabled = true;

        private int ioThreadCount = Runtime.getRuntime().availableProcessors();

        private String userAgent;

        public HttpClientConfigBuilder setConTimeOutMillis(int conTimeOutMillis) {
            this.conTimeOutMillis = conTimeOutMillis;
            return this;
        }

        public HttpClientConfigBuilder setReadTimeOutMillis(int readTimeOutMillis) {
            this.readTimeOutMillis = readTimeOutMillis;
            return this;
        }

        public HttpClientConfigBuilder setConnectionTimeToLive(long connTimeToLive, TimeUnit connTimeToLiveTimeUnit) {
            this.connTimeToLive = connTimeToLive;
            this.connTimeToLiveTimeUnit = connTimeToLiveTimeUnit;
            return this;
        }

        public HttpClientConfigBuilder setConnectionRequestTimeout(int connectionRequestTimeout) {
            this.connectionRequestTimeout = connectionRequestTimeout;
            return this;
        }

        public HttpClientConfigBuilder setMaxRedirects(int maxRedirects) {
            this.maxRedirects = maxRedirects;
            return this;
        }

        public HttpClientConfigBuilder setMaxConnTotal(int maxConnTotal) {
            this.maxConnTotal = maxConnTotal;
            return this;
        }

        public HttpClientConfigBuilder setMaxConnPerRoute(int maxConnPerRoute) {
            this.maxConnPerRoute = maxConnPerRoute;
            return this;
        }

        public HttpClientConfigBuilder setContentCompressionEnabled(boolean contentCompressionEnabled) {
            this.contentCompressionEnabled = contentCompressionEnabled;
            return this;
        }

        public HttpClientConfigBuilder setIoThreadCount(int ioThreadCount) {
            this.ioThreadCount = ioThreadCount;
            return this;
        }

        public HttpClientConfigBuilder setUserAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        /**
         * build http client config.
         *
         * @return HttpClientConfig
         */
        public HttpClientConfig build() {
            return new HttpClientConfig(conTimeOutMillis, readTimeOutMillis, connTimeToLive, connTimeToLiveTimeUnit,
                    connectionRequestTimeout, maxRedirects, maxConnTotal, maxConnPerRoute,
                    ioThreadCount, userAgent);
        }
    }

}
