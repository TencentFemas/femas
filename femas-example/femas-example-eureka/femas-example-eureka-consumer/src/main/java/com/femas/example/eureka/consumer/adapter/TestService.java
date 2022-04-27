package com.femas.example.eureka.consumer.adapter;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author : MentosL
 * @date : 2022/4/27 11:49
 */
@FeignClient(value = "MONTOS-EUREKA-PROVIDER")
public interface TestService {

    @GetMapping("/get/{name}")
    String getName(@PathVariable("name") String name);
}