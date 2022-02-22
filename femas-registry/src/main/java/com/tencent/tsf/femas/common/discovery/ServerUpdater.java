package com.tencent.tsf.femas.common.discovery;

import java.util.concurrent.ScheduledFuture;

/**
 * @Author leoziltong
 * @Description //TODO
 * @Date: 2021/3/23 18:01
 * @Version 1.0
 */
public interface ServerUpdater {

    ScheduledFuture<?> start(ServerUpdater.UpdateAction action);

    void stop(ScheduledFuture scheduledFuture);

    /**
     * 不必要继承接口
     *
     * @return
     */
    default String getLastUpdate() {
        return null;
    }

    default long getDurationSinceLastUpdateMs() {
        return 0L;
    }

    default int getCoreThreads() {
        return 0;
    }

    interface UpdateAction {

        void doUpdate();
    }

}
