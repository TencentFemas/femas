package com.tencent.tsf.femas.extension.springcloud.common.discovery.nacos;

import com.tencent.tsf.femas.common.context.Context;
import com.tencent.tsf.femas.common.context.ContextConstant;
import com.tencent.tsf.femas.common.context.factory.ContextFactory;
import com.tencent.tsf.femas.common.util.StringUtils;

public class NacosEnv {

    private static volatile ContextConstant contextConstant = ContextFactory.getContextConstantInstance();

    public static void init() {
        // 需要优先于 NacosDiscoveryEndpointAutoConfiguration 进行环境变量设置
        String namespace = Context.getSystemTag(contextConstant.getNamespaceId());
        if (StringUtils.isNotEmpty(namespace)) {
            System.setProperty("spring.cloud.nacos.discovery.namespace", namespace);
            System.setProperty("spring.cloud.nacos.config.namespace", namespace);
        }
    }
}
