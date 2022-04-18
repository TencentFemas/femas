package com.tencent.tsf.femas.adaptor.paas.governance.loadbalance;

import com.google.common.collect.Sets;
import com.tencent.tsf.femas.adaptor.paas.config.FemasPaasConfigManager;
import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.common.util.StringUtils;
import com.tencent.tsf.femas.config.Config;
import com.tencent.tsf.femas.config.ConfigChangeListener;
import com.tencent.tsf.femas.config.model.ConfigChangeEvent;
import com.tencent.tsf.femas.governance.plugin.config.ConfigHandler;
import com.tencent.tsf.femas.governance.plugin.config.enums.ConfigHandlerTypeEnum;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FemasLoadBalancerHandler extends ConfigHandler {

    private static final Logger logger = LoggerFactory.getLogger(FemasLoadBalancerHandler.class);


    private static Set<String> subscribedNamespace = Sets.newConcurrentHashSet();

    /**
     * @see com.tencent.tsf.femas.common.spi.SpiExtensionClass#getType()
     */
    @Override
    public String getType() {
        return ConfigHandlerTypeEnum.LOAD_BALANCER.getType();
    }

    /**
     * 此处供用户切换路由算法
     *
     * @param service
     */
    @Override
    public synchronized void subscribeServiceConfig(Service service) {
        if (StringUtils.isEmpty(service.getNamespace()) || subscribedNamespace.contains(service.getNamespace())) {
            return;
        }
        String affinityKey = "affinity/" + service.getNamespace() + "/";
        Config config = FemasPaasConfigManager.getConfig();
        config.subscribe(affinityKey, new ConfigChangeListener<String>() {
            @Override
            public void onChange(List<ConfigChangeEvent<String>> configChangeEvents) {
            }

            @Override
            public void onChange(ConfigChangeEvent<String> changeEvent) {
//                LoadbalancerManager.update(Loadbalancer);
            }
        });
        subscribedNamespace.add(service.getNamespace());
    }

}
