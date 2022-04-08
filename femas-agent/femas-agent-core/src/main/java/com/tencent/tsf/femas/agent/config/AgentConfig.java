package com.tencent.tsf.femas.agent.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TreeTraversingParser;
import com.tencent.tsf.femas.agent.tools.ConfigUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * 解析配置文件
 */
public class AgentConfig {

    private final static String filePath = "/agent.yml";

    private static final Map<String, Object> conf;
    private static final JsonNode finalYamlLocations;
    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        File configFile = new File(filePath);
        conf = ConfigUtils.loadAbsoluteConfig(configFile);
        ObjectMapper mapper = new ObjectMapper();
        finalYamlLocations = mapper.convertValue(conf, JsonNode.class);
    }

    public static List<InterceptPluginLoader> getInterceptConfig() {
        InterceptPluginLoader interceptPluginLoader = null;
        TreeTraversingParser treeTraversingParser = new TreeTraversingParser(finalYamlLocations);
        try {
            interceptPluginLoader = mapper.readValue(treeTraversingParser, InterceptPluginLoader.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return interceptPluginLoader.getConfigs();
    }
}
