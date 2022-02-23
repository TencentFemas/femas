package com.tencent.tsf.femas.extension.springcloud.common.instrumentation.resttemplate;

import com.tencent.tsf.femas.api.ExtensionManager;
import com.tencent.tsf.femas.api.IExtensionLayer;
import com.tencent.tsf.femas.common.context.Context;
import com.tencent.tsf.femas.common.context.ContextConstant;
import com.tencent.tsf.femas.common.context.RpcContext;
import com.tencent.tsf.femas.common.context.TracingContext;
import com.tencent.tsf.femas.common.context.factory.ContextFactory;
import com.tencent.tsf.femas.common.entity.Request;
import com.tencent.tsf.femas.common.entity.Response;
import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.common.entity.ServiceInstance;
import java.io.IOException;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

public class FemasRestTemplateInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(FemasRestTemplateInterceptor.class);

    private IExtensionLayer extensionLayer = ExtensionManager.getExtensionLayer();
    private volatile ContextConstant contextConstant = ContextFactory.getContextConstantInstance();
    private String namespace = Context.getSystemTag(contextConstant.getNamespaceId());

    /**
     * FemasRestTemplateInterceptor#intercept -> chooseInstance
     */
    @Override
    public ClientHttpResponse intercept(HttpRequest httpRequest, byte[] bytes,
            ClientHttpRequestExecution clientHttpRequestExecution) throws IOException {
        // rest 的时候，intercept 比 ribbon filterAllServer 的时机要早
        Request femasRequest = getFemasRequest(httpRequest);
        String httpMethod = httpRequest.getMethod().name();
        femasRequest.setInterfaceName(httpRequest.getURI().getPath());
        femasRequest.setTargetMethodSig(httpMethod + "/" + httpRequest.getURI().getPath());
        Context.getRpcInfo().setRequest(femasRequest);
        femasRequest.setDoneChooseInstance(false);
        RpcContext rpcContext = extensionLayer
                .beforeClientInvoke(femasRequest, new RestTemplateHeaderUtils(httpRequest));

        ClientHttpResponse response = null;
        Throwable error = null;
        try {
            response = clientHttpRequestExecution.execute(httpRequest, bytes);
        } catch (Throwable throwable) {
            error = throwable;
            throw throwable;
        } finally {
            Response femasResponse = new Response();
            if (error != null) {
                femasResponse.setError(error);
            } else if (response.getRawStatusCode() >= HttpStatus.SC_BAD_REQUEST) {
                // 设置 error，保持 afterClientInvoke 逻辑统一
                femasResponse.setError(new RuntimeException(String.valueOf(response.getRawStatusCode())));
            }
            fillTracingContext(rpcContext, femasRequest, httpRequest, response);
            extensionLayer.afterClientInvoke(femasRequest, femasResponse, rpcContext);
            Context.getRpcInfo().setRequest(null);
        }
        return response;
    }

    Request getFemasRequest(HttpRequest httpRequest) {
        Service service = new Service();
        service.setName(httpRequest.getURI().getHost());
        service.setNamespace(namespace);

        Request request = new Request();
        request.setTargetService(service);
        return request;
    }

    private void fillTracingContext(RpcContext rpcContext, com.tencent.tsf.femas.common.entity.Request femasRequest,
            HttpRequest httpRequest, ClientHttpResponse response) throws IOException {
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
        tracingContext.setRemoteHttpMethod(httpRequest.getMethodValue());
        ServiceInstance serviceInstance = femasRequest.getTargetServiceInstance();
        if (serviceInstance != null && serviceInstance.getAllMetadata() != null) {
            tracingContext.setRemoteIpv4(serviceInstance.getHost());
            tracingContext.setRemotePort(serviceInstance.getPort());
            tracingContext.setRemoteApplicationVersion(
                    serviceInstance.getMetadata(contextConstant.getMetaApplicationVersionKey()));
            tracingContext.setRemoteInstanceId(serviceInstance.getMetadata(contextConstant.getMetaInstanceIdKey()));
        }
        tracingContext.setRemoteInterface(httpRequest.getURI().getPath());
        Service targetService = femasRequest.getTargetService();
        if (targetService != null) {
            tracingContext.setRemoteServiceName(targetService.getName());
            tracingContext.setRemoteNamespaceId(targetService.getNamespace());
        }
        if (response != null) {
            tracingContext.setResultStatus(String.valueOf(response.getRawStatusCode()));
        }
        // clean
        Context.getRpcInfo().put(contextConstant.getInterface(), null);
        Context.getRpcInfo().put(contextConstant.getRequestHttpMethod(), null);
    }
}
