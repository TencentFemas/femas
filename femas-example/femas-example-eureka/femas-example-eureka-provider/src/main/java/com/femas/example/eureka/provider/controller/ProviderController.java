package com.femas.example.eureka.provider.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author : MentosL
 * @date : 2022/4/27 11:34
 */
@RestController
public class ProviderController {

    @GetMapping("/get/{name}")
    public String getName(@PathVariable String name) {
        return "provider:" + name;
    }
}