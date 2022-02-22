package com.tencent.tsf.femas.common.util;

/**
 * @Author leoziltong
 * @Description //TODO
 * @Date: 2021/3/31 19:29
 * @Version 1.0
 */
public class HttpElement {


    public final class HttpMethod {

        public static final String GET = "GET";
        public static final String HEAD = "HEAD";
        public static final String POST = "POST";
        public static final String PUT = "PUT";
        public static final String PATCH = "PATCH";
        public static final String DELETE = "DELETE";
        public static final String OPTIONS = "OPTIONS";
        public static final String TRACE = "TRACE";
    }

    public final class MediaType {

        public static final String APPLICATION_FORM_URLENCODED = "application/x-www-form-urlencoded;charset=UTF-8";
        public static final String APPLICATION_XHTML_XML = "application/xhtml+xml";
        public static final String APPLICATION_XML = "application/xml;charset=UTF-8";
        public static final String APPLICATION_JSON = "application/json;charset=UTF-8";
        public static final String MULTIPART_FORM_DATA = "multipart/form-data;charset=UTF-8";
        public static final String TEXT_HTML = "text/html;charset=UTF-8";
        public static final String TEXT_PLAIN = "text/plain;charset=UTF-8";
    }
}