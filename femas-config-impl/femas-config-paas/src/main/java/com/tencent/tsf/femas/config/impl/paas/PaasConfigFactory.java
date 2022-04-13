package com.tencent.tsf.femas.config.impl.paas;

import com.tencent.tsf.femas.config.Config;
import com.tencent.tsf.femas.config.ConfigFactory;
import com.tencent.tsf.femas.config.enums.FemasConfigTypeEnum;

import java.util.Map;

/**
 * @author zhixinzxliu
 */
public class PaasConfigFactory implements ConfigFactory {

    @Override
    public String getType() {
        return FemasConfigTypeEnum.PAAS.getType();
    }

    @Override
    public Config create(Map<String, String> configMap) {
        return new PaasConfig(configMap);
    }
}
