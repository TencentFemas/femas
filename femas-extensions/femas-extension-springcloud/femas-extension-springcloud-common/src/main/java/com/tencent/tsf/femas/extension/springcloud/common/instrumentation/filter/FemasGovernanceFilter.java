package com.tencent.tsf.femas.extension.springcloud.common.instrumentation.filter;

import com.tencent.tsf.femas.api.ExtensionManager;
import com.tencent.tsf.femas.api.IExtensionLayer;
import com.tencent.tsf.femas.common.context.Context;
import com.tencent.tsf.femas.common.context.ContextConstant;
import com.tencent.tsf.femas.common.context.RpcContext;
import com.tencent.tsf.femas.common.context.TracingContext;
import com.tencent.tsf.femas.common.context.factory.ContextFactory;
import com.tencent.tsf.femas.common.entity.ErrorStatus;
import com.tencent.tsf.femas.common.entity.Request;
import com.tencent.tsf.femas.common.entity.Response;
import com.tencent.tsf.femas.common.entity.Service;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * http 入流量拦截
 */
@Order(FemasGovernanceFilter.ORDER)
public class FemasGovernanceFilter extends OncePerRequestFilter {

    public static final int ORDER = Ordered.HIGHEST_PRECEDENCE + 12;
    private static final Logger logger = LoggerFactory.getLogger(FemasGovernanceFilter.class);
//    @Value("${server.port:}")
//    Integer port;
    private IExtensionLayer extensionLayer = ExtensionManager.getExtensionLayer();
    private volatile ContextConstant contextConstant = ContextFactory.getContextConstantInstance();

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
            FilterChain filterChain) throws ServletException, IOException {

        Request request = getFemasRequest();
        RpcContext rpcContext = extensionLayer
                .beforeServerInvoke(request, new HttpServletHeaderUtils(httpServletRequest));

        Throwable error = null;
        try {
            if (ErrorStatus.UNAUTHENTICATED.equals(rpcContext.getErrorStatus())) {
                httpServletResponse
                        .sendError(HttpServletResponse.SC_FORBIDDEN, ErrorStatus.UNAUTHENTICATED.getMessage());
            } else if (ErrorStatus.RESOURCE_EXHAUSTED.equals(rpcContext.getErrorStatus())) {
                httpServletResponse.sendError(ErrorStatus.RESOURCE_EXHAUSTED.getCode().Value(),
                        ErrorStatus.RESOURCE_EXHAUSTED.getMessage());
            } else {
                filterChain.doFilter(httpServletRequest, httpServletResponse);
            }
        } catch (Throwable throwable) {
            // 异常时，如果未设置 status 则设置 500，原本是外层 WebMvcMetricsFilter 才设置，这里提前设置以获取 tracing 信息
            if (httpServletResponse.getStatus() == HttpStatus.OK.value()) {
                httpServletResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            }
            error = throwable;
            throw throwable;
        } finally {
            Response response = new Response();
            response.setError(error);
            fillTracingContext(rpcContext, httpServletRequest, httpServletResponse);
            extensionLayer.afterServerInvoke(response, rpcContext);
        }
    }

    private Request getFemasRequest() {

        String serviceName = Context.getSystemTag(contextConstant.getServiceName());
        String namespace = Context.getSystemTag(contextConstant.getNamespaceId());

        Service service = new Service(namespace, serviceName);
        Request request = new Request();
        request.setTargetService(service);

        return request;
    }

    private void fillTracingContext(RpcContext rpcContext, HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) {
        TracingContext tracingContext = rpcContext.getTracingContext();
        tracingContext.setLocalServiceName(Context.getSystemTag(contextConstant.getServiceName()));
        tracingContext.setLocalNamespaceId(Context.getSystemTag(contextConstant.getNamespaceId()));
        tracingContext.setLocalInstanceId(Context.getSystemTag(contextConstant.getInstanceId()));
        tracingContext.setLocalApplicationVersion(Context.getSystemTag(contextConstant.getApplicationVersion()));
        tracingContext.setLocalHttpMethod(httpServletRequest.getMethod());
        tracingContext.setLocalInterface(httpServletRequest.getRequestURI());
        String localPort = Context.getSystemTag(contextConstant.getLocalPort());
        if (StringUtils.isNotEmpty(localPort)) {
            tracingContext.setLocalPort(Integer.valueOf(localPort));
        }
        tracingContext.setLocalIpv4(Context.getSystemTag(contextConstant.getLocalIp()));
        tracingContext.setResultStatus(String.valueOf(httpServletResponse.getStatus()));
    }
}
