package com.tencent.tsf.femas.common.callback;

import com.tencent.tsf.femas.common.entity.RequestBase;

public interface FemasCallback<T> {

    /**
     * will callback this method when server return response success
     *
     * @param appResponse response object
     * @param methodName the invoked method
     * @param request the invoked request object
     */
    void onSuccess(Object appResponse, String methodName, RequestBase request);

    /**
     * will callback this method when server meet exception
     *
     * @param throwable app's exception
     * @param methodName the invoked method
     * @param request the invoked request
     */
    void onException(Throwable throwable, String methodName, RequestBase request);
}
