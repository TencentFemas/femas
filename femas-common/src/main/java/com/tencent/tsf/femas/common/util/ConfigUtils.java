package com.tencent.tsf.femas.common.util;

import com.tencent.tsf.femas.agent.classloader.AgentPackagePathScanner;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class ConfigUtils {

    private final static Logger logger = LoggerFactory.getLogger(ConfigUtils.class);
    private final static Yaml yml = new Yaml();

    public static Map<String, Object> loadRelativeConfig(String fileName) {
        String content = null;
        try {
            content = FileUtils.file2String(fileName, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content2Map(content);
    }

    public static Map<String, Object> loadRelativeYamlConfig(String relativePath) {

        Map<String, Object> map = new HashMap<>();
        InputStream is = null;
        InputStreamReader reader = null;
        BufferedReader bufferedReader = null;
        try {
            is = Thread.currentThread().getContextClassLoader().getResourceAsStream(relativePath);
            if (is == null) {
                return map;
            }
            reader = new InputStreamReader(is, "UTF-8");
            bufferedReader = new BufferedReader(reader);
            map = yml.loadAs(bufferedReader, HashMap.class);
        } catch (Exception e) {
            logger.info("load agent Config failed...");
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (reader != null) {
                    reader.close();
                }
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException e) {
                logger.error("agent Config reader close failed...");
            }
        }
        return map;
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
            return yml.load(content);
        }
        return MapUtils.EMPTY_MAP;
    }
}
