package com.tencent.tsf.femas.extension.springcloud.common.instrumentation.resttemplate;

import com.tencent.tsf.femas.common.header.AbstractRequestMetaUtils;
import com.tencent.tsf.femas.governance.lane.LaneService;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;


public class RestTemplateHeaderUtils extends AbstractRequestMetaUtils {

    private static final Logger logger = LoggerFactory.getLogger(RestTemplateHeaderUtils.class);

    private HttpRequest httpRequest;

    public RestTemplateHeaderUtils(HttpRequest httpRequest) {
        this.httpRequest = httpRequest;
    }

    @Override
    public void preprocess() {
        LaneService.headerPreprocess();
    }

    @Override
    public void setRequestMeta(String name, String value) {
        try {
            httpRequest.getHeaders().add(name, URLEncoder.encode(value, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            logger.trace("[UnsupportedEncodingException] name:{}, value:{}", name, value);
            httpRequest.getHeaders().add(name, value);
        }
    }
}
