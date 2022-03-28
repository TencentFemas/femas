package com.tencent.tsf.femas.example.apache.dubbo.provider;

import com.tencent.tsf.femas.example.apache.dubbo.api.ApacheDubboExampleService;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService(version = "1.0")
public class ApacheDubboProvider  implements ApacheDubboExampleService {

    @Override
    public String sayHello(String name) {
        return "hello " + name;
    }
}
