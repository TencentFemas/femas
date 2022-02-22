package com.tencent.tsf.femas.registry.impl.nacos.serviceregistry;

import com.tencent.tsf.femas.common.RegistryEnum;
import com.tencent.tsf.femas.common.serviceregistry.ServiceRegistry;
import com.tencent.tsf.femas.common.serviceregistry.ServiceRegistryFactory;
import java.util.Map;

/**
 * @author leo
 */
public class NacosServiceRegistryFactory implements ServiceRegistryFactory {

    @Override
    public ServiceRegistry getServiceRegistry(Map<String, String> configMap) {
        return new NacosServiceRegistry(configMap);
    }

    @Override
    public String getType() {
        return RegistryEnum.NACOS.name();
    }
}
