package com.tencent.tsf.femas.common.tag.constant;

/**
 * 标签常量.
 *
 * @author zhixinzxliu
 */
public class TagConstant {

    /**
     * 标签类型
     */
    public static class TYPE {

        /**
         * 系统标签.
         */
        public static final String SYSTEM = "S";

        /**
         * 用户自定义标签
         */
        public static final String CUSTOM = "U";
    }

    /**
     * 操作符
     */
    public static class OPERATOR {

        /**
         * 包含
         */
        public static final String IN = "IN";
        /**
         * 不包含
         */
        public static final String NOT_IN = "NOT_IN";
        /**
         * 等于
         */
        public static final String EQUAL = "EQUAL";
        /**
         * 不等于
         */
        public static final String NOT_EQUAL = "NOT_EQUAL";
        /**
         * 正则
         */
        public static final String REGEX = "REGEX";
    }
}
