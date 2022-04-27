package com.femas.example.eureka.consumer.controller;

import com.femas.example.eureka.consumer.adapter.TestService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;


/**
 * @author : MentosL
 * @date : 2022/4/27 11:53
 */
@RestController
@Slf4j
@AllArgsConstructor
public class ConsumerController {

    private final RestTemplate restTemplate;
    private final TestService testService;


    @GetMapping("/testRest")
    public String testRest() {
        val identity = restTemplate.getForEntity(String.format("http://%s/get/test", "montos-eureka-provider"), String.class).getBody();
        log.info("testRest is successful，result is {}",identity);
        return identity;
    }


    @GetMapping("/testFeign")
    public String testFeign() {
        String identity = testService.getName("montos");
        log.info("testFeign is successful，result is {}",identity);
        return identity;
    }

}