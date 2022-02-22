package com.tencent.tsf.femas.common.util;

public class PropertiesUtil {

    /**
     * 参考Spring的优先级
     * -D优先级高于Env
     *
     * @param propertyName
     * @return
     */
    public static String getFromEnvAndProperty(String propertyName) {
        return getFromEnvAndProperty(propertyName, null);
    }

    public static String getFromEnvAndProperty(String propertyName, String defaultValue) {
        String result = System.getProperty(propertyName);

        if (result == null || result.isEmpty()) {
            String envName = propertyName.replace('.', '_');
            envName = envName.replace('-', '_');
            result = System.getenv(envName);
        }

        if (result == null) {
            return defaultValue;
        }

        return result;
    }
}
