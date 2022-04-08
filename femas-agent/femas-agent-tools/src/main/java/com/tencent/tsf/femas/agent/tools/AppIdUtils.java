package com.tencent.tsf.femas.agent.tools;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class AppIdUtils {
    private static final String APP_PROPERTIES_CLASSPATH = "/META-INF/app.properties";
    private static Properties m_appProperties = new Properties();

    static {
        try {
            InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(APP_PROPERTIES_CLASSPATH);
            if (in == null) {
                in = AppIdUtils.class.getResourceAsStream(APP_PROPERTIES_CLASSPATH);
            }
            m_appProperties.load(new InputStreamReader(in, StandardCharsets.UTF_8));
        } catch (Throwable ex) {

        }
    }

    public static String getAppId() {
        String appId = System.getProperty("app.id");
        if (appId != null) {
            return appId;
        }
        appId = System.getProperty("APP_ID");
        if (appId != null) {
            return appId;
        }
        Object appIdObj = m_appProperties.get("app.id");
        if (appIdObj == null) {
            return null;
        } else {
            return appIdObj.toString();
        }
    }
}
