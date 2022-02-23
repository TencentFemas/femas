package com.tencent.tsf.femas.registry.impl.consul.serviceregistry;

import com.tencent.tsf.femas.common.RegistryEnum;
import com.tencent.tsf.femas.common.serviceregistry.ServiceRegistry;
import com.tencent.tsf.femas.common.serviceregistry.ServiceRegistryFactory;
import java.util.Map;

/**
 * @author zhixinzxliu
 */
public class ConsulServiceRegistryFactory implements ServiceRegistryFactory {

    @Override
    public ServiceRegistry getServiceRegistry(Map<String, String> configMap) {
        return new ConsulServiceRegistry(configMap);
    }

    @Override
    public String getType() {
        return RegistryEnum.CONSUL.name();
    }
}
