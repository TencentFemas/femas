package com.tencent.tsf.femas.plugin;

import com.tencent.tsf.femas.common.exception.FemasRuntimeException;
import com.tencent.tsf.femas.plugin.context.ConfigContext;

/**
 * @Author leoziltong
 * @Date: 2021/4/19 17:19
 * @Version 1.0
 */
public interface Lifecycle {

    /**
     * 根据配置初始化配置
     *
     * @param conf 插件上下文信息
     * @throws FemasRuntimeException 异常信息
     */
    void init(final ConfigContext conf) throws FemasRuntimeException;

    /**
     * 释放资源
     */
    void destroy();

}
