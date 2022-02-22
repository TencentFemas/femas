package com.tencent.tsf.femas.registry.impl.eureka.serviceregistry;

import com.tencent.tsf.femas.common.RegistryEnum;
import com.tencent.tsf.femas.common.serviceregistry.ServiceRegistry;
import com.tencent.tsf.femas.common.serviceregistry.ServiceRegistryFactory;
import java.util.Map;

/**
 * @author leo
 */
public class EurekaServiceRegistryFactory implements ServiceRegistryFactory {

    @Override
    public ServiceRegistry getServiceRegistry(Map<String, String> configMap) {
        return new EurekaServiceRegistry(configMap);
    }

    @Override
    public String getType() {
        return RegistryEnum.EUREKA.name();
    }
}
