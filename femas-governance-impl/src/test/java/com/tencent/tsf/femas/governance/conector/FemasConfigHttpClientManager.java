package com.tencent.tsf.femas.governance.conector;

import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.common.exception.FemasRuntimeException;
import com.tencent.tsf.femas.governance.connector.server.ServerConnectorManager;
import com.tencent.tsf.femas.plugin.config.global.ServerConnectorEnum;
import com.tencent.tsf.femas.plugin.context.ConfigContext;

public class FemasConfigHttpClientManager implements ServerConnectorManager {
    @Override
    public String getName() {
        return ServerConnectorEnum.HTTP.name();
    }

    @Override
    public String getType() {
        return ServerConnectorEnum.HTTP.name();
    }

    @Override
    public void reportApis(String namespaceId, String serviceName, String applicationVersion, String data) {

    }

    @Override
    public String fetchKVValue(String key, String namespaceId) {
        return null;
    }

    @Override
    public void initNamespace(String registryAddress, String namespaceId) {

    }

    @Override
    public void reportEvent(Service service, String eventId, String data) {

    }

    @Override
    public void init(ConfigContext conf) throws FemasRuntimeException {

    }

    @Override
    public void destroy() {

    }
}
