package com.tencent.tsf.femas.gray.example.springcloud.rest;

import com.tencent.tsf.femas.gray.example.springcloud.feign.FemasFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FemaGrayConsumerController {

    @Autowired
    private FemasFeignClient femasFeignClient;

    @Value("${spring.application.name}")
    private String application;

    @Value("${server.port}")
    private String port;

    private final String VERSION = System.getProperty("femas_prog_version");

    @GetMapping(value = "/get")
    public String get (){
        String result = femasFeignClient.get();
        String consumerInfo = new StringBuilder("[")
                        .append("application=")
                        .append(application)
                        .append(",")
                        .append("port=")
                        .append(port)
                        .append(",")
                        .append("version=")
                        .append(VERSION)
                        .append("]")
                        .toString();
        System.out.println("consumerInfo =>" + consumerInfo);
        System.out.println("result =>"+result);
        return consumerInfo.concat("---").concat(result);
    }

}
