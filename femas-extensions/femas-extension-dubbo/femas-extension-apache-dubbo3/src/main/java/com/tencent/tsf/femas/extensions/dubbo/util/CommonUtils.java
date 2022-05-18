package com.tencent.tsf.femas.extensions.dubbo.util;

import com.tencent.tsf.femas.common.context.Context;
import com.tencent.tsf.femas.common.context.ContextConstant;
import com.tencent.tsf.femas.common.context.factory.ContextFactory;
import com.tencent.tsf.femas.common.entity.Request;
import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.common.entity.ServiceInstance;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.logger.Logger;
import org.apache.dubbo.common.logger.LoggerFactory;
import org.apache.dubbo.registry.client.InstanceAddressURL;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.listener.ListenerInvokerWrapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommonUtils {

    private static final Logger logger = LoggerFactory.getLogger(CommonUtils.class.getName());
    private static volatile ContextConstant contextConstant = ContextFactory.getContextConstantInstance();
    private static String namespace = Context.getSystemTag(contextConstant.getNamespaceId());

    private CommonUtils(){}

    public static <T> Map<ServiceInstance, Invoker<T>> parseInvokers(List<Invoker<T>> invokers, Service service) {
        Map<ServiceInstance, Invoker<T>> serviceInstances = new HashMap<>();
        for (Invoker<T> invoker : invokers) {
            serviceInstances.put(parseInvoker(invoker, service), invoker);
        }
        return serviceInstances;
    }

    public static <T> ServiceInstance parseInvoker(Invoker<T> invoker, Service service) {
        URL url = invoker.getUrl();
        ServiceInstance serviceInstance = new ServiceInstance();
        serviceInstance.setHost(url.getHost());
        serviceInstance.setPort(url.getPort());
        serviceInstance.setService(service);
        serviceInstance.setAllMetadata(url.getParameters());
        return serviceInstance;
    }

    public static Request getFemasRequest(URL url, Invocation invocation) {
        String serviceName = null;
        if (url instanceof InstanceAddressURL){
            InstanceAddressURL instanceAddressURL = (InstanceAddressURL) url;
            serviceName = instanceAddressURL.getInstance().getServiceName();
        }
        Service service = new Service(namespace, serviceName);
        Request femasRequest = new Request();
        femasRequest.setTargetMethodName(invocation.getMethodName());
        femasRequest.setInterfaceName(invocation.getMethodName());
        femasRequest.setTargetService(service);
        return femasRequest;
    }

    public static Service buildService(URL url) {
        return new Service(namespace, url.getServiceInterface());
    }
}
