package com.tencent.tsf.femas.adaptor.paas.logger;

public interface ConverterBase {

    /**
     * MDC Map为空时的默认值
     */
    String DEFAULT_VALUE = "[,,,]";

    /**
     * value为空时的默认值
     */
    String DEFAULT_SINGLE_VALUE = "";

    /**
     * MDC中获取traceid、spanid、span-export的key
     */
    String[] MDC_KEYS = new String[]{"X-B3-TraceId", "X-B3-SpanId", "X-Span-Export"};
}
