package com.tencent.tsf.femas.adaptor.paas.config;

import com.tencent.tsf.femas.common.context.FemasContext;
import com.tencent.tsf.femas.config.ConfigService;
import com.tencent.tsf.femas.config.enums.FemasConfigTypeEnum;
import com.tencent.tsf.femas.config.impl.paas.PaasConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FemasPaasConfigManager {

    private static final  Logger logger = LoggerFactory.getLogger(FemasPaasConfigManager.class);


    public static PaasConfig getConfig() {
        return Singleton.SINGLETON.getInstance();
    }

    private enum Singleton {
        SINGLETON;

        private PaasConfig paasConfig;

        Singleton() {
            paasConfig = (PaasConfig) ConfigService
                    .createConfig(FemasConfigTypeEnum.PAAS.getType(), FemasContext.REGISTRY_CONFIG_MAP);
        }

        public PaasConfig getInstance() {
            return paasConfig;
        }
    }

}
