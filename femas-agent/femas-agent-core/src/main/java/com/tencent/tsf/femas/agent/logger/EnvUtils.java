package com.tencent.tsf.femas.agent.logger;

/**
 */
public class EnvUtils {

    public static boolean isOSWindows() {
        String osName = System.getProperty("os.name");
        if (isEmpty(osName)) {
            return false;
        }
        return osName.startsWith("Windows");
    }

    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }
}
