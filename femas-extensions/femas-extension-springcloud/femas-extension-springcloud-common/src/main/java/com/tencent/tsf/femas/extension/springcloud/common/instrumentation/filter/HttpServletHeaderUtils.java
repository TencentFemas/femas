package com.tencent.tsf.femas.extension.springcloud.common.instrumentation.filter;

import com.tencent.tsf.femas.common.context.Context;
import com.tencent.tsf.femas.common.context.ContextConstant;
import com.tencent.tsf.femas.common.context.factory.ContextFactory;
import com.tencent.tsf.femas.common.header.AbstractRequestMetaUtils;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpServletHeaderUtils extends AbstractRequestMetaUtils {

    private static final Logger logger = LoggerFactory.getLogger(HttpServletHeaderUtils.class);

    private volatile ContextConstant contextConstant = ContextFactory.getContextConstantInstance();

    private HttpServletRequest httpServletRequest;

    public HttpServletHeaderUtils(HttpServletRequest httpServletRequest) {
        this.httpServletRequest = httpServletRequest;
    }

    @Override
    public String getRequestMeta(String name) {
        String value = httpServletRequest.getHeader(name);
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

        Enumeration<String> headerEnumeration = httpServletRequest.getHeaderNames();
        if (headerEnumeration != null) {
            while (headerEnumeration.hasMoreElements()) {
                String header = headerEnumeration.nextElement();
                if (header.startsWith(prefix)) {
                    result.put(header, httpServletRequest.getHeader(header));
                }
            }
        }
        return result;
    }

    @Override
    public void getUniqueInfo() {
        // clean at client interceptor#fillTracingContext
        Context.getRpcInfo().put(contextConstant.getInterface(), httpServletRequest.getRequestURI());
        Context.getRpcInfo().put(contextConstant.getRequestHttpMethod(), httpServletRequest.getMethod());
    }

}
