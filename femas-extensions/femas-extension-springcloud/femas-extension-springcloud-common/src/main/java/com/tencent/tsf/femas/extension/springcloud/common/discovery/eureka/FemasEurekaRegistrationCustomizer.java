package com.tencent.tsf.femas.extension.springcloud.common.discovery.eureka;

import com.tencent.tsf.femas.common.serviceregistry.AbstractServiceRegistryMetadata;
import com.tencent.tsf.femas.common.serviceregistry.AbstractServiceRegistryMetadataFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cloud.netflix.eureka.EurekaInstanceConfigBean;
import org.springframework.lang.Nullable;

public class FemasEurekaRegistrationCustomizer implements BeanPostProcessor {

    private volatile AbstractServiceRegistryMetadata serviceRegistryMetadata = AbstractServiceRegistryMetadataFactory
            .getServiceRegistryMetadata();

    @Override
    public Object postProcessAfterInitialization(@Nullable Object bean, @Nullable String beanName) throws BeansException {
        if (bean instanceof EurekaInstanceConfigBean) {
            EurekaInstanceConfigBean prop = (EurekaInstanceConfigBean) bean;
            prop.getMetadataMap().put("protocol", "spring-cloud-eureka");
            prop.getMetadataMap().putAll(serviceRegistryMetadata.getRegisterMetadataMap());
        }
        return bean;
    }
}
