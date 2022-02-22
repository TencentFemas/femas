package com.tencent.tsf.femas.example.gateway.zuul.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.tencent.tsf.femas.common.context.Context;
import com.tencent.tsf.femas.common.context.FemasContext;
import com.tencent.tsf.femas.common.context.RpcInfo;
import com.tencent.tsf.femas.common.util.GsonUtil;
import com.tencent.tsf.femas.common.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;

import javax.servlet.http.HttpServletRequest;

public class TestFilter extends ZuulFilter {

    private static final Logger LOG = LoggerFactory.getLogger(TestFilter.class);

    @Override
    public String filterType() {
        // pre 类型的过滤器
        return FilterConstants.PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        // token 检验应该放在第一位来进行校验，因此需要放在最前面
        return FilterConstants.SERVLET_DETECTION_FILTER_ORDER - 1;
    }

    @Override
    public boolean shouldFilter() {
        // 过滤器是否应该执行， true:表示应该执行  false:表示跳过这个过滤器执行
        return true;
    }

    @Override
    public Object run() {
        RequestContext requestContext = RequestContext.getCurrentContext();
        HttpServletRequest httpServletRequest = requestContext.getRequest();
//        RpcInfo rcpInfo = FemasContext.getRpcInfo();

        LOG.info("URL: " + httpServletRequest.getRequestURL() + ", PARAMS: " + GsonUtil.toJson(httpServletRequest.getParameterMap()));

        String tagName = requestContext.getRequest().getParameter("tagName");
        String tagValue = requestContext.getRequest().getParameter("tagValue");
        if(StringUtils.isNotBlank(tagName)) {
            FemasContext.putTag(tagName, tagValue);
        }
        return null;
    }
}