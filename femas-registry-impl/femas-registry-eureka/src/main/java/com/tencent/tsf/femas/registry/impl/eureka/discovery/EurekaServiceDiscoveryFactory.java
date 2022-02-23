package com.tencent.tsf.femas.registry.impl.eureka.discovery;

import com.tencent.tsf.femas.common.RegistryEnum;
import com.tencent.tsf.femas.common.discovery.ServiceDiscoveryClient;
import com.tencent.tsf.femas.common.discovery.ServiceDiscoveryFactory;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @Author leoziltong
 * @Description //TODO
 * @Date: 2021/3/24 15:25
 * @Version 1.0
 */
public class EurekaServiceDiscoveryFactory implements ServiceDiscoveryFactory {


    private static Map<String, ServiceDiscoveryClient> clientMap = new ConcurrentHashMap<>();

    @Override
    public ServiceDiscoveryClient getServiceDiscovery(Map<String, String> configMap) {
        String key = getKey(configMap);
        if (!clientMap.containsKey(key)) {
            ServiceDiscoveryClient client = new EurekaServiceDiscoveryClient(configMap);
            clientMap.putIfAbsent(key, client);
        }
        return clientMap.get(key);
    }

    @Override
    public String getType() {
        return RegistryEnum.EUREKA.name();
    }
}
