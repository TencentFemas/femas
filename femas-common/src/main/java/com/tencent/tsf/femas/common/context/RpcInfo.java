package com.tencent.tsf.femas.common.context;

import com.tencent.tsf.femas.common.entity.Request;
import com.tencent.tsf.femas.common.entity.Response;
import java.util.Map;

/**
 * RPC INFO
 * 保存了上游以及下游的调用信息
 * 当前系统信息固化在SYS_TAGS中,不允许修改
 *
 * @author zhixinzxliu
 */
public class RpcInfo {

    private ThreadLocalContext<String> metadata = new ThreadLocalContext<>(Context.SYSTEM_TAGS);

    // 将一下通用信息抽象到request中
    // 其余业务相关的tag，自行从metadata中存取
    private ThreadLocal<Request> requestThreadLocal = new ThreadLocal<>();

    private ThreadLocal<Response> responseThreadLocal = new ThreadLocal<>();

    /**
     * 存放RPC INFO的信息
     *
     * @param key
     * @param value
     */
    public void put(String key, String value) {
        metadata.put(key, value);
    }

    public String get(String key) {
        return metadata.get(key);
    }

    public Map<String, String> getAll() {
        return metadata.getAll();
    }

    public Request getRequest() {
        return requestThreadLocal.get();
    }

    public void setRequest(Request request) {
        this.requestThreadLocal.set(request);
    }


    public Response getResponse() {
        return responseThreadLocal.get();
    }

    public void setResponse(Response resp) {
        this.responseThreadLocal.set(resp);
    }

    /**
     * 清理当前RPC调用上下文
     */
    public void reset() {
        metadata.reset();
        metadata.putAll(Context.SYSTEM_TAGS);
        resetInternal();
    }

    public void resetInternal() {
        requestThreadLocal.remove();
        responseThreadLocal.remove();
    }
}
