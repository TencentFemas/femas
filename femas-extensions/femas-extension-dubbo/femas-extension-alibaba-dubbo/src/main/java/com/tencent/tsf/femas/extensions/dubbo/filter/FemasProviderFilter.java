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
import com.tencent.tsf.femas.common.context.ContextConstant;
import com.tencent.tsf.femas.common.context.factory.ContextFactory;
import com.tencent.tsf.femas.common.entity.ErrorStatus;
import com.tencent.tsf.femas.common.entity.Request;
import com.tencent.tsf.femas.common.entity.Response;
import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.extensions.dubbo.util.DubboAttachmentUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Activate(group = Constants.PROVIDER, order = -10000 + 2)
public class FemasProviderFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(FemasProviderFilter.class);

    private volatile Context commonContext = ContextFactory.getContextInstance();
    private volatile ContextConstant contextConstant = ContextFactory.getContextConstantInstance();
    private IExtensionLayer extensionLayer = ExtensionManager.getExtensionLayer();

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        RpcContext context = RpcContext.getContext();

        Request femasRequest = getFemasRequest();
        com.tencent.tsf.femas.common.context.RpcContext rpcContext = extensionLayer
                .beforeServerInvoke(femasRequest, new DubboAttachmentUtils(context));

        Throwable error = null;
        try {
            if (ErrorStatus.UNAUTHENTICATED.equals(rpcContext.getErrorStatus())) {
                throw new RuntimeException("Unauthorized error, Request : " + femasRequest);
            }
            if (ErrorStatus.RESOURCE_EXHAUSTED.equals(rpcContext.getErrorStatus())) {
                throw new RuntimeException("Resource exhausted error, Request : " + femasRequest);
            }
            return invoker.invoke(invocation);
        } catch (Throwable throwable) {
            error = throwable;
            throw throwable;
        } finally {
            Response response = new Response();
            response.setError(error);
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

}
