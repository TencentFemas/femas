package com.tencent.tsf.femas.extension.springcloud.common.instrumentation.config;

import static javax.servlet.DispatcherType.ASYNC;
import static javax.servlet.DispatcherType.ERROR;
import static javax.servlet.DispatcherType.FORWARD;
import static javax.servlet.DispatcherType.INCLUDE;
import static javax.servlet.DispatcherType.REQUEST;

import com.tencent.tsf.femas.extension.springcloud.common.discovery.nacos.NacosEnv;
import com.tencent.tsf.femas.extension.springcloud.common.instrumentation.filter.FemasGovernanceFilter;
import com.tencent.tsf.femas.extension.springcloud.common.instrumentation.resttemplate.FemasRestTemplateInterceptor;
import com.tencent.tsf.femas.api.ExtensionManager;
import com.tencent.tsf.femas.api.IExtensionLayer;
import com.tencent.tsf.femas.common.context.Context;
import com.tencent.tsf.femas.common.context.ContextConstant;
import com.tencent.tsf.femas.common.context.factory.ContextFactory;
import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.common.util.StringUtils;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

@Configuration("femasGovernanceAutoConfiguration")
public class FemasGovernanceAutoConfiguration {

    static {
        // 利用 nacos 对 rest template interceptor 的依赖，优先加载本类，从而早于 NacosDiscoveryEndpointAutoConfiguration 对 nacos env 初始化
        NacosEnv.init();
    }

    @Value("${spring.application.name:}")
    private String applicationName;
    @Value("${server.port:}")
    private Integer port;
    @Autowired(required = false)
    @Qualifier("registryUrl")
    private String registryUrl;
    private IExtensionLayer extensionLayer = ExtensionManager.getExtensionLayer();
    private volatile ContextConstant contextConstant = ContextFactory.getContextConstantInstance();

    @PostConstruct
    public void init() {
        String serviceName = applicationName;
        String namespace = Context.getSystemTag(contextConstant.getNamespaceId());
        Service service = new Service(namespace, serviceName);

        if (StringUtils.isNotEmpty(registryUrl)) {
            extensionLayer.init(service, port, registryUrl);
        } else {
            extensionLayer.init(service, port);
        }
    }

    @Configuration
    // 只有在存在 javax.servlet 时构建
    @ConditionalOnClass(name = "javax.servlet.Filter")
    static class FemasContextFilterConfig {

        @Bean
        public FilterRegistrationBean<FemasGovernanceFilter> femasAuthenticateFilterRegistration(
                FemasGovernanceFilter femasGovernanceFilter) {
            FilterRegistrationBean<FemasGovernanceFilter> filterRegistrationBean = new FilterRegistrationBean<>(
                    femasGovernanceFilter);
            filterRegistrationBean.setDispatcherTypes(ASYNC, ERROR, FORWARD, INCLUDE, REQUEST);
            filterRegistrationBean.setOrder(FemasGovernanceFilter.ORDER);
            return filterRegistrationBean;
        }

        @Bean
        public FemasGovernanceFilter femasGovernanceFilter() {
            return new FemasGovernanceFilter();
        }
    }

    @Configuration
    @ConditionalOnClass(RestTemplate.class)
    static class FemasRestTemplateInterceptorConfig implements ApplicationContextAware {

        private ApplicationContext context;

        @Bean
        public FemasRestTemplateInterceptor femasRestTemplateInterceptor() {
            return new FemasRestTemplateInterceptor();
        }

        @Bean
        BeanPostProcessor femasRouteRestTemplateInterceptorPostProcessor(
                FemasRestTemplateInterceptor femasRestTemplateInterceptor) {
            // 处理多重Bean注入场景
            Map<String, RestTemplate> beans = this.context.getBeansOfType(RestTemplate.class);
            // 手动注册femasRouteRestTemplateInterceptorPostProcessor加载前初始化的restTemplate的bean
            if (null != beans && !beans.isEmpty()) {
                for (RestTemplate restTemplate : beans.values()) {
                    List<ClientHttpRequestInterceptor> interceptors = restTemplate.getInterceptors();
                    // 容错逻辑，避免重复加载，理论上不会触发，一定会进入到if子句
                    if (null != interceptors && !interceptors.contains(femasRestTemplateInterceptor)) {
                        interceptors.add(femasRestTemplateInterceptor);
                    }
                    restTemplate.setInterceptors(interceptors);
                }
            }
            return new FemasRestTemplateInterceptorPostProcessor(femasRestTemplateInterceptor);
        }

        @Override
        public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
            context = applicationContext;
        }

        private static class FemasRestTemplateInterceptorPostProcessor implements BeanPostProcessor {

            private FemasRestTemplateInterceptor femasRestTemplateInterceptor;

            FemasRestTemplateInterceptorPostProcessor(FemasRestTemplateInterceptor femasRestTemplateInterceptor) {
                this.femasRestTemplateInterceptor = femasRestTemplateInterceptor;
            }

            @Override
            public Object postProcessBeforeInitialization(Object bean, String beanName) {
                return bean;
            }

            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) {
                if (bean instanceof RestTemplate) {
                    RestTemplate restTemplate = (RestTemplate) bean;
                    List<ClientHttpRequestInterceptor> interceptors = restTemplate.getInterceptors();
                    // 容错逻辑，避免重复加载，理论上不会触发，一定会进入到if子句
                    if (null != interceptors && !interceptors.contains(this.femasRestTemplateInterceptor)) {
                        interceptors.add(this.femasRestTemplateInterceptor);
                        restTemplate.setInterceptors(interceptors);
                    }
                }
                return bean;
            }
        }
    }
}