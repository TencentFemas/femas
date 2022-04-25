package com.tencent.tsf.femas.registry.impl.etcd.discovery;

import com.tencent.tsf.femas.common.RegistryEnum;
import com.tencent.tsf.femas.common.discovery.ServiceDiscoveryClient;
import com.tencent.tsf.femas.common.discovery.ServiceDiscoveryFactory;

import java.util.Map;

/**
 * @author huyuanxin
 */
public class EtcdServiceDiscoveryFactory implements ServiceDiscoveryFactory {

    @Override
    public String getType() {
        return RegistryEnum.ETCD.name();
    }

    @Override
    public ServiceDiscoveryClient getServiceDiscovery(Map<String, String> configMap) {
        return new EtcdServiceDiscoveryClient(configMap);
    }
}
