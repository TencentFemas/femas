package com.tencent.tsf.femas.common.context;

import com.tencent.tsf.femas.agent.classloader.AgentPackagePathScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AgentConfig {
    private static final Logger logger = LoggerFactory.getLogger(AgentConfig.class);

    private final static String filePath = "/config/femas.yaml";
    private static final Yaml yml = new Yaml();
    private static Map<String, Object> conf = new ConcurrentHashMap<>();
    static {
        FileReader reader = null;
        try {
            reader = new FileReader(AgentPackagePathScanner.getPath() + filePath);
            BufferedReader buffer = new BufferedReader(reader);
            conf = yml.load(buffer);
        } catch (FileNotFoundException e) {
            logger.info("load agent Config failed, 'femas.yaml' file not found");
        } catch (Exception e) {
            logger.info("load agent Config failed...");
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                logger.error("agent Config reader close failed...");
            }
        }
    }

    public static Object doGetProperty(String key) {
        return conf.get(key);
    }

    public static Map<String, Object> getConf() {
        return conf;
    }

}

