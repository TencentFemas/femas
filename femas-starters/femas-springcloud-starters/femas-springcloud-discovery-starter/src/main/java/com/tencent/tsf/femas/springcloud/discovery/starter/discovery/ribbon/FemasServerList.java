package com.tencent.tsf.femas.springcloud.discovery.starter.discovery.ribbon;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractServerList;
import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.common.entity.ServiceInstance;
import com.tencent.tsf.femas.springcloud.discovery.starter.discovery.FemasDiscoveryProperties;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 描述：
 * 创建日期：2022年05月26 00:29:21
 *
 * @author gong zhao
 **/
public class FemasServerList extends AbstractServerList<FemasServer> {

    private FemasDiscoveryProperties discoveryProperties;

    private String serviceId;

    public FemasServerList(FemasDiscoveryProperties discoveryProperties) {
        this.discoveryProperties = discoveryProperties;
    }

    @Override
    public List<FemasServer> getInitialListOfServers() {
        return getServers();
    }

    @Override
    public List<FemasServer> getUpdatedListOfServers() {
        return getServers();
    }

    private List<FemasServer> getServers() {
        try {
            List<ServiceInstance> instances = discoveryProperties.serviceDiscoveryClient()
                    .getInstances(new Service(discoveryProperties.getNamespace(), discoveryProperties.getService()));
            return instancesToServerList(instances);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Can not get service instances from femas, serviceId=" + serviceId,
                    e);
        }
    }

    private List<FemasServer> instancesToServerList(List<ServiceInstance> instances) {
        List<FemasServer> result = new ArrayList<>();
        if (CollectionUtils.isEmpty(instances)) {
            return result;
        }
        for (ServiceInstance instance : instances) {
            result.add(new FemasServer(instance));
        }

        return result;
    }

    public String getServiceId() {
        return serviceId;
    }

    @Override
    public void initWithNiwsConfig(IClientConfig iClientConfig) {
        this.serviceId = iClientConfig.getClientName();
    }
}
