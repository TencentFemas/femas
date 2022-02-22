package com.tencent.tsf.femas.extension.springcloud.common.discovery.nacos;

import com.alibaba.cloud.nacos.registry.NacosRegistration;
import com.alibaba.cloud.nacos.registry.NacosRegistrationCustomizer;
import com.tencent.tsf.femas.common.serviceregistry.AbstractServiceRegistryMetadata;
import com.tencent.tsf.femas.common.serviceregistry.AbstractServiceRegistryMetadataFactory;
import java.util.Map;

/**
 * @author juanyinyang
 */
public class FemasNacosRegistrationCustomizer implements NacosRegistrationCustomizer {

    private volatile AbstractServiceRegistryMetadata serviceRegistryMetadata = AbstractServiceRegistryMetadataFactory
            .getServiceRegistryMetadata();

    public FemasNacosRegistrationCustomizer() {
    }

    @Override
    public void customize(NacosRegistration registration) {
        Map<String, String> registerMetadataMap = serviceRegistryMetadata.getRegisterMetadataMap();
        registerMetadataMap.put("protocol", "spring-cloud-nacos");
        registration.getMetadata().putAll(registerMetadataMap);
    }
}
