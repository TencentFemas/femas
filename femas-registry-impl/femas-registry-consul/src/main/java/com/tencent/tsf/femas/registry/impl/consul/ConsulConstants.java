package com.tencent.tsf.femas.registry.impl.consul;

public class ConsulConstants {

    //    public static final String CONSUL_HOST = "consulHost";
//    public static final String CONSUL_PORT = "consulPort";
    public static final String CONSUL_ACCESS_TOKEN = "consulAccessToken";

    public static final String CONSUL_ENABLE_TTL = "consulEnableTtl";
    public static final Boolean DEFAULT_CONSUL_ENABLE_TTL = true;

    public static final String CONSUL_TIME_UNIT = "s";

    public static final String CONSUL_TTL = "consulTtl";
    public static final String DEFAULT_CONSUL_TTL = "30";

    public static final String CONSUL_FAIL_FAST = "consulFailFast";
    public static final Boolean DEFAULT_CONSUL_FAIL_FAST = false;

    public static final String CONSUL_HEALTH_CHECK_URL = "consulHealthCheckUrl";

    public static final String CONSUL_HEALTH_CHECK_INTERVAL = "consulHealthCheckInterval";
    public static final String DEFAULT_CONSUL_HEALTH_CHECK_INTERVAL = "10s";

    public static final String CONSUL_HEALTH_CHECK_TIMEOUT = "consulHealthCheckTimeout";
    public static final String DEFAULT_CONSUL_HEALTH_CHECK_TIMEOUT = "5s";

    public static final String CONSUL_HEALTH_CHECK_CRITICAL_TIMEOUT = "consulHealthCheckCriticalTimeout";
    public static final String DEFAULT_CONSUL_HEALTH_CHECK_CRITICAL_TIMEOUT = "30m";

    public static final String CONSUL_HEALTH_CHECK_TLS_SKIP_VERIFY = "consulHealthCheckTlsSkipVerify";

    public static final char SEPARATOR = '-';

    /**
     * service 最长存活周期（Time To Live），单位秒。 每个service会注册一个ttl类型的check，在最长TTL秒不发送心跳 就会将service变为不可用状态。
     */
    public static final int TTL = 3;

    /**
     * 心跳周期，取ttl的2/3
     */
    public static final int HEARTBEAT_CIRCLE = (TTL * 1000 * 2) / 3 / 10;

    /**
     * consul服务查询默认间隔时间。单位毫秒
     */
    public static final int DEFAULT_LOOKUP_INTERVAL = 3000;

    /**
     * consul block 查询时 block的最长时间,单位，秒
     */
    public static final long CONSUL_BLOCK_TIME_SECONDS = 55;
}
