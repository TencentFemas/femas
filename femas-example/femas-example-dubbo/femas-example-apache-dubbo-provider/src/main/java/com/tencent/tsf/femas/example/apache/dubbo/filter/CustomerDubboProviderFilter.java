//package com.tencent.tsf.femas.example.apache.dubbo.filter;
//
//import org.apache.dubbo.common.constants.CommonConstants;
//import org.apache.dubbo.common.extension.Activate;
//import org.apache.dubbo.rpc.*;
//
//@Activate(group = CommonConstants.PROVIDER, order = -10000 + 2)
//public class CustomerDubboProviderFilter implements Filter {
//    @Override
//    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
//                RpcContext.getServiceContext().setInvoker(invoker)
//                .setInvocation(invocation);
//
//        System.out.println(invocation);
//        return new AppResponse(invocation);
//    }
//}
