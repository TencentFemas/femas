package com.tencent.tsf.femas.registry.impl.consul.discovery;

import com.tencent.tsf.femas.common.RegistryEnum;
import com.tencent.tsf.femas.common.discovery.ServiceDiscoveryClient;
import com.tencent.tsf.femas.common.discovery.ServiceDiscoveryFactory;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author zhixinzxliu
 */
public class ConsulServiceDiscoveryFactory implements ServiceDiscoveryFactory {

    private static Map<String, ServiceDiscoveryClient> clientMap = new ConcurrentHashMap<>();

    @Override
    public String getType() {
        return RegistryEnum.CONSUL.name();
    }

    @Override
    public ServiceDiscoveryClient getServiceDiscovery(Map<String, String> configMap) {
        String key = getKey(configMap);
        if (!clientMap.containsKey(key)) {
            ServiceDiscoveryClient client = new ConsulServiceDiscoveryClient(configMap);
            clientMap.putIfAbsent(key, client);
        }
        return clientMap.get(key);
    }

}
