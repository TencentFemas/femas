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
import com.tencent.tsf.femas.governance.metrics.MetricsExporter;
import com.tencent.tsf.femas.governance.plugin.context.ConfigContext;
import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @Author leoziltong
 * @Date: 2021/7/15 14:34
 */
public abstract class MicroMeterExporter implements MetricsExporter {


    public static final int DEFAULT_THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors();
    private static final  Log LOG = LogFactory.getLog(MicroMeterExporter.class);
    public AtomicBoolean microMeterExporterActive = new AtomicBoolean(false);

    private ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(
            DEFAULT_THREAD_POOL_SIZE, r -> {
        Thread thread = new Thread(r);
        thread.setDaemon(true);
        thread.setName("com.tencent.microMeter.exporter");
        return thread;
    });

    public void registerMetricsEvent(CompositeMetricsData data) {

    }

    public void unRegisterMetricsEvent(CompositeMetricsData data) {
    }

    @Override
    public void report() {
        if (this.microMeterExporterActive.compareAndSet(true, false)) {
            fireExporter();
        }
    }

    public abstract void run();

    private void fireExporter() {
        scheduledExecutorService.scheduleAtFixedRate(this::run, 10L, step().getSeconds(), TimeUnit.SECONDS);
    }


    /**
     * 10s上报一次
     *
     * @return
     */
    @Override
    public Duration step() {
        return Duration.ofSeconds(10);
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
        return null;
    }
}
