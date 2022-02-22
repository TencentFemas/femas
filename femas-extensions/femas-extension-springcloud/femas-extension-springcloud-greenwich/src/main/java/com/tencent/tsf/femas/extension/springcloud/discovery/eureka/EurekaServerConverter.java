package com.tencent.tsf.femas.extension.springcloud.discovery.eureka;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.loadbalancer.Server;
import com.netflix.niws.loadbalancer.DiscoveryEnabledServer;
import com.tencent.tsf.femas.common.entity.EndpointStatus;
import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.common.entity.ServiceInstance;
import com.tencent.tsf.femas.extension.springcloud.discovery.ribbon.DiscoveryServerConverter;
import java.util.Map;
import org.apache.commons.collections.MapUtils;

/**
 * @Author leoziltong
 * @Date: 2021/6/16 16:32
 */
public class EurekaServerConverter implements DiscoveryServerConverter {

    @Override
    public ServiceInstance convert(Server server) {
        ServiceInstance instance = new ServiceInstance();
        if (server instanceof DiscoveryEnabledServer) {
            DiscoveryEnabledServer eurekaServer = (DiscoveryEnabledServer) server;
            InstanceInfo i = eurekaServer.getInstanceInfo();
            instance.setAllMetadata(i.getMetadata());
            instance.setHost(i.getIPAddr());
            instance.setPort(i.getPort());
            instance.setService(new Service(getNamespace(), getServiceName()));
            instance.setStatus(EndpointStatus.getTypeByName(i.getStatus().name()));
            instance.setOrigin(server);
            return instance;
        }
        return null;
    }

    @Override
    public Server getOrigin(ServiceInstance serviceInstance) {
        if (serviceInstance.getOrigin() instanceof DiscoveryEnabledServer) {
            return (Server) serviceInstance.getOrigin();
        }
        return null;
    }

    @Override
    public Map<String, String> getServerMetadata(Server server) {
        if (server instanceof DiscoveryEnabledServer) {
            DiscoveryEnabledServer discoveryEnabledServer = (DiscoveryEnabledServer) server;
            return discoveryEnabledServer.getInstanceInfo().getMetadata();
        }
        return MapUtils.EMPTY_MAP;
    }
}