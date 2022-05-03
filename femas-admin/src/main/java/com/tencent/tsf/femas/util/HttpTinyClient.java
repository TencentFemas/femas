package com.tencent.tsf.femas.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class HttpTinyClient {

    public static HttpResult httpGet(String url, HashMap<String, String> paramValues) throws IOException {
        return httpGet(url, new ArrayList<String>(), paramValues, "UTF-8", 1000);
    }

    public static HttpResult httpGet(String url, HashMap<String, String> paramValues, long readTimeoutMs)
            throws IOException {
        return httpGet(url, new ArrayList<String>(), paramValues, "UTF-8", readTimeoutMs);
    }

    public static HttpResult httpGet(String url, List<String> headers, HashMap<String, String> paramValues,
            String encoding, long readTimeoutMs) throws IOException {
        String encodedContent = encodingParams(paramValues, encoding);
        url += (null == encodedContent) ? "" : ("?" + encodedContent);

        HttpURLConnection conn = null;
        String resp = null;
        Integer respCode = null;
        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout((int) readTimeoutMs);
            conn.setReadTimeout((int) readTimeoutMs);
            setHeaders(conn, headers, encoding);
            conn.connect();
            respCode = conn.getResponseCode();
            if (HttpURLConnection.HTTP_OK == respCode) {
                resp = IOTinyUtils.toString(conn.getInputStream(), encoding);
            } else {
                resp = IOTinyUtils.toString(conn.getErrorStream(), encoding);
            }
        } finally {
            if (conn != null) {
                if (conn.getInputStream() != null) {
                    conn.getInputStream().close();
                }
                if (conn.getErrorStream() != null) {
                    conn.getErrorStream().close();
                }
                conn.disconnect();
            }
        }
        return new HttpResult(respCode, resp);
    }

    private static String encodingParams(HashMap<String, String> params, String encoding)
            throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        if (null == params) {
            return null;
        }
        int size = params.size();
        int index = 0;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            index++;
            sb.append(entry.getKey()).append("=");
            sb.append(URLEncoder.encode(entry.getValue(), encoding));
            if (index != size) {
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
    public static HttpResult httpPost(String url, List<String> headers, HashMap<String, String> paramValues,
            String encoding, long readTimeoutMs) throws IOException {
        String encodedContent = encodingParams(paramValues, encoding);

        HttpURLConnection conn = null;
        String resp = null;
        Integer respCode = null;
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

            respCode = conn.getResponseCode();

            if (HttpURLConnection.HTTP_OK == respCode) {
                resp = IOTinyUtils.toString(conn.getInputStream(), encoding);
            } else {
                resp = IOTinyUtils.toString(conn.getErrorStream(), encoding);
            }
        } finally {
            if (null != conn) {
                InputStream inputStream = conn.getInputStream();
                OutputStream outputStream = conn.getOutputStream();
                InputStream errorStream = conn.getErrorStream();
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
                if (errorStream != null) {
                    errorStream.close();
                }
                conn.disconnect();
            }
        }
        return new HttpResult(respCode, resp);
    }

    public static class HttpResult {

        final public int code;
        final public String content;

        public HttpResult(int code, String content) {
            this.code = code;
            this.content = content;
        }

        public int getCode() {
            return code;
        }

        public String getContent() {
            return content;
        }
    }
}
