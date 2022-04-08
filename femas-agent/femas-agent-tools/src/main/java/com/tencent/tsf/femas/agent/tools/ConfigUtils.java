package com.tencent.tsf.femas.agent.tools;


import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

public class ConfigUtils {

    public static Map<String, Object> loadRelativeConfig(String fileName) {
        String content = null;
        try {
            content = FileUtils.file2String(fileName, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content2Map(content);
    }

    public static Map<String, Object> loadAbsoluteConfig(File file) {
        String content = null;
        try {
            content = FileUtils.file2String(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content2Map(content);
    }

    public static Map<String, Object> content2Map(String content) {
        if (StringUtils.isNotEmpty(content)) {
            Yaml yaml = new Yaml();
            return yaml.load(content);
        }
        return Collections.emptyMap();
    }
}
