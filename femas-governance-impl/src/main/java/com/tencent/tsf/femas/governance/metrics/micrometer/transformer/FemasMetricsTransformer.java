package com.tencent.tsf.femas.governance.metrics.micrometer.transformer;

import com.tencent.tsf.femas.common.exception.FemasRuntimeException;
import com.tencent.tsf.femas.governance.metrics.micrometer.exporter.logger.LoggerMeterRegistry;
import com.tencent.tsf.femas.governance.metrics.micrometer.exporter.logger.LoggerRegistryConfig;
import com.tencent.tsf.femas.plugin.context.ConfigContext;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.FunctionCounter;
import io.micrometer.core.instrument.FunctionTimer;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.LongTaskTimer;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.TimeGauge;
import io.micrometer.core.instrument.Timer;
import java.util.concurrent.TimeUnit;

/**
 * @Author p_mtluo
 * @Date 2021-11-09 11:32
 * @Description default transformer
 **/
public class FemasMetricsTransformer implements MicroMeterTransformer {


    @Override
    public String transform(Timer timer, LoggerMeterRegistry.Printer print, LoggerRegistryConfig config,
            TimeUnit timeUnit) {
        return null;
    }

    @Override
    public String getType() {
        return null;
    }

    @Override
    public void init(ConfigContext conf) throws FemasRuntimeException {

    }

    @Override
    public void destroy() {

    }

    @Override
    public String getName() {
        return "femas";
    }

    @Override
    public String transform(TimeGauge timeGauge, LoggerMeterRegistry.Printer print, LoggerRegistryConfig config,
            TimeUnit timeUnit) {
        return null;
    }

    @Override
    public String transform(Gauge gauge, LoggerMeterRegistry.Printer print, LoggerRegistryConfig config,
            TimeUnit timeUnit) {
        return null;
    }

    @Override
    public String transform(Counter counter, LoggerMeterRegistry.Printer print, LoggerRegistryConfig config,
            TimeUnit timeUnit) {
        return null;
    }

    @Override
    public String transform(DistributionSummary distributionSummary, LoggerMeterRegistry.Printer print,
            LoggerRegistryConfig config, TimeUnit timeUnit) {
        return null;
    }

    @Override
    public String transform(LongTaskTimer longTaskTimer, LoggerMeterRegistry.Printer print, LoggerRegistryConfig config,
            TimeUnit timeUnit) {
        return null;
    }

    @Override
    public String transform(FunctionCounter functionCounter, LoggerMeterRegistry.Printer print,
            LoggerRegistryConfig config, TimeUnit timeUnit) {
        return null;
    }

    @Override
    public String transform(FunctionTimer functionTimer, LoggerMeterRegistry.Printer print, LoggerRegistryConfig config,
            TimeUnit timeUnit) {
        return null;
    }

    @Override
    public String transform(Meter meter, LoggerMeterRegistry.Printer print, LoggerRegistryConfig config,
            TimeUnit timeUnit) {
        return null;
    }
}
