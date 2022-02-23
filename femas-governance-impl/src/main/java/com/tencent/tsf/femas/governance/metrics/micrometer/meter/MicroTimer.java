package com.tencent.tsf.femas.governance.metrics.micrometer.meter;

import com.tencent.tsf.femas.governance.metrics.MeterEnum;
import com.tencent.tsf.femas.governance.metrics.Timer;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Author p_mtluo
 * @Date 2021-08-17 17:09
 * @Description timer
 **/
public class MicroTimer extends MicroMeter<io.micrometer.core.instrument.Timer>
        implements Timer<io.micrometer.core.instrument.Timer> {


    public MicroTimer(MeterRegistry registry, MeterEnum meterType, String meterName, List<Tag> tagList) {
        super(registry, meterType, meterName, tagList);
    }

    @Override
    public io.micrometer.core.instrument.Timer createMeter() {
        return io.micrometer.core.instrument.Timer.builder(this.meterName).tags(this.tagList)
                .publishPercentiles(0.5, 0.75, 0.9, 0.99)
//                .publishPercentileHistogram()
//                .sla(Duration.ofSeconds(60))
//                .minimumExpectedValue(Duration.ofMillis(1)).maximumExpectedValue(Duration.ofSeconds(30))
                .register(this.registry);
//        return this.registry.timer(this.meterName, this.tagList);

//        return io.micrometer.core.instrument.Timer.builder(this.meterName).tags(this.tagList).register(this.registry);
    }


    @Override
    public void record(long amount, TimeUnit unit) {
        this.meter.record(amount, unit);
    }
}
