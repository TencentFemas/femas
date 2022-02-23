package com.tencent.tsf.femas.governance.metrics.micrometer.meter;

import com.tencent.tsf.femas.governance.metrics.MeterEnum;
import io.micrometer.core.instrument.LongTaskTimer;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import java.util.List;

/**
 * @Author p_mtluo
 * @Date 2021-08-17 20:12
 * @Description long task timer
 **/
public class MicroLongTaskTimer extends MicroMeter<io.micrometer.core.instrument.LongTaskTimer>
        implements com.tencent.tsf.femas.governance.metrics.LongTaskTimer<LongTaskTimer, LongTaskTimer.Sample> {

    public MicroLongTaskTimer(MeterRegistry registry, MeterEnum meterType, String meterName, List<Tag> tagList) {
        super(registry, meterType, meterName, tagList);
    }

    @Override
    public LongTaskTimer createMeter() {
        return this.registry.more().longTaskTimer(this.meterName, this.tagList);
    }

    @Override
    public LongTaskTimer.Sample start() {
        return this.meter.start();
    }

    @Override
    public long stop(long task) {
        return this.meter.stop(task);
    }
}
