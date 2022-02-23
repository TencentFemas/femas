package com.tencent.tsf.femas.registry.impl.consul.util;

import com.tencent.tsf.femas.registry.impl.consul.ConsulConstants;

public class NormalizeUtil {

    /**
     * copy from Spring
     *
     * @param s
     * @return
     */
    public static String normalizeForDns(String s) {
        // tsf not check consul service instance id start with letter and end with
        // letter or digit
        if (s == null) {
            throw new IllegalArgumentException("Consul service ids must not be empty");
        }

        StringBuilder normalized = new StringBuilder();
        Character prev = null;
        for (char curr : s.toCharArray()) {
            Character toAppend = null;
            if (Character.isLetterOrDigit(curr)) {
                toAppend = curr;
            } else if (prev == null || !(prev == ConsulConstants.SEPARATOR)) {
                toAppend = ConsulConstants.SEPARATOR;
            }
            if (toAppend != null) {
                normalized.append(toAppend);
                prev = toAppend;
            }
        }

        return normalized.toString();
    }
}
