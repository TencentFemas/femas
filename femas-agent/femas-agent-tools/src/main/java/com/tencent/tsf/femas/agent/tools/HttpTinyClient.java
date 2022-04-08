package com.tencent.tsf.femas.agent.tools;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class HttpTinyClient {

    public static HttpResult httpGet(String url, List<String> paramValues) throws IOException {
        return httpGet(url, new ArrayList<String>(), paramValues, "UTF-8", 1000);
    }

    public static HttpResult httpGet(String url, List<String> paramValues, long readTimeoutMs) throws IOException {
        return httpGet(url, new ArrayList<String>(), paramValues, "UTF-8", readTimeoutMs);
    }

    public static HttpResult httpGet(String url, List<String> headers, List<String> paramValues,
                                     String encoding, long readTimeoutMs) throws IOException {
        String encodedContent = encodingParams(paramValues, encoding);
        url += (null == encodedContent) ? "" : ("?" + encodedContent);

        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout((int) readTimeoutMs);
            conn.setReadTimeout((int) readTimeoutMs);
            setHeaders(conn, headers, encoding);

            conn.connect();
            int respCode = conn.getResponseCode();
            String resp = null;

            if (HttpURLConnection.HTTP_OK == respCode) {
                resp = IOTinyUtils.toString(conn.getInputStream(), encoding);
            } else {
                resp =IOTinyUtils.toString(conn.getErrorStream(), encoding);
            }
            return new HttpResult(respCode, resp);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private static String encodingParams(List<String> paramValues, String encoding)
            throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        if (null == paramValues) {
            return null;
        }

        for (Iterator<String> iter = paramValues.iterator(); iter.hasNext(); ) {
            sb.append(iter.next()).append("=");
            sb.append(URLEncoder.encode(iter.next(), encoding));
            if (iter.hasNext()) {
                sb.append("&");
            }
        }
        return sb.toString();
    }

    private static void setHeaders(HttpURLConnection conn, List<String> headers, String encoding) {
        if (null != headers) {
            for (Iterator<String> iter = headers.iterator(); iter.hasNext(); ) {
                conn.addRequestProperty(iter.next(), iter.next());
            }
        }
        //conn.addRequestProperty("Client-Version", MQVersion.getVersionDesc(MQVersion.CURRENT_VERSION));
        //conn.addRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + encoding);

        String ts = String.valueOf(System.currentTimeMillis());
        conn.addRequestProperty("Metaq-Client-RequestTS", ts);
    }

    /**
     * @return the http response of given http post request
     */
    public static HttpResult httpPost(String url, List<String> headers, List<String> paramValues,
                                      String encoding, long readTimeoutMs) throws IOException {
        String encodedContent = encodingParams(paramValues, encoding);

        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(3000);
            conn.setReadTimeout((int) readTimeoutMs);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            setHeaders(conn, headers, encoding);

            //conn.getOutputStream().write(encodedContent.getBytes(MixAll.DEFAULT_CHARSET));

            conn.getOutputStream().write(encodedContent.getBytes());

            int respCode = conn.getResponseCode();
            String resp = null;

            if (HttpURLConnection.HTTP_OK == respCode) {
                resp = IOTinyUtils.toString(conn.getInputStream(), encoding);
            } else {
                resp = IOTinyUtils.toString(conn.getErrorStream(), encoding);
            }
            return new HttpResult(respCode, resp);
        } finally {
            if (null != conn) {
                conn.disconnect();
            }
        }
    }

    public static class HttpResult {
        final public int code;
        final public String content;

        public HttpResult(int code, String content) {
            this.code = code;
            this.content = content;
        }
    }
}
