package com.tencent.tsf.femas.governance.metrics.micrometer.transformer;

import com.tencent.tsf.femas.governance.metrics.MetricsTransformer;
import com.tencent.tsf.femas.governance.metrics.micrometer.exporter.logger.LoggerMeterRegistry;
import com.tencent.tsf.femas.governance.metrics.micrometer.exporter.logger.LoggerRegistryConfig;
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
 * @Date 2021-11-09 16:10
 * @Description micrometer transformer
 **/
public interface MicroMeterTransformer extends MetricsTransformer {

    String transform(Timer timer, LoggerMeterRegistry.Printer print, LoggerRegistryConfig config, TimeUnit timeUnit);

    String transform(TimeGauge timeGauge, LoggerMeterRegistry.Printer print, LoggerRegistryConfig config,
            TimeUnit timeUnit);

    String transform(Gauge gauge, LoggerMeterRegistry.Printer print, LoggerRegistryConfig config, TimeUnit timeUnit);

    String transform(Counter counter, LoggerMeterRegistry.Printer print, LoggerRegistryConfig config,
            TimeUnit timeUnit);

    String transform(DistributionSummary distributionSummary, LoggerMeterRegistry.Printer print,
            LoggerRegistryConfig config, TimeUnit timeUnit);

    String transform(LongTaskTimer longTaskTimer, LoggerMeterRegistry.Printer print, LoggerRegistryConfig config,
            TimeUnit timeUnit);

    String transform(FunctionCounter functionCounter, LoggerMeterRegistry.Printer print, LoggerRegistryConfig config,
            TimeUnit timeUnit);

    String transform(FunctionTimer functionTimer, LoggerMeterRegistry.Printer print, LoggerRegistryConfig config,
            TimeUnit timeUnit);

    String transform(Meter meter, LoggerMeterRegistry.Printer print, LoggerRegistryConfig config, TimeUnit timeUnit);
}
