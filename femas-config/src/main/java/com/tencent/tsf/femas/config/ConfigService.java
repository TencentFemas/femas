package com.tencent.tsf.femas.config;

import com.tencent.tsf.femas.common.spi.SpiService;
import java.util.Map;

/**
 * Entry point for client config use
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public class ConfigService {

    /**
     * key是注册中心的类型
     * value是注册中心的类别
     */
    private static Map<String, ConfigFactory> CONFIG_FACTORIES;

    /**
     * 创建新的Registry然后返回
     * 请自行保存返回后的Config并且复用，请勿每次调用获取该对象
     *
     * @param type
     * @param configs
     * @return
     */
    public static synchronized Config createConfig(String type, Map<String, String> configs) {
        if (CONFIG_FACTORIES == null) {
            CONFIG_FACTORIES = SpiService.init(ConfigFactory.class);
        }

        ConfigFactory configFactory = CONFIG_FACTORIES.get(type);
        if (configFactory == null) {
            throw new IllegalArgumentException("Invalid type " + type + ". ConfigFactory : Type not registered.");
        }

        Config config = configFactory.create(configs);
        return config;
    }
}
