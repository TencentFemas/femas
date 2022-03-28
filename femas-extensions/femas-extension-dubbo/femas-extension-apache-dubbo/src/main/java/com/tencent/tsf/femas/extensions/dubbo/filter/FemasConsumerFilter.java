package com.tencent.tsf.femas.extensions.dubbo.filter;

import com.tencent.tsf.femas.api.ExtensionManager;
import com.tencent.tsf.femas.api.IExtensionLayer;
import com.tencent.tsf.femas.common.context.Context;
import com.tencent.tsf.femas.common.entity.ErrorStatus;
import com.tencent.tsf.femas.common.entity.Request;
import com.tencent.tsf.femas.common.entity.Response;
import com.tencent.tsf.femas.extensions.dubbo.util.ApacheDubboAttachmentUtils;
import com.tencent.tsf.femas.extensions.dubbo.util.CommonUtils;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
@Activate(group = CommonConstants.CONSUMER, order = -10000 + 2)
public class FemasConsumerFilter implements Filter {
    private static final Logger LOGGER = LoggerFactory.getLogger(FemasConsumerFilter.class);
    private IExtensionLayer extensionLayer = ExtensionManager.getExtensionLayer();

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        Request femasRequest = Context.getRpcInfo().getRequest();
        RpcContext rpcContext = RpcContext.getContext();

        if (Objects.isNull(femasRequest)) {
            femasRequest = CommonUtils.getFemasRequest(invoker.getUrl(), invocation);
        }

        com.tencent.tsf.femas.common.context.RpcContext femasRpcContext =
                extensionLayer.beforeServerInvoke(femasRequest, ApacheDubboAttachmentUtils.create(rpcContext));

        Throwable error = null;

        try {
            if (Objects.nonNull(femasRpcContext.getErrorStatus()) && Objects.equals(ErrorStatus.Code.CIRCUIT_BREAKER,
                    femasRpcContext.getErrorStatus().getCode())) {
                throw new RuntimeException("CircuitBreaker Error. IsolationLevel : " +
                        femasRpcContext.getErrorStatus().getMessage() + ", Request : " + femasRequest);
            }

            return invoker.invoke(invocation);
        } catch (Throwable throwable) {
            error = throwable;
            throw  throwable;
        } finally {
            Response response = new Response();
            if (error != null) {
                response.setError(error);
            }
            extensionLayer.afterClientInvoke(femasRequest, response, femasRpcContext);
        }
    }

}
