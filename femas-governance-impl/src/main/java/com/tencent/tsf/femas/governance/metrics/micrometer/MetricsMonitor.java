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

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * @Author leoziltong
 * @Date: 2021/7/8 15:25
 */
public class MetricsMonitor {

    /**
     * 多个Metrics 一个exporter
     */
//    private static GovernanceMeterRegistry governanceMeterRegistry;
////    private static RpcMeterRegistry rpcMeterRegistry;
////    private static JmxMeterRegistry jmxMeterRegistry;
//    private static MetricsExporter exporter;

    static {
//        governanceMeterRegistry = (GovernanceMeterRegistry) DefaultConfigurablePluginHolder.getSDKContext().getPlugin(SPIPluginType.METRICS.getInterfaces(), "microMeter");
////        rpcMeterRegistry = (RpcMeterRegistry) DefaultConfigurablePluginHolder.getSDKContext().getPlugin(SPIPluginType.METRICS.getInterfaces(), "rpcMeter");
////        jmxMeterRegistry = (JmxMeterRegistry) DefaultConfigurablePluginHolder.getSDKContext().getPlugin(SPIPluginType.METRICS.getInterfaces(), "jmxMeter");
//        exporter = FemasPluginContext.getMetricsExporter();
////        jmxMeterRegistry.register();
//        exporter.report();
    }

    public static void main(String[] args) throws Exception {

        System.out.println("-------------count-------------");
        //tag必须成对出现，也就是偶数个
        Counter counter = Counter.builder("counter")
                .tag("counter", "counter")
                .description("counter")
                .register(new SimpleMeterRegistry());
        counter.increment();
        counter.increment(2D);
        System.out.println(counter.count());
        System.out.println(counter.measure());
        //全局静态方法
        Metrics.addRegistry(new SimpleMeterRegistry());
        counter = Metrics.counter("counter", "counter", "counter");
        counter.increment(10086D);
        counter.increment(10087D);
        System.out.println(counter.count());
        System.out.println(counter.measure());

        System.out.println("-------------gauge-------------");
        AtomicInteger atomicInteger = new AtomicInteger();
        Gauge gauge = Gauge.builder("gauge", atomicInteger, AtomicInteger::get)
                .tag("gauge", "gauge")
                .description("gauge")
                .register(new SimpleMeterRegistry());
        atomicInteger.addAndGet(5);
        System.out.println(gauge.value());
        System.out.println(gauge.measure());
        atomicInteger.decrementAndGet();
        System.out.println(gauge.value());
        System.out.println(gauge.measure());
        //全局静态方法，返回值竟然是依赖值，有点奇怪，暂时不选用
        Metrics.addRegistry(new SimpleMeterRegistry());
        AtomicInteger other = Metrics.gauge("gauge", atomicInteger, AtomicInteger::get);

        System.out.println("-------------timer-------------");
        Timer timer = Timer.builder("timer")
                .tag("timer", "timer")
                .description("timer")
                .register(new SimpleMeterRegistry());

        timer.record(1, TimeUnit.MILLISECONDS);
        timer.record(() -> {
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                //ignore
            }
        });
        System.out.println(timer.count());
        System.out.println(timer.measure());
        System.out.println(timer.totalTime(TimeUnit.SECONDS));
        System.out.println(timer.mean(TimeUnit.SECONDS));
        System.out.println(timer.max(TimeUnit.SECONDS));

//        SimpleMeterRegistry registry = new SimpleMeterRegistry();

        System.out.println("-------------summary-------------");
        DistributionSummary summary = DistributionSummary.builder("summary")
                .tag("summary", "summary")
                .description("summary")
                .register(new SimpleMeterRegistry());
        summary.record(2D);
        summary.record(3D);
        summary.record(4D);
        System.out.println(summary.measure());
        System.out.println(summary.count());
        System.out.println(summary.max());
        System.out.println(summary.mean());
        System.out.println(summary.totalAmount());
    }

//    public static void timerRecord(Endpoint endpoint, ErrorStatus status, Long timestamp) {
//        governanceMeterRegistry.timerRecord(endpoint, timestamp);
//    }
//
//    public static void counterFailed(Endpoint endpoint, ModuleEnum module, ErrorStatus status) {
//        governanceMeterRegistry.counterFailed(endpoint, module);
//    }

}