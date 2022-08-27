package com.tencent.tsf.femas.governance.metrics.micrometer.exporter.logger;

import com.tencent.tsf.femas.plugin.impl.FemasPluginContext;
import com.tencent.tsf.femas.governance.metrics.MetricsTransformer;
import com.tencent.tsf.femas.governance.metrics.micrometer.transformer.MicroMeterTransformer;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.binder.BaseUnits;
import io.micrometer.core.instrument.config.NamingConvention;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;
import io.micrometer.core.instrument.distribution.HistogramSnapshot;
import io.micrometer.core.instrument.distribution.pause.PauseDetector;
import io.micrometer.core.instrument.step.StepDistributionSummary;
import io.micrometer.core.instrument.step.StepMeterRegistry;
import io.micrometer.core.instrument.step.StepTimer;
import io.micrometer.core.instrument.util.TimeUtils;
import io.micrometer.core.util.internal.logging.InternalLogger;
import io.micrometer.core.util.internal.logging.InternalLoggerFactory;

import java.io.File;
import java.time.Duration;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.stream.StreamSupport;

import static io.micrometer.core.instrument.util.DoubleFormat.decimalOrNan;
import static java.util.stream.Collectors.joining;

/**
 * @Author p_mtluo
 * @Date 2021-11-08 17:00
 * @Description LoggerMeterRegistry
 **/
public class LoggerMeterRegistry extends StepMeterRegistry {

    private static final  InternalLogger log = InternalLoggerFactory.getInstance(LoggerMeterRegistry.class);

    private final String METRICS_LOGGER_NAME = "METRICS_LOGGER";
    private final java.util.logging.Logger metricLogger = java.util.logging.Logger.getLogger(METRICS_LOGGER_NAME);

    private final LoggerRegistryConfig config;

    private MicroMeterTransformer microMeterTransformer;

    public LoggerMeterRegistry() {
        this(LoggerRegistryConfig.DEFAULT, Clock.SYSTEM);
    }

    private LoggerMeterRegistry(LoggerRegistryConfig config, Clock clock) {
        super(config, clock);
        this.config = config;
        config().namingConvention(NamingConvention.dot);
        this.setLogger();
    }

    public static LoggerMeterRegistry.Builder builder(LoggerRegistryConfig config) {
        return new LoggerMeterRegistry.Builder(config);
    }

    private void setMetricsTransformer() {
        MetricsTransformer metricsTransformer = FemasPluginContext.getMetricsTransformer();
        if (metricsTransformer instanceof MicroMeterTransformer) {
            microMeterTransformer = (MicroMeterTransformer) metricsTransformer;
        } else {
            microMeterTransformer = null;
            log.info("not set MicroMeterTransformer");
        }
    }

    public Duration getStep() {
        return this.config.step();
    }

    private void setLogger() {
        try {
            // "/"    the local pathname separator </li>
            // "%t"   the system temporary directory </li>
            // "%h"   the value of the "user.home" system property </li>
            // "%g"   the generation number to distinguish rotated logs </li>
            // "%u"   a unique number to resolve conflicts </li>
            // "%%"   translates to a single percent sign "%" </li>

            String filePath = config.getFilePath();

            File invocationStatDir = new File(filePath);
            if (!invocationStatDir.exists()) {
                invocationStatDir.mkdirs();
            }

            Handler metricHandler = new FileHandler(filePath + config.getFileName() + "%u.log", config.getFileLimit(),
                    config.getFileCount());
            metricHandler.setFormatter(new LoggerFormatter());
            metricLogger.addHandler(metricHandler);
            metricLogger.setLevel(Level.ALL);
            metricLogger.setUseParentHandlers(false);
        } catch (Exception ex) {
            log.error("set metrics log error, message: [" + ex.getMessage() + "]");
            // throw ex
        }
    }

    public void publishRun() {
        try {
            if (null == microMeterTransformer) {
                this.setMetricsTransformer();
            }
            publish();
        } catch (Throwable e) {
            log.warn("Unexpected exception thrown while publishing metrics for " + this.getClass().getSimpleName(), e);
        }
    }

    @Override
    public void start(ThreadFactory threadFactory) {
        if (config.enabled()) {
            log.info("publishing metrics to logs every " + TimeUtils.format(config.step()));
        }
        super.start(threadFactory);
    }

