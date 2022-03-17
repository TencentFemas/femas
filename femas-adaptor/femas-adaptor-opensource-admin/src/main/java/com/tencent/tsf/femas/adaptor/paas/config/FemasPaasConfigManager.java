package com.tencent.tsf.femas.adaptor.paas.config;

import com.tencent.tsf.femas.adaptor.paas.common.FemasConstant;
import com.tencent.tsf.femas.common.context.FemasContext;
import com.tencent.tsf.femas.config.ConfigService;
import com.tencent.tsf.femas.config.impl.paas.PaasConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FemasPaasConfigManager {

    private static final  Logger logger = LoggerFactory.getLogger(FemasPaasConfigManager.class);

    private static volatile PaasConfig PAAS_CONFIG;

    public static PaasConfig getConfig() {
        if (PAAS_CONFIG == null) {
            synchronized (FemasPaasConfigManager.class) {
                if (PAAS_CONFIG == null) {
                    PAAS_CONFIG = (PaasConfig) ConfigService
                            .createConfig(FemasConstant.FEMAS_CONFIG_PAAS, FemasContext.REGISTRY_CONFIG_MAP);
                }
            }
        }
        return PAAS_CONFIG;
    }

}
