package com.tencent.tsf.femas.example.apache.dubbo;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableDubbo(scanBasePackages = {"com.tencent.tsf.femas.example.apache.dubbo.provider"})
@SpringBootApplication
public class ApacheDubboV2ProviderApp {

    public static void main(String[] args) {
        System.setProperty("spring.profiles.active","v2");
        SpringApplication.run(ApacheDubboV2ProviderApp.class, args);
    }
}
