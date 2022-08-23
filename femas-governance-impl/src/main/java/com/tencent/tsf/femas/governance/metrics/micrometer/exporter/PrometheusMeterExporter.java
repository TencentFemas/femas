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

package com.tencent.tsf.femas.governance.metrics.micrometer.exporter;

import com.tencent.tsf.femas.plugin.impl.FemasPluginContext;
import com.tencent.tsf.femas.governance.metrics.micrometer.CompositeMetricsData;
import com.tencent.tsf.femas.governance.metrics.micrometer.MicroMeterExporter;
import com.tencent.tsf.femas.governance.metrics.micrometer.MicrometerMeterRegistry;
import io.micrometer.core.instrument.Clock;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.prometheus.client.CollectorRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author leoziltong
 * @Date: 2021/7/26 16:47
 */
public class PrometheusMeterExporter extends MicroMeterExporter {

    private static final Logger LOG = LoggerFactory.getLogger(PrometheusMeterExporter.class);

    private CollectorRegistry collectorRegistry = new CollectorRegistry(true);

    public PrometheusMeterExporter() {
        LOG.info("femas meter prometheus init.....");
    }

    public void initPrometheus() {
        MicrometerMeterRegistry micrometerMeterRegistry = (MicrometerMeterRegistry) FemasPluginContext
                .getMeterRegistry().get(0);
        PrometheusMeterRegistry registry = new PrometheusMeterRegistry(new PrometheusPropertiesConfigAdapter(),
                collectorRegistry, Clock.SYSTEM);
        micrometerMeterRegistry.addMeterRegistry(registry);
    }

    public CollectorRegistry getCollectorRegistry() {
        return collectorRegistry;
    }

    @Override
    public void unRegisterMetricsEvent(CompositeMetricsData data) {
        super.unRegisterMetricsEvent(data);
    }

    @Override
    public void report() {
        super.report();
    }

    @Override
    public String getName() {
        return "prometheus";
    }

    @Override
    public void run() {

    }
}
