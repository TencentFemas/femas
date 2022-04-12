package com.tencent.tsf.femas;

import com.spring4all.swagger.EnableSwagger2Doc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @Author leoziltong
 * @Description //TODO
 * @Date: 2021/4/6 16:01
 * @Version 1.0
 */
@EnableScheduling
@SpringBootApplication
@EnableSwagger2Doc
public class FemasApplication {

    public static void main(String[] args) {
        SpringApplication.run(FemasApplication.class, args);
    }

}
