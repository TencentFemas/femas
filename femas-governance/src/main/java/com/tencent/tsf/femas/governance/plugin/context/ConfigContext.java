package com.tencent.tsf.femas.governance.plugin.context;

import com.tencent.tsf.femas.governance.plugin.config.Configuration;

/**
 * 插件初始化相关的上下文接口
 *
 * @author leoziltong
 */
public class ConfigContext extends ContextAware {

    private final Configuration config;

    public ConfigContext(Configuration config) {
        this.config = config;
    }

    public Configuration getConfig() {
        return config;
    }

}
