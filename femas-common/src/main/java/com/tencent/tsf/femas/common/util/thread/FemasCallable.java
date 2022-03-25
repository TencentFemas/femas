package com.tencent.tsf.femas.common.util.thread;

import com.tencent.tsf.femas.common.context.Context;
import com.tencent.tsf.femas.common.util.StringUtils;

import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author MentosL
 * @version 1.0
 * @date 2022/3/25 3:28 PM
 */
public class FemasCallable<V> implements Callable<V> {

    private Callable<V> callable;
    private Map<String, String> contextData;

    public FemasCallable(Callable<V> callable) {
        this.callable = callable;
        this.contextData = Context.getRpcInfo().getAll();
    }

    @Override
    public V call() throws Exception {
        try {
            if (contextData != null && contextData.size() > 0) {
                for (Map.Entry<String, String> entry : contextData.entrySet()) {
                    if (StringUtils.isNotEmpty(entry.getValue())) {
                        Context.getRpcInfo().put(entry.getKey(), entry.getValue());
                    }
                }
            }
            if (null != this.callable) {
                return this.callable.call();
            }
        } finally {
<<<<<<< HEAD
            contextData = null;
=======
>>>>>>> 0104159 (统一解决项目内线程上下文变量传递入口)
            Context.getRpcInfo().reset();
        }
        return null;
    }
}