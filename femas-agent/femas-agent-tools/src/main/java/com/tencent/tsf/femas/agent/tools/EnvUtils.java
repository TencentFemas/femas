package com.tencent.tsf.femas.agent.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class EnvUtils {
    private static final String SERVER_PROPERTIES_LINUX = "/opt/settings/server.properties";
    private static final String SERVER_PROPERTIES_WINDOWS = "C:/opt/settings/server.properties";
    private static String m_env;
    private static String m_dc;

    private static Properties m_serverProperties = new Properties();

    static {
        try {
            String path = isOSWindows() ? SERVER_PROPERTIES_WINDOWS : SERVER_PROPERTIES_LINUX;

            File file = new File(path);
            if (file.exists() && file.canRead()) {
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(file);
                    if (fis != null) {
                        m_serverProperties.load(new InputStreamReader(fis, StandardCharsets.UTF_8));
                    }
                } finally {
                    if (fis != null) {
                        fis.close();
                    }
                }
            }
            initEnvType();
            initDataCenter();
        } catch (Throwable ex) {
        }
    }

    private static void initEnvType() {
        // 1. Try to get environment from JVM system property
        m_env = System.getProperty("env");
        if (!isEmpty(m_env)) {
            m_env = m_env.trim();
            return;
        }

        // 2. Try to get environment from OS environment variable
        m_env = System.getenv("ENV");
        if (!isEmpty(m_env)) {
            m_env = m_env.trim();
            return;
        }

        // 3. Try to get environment from file "server.properties"
        m_env = m_serverProperties.getProperty("env");
        if (!isEmpty(m_env)) {
            m_env = m_env.trim();
            return;
        }

        // 4. Set environment to null.
        m_env = null;
    }

    private static void initDataCenter() {
        m_dc = System.getProperty("idc");
        if (isEmpty(m_dc)) {
            m_dc = m_dc.trim();
            return;
        }
        m_dc = System.getenv("IDC");
        if (!isEmpty(m_dc)) {
            m_dc = m_dc.trim();
            return;
        }
        m_dc = m_serverProperties.getProperty("idc");
        if (!isEmpty(m_dc)) {
            m_dc = m_dc.trim();
            return;
        }
        m_dc = null;
    }

    public static boolean isOSWindows() {
        String osName = System.getProperty("os.name");
        if (isEmpty(osName)) {
            return false;
        }
        return osName.startsWith("Windows");
    }

    private static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    public static String getDataCenter() {
        return m_dc;
    }

    public static boolean isDataCenterSet() {
        return m_dc != null;
    }

    public static String getEnvType() {
        return m_env;
    }

    public static String getEnv(String key) {
        if (System.getProperty(key) != null) {
            return System.getProperty(key).toString();
        }
        return m_serverProperties.get(key) == null ? null : m_serverProperties.get(key).toString();
    }

    public static boolean isFatEnv() {
        if (System.getProperty("env") != null) {
            return "fat".equals(System.getProperty("env").toString());
        }
        return "fat".equals(m_serverProperties.get("env") == null ? null : m_serverProperties.get("env").toString());
    }
}
