package com.tencent.tsf.femas.common.discovery;

import java.util.concurrent.ScheduledFuture;

/**
 * @Author leoziltong
 * @Description //TODO
 * @Date: 2021/3/23 18:01
 * @Version 1.0
 */
public interface ServerUpdater {

    /**
     * 开始UpdateAction
     *
     * @param action UpdateAction
     * @return ScheduledFuture
     */
    ScheduledFuture<?> start(ServerUpdater.UpdateAction action);

    /**
     * 停止
     *
     * @param scheduledFuture ScheduledFuture
     */
    void stop(ScheduledFuture<?> scheduledFuture);

    /**
     * 不必要继承接口
     *
     * @return 不必要继承接口
     */
    default String getLastUpdate() {
        return null;
    }

    /**
     * 获得距离上次更新的时间
     *
     * @return 距离上次更新的时间
     */
    default long getDurationSinceLastUpdateMs() {
        return 0L;
    }

    /**
     * 获得核心线程池数
     *
     * @return 核心线程池数
     */
    default int getCoreThreads() {
        return 0;
    }

    interface UpdateAction {

        /**
         * 更新操作
         */
        void doUpdate();
    }

}
