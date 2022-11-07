package com.tencent.tsf.femas.gray.example.springcloud.hoxton.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GrayExampleController {

    @Autowired
    private DiscoveryClient discoveryClient;

    @Value("${spring.application.name}")
    private String application;

    @Value("${server.port}")
    private String port;

    private final String VERSION = System.getProperty("femas_prog_version");

    @GetMapping(value = "/get")
    public String get (){
        return  new StringBuilder("[")
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
    }
}
