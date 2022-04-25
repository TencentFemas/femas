package com.tencent.tsf.femas.constant;

/**
 * @author jianzhi
 * @date 2021/8/16 19:34
 */
public class DcfgConstants {

    public static class RELEASE_STATUS {

        /**
         * 未发布
         */
        public static final String UN_RELEASE = "U";

        /**
         * 发布成功
         */
        public static final String RELEASE_SUCCESS = "S";
        /**
         * 发布失败
         */
        public static final String RELEASE_FAILED = "F";
        /**
         * 生效中
         */
        public static final String RELEASE_VALID = "V";
        /**
         * 回滚成功
         */
        public static final String ROLLBACK_SUCCESS = "RS";
        /**
         * 回滚失败
         */
        public static final String ROLLBACK_FAILED = "RF";
        /**
         * 删除成功
         */
        public static final String DELETE_SUCCESS = "DS";
        /**
         * 删除失败
         */
        public static final String DELETE_FAILED = "DF";

    }
}
