package com.tencent.tsf.femas.extensions.dubbo.router;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.cluster.Router;
import com.alibaba.dubbo.rpc.cluster.RouterFactory;

public class FemasRouterFactory implements RouterFactory {

    @Override
    public Router getRouter(URL url) {
        return new FemasRouter(url);
    }
}
