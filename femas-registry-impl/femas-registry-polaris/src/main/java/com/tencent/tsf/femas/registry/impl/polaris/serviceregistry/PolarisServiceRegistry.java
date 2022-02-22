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

import java.util.Map;

import com.tencent.polaris.api.core.ProviderAPI;
import com.tencent.polaris.api.rpc.InstanceDeregisterRequest;
import com.tencent.polaris.api.rpc.InstanceRegisterRequest;
import com.tencent.polaris.api.rpc.InstanceRegisterResponse;
import com.tencent.polaris.factory.api.DiscoveryAPIFactory;
import com.tencent.tsf.femas.common.entity.EndpointStatus;
import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.common.entity.ServiceInstance;
import com.tencent.tsf.femas.common.serviceregistry.AbstractServiceRegistry;

/**
* <pre>  
* 文件名称：PolarisServiceRegistry.java  
* 创建时间：Jan 2, 2022 4:34:11 PM   
* @author juanyinyang  
* 类说明：  
*/
public class PolarisServiceRegistry extends AbstractServiceRegistry {
    
    private ProviderAPI providerAPI = null;

    public PolarisServiceRegistry(Map<String, String> configMap) {
        providerAPI = DiscoveryAPIFactory.createProviderAPI();
    }
    
    /** 
     * @see com.tencent.tsf.femas.common.serviceregistry.AbstractServiceRegistry#doRegister(com.tencent.tsf.femas.common.entity.ServiceInstance)
     */
    @Override
    protected void doRegister(ServiceInstance serviceInstance) {
        logger.info("Registering service with polaris: " + serviceInstance);
        try {
            //执行服务注册 
            InstanceRegisterRequest request = new InstanceRegisterRequest();
            Service service = serviceInstance.getService();
            request.setNamespace(service.getNamespace());
            request.setService(service.getName());
            request.setHost(serviceInstance.getHost());
            request.setPort(serviceInstance.getPort());//实例port
            //TODO
//        request.setTtl(serviceInstance.get);//设置健康检查ttl
            logger.info("Service register to polaris request:"+ request);
            InstanceRegisterResponse instanceRegisterResponse = providerAPI.register(request);
            logger.info("Service register to polaris instanceRegisterResponse:" + instanceRegisterResponse);
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
            // 执行服务反注册
            InstanceDeregisterRequest request = new InstanceDeregisterRequest();
            Service service = serviceInstance.getService();
            request.setNamespace(service.getNamespace());
            request.setService(service.getName());
            request.setHost(serviceInstance.getHost());
            request.setPort(serviceInstance.getPort());//实例port
            logger.info("Service deregister to polaris request:"+ request);
            providerAPI.deRegister(request);
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
  