package com.tencent.tsf.femas.governance.metrics.micrometer.exporter.logger;

import com.tencent.tsf.femas.plugin.impl.FemasPluginContext;
import com.tencent.tsf.femas.governance.metrics.micrometer.MicroMeterExporter;
import com.tencent.tsf.femas.governance.metrics.micrometer.MicrometerMeterRegistry;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author p_mtluo
 * @Date 2021-11-08 16:37
 * @Description meter export to file
 **/
public class LoggerMeterExporter extends MicroMeterExporter {

    private static final Logger LOG = LoggerFactory.getLogger(LoggerMeterExporter.class);
    LoggerMeterRegistry registry = new LoggerMeterRegistry();

    public LoggerMeterExporter() {
        this.microMeterExporterActive.set(true);
        super.report();
        LOG.info("femas logger exporter init.....");
    }

    @Override
    public Duration step() {
        return registry.getStep();
    }

    @Override
    public void run() {
        if (null != FemasPluginContext.getMeterRegistry() && !FemasPluginContext.getMeterRegistry().isEmpty()) {
            MicrometerMeterRegistry micrometerMeterRegistry = (MicrometerMeterRegistry) FemasPluginContext
                    .getMeterRegistry().get(0);
            micrometerMeterRegistry.addMeterRegistry(registry);
        }
        registry.publishRun();
    }

    @Override
    public String getName() {
        return "loggerMetricsExporter";
    }
}
