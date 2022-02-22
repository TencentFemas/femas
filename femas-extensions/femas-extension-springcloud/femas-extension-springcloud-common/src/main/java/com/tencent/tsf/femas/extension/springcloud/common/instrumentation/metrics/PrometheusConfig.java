package com.tencent.tsf.femas.extension.springcloud.common.instrumentation.metrics;

import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author p_mtluo
 * @Date 2021-08-18 16:17
 * @Description prometheus 配置类 用于生成拉取接口的bean
 **/
@Configuration
@ConditionalOnClass(PrometheusMeterRegistry.class)
public class PrometheusConfig {

//    @Bean
//    @ConditionalOnMissingBean
//    public io.micrometer.prometheus.PrometheusConfig prometheusConfig() {
//        return new PrometheusPropertiesConfigAdapter();
//    }
//
//    @Bean
//    @ConditionalOnMissingBean
//    public Clock micrometerClock() {
//        return Clock.SYSTEM;
//    }
//
//    @Bean
//    @ConditionalOnMissingBean
//    public PrometheusMeterRegistry prometheusMeterRegistry(io.micrometer.prometheus.PrometheusConfig prometheusConfig,
//                                                           CollectorRegistry collectorRegistry, Clock clock) {
//
//        PrometheusMeterRegistry registry = new PrometheusMeterRegistry(prometheusConfig, collectorRegistry, clock);
//        MetricsExporter metricsExporter = FemasPluginContext.getMetricsExporter();
//        if(metricsExporter instanceof PrometheusMeterExporter){
//            MicrometerMeterRegistry micrometerMeterRegistry = (MicrometerMeterRegistry) FemasPluginContext.getMeterRegistry().get(0);
//            micrometerMeterRegistry.addMeterRegistry(registry);
//        }
//        return registry;
//    }
//
//    @Bean
//    @ConditionalOnMissingBean
//    public CollectorRegistry collectorRegistry() {
//        return new CollectorRegistry(true);
//    }
//
//
//    @Bean
//    @ConditionalOnMissingBean
//    public PrometheusController prometheusEndpoint(CollectorRegistry collectorRegistry) {
//        return new PrometheusController(collectorRegistry);
//    }

    @Bean
    @ConditionalOnMissingBean
    public PrometheusController prometheusController() {
        return new PrometheusController();
    }

}


