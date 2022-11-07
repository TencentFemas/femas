package com.tencent.tsf.femas.gray.example.springcloud;

import com.tencent.tsf.femas.common.constant.FemasConstant;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;


@SpringBootApplication
@EnableDiscoveryClient
public class FemasGrayPrviderServiceP1 {
    public static void main(String[] args) {
        System.setProperty("spring.profiles.active", "p1");
        System.setProperty(FemasConstant.FEMAS_APPLICATION_VERSION_KEY, "1.0");
        SpringApplication.run(FemasGrayPrviderServiceP1.class, args);
    }


}
