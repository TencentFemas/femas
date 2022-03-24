package com.tencent.tsf.femas.common.httpclient;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.HttpResponse;
import org.apache.http.client.utils.HttpClientUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @Author leoziltong
 * @Description //TODO
 * @Date: 2021/3/31 21:28
 * @Version 1.0
 */
public class FemasApacheClientHttpResponse implements HttpClientResponse {

    private static final  Logger log = LoggerFactory.getLogger(FemasApacheClientHttpResponse.class);

    private HttpResponse response;

    public FemasApacheClientHttpResponse(HttpResponse response) {
        this.response = response;
    }

    @Override
    public String getStatusCode() {
        return String.valueOf(this.response.getStatusLine().getStatusCode());
    }

    @Override
    public Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>();
        org.apache.http.Header[] allHeaders = response.getAllHeaders();
        for (org.apache.http.Header header : allHeaders) {
            headers.put(header.getName(), header.getValue());
        }
        return headers;
    }

    @Override
    public InputStream getBody() throws IOException {
        return response.getEntity().getContent();
    }

    @Override
    public void close() {
        try {
            if (this.response != null) {
                HttpClientUtils.closeQuietly(response);
            }
        } catch (Exception ex) {
            log.warn("close response Quietly failed");
        }
    }
}
