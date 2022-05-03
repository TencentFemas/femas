package com.tencent.tsf.femas.constant;

import java.util.HashMap;

public class ModuleConstants {


    public static HashMap<String, String> map;

    static {
        map = new HashMap<>();
        map.put("NamespaceManageEndpoint", "命名空间");
        map.put("RegistryManageEndpoint", "注册中心");
        map.put("AuthEndpoint", "服务鉴权");
        map.put("BreakerEndpoint", "服务熔断");
        map.put("LimitEndpoint", "服务限流");
        map.put("RouteEndpoint", "服务路由");
    }
}
