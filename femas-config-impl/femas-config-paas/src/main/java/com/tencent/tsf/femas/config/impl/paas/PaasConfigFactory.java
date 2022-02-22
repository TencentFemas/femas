package com.tencent.tsf.femas.config.impl.paas;

import com.tencent.tsf.femas.config.Config;
import com.tencent.tsf.femas.config.ConfigFactory;
import java.util.Map;

/**
 * @author zhixinzxliu
 */
public class PaasConfigFactory implements ConfigFactory {

    @Override
    public String getType() {
        return "paas";
    }

    @Override
    public Config create(Map<String, String> configMap) {
        return new PaasConfig(configMap);
    }
}
