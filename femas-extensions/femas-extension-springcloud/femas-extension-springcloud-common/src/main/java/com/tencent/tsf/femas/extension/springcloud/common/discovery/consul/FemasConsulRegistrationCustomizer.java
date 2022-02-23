package com.tencent.tsf.femas.extension.springcloud.common.discovery.consul;

import com.tencent.tsf.femas.common.serviceregistry.AbstractServiceRegistryMetadata;
import com.tencent.tsf.femas.common.serviceregistry.AbstractServiceRegistryMetadataFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.cloud.consul.serviceregistry.ConsulRegistration;
import org.springframework.cloud.consul.serviceregistry.ConsulRegistrationCustomizer;

/**
 * @author juanyinyang
 */
public class FemasConsulRegistrationCustomizer implements ConsulRegistrationCustomizer {

    private volatile AbstractServiceRegistryMetadata serviceRegistryMetadata = AbstractServiceRegistryMetadataFactory
            .getServiceRegistryMetadata();

    public FemasConsulRegistrationCustomizer() {
    }

    @Override
    public void customize(ConsulRegistration registration) {
        /**
         * 设置Metadata和塞入protocol，其他注册信息在ConsulServiceMetadataWrapper里
         */
        Map<String, String> registerMetadataMap = serviceRegistryMetadata.getRegisterMetadataMap();
        registerMetadataMap.put("protocol", "spring-cloud-consul");
        registration.getService().setMeta(registerMetadataMap);
        // 合并 tags
        registration.getService().getTags().addAll(convertTags(registerMetadataMap));
    }

    List<String> convertTags(Map<String, String> tagsMeta) {
        List<String> tags = new ArrayList<>();
        for (Map.Entry<String, String> entity : tagsMeta.entrySet()) {
            tags.add(entity.getKey().concat("=").concat(entity.getValue()));
        }
        return tags;
    }

}
