package com.tencent.tsf.femas.extension.springcloud.common.instrumentation.feign;

import com.tencent.tsf.femas.common.context.Context;
import com.tencent.tsf.femas.common.context.factory.ContextFactory;
import com.tencent.tsf.femas.common.util.StringUtils;
import com.tencent.tsf.femas.governance.lane.LaneService;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FeignHeaderInterceptor implements RequestInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(FeignHeaderInterceptor.class);

    private volatile Context commonContext = ContextFactory.getContextInstance();

    /**
     * 早于 beforeClientInvoke
     *
     * @param template
     */
    @Override
    public void apply(RequestTemplate template) {
        LaneService.headerPreprocess();

        for (Map.Entry<String, String> entry :
                commonContext.getRequestMetaSerializeTags().entrySet()) {
            if (StringUtils.isNotEmpty(entry.getValue())) {
                try {
                    template.header(entry.getKey(),
                            URLEncoder.encode(entry.getValue(), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    logger.warn("[UnsupportedEncodingException] name:{}, value:{}",
                            entry.getKey(), entry.getValue());
                    template.header(entry.getKey(), entry.getValue());
                }
            }
        }
    }
}
