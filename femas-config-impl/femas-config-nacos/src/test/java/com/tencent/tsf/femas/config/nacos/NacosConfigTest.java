package com.tencent.tsf.femas.config.nacos;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.ConfigType;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.tencent.tsf.femas.config.Config;
import com.tencent.tsf.femas.config.ConfigChangeListener;
import com.tencent.tsf.femas.config.FemasConfigManagerFactory;
import com.tencent.tsf.femas.config.impl.nacos.FemasNacosConfig;
import com.tencent.tsf.femas.config.model.ConfigChangeEvent;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.Executor;
import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class NacosConfigTest {

    String serverAddr = "127.0.0.1:8848";
    //    String dataIdProperties = "provider-demo.properties";
//    String dataIdYaml = "provider-demo.yaml";
    String dataIdProperties = "testDataId";
    String dataIdYaml = "test123";
    String group = "DEFAULT_GROUP";
    String namespaceId = "ns-mldqvo58";
    ConfigService configService = null;

    public NacosConfigTest() throws NacosException {
        Properties properties = new Properties();
        properties.put("serverAddr", serverAddr);
        properties.put("namespace", namespaceId);
        configService = NacosFactory.createConfigService(properties);
    }

    @Test
    public void putAndGetByNacosConfig() {
        try {
            boolean isPublishOk = configService.publishConfig(dataIdYaml, group, "user:\n" +
                    "  name: juanYaml666", ConfigType.YAML.getType());
            System.out.println("isPublishYamlOk:" + isPublishOk);

            String content = configService.getConfig(dataIdYaml, group, 5000);
            System.out.println("getContentWithYaml:\n" + content);

            isPublishOk = configService.publishConfig(dataIdProperties, group, "user.name=juanyinProperties666",
                    ConfigType.PROPERTIES.getType());
            System.out.println("isPublishPropretiesOk:" + isPublishOk);

            content = configService.getConfig(dataIdProperties, group, 5000);
            System.out.println("getContentWithProperties:\n" + content);
        } catch (NacosException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void putConfigAndAddListenerByNacosConfig() {
        try {
            addListener();

            int i = new Random().nextInt(1000);
            boolean isPublishOk = configService
                    .publishConfig(dataIdProperties, group, "user.id=1\nuser.name=juanying" + i);
            System.out.println(isPublishOk);

            // sleep 2s，保证在打印出订阅到的数据以后再退出
            Thread.sleep(2000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addListener() {
        try {
            String content = configService.getConfig(dataIdProperties, group, 5000);
            System.out.println(content);
            configService.addListener(dataIdProperties, group, new Listener() {
                @Override
                public void receiveConfigInfo(String configInfo) {
                    System.out.println("recieve1:" + configInfo);
                }

                @Override
                public Executor getExecutor() {
                    return null;
                }
            });
        } catch (NacosException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void putAndGetByAtomNacosConfig() {
        try {
            // 设置SERVER-ADDR
//            System.getProperties().setProperty("spring.cloud.nacos.config.server-addr", "127.0.0.1:8848");

            String configId = dataIdProperties;
            String systemTag = "{\"group\":\"" + group + "\"}";
            System.out.println(systemTag);

            String configValue = "user.name=juanyingTest666";
            String configType = ConfigType.PROPERTIES.getType();
            Object[] params = new Object[]{namespaceId, configId, systemTag, configValue, configType};

            Config atomNacosConfig = FemasConfigManagerFactory.getConfigManagerInstance().getConfig();
            boolean publishResult = atomNacosConfig.publishConfig(params);
            System.out.println("publishResult:" + publishResult);

            String key = namespaceId + FemasNacosConfig.NUMBER_SIGN_SEPARATOR + dataIdProperties
                    + FemasNacosConfig.NUMBER_SIGN_SEPARATOR + group;
            String content = (String) atomNacosConfig.getProperty(key, null);
            System.out.println("getContent:\n" + content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void putConfigAndAddListenerByAtomNacosConfig() {
        try {
            String key = namespaceId + FemasNacosConfig.NUMBER_SIGN_SEPARATOR + dataIdProperties
                    + FemasNacosConfig.NUMBER_SIGN_SEPARATOR + group;
            Config atomNacosConfig = FemasConfigManagerFactory.getConfigManagerInstance().getConfig();
            atomNacosConfig.subscribe(key, new ConfigChangeListener<String>() {

                @Override
                public void onChange(List<ConfigChangeEvent<String>> changeEvents) {
                }

                @Override
                public void onChange(ConfigChangeEvent<String> changeEvent) {
                    System.out.println("receive " + changeEvent);
                }

            });

            int i = new Random().nextInt(1000);

            String configId = dataIdProperties;
            String systemTag = "{\"group\":\"" + group + "\"}";
            System.out.println(systemTag);

            String configValue = "user.name=juanyingTest" + i;
            String configType = ConfigType.PROPERTIES.getType();
            Object[] params = new Object[]{namespaceId, configId, systemTag, configValue, configType};
            atomNacosConfig.publishConfig(params);

            // sleep 2s，保证在打印出订阅到的数据以后再退出
            Thread.sleep(2000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
