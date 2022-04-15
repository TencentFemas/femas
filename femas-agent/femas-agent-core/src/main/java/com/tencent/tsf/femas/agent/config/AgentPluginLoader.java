package com.tencent.tsf.femas.agent.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TreeTraversingParser;
import com.tencent.tsf.femas.agent.classloader.AgentPackagePathScanner;
import com.tencent.tsf.femas.agent.tools.Resource;
import com.tencent.tsf.femas.agent.tools.YamlPropertiesFactoryBean;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.*;

/**
 * 解析配置文件
 */
public class AgentPluginLoader {

    private final static String filePath = "/config/plugin.yaml";

    private static Map<String, Object> conf;
    private static JsonNode finalYamlLocations;
    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        Yaml yml = new Yaml();
        FileReader reader = null;
        try {
            reader = new FileReader(AgentPackagePathScanner.getPath() + filePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BufferedReader buffer = new BufferedReader(reader);
        conf = yml.load(buffer);
        finalYamlLocations = mapper.convertValue(conf, JsonNode.class);
    }

    public static List<GlobalInterceptPluginConfig> getInterceptConfig() {
        MechaRuntimeConfiguration mechaRuntimeConfiguration = null;
        JsonNode node = finalYamlLocations.findValue("femas").findValue("agent");
        TreeTraversingParser treeTraversingParser = new TreeTraversingParser(node);
        try {
            mechaRuntimeConfiguration = mapper.readValue(treeTraversingParser, MechaRuntimeConfiguration.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mechaRuntimeConfiguration.getInterceptors();
    }
}
