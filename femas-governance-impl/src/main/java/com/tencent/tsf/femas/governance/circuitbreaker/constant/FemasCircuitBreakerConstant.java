package com.tencent.tsf.femas.governance.circuitbreaker.constant;

public class FemasCircuitBreakerConstant {

    //统计滚动的时间窗口
    public static final Integer MAX_SLIDING_WINDOW_SIZE = 9999;
    public static final Integer MIN_SLIDING_WINDOW_SIZE = 1;

    // 失败请求比例
    public static final Integer MAX_FAILURE_RATE_THRESHOLD = 100;
    public static final Integer MIN_FAILURE_RATE_THRESHOLD = 1;

    // 失败请求比例
    public static final Integer MAX_EJECTION_RATE_THRESHOLD = 100;
    public static final Integer MIN_EJECTION_RATE_THRESHOLD = 0;

    // 熔断开启到半开间隔
    public static final Integer MAX_WAIT_DURATION_IN_OPEN_STATE = 9999;
    public static final Integer MIN_WAIT_DURATION_IN_OPEN_STATE = 1;

    // 最小失败请求数
    public static final Integer MINIMUN_NUMBER_OF_CALLS = 1;

    public static final RuntimeException MOCK_EXCEPTION = new RuntimeException();
}
