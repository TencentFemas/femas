package com.tencent.tsf.femas.common.header;

import com.tencent.tsf.femas.common.util.StringUtils;

import java.util.Map;

/**
 * RequestMeta 处理的抽象类，根据不同的网络协议，client 进行实现，负责
 * 1. 从网络协议中的 RequestMeta（http header/dubbo attachment/grpc metadata）获取对应的元信息
 * 2. 将对应的元信息放置到网络协议中
 */
public abstract class AbstractRequestMetaUtils {

    /**
     *  统一的预处理，生成必要的 meta （例如泳道信息）到 Context 中
     */
    public void preprocess() {
        throw new UnsupportedOperationException("method preprocess has no implementation");
    }

    public void setRequestMeta(String name, String value) {
        throw new UnsupportedOperationException("method setRequestMeta has no implementation");
    }

    public void setRequestMetas(Map<String, String> kvs) {
        for (Map.Entry<String, String> entry : kvs.entrySet()) {
            if (StringUtils.isNotEmpty(entry.getValue())) {
                setRequestMeta(entry.getKey(), entry.getValue());
            }
        }
    }

    public String getRequestMeta(String name) {
        throw new UnsupportedOperationException("method getRequestMeta has no implementation");
    }

    public Map<String, String> getPrefixRequestMetas(String prefix){
        throw new UnsupportedOperationException
                ("method getPrefixRequestMetas has no implementation");
    }

    /**
     * 从网络协议中解析独有信息（例如 HttpServletRequest 的 RequestURI 等）到 Context.getRpcInfo()
     */
    public void getUniqueInfo() {
        throw new UnsupportedOperationException("method getUniqueInfo has no implementation");
    }
}