package com.tencent.tsf.femas.agent.tools;

import java.util.Collection;
import java.util.Map;

/**
 * copy from org.springframework.util.CollectionUtils
 */
public class AgentCollectionUtils {
    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

}
