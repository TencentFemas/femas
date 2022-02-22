package com.tencent.tsf.femas.common.httpclient;

import static com.tencent.tsf.femas.common.util.HttpHeaderKeys.DEFAULT_ENCODE;

import com.tencent.tsf.femas.common.util.HttpHeaderKeys;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

/**
 * @Author leoziltong
 * @Description //TODO
 * @Date: 2021/3/29 11:14
 * @Version 1.0
 */
public interface HttpClientResponse extends Closeable {

    Map<String, String> getHeaders();

    InputStream getBody() throws IOException;

    String getStatusCode() throws IOException;

    default String getCharset() {
        String acceptCharset = (String) Optional.ofNullable(getHeaders()).orElseGet(() -> Collections.EMPTY_MAP)
                .get(HttpHeaderKeys.ACCEPT_CHARSET);
        if (acceptCharset == null) {
            String contentType = (String) Optional.ofNullable(getHeaders()).orElseGet(() -> Collections.EMPTY_MAP)
                    .get(HttpHeaderKeys.ACCEPT_CHARSET);
            acceptCharset = StringUtils.isNotBlank(contentType) ? analysisCharset(contentType) : DEFAULT_ENCODE;
        }
        return acceptCharset;
    }

    default String analysisCharset(String contentType) {
        String[] values = contentType.split(";");
        String charset = DEFAULT_ENCODE;
        if (values.length == 0) {
            return charset;
        }
        for (String value : values) {
            if (value.startsWith("charset=")) {
                charset = value.substring("charset=".length());
            }
        }
        return charset;
    }

    /**
     * close response InputStream.
     */
    @Override
    void close();
}

