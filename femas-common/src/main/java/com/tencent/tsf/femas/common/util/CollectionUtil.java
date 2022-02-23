package com.tencent.tsf.femas.common.util;

import java.util.Collection;
import java.util.List;

public class CollectionUtil {

    public static boolean isEmpty(Collection<?> collection) {
        return (collection == null || collection.isEmpty());
    }

    public static boolean isNotEmpty(Collection<?> collection) {
        return !(collection == null || collection.isEmpty());
    }

    public static boolean hasString(List<String> list, String target) {
        if (isEmpty(list) || StringUtils.isEmpty(target)) {
            return false;
        }
        for (String str : list) {
            if (target.equals(str)) {
                return true;
            }
        }
        return false;
    }

}
