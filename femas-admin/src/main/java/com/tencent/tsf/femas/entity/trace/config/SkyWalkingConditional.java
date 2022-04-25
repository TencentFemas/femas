package com.tencent.tsf.femas.entity.trace.config;

import static com.tencent.tsf.femas.constant.AdminConstants.TRACE_SERVER_SKYWALKING;
import static com.tencent.tsf.femas.constant.AdminConstants.TRACE_SERVER_TYPE;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * @author Cody
 * @date 2021 2021/7/28 3:55 下午
 */
public class SkyWalkingConditional implements Condition {

    @Override
    public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
        String traceType = System.getProperty(TRACE_SERVER_TYPE);
        if (StringUtils.isEmpty(traceType) || TRACE_SERVER_SKYWALKING.equalsIgnoreCase(traceType)) {
            return true;
        }
        return false;
    }
}
