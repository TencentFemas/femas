package com.tencent.tsf.femas.common.context;

import com.google.common.collect.Sets;
import com.tencent.tsf.femas.common.annotation.AdaptorComponent;
import com.tencent.tsf.femas.common.header.AbstractRequestMetaUtils;
import com.tencent.tsf.femas.common.util.GsonUtil;
import com.tencent.tsf.femas.common.util.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhixinzxliu
 */
@AdaptorComponent
public abstract class Context {

    /**
     * SYSTEM_TAG与线程无关，所有线程共享相同的SYSTEM_TAG
     * 从调用入口处从Header中读出SYS_TAGS，写入RPC_INFO，后续治理逻辑从RPC_INFO中读数据
     * 在正常的调用逻辑中不需要读取该字段
     * request发给下游时，会从RPC_INFO中读取当前调用信息写入SYS_TAG中，并写入request的Header
     */
    protected static final Map<String, String> SYSTEM_TAGS = new ConcurrentHashMap<>();

    /**
     * 自定义tag需要参与序列化的话，可以塞入该tag map中
     */
    protected static final Set<String> RPC_INFO_SERIALIZE_TAGS = Sets.newConcurrentHashSet();

    /**
     * RPC 相关上下文信息
     * 包括上游调用信息，下游调用信息，以及当前调用信息
     */
    private static RpcInfo RPC_INFO = new RpcInfo();

    /**
     * 减少SYS_TAGS_MAP的生成数量，减少GC压力
     */
    private static volatile Map<String, String> SYSTEM_TAGS_UNMODIFIABLE_MAP;
    private static volatile String SYSTEM_TAGS_STRING;

    // 用户标签放在实现类里，支持不同的处理

    /**
     * SYSTEM_TAG为了防止用户误调用，只接受已经存在的key
     * 这批key在初始化时已经放入
     * <p>
     * 更新SYSTEM TAG是非常低频的操作，比如更改机器所属泳道等
     * 所以加锁防止多线程问题
     *
     * @param key
     * @param value
     */
    protected synchronized static void putSystemTag(String key, String value) {
        SYSTEM_TAGS.put(key, value);

        // 重新生成不可变MAP
        SYSTEM_TAGS_UNMODIFIABLE_MAP = Collections.unmodifiableMap(SYSTEM_TAGS);
        SYSTEM_TAGS_STRING = GsonUtil.serializeToJson(SYSTEM_TAGS);
    }

    /**
     * 获取系统标签
     *
     * @param key
     * @return
     */
    public static String getSystemTag(String key) {
        return SYSTEM_TAGS.get(key);
    }

    /**
     * 防止用户修改系统标签
     *
     * @return
     */
    public static Map<String, String> getAllSystemTags() {
        if (SYSTEM_TAGS_UNMODIFIABLE_MAP == null) {
            synchronized (SYSTEM_TAGS) {
                if (SYSTEM_TAGS_UNMODIFIABLE_MAP == null) {
                    SYSTEM_TAGS_UNMODIFIABLE_MAP = Collections.unmodifiableMap(SYSTEM_TAGS);
                }
            }
        }

        return SYSTEM_TAGS_UNMODIFIABLE_MAP;
    }

    /**
     * 获取当前的RPC调用信息
     *
     * @return
     */
    public static RpcInfo getRpcInfo() {
        return RPC_INFO;
    }

    public static void putSourceTag(String key, String value) {
        RPC_INFO.put(ContextConstant.SOURCE + key, value);
    }

    /**
     * 直接将本机的SYSTEM-Tags传给下游
     *
     * @return
     */
    public static String serializeSystemTags() {
        if (RPC_INFO_SERIALIZE_TAGS.isEmpty()) {
            if (SYSTEM_TAGS_STRING == null) {
                SYSTEM_TAGS_STRING = GsonUtil.serializeToJson(SYSTEM_TAGS);
            }

            return SYSTEM_TAGS_STRING;
        }

        Map<String, String> keys = new HashMap<>(SYSTEM_TAGS);

        for (String tag : RPC_INFO_SERIALIZE_TAGS) {
            Object value = RPC_INFO.get(tag);

            if (value != null) {
                keys.put(tag, value.toString());
            }
        }

        return GsonUtil.serializeToJson(keys);
    }

    public abstract void putTags(Map<String, String> tagMap);

    /**
     * 重置 USER_TAGS(实现类完成) 与 RPC_INFO
     * SYS_TAG 不进行重制
     */
    public void reset() {
        RPC_INFO.reset();
    }

    /**
     * 获取上游的用户标签
     *
     * @return
     */
    public abstract Map<String, String> getUpstreamTags();

    /**
     * 获取当前的用户标签
     *
     * @return
     */
    public abstract Map<String, String> getCurrentTags();

    public void init(String serviceName, Integer port) {
        throw new UnsupportedOperationException("Context method init has no implementation");
    }

    public String getServiceNameFromContext() {
        throw new UnsupportedOperationException("Context method getServiceName has no implementation");
    }

    public String getSourceServiceName() {
        throw new UnsupportedOperationException("Context method getSourceServiceName has no implementation");
    }

    public void putCurrentServiceName(String currentServiceName) {
        throw new UnsupportedOperationException("Context method putCurrentServiceName has no implementation");
    }

    public Map<String, String> getRegistryConfigMap() {
        throw new UnsupportedOperationException("Context method getRegistryConfigMap has no implementation");
    }

    public String getToken() {
        throw new UnsupportedOperationException("Context method getToken has no implementation");
    }

    /**
     * 获取当前序列化字符串，准备放入 RequestMeta（如 header）
     *
     * @return
     */
    public Map<String, String> getRequestMetaSerializeTags() {
        throw new UnsupportedOperationException(
                "Context method getRequestMetaSerializeTags has no implementation");
    }

    /**
     * 从 RequestMeta（如 header） 中获取序列化信息，放入上下文
     *
     * @param headerUtils
     */
    public void getSerializeTagsFromRequestMeta(AbstractRequestMetaUtils headerUtils) {
        throw new UnsupportedOperationException(
                "Context method getSerializeTagsFromRequestMeta has no implementation");
    }

    public boolean isEmptyPaasServer() {
        return false;
    }

    /**
     * 获取当前 rpc info 的内容拷贝（即使修改了也不影响原内容）
     *
     * @return
     */
    public static Map<String, String> getCopyRpcInfoMap() {
        return new HashMap<>(Context.getRpcInfo().getAll());
    }

    public static void restoreRpcInfo(Map<String, String> rpcContextInfo) {
        Context.getRpcInfo().reset();
        for (Map.Entry<String, String> entry : rpcContextInfo.entrySet()) {
            if (StringUtils.isNotEmpty(entry.getValue())) {
                Context.getRpcInfo().put(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * 获取当前上下文的的内容拷贝（即使修改了也不影响原内容）
     * 不同 adaptor 可以由不同的实现，默认是获取 rpc info
     *
     * @return
     */
    public Object getCopyRpcContext() {
        return getCopyRpcInfoMap();
    }

    /**
     * 适配 reactive 跨线程时使用，跨线程前通过 getCopyRpcContext 获取上下文信息的拷贝，跨线程后恢复上下文
     * 不同 adaptor 可以由不同的实现，默认是恢复 rpc info
     *
     * @param rpcContext
     */
    public void restoreRpcContext(Object rpcContext) {
        if (rpcContext instanceof Map) {
            restoreRpcInfo((Map<String, String>) rpcContext);
        } else {
            throw new RuntimeException("restoreRpcContext error, rpcContext type not match");
        }
    }

}