    @Override
    protected void publish() {
        if (config.enabled()) {
            getMeters().stream()
                    .sorted((m1, m2) -> {
                        int typeComp = m1.getId().getType().compareTo(m2.getId().getType());
                        if (typeComp == 0) {
                            return m1.getId().getName().compareTo(m2.getId().getName());
                        }
                        return typeComp;
                    })
                    .forEach(m -> {
                        Printer print = new Printer(m);
                        m.use(
                                gauge -> {
                                    log.debug(print.id() + " value=" + print.value(gauge.value()));
                                },
                                counter -> {
                                    double count = counter.count();
                                    if (!config.logInactive() && count == 0) {
                                        return;
                                    }
                                    log.debug(print.id() + " throughput=" + print.rate(count));
                                },
                                timer -> {
                                    HistogramSnapshot snapshot = timer.takeSnapshot();
                                    long count = snapshot.count();
                                    if (!config.logInactive() && count == 0) {
                                        return;
                                    }

                                    String result = microMeterTransformer
                                            .transform(timer, print, config, getBaseTimeUnit());
                                    log.debug("metrics result=" + result);
                                    metricLogger.info(result);
                                },
                                summary -> {
                                    HistogramSnapshot snapshot = summary.takeSnapshot();
                                    long count = snapshot.count();
                                    if (!config.logInactive() && count == 0) {
                                        return;
                                    }
                                    log.debug(print.id() + " throughput=" + print.unitlessRate(count) +
                                            " mean=" + print.value(snapshot.mean()) +
                                            " max=" + print.value(snapshot.max()));
                                },
                                longTaskTimer -> {
                                    int activeTasks = longTaskTimer.activeTasks();
                                    if (!config.logInactive() && activeTasks == 0) {
                                        return;
                                    }
                                    log.debug(print.id() +
                                            " active=" + print.value(activeTasks) +
                                            " duration=" + print.time(longTaskTimer.duration(getBaseTimeUnit())));
                                },
                                timeGauge -> {
                                    double value = timeGauge.value(getBaseTimeUnit());
                                    if (!config.logInactive() && value == 0) {
                                        return;
                                    }
                                    log.debug(print.id() + " value=" + print.time(value));
                                },
                                counter -> {
                                    double count = counter.count();
                                    if (!config.logInactive() && count == 0) {
                                        return;
                                    }
                                    log.debug(print.id() + " throughput=" + print.rate(count));
                                },
                                timer -> {
                                    double count = timer.count();
                                    if (!config.logInactive() && count == 0) {
                                        return;
                                    }
                                    log.debug(print.id() + " throughput=" + print.rate(count) +
                                            " mean=" + print.time(timer.mean(getBaseTimeUnit())));
                                },
                                meter -> {
                                    log.debug(writeMeter(meter, print));
                                }
                        );
                    });
        }
    }

    String writeMeter(Meter meter, Printer print) {
        return StreamSupport.stream(meter.measure().spliterator(), false)
                .map(ms -> {
                    String msLine = ms.getStatistic().getTagValueRepresentation() + "=";
                    switch (ms.getStatistic()) {
                        case TOTAL:
                        case MAX:
                        case VALUE:
                            return msLine + print.value(ms.getValue());
                        case TOTAL_TIME:
                        case DURATION:
                            return msLine + print.time(ms.getValue());
                        case COUNT:
                            return "throughput=" + print.rate(ms.getValue());
                        default:
                            return msLine + decimalOrNan(ms.getValue());
                    }
                })
                .collect(joining(", ", print.id() + " ", ""));
    }

    @Override
    protected Timer newTimer(Meter.Id id, DistributionStatisticConfig distributionStatisticConfig,
            PauseDetector pauseDetector) {
        return new StepTimer(id, clock, distributionStatisticConfig, pauseDetector, getBaseTimeUnit(),
                this.config.step().toMillis(), false);
    }

    @Override
    protected DistributionSummary newDistributionSummary(Meter.Id id,
            DistributionStatisticConfig distributionStatisticConfig, double scale) {
        return new StepDistributionSummary(id, clock, distributionStatisticConfig, scale,
                config.step().toMillis(), false);
    }

    @Override
    protected TimeUnit getBaseTimeUnit() {
        return TimeUnit.MILLISECONDS;
    }

    public static class Builder {

        private final LoggerRegistryConfig config;

        private Clock clock = Clock.SYSTEM;

        Builder(LoggerRegistryConfig config) {
            this.config = config;
        }

        public LoggerMeterRegistry.Builder clock(Clock clock) {
            this.clock = clock;
            return this;
        }


        public LoggerMeterRegistry build() {
            return new LoggerMeterRegistry(config, clock);
        }
    }

    public class Printer {

        private final Meter meter;

        Printer(Meter meter) {
            this.meter = meter;
        }

        public String id() {
            return getConventionName(meter.getId()) + getConventionTags(meter.getId()).stream()
                    .map(t -> t.getKey() + "=" + t.getValue())
                    .collect(joining(",", "{", "}"));
        }

        public String time(double time) {
            return TimeUtils
                    .format(Duration.ofNanos((long) TimeUtils.convert(time, getBaseTimeUnit(), TimeUnit.NANOSECONDS)));
        }

        public String rate(double rate) {
            return humanReadableBaseUnit(rate / (double) config.step().getSeconds()) + "/s";
        }

        public String unitlessRate(double rate) {
            return decimalOrNan(rate / (double) config.step().getSeconds()) + "/s";
        }

        public String value(double value) {
            return humanReadableBaseUnit(value);
        }

        // see https://stackoverflow.com/a/3758880/510017
        public String humanReadableByteCount(double bytes) {
            int unit = 1024;
            if (bytes < unit || Double.isNaN(bytes)) {
                return decimalOrNan(bytes) + " B";
            }
            int exp = (int) (Math.log(bytes) / Math.log(unit));
            String pre = "KMGTPE".charAt(exp - 1) + "i";
            return decimalOrNan(bytes / Math.pow(unit, exp)) + " " + pre + "B";
        }

        public String humanReadableBaseUnit(double value) {
            String baseUnit = meter.getId().getBaseUnit();
            if (BaseUnits.BYTES.equals(baseUnit)) {
                return humanReadableByteCount(value);
            }
            return decimalOrNan(value) + (baseUnit != null ? " " + baseUnit : "");
        }
    }
}
