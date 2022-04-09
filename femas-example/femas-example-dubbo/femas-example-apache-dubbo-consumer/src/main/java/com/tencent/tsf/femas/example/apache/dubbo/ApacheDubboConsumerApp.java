package com.tencent.tsf.femas.example.apache.dubbo;

import com.tencent.tsf.femas.example.apache.dubbo.api.ApacheDubboExampleService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@EnableDubbo
@RestController
@SpringBootApplication
public class ApacheDubboConsumerApp {
    @DubboReference(version = "1.0",check = false)
    private ApacheDubboExampleService dubboExampleService;


    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(ApacheDubboConsumerApp.class, args);

//        ApacheDubboConsumerApp dubboConsumer = context.getBean(ApacheDubboConsumerApp.class);
//        String consumer = dubboConsumer.doSayHello("femas example dubbo consumer");
//        System.out.println("consumer--------"+ consumer);
    }

    public String doSayHello(String name) {
        return dubboExampleService.sayHello(name);
    }

    @GetMapping(value = "/hello/{name}")
    public String hello(@PathVariable String name){
        return dubboExampleService.sayHello(name);
    }
}
