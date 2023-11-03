package com.tencent.tsf.femas.plugin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tencent.tsf.femas.common.context.AgentConfig;
import com.tencent.tsf.femas.common.util.ConfigUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import static com.tencent.tsf.femas.common.context.ContextConstant.START_AGENT_FEMAS;

/**
 * 加载配置文件
 *
 * @Author leoziltong
 * @Date: 2021/4/19 17:19
 * @Version 1.0
 */
public class PluginDefinitionReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginDefinitionReader.class);

    /**
     * 默认加载顺序，跟springboot保持一致，参见ConfigFileApplicationListener
     */
    private static final String DEFAULT_SEARCH_LOCATIONS = "classpath:/,classpath:/config/";
    public static final String CLASSPATH_URL_PREFIX = "classpath:/";
    private static final String YAML_DEFAULT_NAMES = "femas.yaml";

    public static final String FEMAS_CONF_LOCATION_PROPERTY = "femas.yaml";

    private static final Map<String, Object> CONF;
    private static final JsonNode FINAL_YAML_LOCATIONS;
    private static final Properties FINAL_PROPERTIES_LOCATIONS;

    static {
        // 加载外部配置文件
        Properties properties = System.getProperties();
        String location = properties.getProperty(FEMAS_CONF_LOCATION_PROPERTY);
        if (StringUtils.isNotBlank(location)) {
            File configFile = new File(location);
            CONF = ConfigUtils.loadAbsoluteConfig(configFile);
        } else {
            CONF = new TreeMap<>();
            // 加载 classpath 配置文件
            String[] locationAdds = DEFAULT_SEARCH_LOCATIONS.split(",");
            for (String lo : locationAdds) {
                lo = lo.substring(CLASSPATH_URL_PREFIX.length()).concat(YAML_DEFAULT_NAMES);
                CONF.putAll(ConfigUtils.loadRelativeYamlConfig(lo));
            }
        }
        if (AgentConfig.doGetProperty(START_AGENT_FEMAS) != null && ((Boolean) AgentConfig.doGetProperty(START_AGENT_FEMAS))) {
            CONF.putAll(AgentConfig.getConf());
        }
        CONF.putAll(new LinkedHashMap<>(System.getenv()));
        // 注意部分属性可能被覆盖为字符串
        CONF.putAll(new LinkedHashMap(System.getProperties()));

        ObjectMapper mapper = new ObjectMapper();
        FINAL_YAML_LOCATIONS = mapper.convertValue(CONF, JsonNode.class);

        FINAL_PROPERTIES_LOCATIONS = new Properties();
        FINAL_PROPERTIES_LOCATIONS.putAll(CONF);
    }

    public Object getProperty(String serviceName) {
        return CONF.get(serviceName);
    }

    public JsonNode getJsonNode() {
        return FINAL_YAML_LOCATIONS;
    }

    public Properties getProperties() {
        return FINAL_PROPERTIES_LOCATIONS;
    }

}
