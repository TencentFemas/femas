package com.tencent.tsf.femas.util;

import com.tencent.tsf.femas.common.util.StringUtils;
import com.tencent.tsf.femas.constant.AdminConstants;

/**
 * @author Cody
 * @date 2021 2021/9/26 11:12 上午
 */
public class EnvUtil {

    // 获取路由前缀
    public static String getFemasPrefix() {
        String FEMAS_BASE_PATH = System.getProperty(AdminConstants.FEMAS_BASE_PATH);
        if (FEMAS_BASE_PATH == null) {
            FEMAS_BASE_PATH = "";
        }
        if (!StringUtils.isEmpty(FEMAS_BASE_PATH) && FEMAS_BASE_PATH.charAt(0) != '/') {
            FEMAS_BASE_PATH = '/' + FEMAS_BASE_PATH;
        }
        return FEMAS_BASE_PATH;
    }
}
