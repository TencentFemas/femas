package com.tencent.tsf.femas.gray.example.springcloud.hoxton;

import com.tencent.tsf.femas.common.constant.FemasConstant;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.web.bind.annotation.RestController;


@RestController
@SpringBootApplication
@EnableDiscoveryClient
public class FemasGrayPrviderServiceP2 {
    public static void main(String[] args) {
        System.setProperty("spring.profiles.active", "p2");
        System.setProperty(FemasConstant.FEMAS_APPLICATION_VERSION_KEY, "2.0");
        SpringApplication.run(FemasGrayPrviderServiceP2.class, args);
    }

}
