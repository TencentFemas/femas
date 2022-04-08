package com.tencent.tsf.femas.agent.tools;

/**
 */
public class ConfigPropertiesProxy {
    private static final String ConfigClass = "org.flamingo.gray.agent.config.ConfigProperties";
    private final static String GrayServerAddress = "gray.server.address";

    public static String getGrayServerAddress() {
        return getConfig(GrayServerAddress);
    }

    public static String getConfig(String configKey) {
        return (String) ReflectionUtils.invokeStaticMethod(ConfigClass, "getConfig", configKey);

    }
}
