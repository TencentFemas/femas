package com.tencent.tsf.femas.api;

import com.tencent.tsf.femas.common.annotation.AdaptorComponent;
import com.tencent.tsf.femas.common.context.Context;
import com.tencent.tsf.femas.common.context.RpcContext;
import com.tencent.tsf.femas.common.discovery.ServiceNotifyListener;
import com.tencent.tsf.femas.common.entity.Request;
import com.tencent.tsf.femas.common.entity.Response;
import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.common.entity.ServiceInstance;
import com.tencent.tsf.femas.common.header.AbstractRequestMetaUtils;

import java.util.List;

/**
 * 将大部分组件能力进行封装
 * 方便用户实现 Extension
 * <p>
 * 主要包括生命周期，rpc 部分
 * 原则上每个部分只抽象最少集，多余能力，允许用户自己使用相应组件接口自己封装
 */
@AdaptorComponent
public interface IExtensionLayer {

    /**
     * 处理某个服务的初始化工作
     * 包括但不限于 Context，各个治理组件，Config等
     *
     * @param service
     */
    void init(Service service, Integer port);

    void init(Service service, Integer port, String registryUrl);

    /**
     * 处理某个服务的销毁工作
     * 包括但不限于 Context，各个治理组件，Config等
     *
     * @param service
     */
    void destroy(Service service);

    /**
     * 实例注册
     * 实例中应当包含 Service 字段，以及 userTags 等信息
     *
     * @param instance
     */
    void register(ServiceInstance instance);

    /**
     * 实例反注册
     *
     * @param instance
     */
    void deregister(ServiceInstance instance);

    List<ServiceInstance> subscribe(Service service, List<ServiceNotifyListener> listeners);

    /**
     * 此方法中，需要封装
     * - 熔断
     * - 路由
     * - 泳道
     * - 负载均衡
     * - 故障注入
     *
     * @param instances
     * @return
     */
    ServiceInstance chooseServiceInstance(Request request, List<ServiceInstance> instances);


    /**
     * 封装
     * - metrics
     * - 限流
     * - Auth
     * - 故障注入
     *
     * @return 返回当前请求的上下文，和整个Context体系无关，为了解决异步模型，以及client覆盖server context的问题
     */
    RpcContext beforeServerInvoke(Request request, AbstractRequestMetaUtils headerUtils);

    /**
     * 封装
     * - metrics
     * - 故障注入
     *
     * @return 返回当前请求的上下文，和整个Context体系无关，为了解决异步模型，以及client覆盖server context的问题
     */
    RpcContext beforeClientInvoke(Request request, AbstractRequestMetaUtils headerUtils);

    /**
     * 封装
     * - metrics
     *
     * @param response
     * @param rpcContext 将 beforeInvoke 返回的 RpcContext 传入
     */
    void afterServerInvoke(Response response, RpcContext rpcContext);

    /**
     * 封装
     * - metrics
     * - 容错
     * - 熔断
     *
     * @param response
     * @param rpcContext 将 beforeInvoke 返回的 RpcContext 传入
     */
    void afterClientInvoke(Request request, Response response, RpcContext rpcContext);

    /**
     * 获取全局的上下文
     *
     * @return
     */
    Context getCommonContext();

}