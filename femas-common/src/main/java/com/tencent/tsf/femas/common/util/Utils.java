package com.tencent.tsf.femas.common.util;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common util class.
 *
 * @author andrewshan
 * @date 2019/8/24
 */
public class Utils {

    private static final Logger LOG = LoggerFactory.getLogger(Utils.class);

    public static long sleepUninterrupted(long millis) {
        long currentTime = System.currentTimeMillis();
        long deadline = currentTime + millis;
        while (currentTime < deadline) {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                LOG.debug(String.format("interrupted while sleeping %d", millis), e);
            }
            currentTime = System.currentTimeMillis();
        }
        return currentTime;
    }

    public static Map<String, Object> objectToMap(Object obj) throws Exception {
        if (obj == null) {
            return null;
        }

        Map<String, Object> map = new HashMap<String, Object>();

        BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass());
        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        if (null == propertyDescriptors) {
            return null;
        }
        for (PropertyDescriptor property : propertyDescriptors) {
            String key = property.getName();
            if (key.compareToIgnoreCase("class") == 0) {
                continue;
            }
            Method getter = property.getReadMethod();
            Object value = getter != null ? getter.invoke(obj) : null;
            map.put(key, value);
        }

        return map;
    }

    public static String translatePath(String path) {
        if (path.startsWith("$HOME")) {
            String userHome = System.getProperty("user.home");
            return org.apache.commons.lang3.StringUtils.replace(path, "$HOME", userHome);
        }
        return path;
    }

    public static boolean regMatch(String regex, String input) {
        /**
         * 用正则表达式来判断
         * 1.compile(String regex)    将给定的正则表达式编译到模式中。
         * 2.matcher(CharSequence input)    创建匹配给定输入与此模式的匹配器。
         * 3.matches()    尝试将整个区域与模式匹配。
         */
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(input);
        return m.matches();
    }
}
