package com.tencent.tsf.femas.registry.impl.zookeeper.serviceregistry;

import com.tencent.tsf.femas.common.RegistryEnum;
import com.tencent.tsf.femas.common.serviceregistry.ServiceRegistry;
import com.tencent.tsf.femas.common.serviceregistry.ServiceRegistryFactory;

import java.util.Map;

/**
 * @author huyuanxin
 */
public class ZookeeperServiceRegistryFactory implements ServiceRegistryFactory {
    @Override
    public String getType() {
        return RegistryEnum.ZOOKEEPER.name();
    }

    @Override
    public ServiceRegistry getServiceRegistry(Map<String, String> configMap) {
        return new ZookeeperServiceRegistry(configMap);
    }
}
