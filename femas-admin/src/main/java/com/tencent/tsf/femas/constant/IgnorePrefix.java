package com.tencent.tsf.femas.constant;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Cody
 * @date 2021 2021/9/28 11:30 上午
 *         去除接口公共前缀
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface IgnorePrefix {

}
