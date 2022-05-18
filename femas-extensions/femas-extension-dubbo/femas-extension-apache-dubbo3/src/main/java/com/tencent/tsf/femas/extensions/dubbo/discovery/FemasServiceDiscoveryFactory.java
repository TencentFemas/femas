package com.tencent.tsf.femas.extensions.dubbo.discovery;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.registry.client.AbstractServiceDiscoveryFactory;
import org.apache.dubbo.registry.client.ServiceDiscovery;

public class FemasServiceDiscoveryFactory extends AbstractServiceDiscoveryFactory {

    @Override
    protected ServiceDiscovery createDiscovery(URL registryURL) {
        return new FemasServiceDiscovery(applicationModel, registryURL);
    }
}
