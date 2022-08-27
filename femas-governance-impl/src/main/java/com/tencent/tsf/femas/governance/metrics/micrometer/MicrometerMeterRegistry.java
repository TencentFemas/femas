/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tencent.tsf.femas.governance.metrics.micrometer;

import com.tencent.tsf.femas.common.exception.FemasRuntimeException;
import com.tencent.tsf.femas.governance.metrics.IMeterRegistry;
import com.tencent.tsf.femas.governance.metrics.MeterEnum;
import com.tencent.tsf.femas.governance.metrics.TagPair;
import com.tencent.tsf.femas.governance.metrics.micrometer.meter.MicroCounter;
import com.tencent.tsf.femas.governance.metrics.micrometer.meter.MicroDistributionSummary;
import com.tencent.tsf.femas.governance.metrics.micrometer.meter.MicroLongTaskTimer;
import com.tencent.tsf.femas.governance.metrics.micrometer.meter.MicroMeter;
import com.tencent.tsf.femas.governance.metrics.micrometer.meter.MicroTimer;
import com.tencent.tsf.femas.plugin.context.ConfigContext;
import io.micrometer.core.instrument.ImmutableTag;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author leoziltong
 * @Date: 2021/7/8 19:09
 */
public abstract class MicrometerMeterRegistry implements IMeterRegistry<MicroMeter, AtomicInteger> {

    protected CompositeMeterRegistry METER_REGISTRY = new CompositeMeterRegistry();

    public void addMeterRegistry(MeterRegistry registry) {
        this.METER_REGISTRY.add(registry);
    }

    @Override
    public MicroDistributionSummary summary(String name, List<TagPair> tags) {
        return new MicroDistributionSummary(METER_REGISTRY, MeterEnum.DISTRIBUTION_SUMMARY, name, buildTags(tags));
    }


    @Override
    public MicroTimer timer(String name, List<TagPair> tags) {
        return new MicroTimer(METER_REGISTRY, MeterEnum.TIMER, name, buildTags(tags));
    }

    @Override
    public MicroCounter counter(String name, List<TagPair> tags) {
        return new MicroCounter(METER_REGISTRY, MeterEnum.COUNTER, name, buildTags(tags));
    }

    @Override
    public MicroLongTaskTimer longTaskTimer(String name, List<TagPair> tags) {
        return new MicroLongTaskTimer(METER_REGISTRY, MeterEnum.LONG_TASK_TIMER, name, buildTags(tags));
    }

    @Override
    public AtomicInteger gauge(String name, List<TagPair> tags) {
        return METER_REGISTRY.gauge(name, buildTags(tags), new AtomicInteger());
    }

    private List<Tag> buildTags(List<TagPair> tags) {
        List<Tag> tagList = new ArrayList<>(tags.size());
        tags.stream().forEach(t -> tagList.add(new ImmutableTag(t.getKey(), t.getValue())));
        return tagList;
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


}