package com.tencent.tsf.femas.registry.impl.eureka.serviceregistry;/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE
 * file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file
 * to You under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

import static com.tencent.tsf.femas.common.RegistryConstants.DEFAULT_THREAD_POOL_SIZE;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.shared.transport.EurekaHttpClient;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author leoziltong
 */
public class EurekaBeatReactor {

    private final Logger log = LoggerFactory.getLogger(EurekaBeatReactor.class);
    private final Map<String, InstanceInfo> eurekaBeat = new ConcurrentHashMap<>();
    //fireHeatBeat执行行一次，在有instance注册时启动
    private final AtomicBoolean heatBeatingInProgress = new AtomicBoolean(true);
    private ScheduledExecutorService executorService;
    //心跳间隔
    private volatile long clientBeatInterval = 5 * 1000;
    private EurekaHttpClient eurekaHttpClient;

    public EurekaBeatReactor(EurekaHttpClient eurekaHttpClient) {
        this.eurekaHttpClient = eurekaHttpClient;
        executorService = new ScheduledThreadPoolExecutor(DEFAULT_THREAD_POOL_SIZE, r -> {
            Thread thread = new Thread(r);
            thread.setDaemon(true);
            thread.setName("com.tencent.femas.eureka.beat.sender");
            return thread;
        });
    }

    public void addInstance(String key, InstanceInfo value) {
        this.eurekaBeat.put(key, value);
        if (this.heatBeatingInProgress.compareAndSet(true, false)) {
            fireHeatBeat();
        }
    }

    public void removeInstance(String key) {
        log.debug("[BEAT] removing beat: {} to beat map.", key);
        this.eurekaBeat.remove(key);
    }

    private void fireHeatBeat() {
        executorService.schedule(new BeatProcessor(), 0, TimeUnit.SECONDS);
    }

    class BeatProcessor implements Runnable {

        @Override
        public void run() {
            try {
                for (Map.Entry<String, InstanceInfo> entry : eurekaBeat.entrySet()) {
                    InstanceInfo instanceInfo = entry.getValue();
                    executorService.schedule(() -> {
                        if (log.isDebugEnabled()) {
                            log.debug("[BEAT] adding beat: {} to beat map.", instanceInfo.getId());
                        }
                        eurekaHttpClient.sendHeartBeat(instanceInfo.getAppName(), instanceInfo.getId(), instanceInfo,
                                InstanceInfo.InstanceStatus.UP);
                    }, 0, TimeUnit.MILLISECONDS);
                }
            } catch (Exception e) {
                log.error("[CLIENT-BEAT] Exception while scheduling beat.", e);
            } finally {
                executorService.schedule(this, clientBeatInterval, TimeUnit.MILLISECONDS);
            }
        }
    }
}



