package com.tencent.tsf.femas.common.entity;

import com.tencent.tsf.femas.common.invoke.Invoker;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class Request extends RequestBase {

    private static final long serialVersionUID = 7329530374415722876L;

    Service targetService;

    ServiceInstance targetServiceInstance;

    Map<String, Object> header = new HashMap<>();

    Invoker invoker;

    boolean doneChooseInstance = true;

    /**
     * 方法对象(为了减少反射缓存）
     */
    private transient Method method;

    /**
     * 接口名
     */
    private transient String interfaceName;

    /**
     * 用户层请求超时，调用级别（客户端使用）
     */
    private transient Integer timeout;

    @Override
    public Service getTargetService() {
        return targetService;
    }

    public void setTargetService(Service targetService) {
        this.targetService = targetService;
    }

    public ServiceInstance getTargetServiceInstance() {
        return targetServiceInstance;
    }

    public void setTargetServiceInstance(ServiceInstance targetServiceInstance) {
        this.targetServiceInstance = targetServiceInstance;
    }

    /**
     * Gets method.
     *
     * @return the method
     */
    public Method getMethod() {
        return method;
    }

    /**
     * Sets method.
     *
     * @param method the method
     */
    public void setMethod(Method method) {
        this.method = method;
    }

    public void setInvoker(Invoker invoker) {
        this.invoker = invoker;
    }

    public Invoker getInvokder() {
        return this.invoker;
    }

    /**
     * Gets interface name.
     *
     * @return the interface name
     */
    public String getInterfaceName() {
        return interfaceName;
    }

    /**
     * Sets interface name.
     *
     * @param interfaceName the interface name
     */
    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    /**
     * Gets timeout.
     *
     * @return the timeout
     */
    public Integer getTimeout() {
        return timeout;
    }

    /**
     * Sets timeout.
     *
     * @param timeout the timeout
     * @return the timeout
     */
    public Request setTimeout(Integer timeout) {
        this.timeout = timeout;
        return this;
    }

    public Map<String, Object> getHeader() {
        return header;
    }

    public boolean isDoneChooseInstance() {
        return doneChooseInstance;
    }

    public void setDoneChooseInstance(boolean doneChooseInstance) {
        this.doneChooseInstance = doneChooseInstance;
    }

    @Override
    public String toString() {
        return "Request{" +
                "targetService=" + targetService +
                ", targetServiceInstance=" + targetServiceInstance +
                '}';
    }
}
