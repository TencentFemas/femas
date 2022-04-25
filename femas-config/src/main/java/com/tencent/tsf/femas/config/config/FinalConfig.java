package com.tencent.tsf.femas.config.config;


import com.tencent.tsf.femas.common.context.AgentConfig;
import com.tencent.tsf.femas.config.config.type.DefaultConfig;
import com.tencent.tsf.femas.config.config.type.EnvConfig;
import com.tencent.tsf.femas.config.config.type.LocalConfig;
import com.tencent.tsf.femas.config.internals.AbstractConfig;
import org.apache.commons.collections.MapUtils;

import java.util.LinkedHashMap;
import java.util.Map;

public class FinalConfig extends AbstractConfig<Object> {

    private static final Map<String, Object> conf;

    static {
        DefaultConfig defaultConfig = new DefaultConfig();
        conf = defaultConfig.getConf();
        EnvConfig envConfig = new EnvConfig();
        if (MapUtils.isNotEmpty(envConfig.getConf())) {
            conf.putAll(envConfig.getConf());
        } else {
            LocalConfig localConfig = new LocalConfig();
            conf.putAll(localConfig.getConf());
        }
        if (MapUtils.isNotEmpty(AgentConfig.getConf())) {
            conf.putAll(AgentConfig.getConf());
        }
        conf.putAll(new LinkedHashMap(System.getenv()));
        conf.putAll(new LinkedHashMap(System.getProperties())); // 注意部分属性可能被覆盖为字符串
    }

    @Override
    protected void doSubscribe(String key) {

    }

    @Override
    protected void doSubscribeDirectory(String key) {

    }

    @Override
    protected void doUnSubscribe(String key) {

    }

    @Override
    protected Object doGetProperty(String key) {
        return this.conf.get(key);
    }
}
