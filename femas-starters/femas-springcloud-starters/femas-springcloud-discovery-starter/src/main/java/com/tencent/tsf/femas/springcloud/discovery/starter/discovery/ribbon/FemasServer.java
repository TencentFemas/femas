package com.tencent.tsf.femas.springcloud.discovery.starter.discovery.ribbon;

import com.netflix.loadbalancer.Server;
import com.tencent.tsf.femas.common.entity.ServiceInstance;

import java.util.Map;

/**
 * 描述：
 * 创建日期：2022年05月25 23:00:47
 *
 * @author gong zhao
 **/
public class FemasServer extends Server {
    private final MetaInfo metaInfo;

    private final ServiceInstance instance;

    private final Map<String, String> metadata;

    public FemasServer(final ServiceInstance instance) {
        super(instance.getHost(), instance.getPort());
        this.instance = instance;
        this.metaInfo = new MetaInfo() {
            @Override
            public String getAppName() {
                return instance.getService().getName();
            }

            @Override
            public String getServerGroup() {
                return null;
            }

            @Override
            public String getServiceIdForDiscovery() {
                return null;
            }

            @Override
            public String getInstanceId() {
                return instance.getId();
            }
        };
        this.metadata = instance.getAllMetadata();
    }

    @Override
    public MetaInfo getMetaInfo() {
        return metaInfo;
    }

    public ServiceInstance getInstance() {
        return instance;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }
}
