package com.tencent.tsf.femas.common.invoke;

import com.tencent.tsf.femas.common.entity.Request;
import com.tencent.tsf.femas.common.entity.Response;

public interface Invoker {

    /**
     * 执行调用
     *
     * @param request 请求
     * @return Response 响应
     */
    Response invoke(Request request);
}
