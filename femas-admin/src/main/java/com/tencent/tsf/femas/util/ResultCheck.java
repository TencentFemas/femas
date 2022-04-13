package com.tencent.tsf.femas.util;

/**
 * @author Cody
 * @date 2021 2021/7/29 11:01 上午
 */
public class ResultCheck {

    private static int CONFIGURE_CHECK_SUCCESS_COUNT = 1;

    public static boolean checkCount(int optionCount) {
        if (optionCount == CONFIGURE_CHECK_SUCCESS_COUNT) {
            return true;
        } else {
            return false;
        }
    }
}
