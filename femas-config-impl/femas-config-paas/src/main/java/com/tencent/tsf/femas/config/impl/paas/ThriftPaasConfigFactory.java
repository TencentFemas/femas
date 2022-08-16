package com.tencent.tsf.femas.config.impl.paas;

import com.tencent.tsf.femas.config.Config;
import com.tencent.tsf.femas.config.ConfigFactory;
import com.tencent.tsf.femas.config.enums.FemasConfigTypeEnum;

import java.util.Map;

/**
 * paas thrift implements, return {@link PaasConfig}
 *
 * @author huyuanxin
 */
public class ThriftPaasConfigFactory implements ConfigFactory {
    @Override
    public String getType() {
        return FemasConfigTypeEnum.THRIFT_PAAS.getType();
    }

    @Override
    public Config create(Map<String, String> configMap) {
        return new PaasConfig(configMap);
    }
}
