package com.tencent.tsf.femas.common.util.thread;

import com.tencent.tsf.femas.common.context.Context;
import com.tencent.tsf.femas.common.util.StringUtils;

import java.util.Map;

/**
 * @author MentosL
 * @version 1.0
 * @date 2022/3/25 1:59 PM
 * 线程包装
 */
public class FemasRunnale implements Runnable {

    private Runnable runnable;

    private Map<String, String> contextData;

    public FemasRunnale(Runnable runnable) {
        this.runnable = runnable;
        this.contextData = Context.getRpcInfo().getAll();
    }

    @Override
    public void run() {
        // 判断当前取值  需要一个个put原因为 规范统一数据入口
        try {
            if (contextData != null && contextData.size() > 0) {
                for (Map.Entry<String, String> entry : contextData.entrySet()) {
                    if (StringUtils.isNotEmpty(entry.getValue())) {
                        Context.getRpcInfo().put(entry.getKey(), entry.getValue());
                    }
                }
            }
            if (null != runnable) {
                runnable.run();
            }
        } finally {
<<<<<<< HEAD
            contextData = null;
=======
>>>>>>> 0104159 (统一解决项目内线程上下文变量传递入口)
            Context.getRpcInfo().reset();
        }
    }
}
