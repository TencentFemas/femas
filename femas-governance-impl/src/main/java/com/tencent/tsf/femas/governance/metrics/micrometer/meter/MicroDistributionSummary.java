package com.tencent.tsf.femas.governance.metrics.micrometer.meter;

import com.tencent.tsf.femas.governance.metrics.MeterEnum;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import java.util.List;

/**
 * @Author p_mtluo
 * @Date 2021-08-17 20:07
 * @Description DistributionSummary
 **/
public class MicroDistributionSummary extends MicroMeter<io.micrometer.core.instrument.DistributionSummary>
        implements com.tencent.tsf.femas.governance.metrics.DistributionSummary<DistributionSummary> {


    public MicroDistributionSummary(MeterRegistry registry, MeterEnum meterType, String meterName, List<Tag> tagList) {
        super(registry, meterType, meterName, tagList);
    }

    @Override
    public DistributionSummary createMeter() {
        return this.registry.summary(this.meterName, this.tagList);
    }

    @Override
    public void record(double amount) {
        this.meter.record(amount);
    }
}
