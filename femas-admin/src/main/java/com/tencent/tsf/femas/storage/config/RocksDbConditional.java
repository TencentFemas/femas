package com.tencent.tsf.femas.storage.config;


import static com.tencent.tsf.femas.constant.AdminConstants.DB_TYPE;
import static com.tencent.tsf.femas.constant.AdminConstants.EMBEDDED;
import static com.tencent.tsf.femas.constant.AdminConstants.EXTERNAL;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * @author Cody
 * @date 2021 2021/7/28 2:49 下午
 */
public class RocksDbConditional implements Condition {

    @Override
    public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
        String dbType = System.getProperty(DB_TYPE);
        if (EMBEDDED.equalsIgnoreCase(dbType)) {
            return true;
        }
        // 默认选择内嵌数据库
        if (!EXTERNAL.equalsIgnoreCase(dbType)) {
            return true;
        }
        return false;
    }

//    @Override
//    public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
//        return false;
//    }
}
