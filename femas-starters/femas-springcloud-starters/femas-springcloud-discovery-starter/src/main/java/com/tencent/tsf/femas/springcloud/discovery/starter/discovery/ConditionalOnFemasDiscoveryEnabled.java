package com.tencent.tsf.femas.springcloud.discovery.starter.discovery;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.ConditionalOnDiscoveryEnabled;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述：
 * 创建日期：2022年05月18 19:42:46
 *
 * @author gong zhao
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@ConditionalOnDiscoveryEnabled
@ConditionalOnProperty(
        value = {"spring.cloud.femas.discovery.enabled"},
        matchIfMissing = true
)
public @interface ConditionalOnFemasDiscoveryEnabled {
}
