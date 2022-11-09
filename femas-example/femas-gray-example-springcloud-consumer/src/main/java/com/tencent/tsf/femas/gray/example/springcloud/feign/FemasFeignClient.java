package com.tencent.tsf.femas.gray.example.springcloud.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "femas-gray-example-cloud-provider")
public interface FemasFeignClient {
    @GetMapping("/get")
    String get();

}
