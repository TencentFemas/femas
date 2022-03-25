package com.tencent.tsf.femas.config.config.type;

import com.tencent.tsf.femas.common.util.ConfigUtils;
import com.tencent.tsf.femas.common.util.FileUtils;
import com.tencent.tsf.femas.config.internals.AbstractConfig;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.*;

public class EnvConfig extends AbstractConfig<Object> {
    private Map<String, Object> conf;

    public static final String FEMAS_CONF_LOCATION_PROPERTY = "femas.conf";

    public EnvConfig() {
        // 加载外部配置文件
        Properties properties = System.getProperties();
        String location = properties.getProperty(FEMAS_CONF_LOCATION_PROPERTY);
        if (StringUtils.isNotBlank(location)) {
            this.conf = ConfigUtils.loadAbsoluteConfig(new File(location));
        }
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

