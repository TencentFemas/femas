package com.tencent.tsf.femas.agent.common;

import com.tencent.tsf.femas.agent.tools.AbstractAgentLogger;
import com.tencent.tsf.femas.agent.tools.AgentLogger;
import com.tencent.tsf.femas.common.context.Context;
import com.tencent.tsf.femas.common.context.ContextConstant;
import com.tencent.tsf.femas.common.context.factory.ContextFactory;
import com.tencent.tsf.femas.common.header.AbstractRequestMetaUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class HttpServletHeaderUtils extends AbstractRequestMetaUtils {
    private static final AbstractAgentLogger LOG = AgentLogger.getLogger(HttpServletHeaderUtils.class);


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
                LOG.info("[UnsupportedEncodingException] getHeader, name:{}, value:{}" + name + value);
            }
        }
        return value;
    }

    @Override
    public Map<String, String> getPrefixRequestMetas(String prefix) {
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
