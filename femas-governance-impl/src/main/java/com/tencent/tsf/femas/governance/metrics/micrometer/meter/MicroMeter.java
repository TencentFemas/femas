package com.tencent.tsf.femas.governance.metrics.micrometer.meter;

import com.tencent.tsf.femas.governance.metrics.Meter;
import com.tencent.tsf.femas.governance.metrics.MeterEnum;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import java.util.List;

/**
 * @Author p_mtluo
 * @Date 2021-08-17 17:09
 * @Description meter
 **/
public abstract class MicroMeter<T extends io.micrometer.core.instrument.Meter> implements Meter<T> {

    final MeterRegistry registry;
    final MeterEnum meterType;
    final String meterName;
    final List<Tag> tagList;
    final T meter;


    public MicroMeter(MeterRegistry registry, MeterEnum meterType, String meterName, List<Tag> tagList) {
        this.registry = registry;
        this.meterType = meterType;
        this.meterName = meterName;
        this.tagList = tagList;
        this.meter = createMeter();
    }

    public abstract T createMeter();

    @Override
    public MeterEnum getMeterType() {
        return this.meterType;
    }

    @Override
    public String getMeterName() {
        return this.meterName;
    }

    @Override
    public T getMeter() {
        return meter;
    }
}
