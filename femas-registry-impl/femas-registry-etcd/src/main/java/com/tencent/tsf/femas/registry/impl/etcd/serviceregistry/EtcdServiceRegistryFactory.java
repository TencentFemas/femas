package com.tencent.tsf.femas.registry.impl.etcd.serviceregistry;

import com.tencent.tsf.femas.common.RegistryEnum;
import com.tencent.tsf.femas.common.serviceregistry.ServiceRegistry;
import com.tencent.tsf.femas.common.serviceregistry.ServiceRegistryFactory;

import java.util.Map;

/**
 * @author huyuanxin
 */
public class EtcdServiceRegistryFactory implements ServiceRegistryFactory {

    @Override
    public String getType() {
        return RegistryEnum.ETCD.name();
    }

    @Override
    public ServiceRegistry getServiceRegistry(Map<String, String> configMap) {
        return new EtcdServiceRegistry(configMap);
    }

}
