/*
 * Tencent is pleased to support the open source community by making Femas available.
 *
 * Copyright (C) 2022, a Tencent company. All rights reserved.
 *
 * Licensed under the BSD 3-Clause License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.tencent.tsf.femas.registry.impl.polaris.serviceregistry;

import com.tencent.polaris.api.core.ProviderAPI;
import com.tencent.polaris.api.exception.ErrorCode;
import com.tencent.polaris.api.exception.PolarisException;
import com.tencent.polaris.api.rpc.InstanceDeregisterRequest;
import com.tencent.polaris.api.rpc.InstanceHeartbeatRequest;
import com.tencent.polaris.api.rpc.InstanceRegisterRequest;
import com.tencent.polaris.api.rpc.InstanceRegisterResponse;
import com.tencent.polaris.factory.api.DiscoveryAPIFactory;
import com.tencent.tsf.femas.common.entity.EndpointStatus;
import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.common.entity.ServiceInstance;
import com.tencent.tsf.femas.common.serviceregistry.AbstractServiceRegistry;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

import java.util.Map;

import java.util.Map;

/**
 * <pre>
 * 文件名称：PolarisServiceRegistry.java
 * 创建时间：Jan 2, 2022 4:34:11 PM
 * @author juanyinyang
 * 类说明：
 */
public class PolarisServiceRegistry extends AbstractServiceRegistry {

    private final ProviderAPI providerApi;
    private final PolarisBeatReactor polarisBeatReactor;

    public PolarisServiceRegistry(Map<String, String> configMap) {
        providerApi = DiscoveryAPIFactory.createProviderAPI();
        polarisBeatReactor = new PolarisBeatReactor(providerApi);
    }

    /**
     * @see com.tencent.tsf.femas.common.serviceregistry.AbstractServiceRegistry#doRegister(com.tencent.tsf.femas.common.entity.ServiceInstance)
     */
    @Override
    protected void doRegister(ServiceInstance serviceInstance) {
        logger.info("Registering service with polaris: " + serviceInstance);
        try {
            // 执行服务注册
            InstanceRegisterRequest request = new InstanceRegisterRequest();
            Service service = serviceInstance.getService();
            // 拿到信息
            String namespace = service.getNamespace();
            String serviceName = service.getName();
            String host = serviceInstance.getHost();
            Integer port = serviceInstance.getPort();

            request.setNamespace(namespace);
            request.setService(serviceName);
            request.setHost(host);
            request.setPort(port);
            InstanceInfo instanceInfo = null;
            Integer ttl = serviceInstance.getTtl();
            // 判断是否需要上报心跳,以及校验心跳间隔的合理性
            if (Boolean.TRUE.equals(serviceInstance.getHeartBeat()) && null != ttl && ttl != 0) {
                // 设置健康检查ttl
                request.setTtl(ttl);
                // 建立一个上报信息,给后面使用
                instanceInfo = new InstanceInfo(null, namespace, serviceName, port, host, ttl);
            }
            logger.info("Service register to polaris request:" + request);
            InstanceRegisterResponse instanceRegisterResponse = providerApi.register(request);
            logger.info("Service register to polaris instanceRegisterResponse:" + instanceRegisterResponse);
            if (null != instanceInfo) {
                instanceInfo.setInstanceId(serviceInstance.getId());
                polarisBeatReactor.addInstance(serviceInstance.getId(), instanceInfo);
            }
        } catch (Exception e) {
            logger.error("Error registering service with polaris: " + serviceInstance, e);
        }
        logger.info("Service " + serviceInstance + " registered.");
    }

    /**
     * @see com.tencent.tsf.femas.common.serviceregistry.AbstractServiceRegistry#doDeregister(com.tencent.tsf.femas.common.entity.ServiceInstance)
     */
    @Override
    protected void doDeregister(ServiceInstance serviceInstance) {
        logger.info("Deregistering service with polaris: " + serviceInstance);
        try {
            // 停止心跳
            polarisBeatReactor.removeInstance(serviceInstance.getId());
            // 执行服务反注册
            InstanceDeregisterRequest request = new InstanceDeregisterRequest();
            Service service = serviceInstance.getService();
            request.setNamespace(service.getNamespace());
            request.setService(service.getName());
            request.setHost(serviceInstance.getHost());
            request.setPort(serviceInstance.getPort());
            logger.info("Service deregister to polaris request:" + request);
            providerApi.deRegister(request);
        } catch (PolarisException e) {
            // 反注册后,如果心跳发送已经被载入schedule,会发生错误
            if (e.getCode().equals(ErrorCode.SERVER_USER_ERROR)) {
                logger.warn("Last heartbeats still in schedule,please ignore this error:{}", serviceInstance, e);
            }
        } catch (Exception e) {
            logger.error("Error deregisterInstance service with polaris:{} ", serviceInstance.toString(), e);
        }
        logger.info("Deregister service with polaris: " + serviceInstance.toString() + " success.");
    }
    /**
     * @see com.tencent.tsf.femas.common.serviceregistry.ServiceRegistry#setStatus(com.tencent.tsf.femas.common.entity.ServiceInstance, com.tencent.tsf.femas.common.entity.EndpointStatus)
     */
    @Override
    public void setStatus(ServiceInstance serviceInstance, EndpointStatus status) {
    }

    /**
     * @see com.tencent.tsf.femas.common.serviceregistry.ServiceRegistry#getStatus(com.tencent.tsf.femas.common.entity.ServiceInstance)
     */
    @Override
    public EndpointStatus getStatus(ServiceInstance serviceInstance) {
        return null;
    }
}