package com.tencent.tsf.femas.example.apache.dubbo.provider;

import com.tencent.tsf.femas.example.apache.dubbo.api.ApacheDubboExampleService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.context.annotation.Profile;

@Profile("v2")
@DubboService(version = "1.1")
public class ApacheDubboV2Provider implements ApacheDubboExampleService {

    @Override
    public String sayHello(String name) {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "hello " + name + "I am dubbo v2 provider";
    }
}
