package com.tencent.tsf.femas.agent.interceptor;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * @Author leoziltong@tencent.com
 */
public interface OriginalInterceptor {
    Object intercept(Object obj,
                     Object[] allArguments,
                     Callable<?> zuper,
                     Method method) throws Throwable;
}
