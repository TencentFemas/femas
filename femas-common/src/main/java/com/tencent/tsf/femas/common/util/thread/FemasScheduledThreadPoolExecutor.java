package com.tencent.tsf.femas.common.util.thread;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

/**
 * @author MentosL
 * @version 1.0
 * @date 2022/3/25 3:32 PM
 */
public class FemasScheduledThreadPoolExecutor extends ScheduledThreadPoolExecutor {
    public FemasScheduledThreadPoolExecutor(int corePoolSize) {
        super(corePoolSize);
    }

    public FemasScheduledThreadPoolExecutor(int corePoolSize, ThreadFactory threadFactory) {
        super(corePoolSize, threadFactory);
    }

    public FemasScheduledThreadPoolExecutor(int corePoolSize, RejectedExecutionHandler handler) {
        super(corePoolSize, handler);
    }

    public FemasScheduledThreadPoolExecutor(int corePoolSize, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        super(corePoolSize, threadFactory, handler);
    }

    @Override
    public void execute(Runnable command) {
        super.execute(new FemasRunnale(command));
    }
}