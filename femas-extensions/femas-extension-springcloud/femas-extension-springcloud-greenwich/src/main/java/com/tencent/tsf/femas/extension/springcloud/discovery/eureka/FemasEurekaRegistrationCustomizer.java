package com.tencent.tsf.femas.extension.springcloud.discovery.eureka;

import com.tencent.tsf.femas.common.serviceregistry.AbstractServiceRegistryMetadata;
import com.tencent.tsf.femas.common.serviceregistry.AbstractServiceRegistryMetadataFactory;

/**
 * TODO 实现待定，eureka 可能没有 RegistrationCustomizer 机制
 */
public class FemasEurekaRegistrationCustomizer {

    private volatile AbstractServiceRegistryMetadata serviceRegistryMetadata = AbstractServiceRegistryMetadataFactory
            .getServiceRegistryMetadata();

    public FemasEurekaRegistrationCustomizer() {
    }

}
