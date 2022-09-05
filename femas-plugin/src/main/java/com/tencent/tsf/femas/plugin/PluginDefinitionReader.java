package com.tencent.tsf.femas.plugin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tencent.tsf.femas.common.context.AgentConfig;
import com.tencent.tsf.femas.common.util.ConfigUtils;
import org.apache.commons.collections.MapUtils;
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

    private static final Logger logger = LoggerFactory.getLogger(PluginDefinitionReader.class);

    //默认加载顺序，跟springboot保持一致，参见ConfigFileApplicationListener
    private static final String DEFAULT_SEARCH_LOCATIONS = "classpath:/,classpath:/config/";
    public static final String CLASSPATH_URL_PREFIX = "classpath:/";
    private static final String YAML_DEFAULT_NAMES = "femas.yaml";

    public static final String FEMAS_CONF_LOCATION_PROPERTY = "femas.yaml";

    private static final Map<String, Object> conf;
    private static final JsonNode finalYamlLocations;
    private static final Properties finalPropertiesLocations;

    static {
        // 加载外部配置文件
        Properties properties = System.getProperties();
        String location = properties.getProperty(FEMAS_CONF_LOCATION_PROPERTY);
        if (StringUtils.isNotBlank(location)) {
            File configFile = new File(location);
            conf = ConfigUtils.loadAbsoluteConfig(configFile);
        } else {
            conf = new TreeMap<>();
            // 加载 classpath 配置文件
            String[] locationAdds = DEFAULT_SEARCH_LOCATIONS.split(",");
            for (String lo : locationAdds) {
                lo = lo.substring(CLASSPATH_URL_PREFIX.length()).concat(YAML_DEFAULT_NAMES);
                conf.putAll(ConfigUtils.loadRelativeYamlConfig(lo));
            }
        }
        if (AgentConfig.doGetProperty(START_AGENT_FEMAS) != null && ((Boolean) AgentConfig.doGetProperty(START_AGENT_FEMAS))) {
            conf.putAll(AgentConfig.getConf());
        }
        conf.putAll(new LinkedHashMap(System.getenv()));
        conf.putAll(new LinkedHashMap(System.getProperties())); // 注意部分属性可能被覆盖为字符串

        ObjectMapper mapper = new ObjectMapper();
        finalYamlLocations = mapper.convertValue(conf, JsonNode.class);

        finalPropertiesLocations = new Properties();
        finalPropertiesLocations.putAll(conf);
    }

    public Object getProperty(String serviceName) {
        return this.conf.get(serviceName);
    }

    public JsonNode getJsonNode() {
        return this.finalYamlLocations;
    }

    public Properties getProperties() {
        return this.finalPropertiesLocations;
    }

}
