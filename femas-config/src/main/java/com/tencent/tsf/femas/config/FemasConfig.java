package com.tencent.tsf.femas.config;

import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.Map;

import com.tencent.tsf.femas.config.config.FinalConfig;
import org.yaml.snakeyaml.Yaml;

public class FemasConfig {

    /**
     * system props & local config props
     */
    static Config<Object> conf = new FinalConfig();
    static Yaml yaml = new Yaml();

    public static  <T> T getProperty(String key, T defaultValue) {
        return (T) conf.getProperty(key, defaultValue);
    }

    public static  String getProperty(String key) {
        return String.valueOf( conf.getProperty(key, ""));
    }

    public static  <T> T parse(String key, Class<T> type) {
        StringWriter sw = new StringWriter();
        Map<String, Object> def = new LinkedHashMap();
        yaml.dump(conf.getProperty(key, def), sw);
        return yaml.loadAs(sw.toString(), type);
    }

    public static  <T> T parse(Class<T> type) {
        ConfigName annotation = type.getDeclaredAnnotation(ConfigName.class);
        String name = "";
        if (annotation == null || annotation.value() == null || annotation.value().length() == 0) {
            name = type.getSimpleName().toLowerCase();
        } else {
            name = annotation.value();
        }
        return parse(name, type);
    }
}
