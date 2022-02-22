package com.tencent.tsf.femas.config.config.type;

import com.tencent.tsf.femas.common.util.ConfigUtils;
import com.tencent.tsf.femas.common.util.FileUtils;
import com.tencent.tsf.femas.config.internals.AbstractConfig;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

public class DefaultConfig extends AbstractConfig<Object> {
    private final static Logger logger = LoggerFactory.getLogger(DefaultConfig.class);

    private static Map<String, Object> conf;

    public static final String FEMAS_DEFAULT_CONFIG = "femas-default.conf";

    public DefaultConfig() {
        // 加载默认配置文件
        this.conf = ConfigUtils.loadRelativeConfig(FEMAS_DEFAULT_CONFIG);
    }

    @Override
    protected void doSubscribe(String key) {
        return;
    }

    @Override
    protected void doSubscribeDirectory(String key) {
        return;
    }

    @Override
    protected void doUnSubscribe(String key) {
        return;
    }

    @Override
    protected Object doGetProperty(String key) {
        return this.conf.get(key);
    }

    public Map<String, Object> getConf() {
        return this.conf;
    }

}

