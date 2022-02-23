package com.tencent.tsf.femas.governance.metrics.micrometer.exporter;

import io.micrometer.prometheus.PrometheusConfig;

/**
 * @Author p_mtluo
 * @Date 2021-08-18 16:54
 * @Description prometheus 配置类
 **/
public class PrometheusPropertiesConfigAdapter implements PrometheusConfig {

    @Override
    public String get(String key) {
        return null;
    }

    @Override
    public String prefix() {
        return "femas.metrics.export.prometheus";
    }
}
