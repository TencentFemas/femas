package com.tencent.tsf.femas.extension.springcloud.common.instrumentation.feign;

import com.tencent.tsf.femas.common.header.AbstractRequestMetaUtils;
import feign.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 由于 feign-core 从 10.12 开始不能通过 feignRequest.headers() 来修改 header，
 * 统一改为从 FeignHeaderInterceptor 设置。preprocess 和 setRequestMeta 空实现
 */
public class FeignHeaderUtils extends AbstractRequestMetaUtils {

    private static final Logger logger = LoggerFactory.getLogger(FemasFeignClientWrapper.class);

    private Request feignRequest;

    public FeignHeaderUtils(Request feignRequest) {
        this.feignRequest = feignRequest;
    }

    @Override
    public void preprocess() {

    }

    @Override
    public void setRequestMeta(String name, String value) {

    }
}
