package com.tencent.tsf.femas.config.config.type;

import java.util.*;

import com.tencent.tsf.femas.common.util.ConfigUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import com.tencent.tsf.femas.common.util.FileUtils;
import com.tencent.tsf.femas.config.internals.AbstractConfig;

public class LocalConfig extends AbstractConfig<Object> {
    private static final  Logger logger = LoggerFactory.getLogger(LocalConfig.class);

    private Map<String, Object> conf;

    //默认加载顺序,很springboot保持一致，参见ConfigFileApplicationListener
    private static final String DEFAULT_SEARCH_LOCATIONS = "classpath:/,classpath:/config/";
    public static final String CLASSPATH_URL_PREFIX = "classpath:/";
    private static final String YAML_DEFAULT_NAMES = "femas.conf";

    public LocalConfig() {
        // 加载 classpath 配置文件
        this.conf = new HashMap<>();
        String[] locationAdds = DEFAULT_SEARCH_LOCATIONS.split(",");
        for (String location : locationAdds) {
            location = location.substring(CLASSPATH_URL_PREFIX.length()).concat(YAML_DEFAULT_NAMES);
            this.conf.putAll(ConfigUtils.loadRelativeConfig(location));
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

