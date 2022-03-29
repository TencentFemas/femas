package com.tencent.tsf.femas.extension.zuul.filter;

import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.tencent.tsf.femas.extension.springcloud.instrumentation.zuul.ZuultHeaderUtils;
import com.tencent.tsf.femas.api.ExtensionManager;
import com.tencent.tsf.femas.api.IExtensionLayer;
import com.tencent.tsf.femas.common.context.Context;
import com.tencent.tsf.femas.common.context.ContextConstant;
import com.tencent.tsf.femas.common.context.RpcContext;
import com.tencent.tsf.femas.common.context.TracingContext;
import com.tencent.tsf.femas.common.context.factory.ContextFactory;
import com.tencent.tsf.femas.common.entity.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cloud.netflix.ribbon.support.RibbonCommandContext;
import org.springframework.cloud.netflix.ribbon.support.RibbonRequestCustomizer;
import org.springframework.cloud.netflix.zuul.filters.ProxyRequestHelper;
import org.springframework.cloud.netflix.zuul.filters.route.RibbonCommandFactory;
import org.springframework.cloud.netflix.zuul.filters.route.RibbonRoutingFilter;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.cloud.netflix.zuul.util.ZuulRuntimeException;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

@Component
public class FemasRibbonRoutingFilter extends RibbonRoutingFilter {

    private static final Log log = LogFactory.getLog(FemasRibbonRoutingFilter.class);

    private volatile ContextConstant contextConstant = ContextFactory.getContextConstantInstance();
    private String namespace = Context.getSystemTag(contextConstant.getNamespaceId());
    private IExtensionLayer extensionLayer = ExtensionManager.getExtensionLayer();

    public FemasRibbonRoutingFilter(ProxyRequestHelper helper, RibbonCommandFactory<?> ribbonCommandFactory, List<RibbonRequestCustomizer> requestCustomizers) {
        super(helper, ribbonCommandFactory, requestCustomizers);
    }

    @Override
    public Object run() {
        RequestContext context = RequestContext.getCurrentContext();
        this.helper.addIgnoredHeaders();

        HttpServletRequest httpServletRequest = context.getRequest();

        URL url = getUrl(httpServletRequest);
        Request femasRequest = Context.getRpcInfo().getRequest();
        if (femasRequest == null) {
            femasRequest = getFemasRequest(context);
        }
        String httpMethod = httpServletRequest.getMethod();
        femasRequest.setInterfaceName(url.getPath());
        femasRequest.setTargetMethodSig(httpMethod + "/" + context.get(FilterConstants.REQUEST_URI_KEY));
        femasRequest.setDoneChooseInstance(false);
        RpcContext rpcContext = extensionLayer.beforeClientInvoke(femasRequest, new ZuultHeaderUtils(context));

        RibbonCommandContext commandContext = buildCommandContext(context);
        ClientHttpResponse response = null;
        Throwable error = null;
        try {
            // 如果需要熔断
            if (rpcContext.getErrorStatus() != null && ErrorStatus.Code.CIRCUIT_BREAKER
                    .equals(rpcContext.getErrorStatus().getCode())) {
                String errorMsg = "CircuitBreaker Error. IsolationLevel : " + rpcContext.getErrorStatus().getMessage() + ", Request : " + femasRequest;
                throw new ZuulRuntimeException(new ZuulException(errorMsg, HttpURLConnection.HTTP_INTERNAL_ERROR, errorMsg));
            }

            response = forward(commandContext);
            setResponse(response);

        } catch (ZuulRuntimeException e) {
            error = e;
            throw e;
        } catch (Exception e) {
            error = e;
            log.error(e.getMessage(), e);
            throw new ZuulRuntimeException(new ZuulException(e.getCause(), HttpURLConnection.HTTP_INTERNAL_ERROR, e.getMessage()));
        } finally {
            try {
                Response femasResponse = new Response();
                if (error != null) {
                    femasResponse.setError(error);
                    femasResponse.setErrorStatus(ErrorStatus.INTERNAL);
                } else if (response.getRawStatusCode() >= org.apache.http.HttpStatus.SC_BAD_REQUEST) {
                    // 设置 error，保持 afterClientInvoke 逻辑统一
                    femasResponse.setErrorStatus(ErrorStatus.INTERNAL);
                    femasResponse.setError(new RuntimeException(String.valueOf(response.getRawStatusCode())));
                }
                fillTracingContext(rpcContext, httpServletRequest, response, femasRequest, url);
                extensionLayer.afterClientInvoke(femasRequest, femasResponse, rpcContext);
                Context.getRpcInfo().setRequest(null);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
        return response;
    }

    private URL getUrl(HttpServletRequest request) {
        URL url = null;
        try {
            url = new URL(new String(request.getRequestURL()));
        } catch (MalformedURLException e) {
            log.warn("MalformedURLException, feign request:" + request, e);
        }
        return url;
    }

    private Request getFemasRequest(RequestContext requestContext) {
        Request request = new Request();

        String serviceName;
        String[] uriArr = requestContext.get("serviceId").toString().split("/");
        if (uriArr.length < 2){
            // 服务端服务名
            serviceName = uriArr[0];
        }else{
            serviceName = uriArr[1];
        }

        Service service = new Service();
        service.setName(serviceName);
        service.setNamespace(namespace);
        request.setTargetService(service);
        Context.getRpcInfo().setRequest(request);
        return request;
    }

    private void fillTracingContext(RpcContext rpcContext, HttpServletRequest request, ClientHttpResponse response
            , Request femasRequest, URL url) throws IOException {
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
        tracingContext.setRemoteHttpMethod(request.getMethod());
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
            tracingContext.setResultStatus(String.valueOf(response.getRawStatusCode()));
        }
        // clean
        Context.getRpcInfo().put(contextConstant.getInterface(), null);
        Context.getRpcInfo().put(contextConstant.getRequestHttpMethod(), null);
    }
}
