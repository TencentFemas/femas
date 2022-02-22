package com.tencent.tsf.femas.governance.metrics.micrometer.meter;

import com.tencent.tsf.femas.governance.metrics.MeterEnum;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import java.util.List;

/**
 * @Author p_mtluo
 * @Date 2021-08-17 20:10
 * @Description counter
 **/
public class MicroCounter extends MicroMeter<io.micrometer.core.instrument.Counter>
        implements com.tencent.tsf.femas.governance.metrics.Counter<Counter> {


    public MicroCounter(MeterRegistry registry, MeterEnum meterType, String meterName, List<Tag> tagList) {
        super(registry, meterType, meterName, tagList);
    }

    @Override
    public Counter createMeter() {
        return this.registry.counter(this.meterName, this.tagList);
    }

    @Override
    public void increment(double amount) {
        this.meter.increment(amount);
    }

    @Override
    public void increment() {
        this.meter.increment();
    }
}
