package com.tencent.tsf.femas.agent.common.discovery;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.loadbalancer.Server;
import com.netflix.niws.loadbalancer.DiscoveryEnabledServer;
import com.tencent.tsf.femas.common.constant.FemasConstant;
import com.tencent.tsf.femas.common.entity.EndpointStatus;
import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.common.entity.ServiceInstance;
import org.apache.commons.collections.MapUtils;

import java.util.Map;

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
            InstanceInfo instanceInfo = eurekaServer.getInstanceInfo();
            instance.setAllMetadata(instanceInfo.getMetadata());
            instance.setHost(instanceInfo.getIPAddr());
            instance.setPort(instanceInfo.getPort());
            Service service = new Service();
            Map<String, String> metadata = instanceInfo.getMetadata();
            String appName = instanceInfo.getAppName();
            String nameSpace = metadata.get(FemasConstant.FEMAS_META_NAMESPACE_ID_KEY);
            service.setNamespace(nameSpace);
            service.setName(appName);
            instance.setService(service);
            instance.setStatus(EndpointStatus.getTypeByName(instanceInfo.getStatus().name()));
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