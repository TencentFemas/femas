package com.tencent.tsf.femas.registry.impl.k8s.serviceregistry;

import com.tencent.tsf.femas.common.entity.EndpointStatus;
import com.tencent.tsf.femas.common.entity.ServiceInstance;
import com.tencent.tsf.femas.common.serviceregistry.AbstractServiceRegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;


/**
 * @author leo
 */
public class K8sServiceRegistry extends AbstractServiceRegistry {
    private static final Logger logger = LoggerFactory.getLogger(K8sServiceRegistry.class);


    public K8sServiceRegistry(Map<String, String> configMap) {
    }

    @Override
    protected void doRegister(ServiceInstance serviceInstance) {

    }

    @Override
    protected void doDeregister(ServiceInstance serviceInstance) {

    }


    @Override
    public void setStatus(ServiceInstance serviceInstance, EndpointStatus status) {

    }

    @Override
    public EndpointStatus getStatus(ServiceInstance serviceInstance) {
        return null;
    }
}
