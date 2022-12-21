package com.tencent.tsf.femas.common.httpclient;

import com.tencent.tsf.femas.common.util.HttpResult;

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
    HttpResult<String> fetchLongPollingKvValue(String key, String namespaceId);
}
