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

package com.tencent.tsf.femas.governance.metrics.micrometer.registry;

import com.tencent.tsf.femas.governance.metrics.micrometer.MicrometerMeterRegistry;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author leoziltong
 * @Date: 2021/7/26 16:44
 */
public class JmxMeterRegistry extends MicrometerMeterRegistry {

    private static final Logger log = LoggerFactory.getLogger(JmxMeterRegistry.class);

    private JvmGcMetrics jvmGcMetrics;

    private JvmMemoryMetrics jvmMemoryMetrics;

    private JvmThreadMetrics jvmThreadMetrics;

    private ClassLoaderMetrics classLoaderMetrics;

    @Override
    public String getName() {
        return "jmxMeter";
    }

    public void register() {
        jvmGcMetrics().bindTo(METER_REGISTRY);
        jvmMemoryMetrics().bindTo(METER_REGISTRY);
        jvmThreadMetrics().bindTo(METER_REGISTRY);
        classLoaderMetrics().bindTo(METER_REGISTRY);
        JVMShutdownHook jvmShutdownHook = new JVMShutdownHook();
        Runtime.getRuntime().addShutdownHook(jvmShutdownHook);
    }

    public JvmGcMetrics jvmGcMetrics() {
        if (jvmGcMetrics == null) {
            jvmGcMetrics = new JvmGcMetrics();
        }
        return jvmGcMetrics;
    }


    public JvmMemoryMetrics jvmMemoryMetrics() {
        if (jvmMemoryMetrics == null) {
            jvmMemoryMetrics = new JvmMemoryMetrics();
        }
        return jvmMemoryMetrics;
    }


    public JvmThreadMetrics jvmThreadMetrics() {
        if (jvmThreadMetrics == null) {
            jvmThreadMetrics = new JvmThreadMetrics();
        }
        return jvmThreadMetrics;
    }


    public ClassLoaderMetrics classLoaderMetrics() {
        if (classLoaderMetrics == null) {
            classLoaderMetrics = new ClassLoaderMetrics();
        }
        return classLoaderMetrics;
    }

    private class JVMShutdownHook extends Thread {

        public void run() {
            try {
                if (jvmGcMetrics != null) {
                    jvmGcMetrics.close();
                }
            } catch (Exception e) {
                log.error("JVMShutdownHook close resource failed", e);
            }
        }
    }

}
