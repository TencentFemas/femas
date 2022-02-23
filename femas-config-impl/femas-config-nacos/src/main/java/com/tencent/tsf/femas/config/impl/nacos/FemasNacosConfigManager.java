package com.tencent.tsf.femas.config.impl.nacos;

import com.tencent.tsf.femas.config.Config;
import com.tencent.tsf.femas.config.ConfigService;
import com.tencent.tsf.femas.config.FemasConfigManager;
import com.tencent.tsf.femas.config.enums.FemasConfigTypeEnum;

public class FemasNacosConfigManager extends FemasConfigManager {

    private volatile static FemasNacosConfig CONFIG;

    public Config getConfig() {
        if (CONFIG == null) {
            synchronized (FemasNacosConfigManager.class) {
                if (CONFIG == null) {
                    CONFIG = (FemasNacosConfig) ConfigService.createConfig(FemasConfigTypeEnum.NACOS.getType(), null);
                }
            }
        }
        return CONFIG;
    }

}
