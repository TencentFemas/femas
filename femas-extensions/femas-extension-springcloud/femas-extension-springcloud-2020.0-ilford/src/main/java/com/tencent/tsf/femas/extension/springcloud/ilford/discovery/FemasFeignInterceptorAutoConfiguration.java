package com.tencent.tsf.femas.extension.springcloud.ilford.discovery;

import com.tencent.tsf.femas.extension.springcloud.common.instrumentation.feign.FeignHeaderInterceptor;
import com.tencent.tsf.femas.extension.springcloud.common.instrumentation.feign.FemasFeignClientWrapper;
import feign.Client;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.loadbalancer.LoadBalancedRetryFactory;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerProperties;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.cloud.openfeign.loadbalancer.FeignBlockingLoadBalancerClient;
import org.springframework.cloud.openfeign.loadbalancer.OnRetryNotEnabledCondition;
import org.springframework.cloud.openfeign.loadbalancer.RetryableFeignBlockingLoadBalancerClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

@Configuration("femasFeignInterceptorAutoConfiguration")
public class FemasFeignInterceptorAutoConfiguration {

    @Configuration
    @ConditionalOnClass(name = "feign.Feign")
    static class FemasFeignInterceptorConfig {

        @Bean
        public FeignHeaderInterceptor feignHeaderInterceptor() {
            return new FeignHeaderInterceptor();
        }

        @Bean
        @ConditionalOnMissingBean
        @Conditional(OnRetryNotEnabledCondition.class)
        public Client feignClient(LoadBalancerClient loadBalancerClient, LoadBalancerProperties properties,
                LoadBalancerClientFactory loadBalancerClientFactory) {
            return new FeignBlockingLoadBalancerClient(new FemasFeignClientWrapper(
                    new Client.Default(null, null)), loadBalancerClient, properties,
                    loadBalancerClientFactory);
        }

        @Bean
        @ConditionalOnMissingBean
        @ConditionalOnClass(name = "org.springframework.retry.support.RetryTemplate")
        @ConditionalOnBean(LoadBalancedRetryFactory.class)
        @ConditionalOnProperty(value = "spring.cloud.loadbalancer.retry.enabled", havingValue = "true",
                matchIfMissing = true)
        public Client feignRetryClient(LoadBalancerClient loadBalancerClient,
                LoadBalancedRetryFactory loadBalancedRetryFactory, LoadBalancerProperties properties,
                LoadBalancerClientFactory loadBalancerClientFactory) {
            return new RetryableFeignBlockingLoadBalancerClient(
                    new FemasFeignClientWrapper(new Client.Default(null, null)), loadBalancerClient,
                    loadBalancedRetryFactory, properties, loadBalancerClientFactory);
        }

    }
}