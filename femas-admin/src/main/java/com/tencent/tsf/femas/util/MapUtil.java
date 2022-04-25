package com.tencent.tsf.femas.util;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.util.StringUtils;

/**
 * TSF Map工具类
 *
 * @author hongweizhu
 */
public class MapUtil {

    /**
     * 将N个对象转换为一个Map<br>
     * <ul>
     * <li>标注了@JsonIgnore的get方法会被忽略</li>
     * <li>总是使用get方法取值，没有get方法则忽略该值</li>
     * <li>多个obj合并时，同名变量会被覆盖（后传入的覆盖先传入的）</li>
     * <li><b>注：这是一个非深度的方法</b></li>
     * </ul>
     *
     * @param ignoreNullValue 是否忽略null值<br>
     *         <ul>
     *         <li>true：值为null的字段不输出</li>
     *         <li>false：值为null的字段输出</li>
     *         </ul>
     * @param objs 需要转换的对象实例数组
     * @return 转换后的Map实例；如果objs为null或者长度为0时，返回null
     */
    public static Map<String, Object> getMapValue(Boolean ignoreNullValue, Object... objs) {
        if (null == objs || objs.length == 0) {
            return null;
        } else {
            Map<String, Object> map = new HashMap<>();
            for (Object obj : objs) {
                // 为空时跳过
                if (null == obj) {
                    continue;
                }
                for (Method m : obj.getClass().getMethods()) {
                    // 屏蔽getClass()方法
                    if ("getClass".equals(m.getName())) {
                        continue;
                    }
                    // 对get方法进行处理，屏蔽掉方法名只有get三个字符的方法
                    if (m.getName().startsWith("get") && m.getName().length() > 3) {
                        try {
                            // 标注了@JsonIgnore的属性会被忽略
                            if (null == m.getAnnotation(JsonIgnore.class)) {
                                Object value = m.invoke(obj);
                                // 如果忽略null值，则跳过值为null的字段
                                if (ignoreNullValue && null == value) {
                                    continue;
                                } else {
                                    map.put(StringUtils.uncapitalize(m.getName().substring(3)), value);
                                }
                            }
                        } catch (Exception e) {
                            // doing nothing
                        }
                    }
                }
            }
            return map;
        }
    }

    /**
     * 深度合并Map，主要用于配置合并场景<br>
     * 对所有Value为Map类型的Entry进行深度优先的遍历
     *
     * @param <K> key type
     * @param map1 高优先级map
     * @param map2 低优先级map
     * @return 返回合并后的map
     */
    @SuppressWarnings({"unchecked"})
    public static <K> Map<K, Object> deepMergeAnyKeyType(Map<K, Object> map1, Map<K, Object> map2) {
        for (K key : map2.keySet()) {
            // 1 对key的存在性进行校验
            if (!map1.containsKey(key)) {
                // 1.1 当map1中不存在key时，直接合并
                map1.put(key, map2.get(key));
            } else {
                // 1.2 key存在时分情况判断
                // 1.2.1 Map2的值是Map类型
                if (map2.get(key) instanceof Map) {
                    if (map1.get(key) instanceof Map) {
                        // 1.2.1.1 Map1的值也是Map类型
                        map1.put(key,
                                deepMergeAnyKeyType((Map<K, Object>) map1.get(key), (Map<K, Object>) map2.get(key)));
                    } else {
                        // 1.2.1.2 两者类型不一致时，不处理
                        continue;
                    }
                }
                // // 1.2.2 Map2的值为List类型，不处理
                // if (map2.get(key) instanceof List) {
                // if (map1.get(key) instanceof List) {
                // // 1.2.2.1 Map1的值也是List
                // } else {
                // // 1.2.2.2 两者类型不一致时，不处理
                // continue;
                // }
                // }
                // 1.2.3 Map2的值为其他类型，一律不处理
            }
        }
        return map1;
    }

    /**
     * 深度合并Map，主要用于配置合并场景<br>
     * 对所有Value为Map类型的Entry进行深度优先的遍历
     *
     * @param map1 高优先级map
     * @param map2 低优先级map
     * @return 返回合并后的map
     */
    public static Map<String, Object> deepMerge(Map<String, Object> map1, Map<String, Object> map2) {
        return deepMergeAnyKeyType(map1, map2);
    }

    /**
     * 列表转换为以ID为key的HashMap，所有异常都会导致当次循环跳过
     *
     * @param list 列表
     * @param getIdMethodName 获取ID的方法名
     * @return 转换好的Map，list为null时返回null
     */
    public static <T> Map<String, T> list2HashMap(List<T> list, String getIdMethodName) {
        if (null == list) {
            return null;
        }
        HashMap<String, T> map = new HashMap<>();
        for (T obj : list) {
            if (null != obj) {
                try {
                    map.put(String.valueOf(obj.getClass().getMethod(getIdMethodName).invoke(obj)), obj);
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
                        | SecurityException e) {
                    // skip this circle
                }
            }
        }
        return map;
    }
}
