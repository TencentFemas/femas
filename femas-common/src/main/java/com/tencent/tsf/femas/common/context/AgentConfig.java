package com.tencent.tsf.femas.common.context;

import com.tencent.tsf.femas.agent.classloader.AgentClassLoader;
import com.tencent.tsf.femas.agent.classloader.AgentPackagePathScanner;
import com.tencent.tsf.femas.agent.classloader.InterceptorClassLoaderCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.tencent.tsf.femas.common.context.ContextConstant.START_AGENT_FEMAS;

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

    public static void getThenSetAgentClassLoaderIfStartAgent(Class<?> clazz, Thread thread) {
        if (AgentConfig.doGetProperty(START_AGENT_FEMAS) != null && (Boolean) AgentConfig.doGetProperty(START_AGENT_FEMAS)) {
            thread.setContextClassLoader(getAgentClassLoader(clazz, thread));
        }
    }

    public static AgentClassLoader getAgentClassLoader(Class<?> clazz, Thread thread) {
        AgentClassLoader agentClassLoader;
        try {
            agentClassLoader = InterceptorClassLoaderCache.getAgentClassLoader(clazz.getClassLoader());
        } catch (Exception e) {
            agentClassLoader = InterceptorClassLoaderCache.getAgentClassLoader(thread.getContextClassLoader());
        }
        return agentClassLoader;
    }

}

