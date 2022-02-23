package com.tencent.tsf.femas.governance.trace;

import com.tencent.tsf.femas.common.context.RpcContext;
import io.opentelemetry.api.trace.Span;

/**
 * @Author p_mtluo
 * @Date 2021-12-13 18:04
 * @Description test
 **/
public class TraceAdapter {


    public static void setSpanAttribute(RpcContext rpcContext) {
        Span span = Span.current();
        if (rpcContext.getTracingContext() != null && rpcContext.getTracingContext().getLocalPort() != null) {
            span.setAttribute("http.port", rpcContext.getTracingContext().getLocalPort());
        }
        if (rpcContext.getTracingContext() != null && rpcContext.getTracingContext().getRemoteServiceName() != null) {
            span.setAttribute("net.peer.service", rpcContext.getTracingContext().getRemoteServiceName());
        }
    }
}
