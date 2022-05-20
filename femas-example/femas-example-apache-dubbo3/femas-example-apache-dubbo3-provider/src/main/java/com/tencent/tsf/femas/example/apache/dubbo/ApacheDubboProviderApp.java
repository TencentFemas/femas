package com.tencent.tsf.femas.example.apache.dubbo;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Profile;

@EnableDubbo(scanBasePackages = {"com.tencent.tsf.femas.example.apache.dubbo.provider"})
@SpringBootApplication
public class ApacheDubboProviderApp {

    public static void main(String[] args) {
        System.setProperty("spring.profiles.active","v1");
        SpringApplication.run(ApacheDubboProviderApp.class, args);
    }
}
