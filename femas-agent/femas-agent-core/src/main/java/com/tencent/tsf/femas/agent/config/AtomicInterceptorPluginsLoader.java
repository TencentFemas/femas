package com.tencent.tsf.femas.agent.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TreeTraversingParser;
import com.tencent.tsf.femas.agent.tools.AgentLogger;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 解析配置文件
 *
 * @Author leoziltong@tencent.com
 */
public class AtomicInterceptorPluginsLoader {

    private final static AgentLogger logger = AgentLogger.getLogger(AtomicInterceptorPluginsLoader.class);

    private final String filePath;
    private Map<String, Object> conf;
    private JsonNode finalYamlLocations;
    private static final Yaml yml = new Yaml();
    private static final ObjectMapper mapper = new ObjectMapper();

    public AtomicInterceptorPluginsLoader(String filePath) {
        this.filePath = filePath;
    }

    public List<InterceptPluginConfig> getInterceptConfig() {
        FileReader reader = null;
        try {
            reader = new FileReader(filePath);
            BufferedReader buffer = new BufferedReader(reader);
            conf = yml.load(buffer);
            finalYamlLocations = mapper.convertValue(conf, JsonNode.class);
        } catch (FileNotFoundException e) {
            logger.error("[femas-core] get atomic interceptor plugin config failed ", e);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                logger.error("[femas-core] get atomic interceptor plugin config failed ", e);
            }
        }
        MechaRuntimeConfiguration atomicInterceptorsConfiguration = null;
        JsonNode node = finalYamlLocations.findValue("femas").findValue("agent");
        TreeTraversingParser treeTraversingParser = new TreeTraversingParser(node);
        try {
            atomicInterceptorsConfiguration = mapper.readValue(treeTraversingParser, MechaRuntimeConfiguration.class);
        } catch (IOException e) {
            logger.error("[femas-core] get atomic interceptor plugin config failed ", e);
        }
        return atomicInterceptorsConfiguration.getInterceptors();
    }
}
