package com.tencent.tsf.femas.extension.springcloud.common.instrumentation.metrics;

import com.tencent.tsf.femas.plugin.impl.FemasPluginContext;
import com.tencent.tsf.femas.governance.metrics.MetricsExporter;
import com.tencent.tsf.femas.governance.metrics.micrometer.exporter.PrometheusMeterExporter;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author p_mtluo
 * @Date 2021-08-18 16:45
 * @Description prometheus 拉取数据的接口
 **/
@RestController
public class PrometheusController {

    private final CollectorRegistry collectorRegistry;

    public PrometheusController() {

        MetricsExporter metricsExporter = FemasPluginContext.getMetricsExporter();

        if (metricsExporter instanceof PrometheusMeterExporter) {
            ((PrometheusMeterExporter) metricsExporter).initPrometheus();
            this.collectorRegistry = ((PrometheusMeterExporter) metricsExporter).getCollectorRegistry();
        } else {
            this.collectorRegistry = null;
        }
    }


    @RequestMapping(value = "/femas/actuator/prometheus", produces = {TextFormat.CONTENT_TYPE_004})
    public String prometheus() {
        if (null == collectorRegistry) {
            return "";
        }
        Writer writer = new StringWriter();
        try {
            TextFormat.write004(writer, this.collectorRegistry.metricFamilySamples());
            return writer.toString();
        } catch (IOException e) {
            throw new RuntimeException("Writing metrics failed", e);
        }


    }
}
