/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */

package com.tencent.tsf.femas.registry.impl.consul.serviceregistry;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.OperationException;
import com.ecwid.consul.v1.agent.model.NewService;
import com.tencent.tsf.femas.common.util.NamedThreadFactory;
import com.tencent.tsf.femas.common.util.TimeUtil;
import com.tencent.tsf.femas.registry.impl.consul.config.ConsulConfig;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zhixinzxliu
 */
public class HeartbeatScheduler {

    private static final Logger logger = LoggerFactory.getLogger(HeartbeatScheduler.class);
    private final static String CHECK_ID_PREFIX = "service:";
    private final Map<String, ScheduledFuture> serviceHeartbeats = new ConcurrentHashMap<>();
    private final ScheduledExecutorService heartbeatServiceExecutor = Executors
            .newScheduledThreadPool(1, new NamedThreadFactory("ConsulHeartbeatServiceTimer", true));
    private final ConsulClient client;
    private final ConsulConfig consulConfig;
    private final ConsulServiceRegistry registry;
    private double intervalRatio = 2.0 / 3.0;
    private Random random = new Random(System.currentTimeMillis());

    public HeartbeatScheduler(ConsulClient client, ConsulConfig consulConfig, ConsulServiceRegistry registry) {
        this.registry = registry;
        this.client = client;
        this.consulConfig = consulConfig;
    }

    public void add(NewService instance) {
        long heartbeatInterval = computeHeartbeatInterval(Integer.parseInt(consulConfig.getTtl()));
        ScheduledFuture task = heartbeatServiceExecutor
                .scheduleAtFixedRate(new ConsulHeartbeatServiceTask(instance, consulConfig.getToken()),
                        3000, heartbeatInterval, TimeUnit.MILLISECONDS);
        ScheduledFuture previousTask = serviceHeartbeats.put(instance.getId(), task);
        if (previousTask != null) {
            previousTask.cancel(true);
        }
    }

    /**
     * 根据ttl计算heartbeat间隔，单位ms
     *
     * @param ttlValue
     * @return
     */
    protected long computeHeartbeatInterval(int ttlValue) {
        // heartbeat rate at ratio * ttl, but no later than ttl -1s and, (under lesser
        // priority), no sooner than 1s from now
        double interval = ttlValue * intervalRatio;
        double max = Math.max(interval, 1);
        max += random.nextInt(10);
        int ttlMinus1 = ttlValue - 1;
        double min = Math.min(ttlMinus1, max);
        long period = Math.round(1000 * min);
        return period;
    }

    public void remove(String instanceId) {
        ScheduledFuture task = serviceHeartbeats.get(instanceId);
        if (task != null) {
            task.cancel(true);
        }
        serviceHeartbeats.remove(instanceId);
    }


    private class ConsulHeartbeatServiceTask implements Runnable {

        private String checkId;
        private String instanceId;
        private String aclToken;
        private NewService instance;

        ConsulHeartbeatServiceTask(NewService instance, String aclToken) {
            this.instance = instance;
            this.instanceId = instance.getId();
            this.checkId = instance.getId();
            this.aclToken = aclToken;
            if (!checkId.startsWith(CHECK_ID_PREFIX)) {
                checkId = CHECK_ID_PREFIX + checkId;
            }
        }

        @Override
        public void run() {
            for (int i = 0; i < 3; i++) {
                // 定时心跳时，如果返回OperationException异常，如不存在相应的注册信息时，进行重新注册
                try {
                    client.agentCheckPass(checkId, null, aclToken);
                    logger.debug("Send consul heartbeat for: " + checkId + " succeed.");

                    return;
                } catch (OperationException ex) {
                    logger.warn("Sending consul heartbeat error, checkId: {}, operationException: ", checkId, ex);
                    logger.warn("Sending consul heartbeat fail, will try register operation...");

                    try {
                        client.agentServiceRegister(instance, aclToken);
                        logger.warn("Register consul instance success, instance: {}", instance);
                    } catch (Exception exception) {
                        logger.error("Sending consul heartbeat register error, checkId: " + instance + ", exception: ",
                                exception);
                    }
                } catch (Throwable t) {
                    logger.warn("Sending consul heartbeat fail, will try send heartbeat...", t);

                    TimeUtil.silentlySleep(2000);
                }
            }

            logger.error("Send consul heartbeat failed!");
        }
    }

}
