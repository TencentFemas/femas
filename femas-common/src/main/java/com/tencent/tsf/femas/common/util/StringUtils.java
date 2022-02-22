package com.tencent.tsf.femas.common.util;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * 字符串操作工具类
 */
public class StringUtils {

    public static final String EMPTY = "";

    public static final String CONTEXT_SEP = "/";

    public static final String ALL = "*";

    public static final String TRUE = "true";

    public static final String FALSE = "false";

    /**
     * 空数组
     */
    public static final String[] EMPTY_STRING_ARRAY = new String[0];

    /**
     * <p>Checks if a CharSequence is empty ("") or null.</p>
     *
     * <pre>
     * StringUtils.isEmpty(null)      = true
     * StringUtils.isEmpty("")        = true
     * StringUtils.isEmpty(" ")       = false
     * StringUtils.isEmpty("bob")     = false
     * StringUtils.isEmpty("  bob  ") = false
     * </pre>
     *
     * <p>NOTE: This method changed in Lang version 2.0.
     * It no longer trims the CharSequence.
     * That functionality is available in isBlank().</p>
     *
     * @param cs the CharSequence to check, may be null
     * @return {@code true} if the CharSequence is empty or null
     * @since 3.0 Changed signature from isEmpty(String) to isEmpty(CharSequence)
     */
    public static boolean isEmpty(CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    /**
     * <p>Checks if a CharSequence is not empty ("") and not null.</p>
     *
     * <pre>
     * StringUtils.isNotEmpty(null)      = false
     * StringUtils.isNotEmpty("")        = false
     * StringUtils.isNotEmpty(" ")       = true
     * StringUtils.isNotEmpty("bob")     = true
     * StringUtils.isNotEmpty("  bob  ") = true
     * </pre>
     *
     * @param cs the CharSequence to check, may be null
     * @return {@code true} if the CharSequence is not empty and not null
     * @since 3.0 Changed signature from isNotEmpty(String) to isNotEmpty(CharSequence)
     */
    public static boolean isNotEmpty(CharSequence cs) {
        return !StringUtils.isEmpty(cs);
    }

    /**
     * <p>Checks if a CharSequence is whitespace, empty ("") or null.</p>
     *
     * <pre>
     * StringUtils.isBlank(null)      = true
     * StringUtils.isBlank("")        = true
     * StringUtils.isBlank(" ")       = true
     * StringUtils.isBlank("bob")     = false
     * StringUtils.isBlank("  bob  ") = false
     * </pre>
     *
     * @param cs the CharSequence to check, may be null
     * @return {@code true} if the CharSequence is null, empty or whitespace
     * @since 3.0 Changed signature from isBlank(String) to isBlank(CharSequence)
     */
    public static boolean isBlank(CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * <p>Checks if a CharSequence is not empty (""), not null and not whitespace only.</p>
     *
     * <pre>
     * StringUtils.isNotBlank(null)      = false
     * StringUtils.isNotBlank("")        = false
     * StringUtils.isNotBlank(" ")       = false
     * StringUtils.isNotBlank("bob")     = true
     * StringUtils.isNotBlank("  bob  ") = true
     * </pre>
     *
     * @param cs the CharSequence to check, may be null
     * @return {@code true} if the CharSequence is
     *         not empty and not null and not whitespace
     * @since 3.0 Changed signature from isNotBlank(String) to isNotBlank(CharSequence)
     */
    public static boolean isNotBlank(CharSequence cs) {
        return !StringUtils.isBlank(cs);
    }

    /**
     * <pre>
     * StringUtils.trim(null)          = null
     * StringUtils.trim("")            = ""
     * StringUtils.trim("     ")       = ""
     * StringUtils.trim("abc")         = "abc"
     * StringUtils.trim("    abc    ") = "abc"
     * </pre>
     *
     * @param str the String to be trimmed, may be null
     * @return the trimmed string, {@code null} if null String input
     */
    public static String trim(String str) {
        return str == null ? null : str.trim();
    }

    /**
     * Converts a <code>byte[]</code> to a String using the specified character encoding.
     *
     * @param bytes the byte array to read from
     * @param charsetName the encoding to use, if null then use the platform default
     * @return a new String
     * @throws UnsupportedEncodingException If the named charset is not supported
     * @throws NullPointerException if the input is null
     * @since 3.1
     */
    public static String toString(byte[] bytes, String charsetName) throws UnsupportedEncodingException {
        return charsetName == null ? new String(bytes) : new String(bytes, charsetName);
    }

    /**
     * 对象转string
     *
     * @param o 对象
     * @param defaultVal 默认值
     * @return 不为null执行toString方法
     */
    public static String toString(Object o, String defaultVal) {
        return o == null ? defaultVal : o.toString();
    }

    /**
     * 对象转string
     *
     * @param o 对象
     * @return 不为null执行toString方法
     */
    public static String toString(Object o) {
        return toString(o, null);
    }


    /**
     * 字符串是否相同
     *
     * @param s1 字符串1
     * @param s2 字符串2
     * @return 是否相同
     */
    public static boolean equals(CharSequence s1, CharSequence s2) {
        return s1 == null ? s2 == null : s1.equals(s2);
    }

    /**
     * 按分隔符分隔的数组，包含空值<br>
     * 例如 "1,2,,3," 返回 [1,2,,3,] 5个值
     *
     * @param src 原始值
     * @param separator 分隔符
     * @return 字符串数组
     */
    public static String[] split(String src, String separator) {
        if (isEmpty(separator)) {
            return new String[]{src};
        }
        if (isEmpty(src)) {
            return StringUtils.EMPTY_STRING_ARRAY;
        }
        return src.split(separator, -1);
    }

    /**
     * 按逗号或者分号分隔的数组，排除空字符<br>
     * 例如 " 1,2 ,, 3 , " 返回 [1,2,3] 3个值<br>
     * " 1;2 ;; 3 ; " 返回 [1,2,3] 3个值<br>
     *
     * @param src 原始值
     * @return 字符串数组
     */
    public static String[] splitWithCommaOrSemicolon(String src) {
        if (isEmpty(src)) {
            return StringUtils.EMPTY_STRING_ARRAY;
        }
        String[] ss = split(src.replace(',', ';'), ";");
        List<String> list = new ArrayList<String>();
        for (String s : ss) {
            if (!isBlank(s)) {
                list.add(s.trim());
            }
        }
        return list.toArray(new String[list.size()]);
    }

    public static String toUpperCase(String str) {
        if (str == null) {
            return null;
        } else {
            return str.toUpperCase();
        }
    }


    public static boolean hasText(String str) {
        return (str != null && !str.isEmpty() && containsText(str));
    }

    public static boolean hasLength(String str) {
        return str != null && !str.isEmpty();
    }

    private static boolean containsText(CharSequence str) {
        int strLen = str.length();
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }

}
