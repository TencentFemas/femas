package com.tencent.tsf.femas.common.httpclient;

/**
 * @author mroccyen
 */
public interface HttpLongPollingConnectorManager {
    /**
     * 使用http长轮询获取数据
     *
     * @param key         key
     * @param namespaceId 命名空间id
     * @return 获取到的数据
     */
    String fetchLongPollingKvValue(String key, String namespaceId);
}
