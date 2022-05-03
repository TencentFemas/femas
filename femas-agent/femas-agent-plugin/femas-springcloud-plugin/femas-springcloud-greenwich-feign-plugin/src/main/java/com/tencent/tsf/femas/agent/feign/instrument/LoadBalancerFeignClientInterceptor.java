package com.tencent.tsf.femas.agent.feign.instrument;

import com.tencent.tsf.femas.agent.interceptor.Interceptor;
import com.tencent.tsf.femas.agent.tools.AgentLogger;
import com.tencent.tsf.femas.api.ExtensionManager;
import com.tencent.tsf.femas.api.IExtensionLayer;
import com.tencent.tsf.femas.common.context.Context;
import com.tencent.tsf.femas.common.context.ContextConstant;
import com.tencent.tsf.femas.common.context.RpcContext;
import com.tencent.tsf.femas.common.context.TracingContext;
import com.tencent.tsf.femas.common.context.factory.ContextFactory;
import com.tencent.tsf.femas.common.entity.ErrorStatus;
import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.common.entity.ServiceInstance;
import com.tencent.tsf.femas.common.header.AbstractRequestMetaUtils;
import feign.Request;
import feign.Response;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Callable;

/**
 * @Author leoziltong@tencent.com
 * @Date: 2022/4/7 16:26
 */
public class LoadBalancerFeignClientInterceptor implements Interceptor {

    private volatile ContextConstant contextConstant = ContextFactory.getContextConstantInstance();
    private String namespace = Context.getSystemTag(contextConstant.getNamespaceId());
    private IExtensionLayer extensionLayer = ExtensionManager.getExtensionLayer();

    /**
     * @param obj          feign.Request/feign.Options
     * @param allArguments method args
     * @param zuper        callable
     * @param method       reflect method
     * @return
     * @throws Throwable
     */
    @Override
    public Object intercept(Object obj, Object[] allArguments, Callable<?> zuper, Method method) throws Throwable {
        try {
            feign.Request request = (feign.Request) allArguments[0];
            com.tencent.tsf.femas.common.entity.Request femasRequest = Context.getRpcInfo().getRequest();
            URL url = getUrl(request);
            if (femasRequest == null) {
                femasRequest = getFemasRequest(request, url);
            }
            String httpMethod = request.httpMethod().name();
            femasRequest.setInterfaceName(url.getPath());
            femasRequest.setTargetMethodSig(httpMethod + "/" + url.getPath());
            femasRequest.setDoneChooseInstance(true);
            RpcContext rpcContext = extensionLayer.beforeClientInvoke(femasRequest, new AbstractRequestMetaUtils() {
                @Override
                public void preprocess() {
                }

                @Override
                public void setRequestMeta(String name, String value) {
                }
            });
            Response response = null;
            Throwable error = null;
            try {
                // 如果需要熔断
                if (rpcContext.getErrorStatus() != null && ErrorStatus.Code.CIRCUIT_BREAKER
                        .equals(rpcContext.getErrorStatus().getCode())) {
                    throw new RuntimeException("CircuitBreaker Error. IsolationLevel : " +
                            rpcContext.getErrorStatus().getMessage() + ", Request : " + femasRequest);
                }
                response = (Response) zuper.call();
            } catch (Throwable throwable) {
                error = throwable;
                throw throwable;
            } finally {
                com.tencent.tsf.femas.common.entity.Response femasResponse = new com.tencent.tsf.femas.common.entity.Response();
                if (error != null) {
                    femasResponse.setError(error);
                } else if (response.status() >= HttpStatus.SC_BAD_REQUEST) {
                    // 设置 error，保持 afterClientInvoke 逻辑统一
                    femasResponse.setError(new RuntimeException(String.valueOf(response.status())));
                }
                fillTracingContext(rpcContext, request, response, femasRequest, url);
                extensionLayer.afterClientInvoke(femasRequest, femasResponse, rpcContext);
                Context.getRpcInfo().setRequest(null);
            }
            return response;
        } catch (Throwable t) {
            AgentLogger.getLogger().severe("FeignExecuteError: " + AgentLogger.getStackTraceString(t));
        } finally {
        }
        return zuper.call();
    }

    private URL getUrl(Request request) {
        URL url = null;
        try {
            url = new URL(request.url());
        } catch (MalformedURLException e) {
            AgentLogger.getLogger().info("MalformedURLException, feign request:" + request.toString());
        }
        return url;
    }

    private com.tencent.tsf.femas.common.entity.Request getFemasRequest(Request request, URL url) {
        com.tencent.tsf.femas.common.entity.Request femasRequest = new com.tencent.tsf.femas.common.entity.Request();
        Service service = new Service();
        if (url != null) {
            service.setName(url.getHost());
        }
        service.setNamespace(namespace);
        femasRequest.setTargetService(service);
        return femasRequest;
    }

    private void fillTracingContext(RpcContext rpcContext, Request request, Response response
            , com.tencent.tsf.femas.common.entity.Request femasRequest, URL url) {
        TracingContext tracingContext = rpcContext.getTracingContext();
        tracingContext.setProtocol("http");
        // local
        tracingContext.setLocalServiceName(Context.getSystemTag(contextConstant.getServiceName()));
        tracingContext.setLocalNamespaceId(Context.getSystemTag(contextConstant.getNamespaceId()));
        tracingContext.setLocalInstanceId(Context.getSystemTag(contextConstant.getInstanceId()));
        tracingContext.setLocalApplicationVersion(Context.getSystemTag(contextConstant.getApplicationVersion()));
        tracingContext.setLocalHttpMethod(Context.getRpcInfo().get(contextConstant.getRequestHttpMethod()));
        tracingContext.setLocalInterface(Context.getRpcInfo().get(contextConstant.getInterface()));
        tracingContext.setLocalIpv4(Context.getSystemTag(contextConstant.getLocalIp()));
        String localPort = Context.getSystemTag(contextConstant.getLocalPort());
        if (StringUtils.isNotEmpty(localPort)) {
            tracingContext.setLocalPort(Integer.valueOf(localPort));
        }

        // remote
        tracingContext.setRemoteHttpMethod(request.method());
        if (url != null) {
            tracingContext.setRemoteInterface(url.getPath());
            tracingContext.setRemoteIpv4(url.getHost());
            tracingContext.setRemotePort(url.getPort());
        }
        ServiceInstance serviceInstance = femasRequest.getTargetServiceInstance();
        if (serviceInstance != null && serviceInstance.getAllMetadata() != null) {
            tracingContext.setRemoteApplicationVersion(
                    serviceInstance.getMetadata(contextConstant.getMetaApplicationVersionKey()));
            tracingContext.setRemoteInstanceId(serviceInstance.getMetadata(contextConstant.getMetaInstanceIdKey()));
        }
        Service targetService = femasRequest.getTargetService();
        if (targetService != null) {
            tracingContext.setRemoteServiceName(targetService.getName());
            tracingContext.setRemoteNamespaceId(targetService.getNamespace());
        }
        if (response != null) {
            tracingContext.setResultStatus(String.valueOf(response.status()));
        }
        // clean
        Context.getRpcInfo().put(contextConstant.getInterface(), null);
        Context.getRpcInfo().put(contextConstant.getRequestHttpMethod(), null);
    }
}