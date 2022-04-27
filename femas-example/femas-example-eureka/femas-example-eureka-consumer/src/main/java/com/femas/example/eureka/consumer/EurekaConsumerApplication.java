package com.femas.example.eureka.consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author : MentosL
 * @date : 2022/4/27 11:48
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class EurekaConsumerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaConsumerApplication.class);
    }
}