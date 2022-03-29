package com.tencent.tsf.femas.extensions.dubbo.router;


import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.cluster.router.AbstractRouter;
import com.tencent.tsf.femas.api.ExtensionManager;
import com.tencent.tsf.femas.api.IExtensionLayer;
import com.tencent.tsf.femas.common.entity.Request;
import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.common.entity.ServiceInstance;
import com.tencent.tsf.femas.extensions.dubbo.util.CommonUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class FemasRouter extends AbstractRouter {
    private IExtensionLayer extensionLayer = ExtensionManager.getExtensionLayer();

    public FemasRouter(URL url) {
        this.url = url;
    }

    @Override
    public <T> List<Invoker<T>> route(List<Invoker<T>> invokers, URL url, Invocation invocation) throws RpcException {
        Service service = CommonUtils.buildService(url);
        Map<ServiceInstance, Invoker<T>> serviceInstanceInvokerMap = CommonUtils.parseInvokers(invokers, service);
        List<ServiceInstance> serviceInstanceList = new ArrayList<>(serviceInstanceInvokerMap.keySet());
        Request femasRequest = CommonUtils.getFemasRequest(url, invocation);
        ServiceInstance instance = null;
        try {
            instance = extensionLayer.chooseServiceInstance(femasRequest, serviceInstanceList);
        } catch (Exception e) {
            return Collections.emptyList();
        }
        Invoker<T> invoker = serviceInstanceInvokerMap.get(instance);
        return Arrays.asList(invoker);
    }
}