package com.tencent.tsf.femas.extension.springcloud.instrumentation.zuul;

import com.netflix.zuul.context.RequestContext;
import com.tencent.tsf.femas.common.context.Context;
import com.tencent.tsf.femas.common.context.ContextConstant;
import com.tencent.tsf.femas.common.context.factory.ContextFactory;
import com.tencent.tsf.femas.common.header.AbstractRequestMetaUtils;
import com.tencent.tsf.femas.governance.lane.LaneService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class ZuultHeaderUtils extends AbstractRequestMetaUtils {

    private static final Logger logger = LoggerFactory.getLogger(ZuultHeaderUtils.class);

    private volatile ContextConstant contextConstant = ContextFactory.getContextConstantInstance();

    private RequestContext requestContext;

    private volatile Context commonContext = ContextFactory.getContextInstance();

    public ZuultHeaderUtils(RequestContext requestContext) {
        this.requestContext = requestContext;
    }

    @Override
    public void preprocess() {

    }

    @Override
    public void setRequestMeta(String name, String value) {
        LaneService.headerPreprocess();

        for (Map.Entry<String, String> entry : commonContext.getRequestMetaSerializeTags().entrySet()) {
            if (com.tencent.tsf.femas.common.util.StringUtils.isNotEmpty(entry.getValue())) {
                try {
                    requestContext.addZuulRequestHeader(
                            entry.getKey(), URLEncoder.encode(entry.getValue(), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    logger.warn("[UnsupportedEncodingException] name:{}, value:{}", entry.getKey(), entry.getValue());
                    requestContext.addZuulRequestHeader(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    @Override
    public String getRequestMeta(String name) {
        String value = requestContext.getZuulRequestHeaders().get(name);
        if (StringUtils.isNotEmpty(value)) {
            try {
                value = URLDecoder.decode(value, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                logger.warn("[UnsupportedEncodingException] getHeader, name:{}, value:{}", name, value);
            }
        }
        return value;
    }

    @Override
    public Map<String, String> getPrefixRequestMetas(String prefix){
        Map<String, String> result = new HashMap<>();
        Map<String, String> headersMap = requestContext.getZuulRequestHeaders();
        for (Map.Entry<String, String> entry : headersMap.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith(prefix)) {
                result.put(key, entry.getValue());
            }
        }
        return result;
    }

    @Override
    public void getUniqueInfo() {
        // clean at client interceptor#fillTracingContext
        Context.getRpcInfo().put(contextConstant.getInterface(), requestContext.getRequest().getRequestURI());
        Context.getRpcInfo().put(contextConstant.getRequestHttpMethod(), requestContext.getRequest().getMethod());
    }

}
