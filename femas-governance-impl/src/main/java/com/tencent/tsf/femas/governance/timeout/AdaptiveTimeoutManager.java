package com.tencent.tsf.femas.governance.timeout;

import com.tencent.tsf.femas.common.context.Context;
import com.tencent.tsf.femas.common.util.StringUtils;
import com.tencent.tsf.femas.common.util.TimeUtil;

/**
 * 自适应超时
 */
public class AdaptiveTimeoutManager {

    /**
     * 传递给下游的超时时间
     */
    public static final String FEMAS_DOWNSTREAM_ADAPTIVE_TIMEOUT = "femas_downstream_adaptive_timeout";
    public static final String SOURCE_FEMAS_DOWNSTREAM_ADAPTIVE_TIMEOUT = "source.femas_downstream_adaptive_timeout";

    /**
     * 本地计算的超时时间
     */
    public static final ThreadLocal<Long> FEMAS_ADAPTIVE_TIMEOUT_TIMESTAMP = new ThreadLocal<>();

    public static final int FEMAS_ADAPTIVE_TIMEOUT_DEFAULT = -1;

    /**
     * 网络开销的时间
     * <p>
     * 单位ms
     */
    private static int networkTime = 20;

    /**
     * @return
     */
    public static int getAdaptiveTimeout() {
        return getTimeout();
    }

    /**
     * 获取传递给下游的timeout时间
     * <p>
     * 需要在当前上下文的timeout基础上减去一定的网络开销
     * 如果低于网络开销，则设置为网络开销时间作为timeout值
     *
     * @return
     */
    public static void refreshDownStreamAdaptiveTimeout() {
        int timeout = getTimeout();

        if (timeout > 0) {
            timeout = timeout - networkTime;

            if (timeout < networkTime) {
                timeout = networkTime;
            }

            Context.getRpcInfo().put(FEMAS_DOWNSTREAM_ADAPTIVE_TIMEOUT, String.valueOf(timeout));
        }
    }

    private static int getTimeout() {
        Long timeoutTimestamp = FEMAS_ADAPTIVE_TIMEOUT_TIMESTAMP.get();

        /**
         * 当前请求上下文并未初始化超时时
         */
        if (timeoutTimestamp == null) {
            /**
             * 获取上游传递下来的超时时间
             */
            String currentTimeout = Context.getRpcInfo().get(SOURCE_FEMAS_DOWNSTREAM_ADAPTIVE_TIMEOUT);

            if (StringUtils.isEmpty(currentTimeout)) {
                return FEMAS_ADAPTIVE_TIMEOUT_DEFAULT;
            }

            /**
             * 设置当前上下文绝对时间戳
             */
            int timeout = Integer.parseInt(currentTimeout);
            FEMAS_ADAPTIVE_TIMEOUT_TIMESTAMP.set(TimeUtil.currentTimeMillis() + timeout);

            return timeout;
        }

        /**
         * 计算当前请求的超时时间
         */
        int timeout = (int) (timeoutTimestamp - TimeUtil.currentTimeMillis());
        if (timeout > networkTime) {
            return timeout;
        }

        return networkTime;
    }

    public static void setNetworkTime(int networkTime) {
        AdaptiveTimeoutManager.networkTime = networkTime;
    }
}
