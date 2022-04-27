package com.femas.example.eureka.consumer.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;


/**
 * @author : MentosL
 * @date : 2022/4/27 11:51
 */
@Configuration
public class RestConfig {

    @Bean
    @LoadBalanced //负载均衡
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}