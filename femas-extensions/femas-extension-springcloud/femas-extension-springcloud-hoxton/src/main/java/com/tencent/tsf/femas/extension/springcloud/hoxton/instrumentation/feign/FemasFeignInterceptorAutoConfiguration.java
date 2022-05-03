package com.tencent.tsf.femas.extension.springcloud.hoxton.instrumentation.feign;

import com.tencent.tsf.femas.extension.springcloud.common.instrumentation.feign.FeignHeaderInterceptor;
import com.tencent.tsf.femas.extension.springcloud.common.instrumentation.feign.FemasFeignClientWrapper;
import feign.Client;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.cloud.openfeign.ribbon.CachingSpringLoadBalancerFactory;
import org.springframework.cloud.openfeign.ribbon.LoadBalancerFeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration("femasFeignInterceptorAutoConfiguration")
public class FemasFeignInterceptorAutoConfiguration {

    @Configuration
    @ConditionalOnClass(name = "feign.Feign")
    static class FemasFeignInterceptorConfig {

        @Bean
        public Client feignClient(CachingSpringLoadBalancerFactory cachingFactory,
                SpringClientFactory clientFactory) {
            // 需要  LoadBalancerFeignClient 在最外层，否则直接 feign client 通过 url 访问有问题
            return new LoadBalancerFeignClient(new FemasFeignClientWrapper(new Client.Default(null, null)),
                    cachingFactory,
                    clientFactory);
        }

        @Bean
        public FeignHeaderInterceptor feignHeaderInterceptor() {
            return new FeignHeaderInterceptor();
        }
    }
}