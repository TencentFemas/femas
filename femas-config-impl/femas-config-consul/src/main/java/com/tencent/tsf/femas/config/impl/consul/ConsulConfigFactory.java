package com.tencent.tsf.femas.config.impl.consul;

import com.tencent.tsf.femas.config.Config;
import com.tencent.tsf.femas.config.ConfigFactory;
import java.util.Map;

/**
 * @author zhixinzxliu
 */
public class ConsulConfigFactory implements ConfigFactory {

    @Override
    public String getType() {
        return "consul";
    }

    @Override
    public Config create(Map<String, String> configMap) {
        return new ConsulConfig(configMap);
    }
}
