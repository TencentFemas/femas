package com.tencent.tsf.femas.extensions.dubbo.filter;


import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcException;
import com.tencent.tsf.femas.api.ExtensionManager;
import com.tencent.tsf.femas.api.IExtensionLayer;
import com.tencent.tsf.femas.common.context.Context;
import com.tencent.tsf.femas.common.entity.ErrorStatus;
import com.tencent.tsf.femas.common.entity.Request;
import com.tencent.tsf.femas.common.entity.Response;
import com.tencent.tsf.femas.extensions.dubbo.util.CommonUtils;
import com.tencent.tsf.femas.extensions.dubbo.util.DubboAttachmentUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Activate(group = Constants.CONSUMER, order = -10000 + 2)
public class FemasConsumerFilter implements Filter {
    private static final Logger LOGGER = LoggerFactory.getLogger(FemasConsumerFilter.class);
    private IExtensionLayer extensionLayer = ExtensionManager.getExtensionLayer();

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        Request femasRequest = Context.getRpcInfo().getRequest();
        RpcContext context = RpcContext.getContext();

        if (femasRequest == null) {
            femasRequest = CommonUtils.getFemasRequest(invoker.getUrl(), invocation);
        }
        com.tencent.tsf.femas.common.context.RpcContext rpcContext
                = extensionLayer.beforeClientInvoke(femasRequest, new DubboAttachmentUtils(context));

        Throwable error = null;
        try {
            // 如果需要熔断
            if (rpcContext.getErrorStatus() != null && ErrorStatus.Code.CIRCUIT_BREAKER
                    .equals(rpcContext.getErrorStatus().getCode())) {
                throw new RuntimeException("CircuitBreaker Error. IsolationLevel : " +
                        rpcContext.getErrorStatus().getMessage() + ", Request : " + femasRequest);
            }
            return invoker.invoke(invocation);
        } catch (Throwable throwable) {
            error = throwable;
            throw throwable;
        } finally {
            Response response = new Response();
            if (error != null) {
                response.setError(error);
            }
            extensionLayer.afterClientInvoke(femasRequest, response, rpcContext);
        }
    }
}