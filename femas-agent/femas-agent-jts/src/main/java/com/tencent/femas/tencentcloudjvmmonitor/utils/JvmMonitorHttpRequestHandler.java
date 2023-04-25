/**
 * Copyright 2010-2021 the original author or authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.tencent.femas.tencentcloudjvmmonitor.utils;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;

public abstract class JvmMonitorHttpRequestHandler implements HttpHandler {

    private static final Logger LOGGER =
            Logger.getLogger(JvmMonitorHttpRequestHandler.class);

    private static final int BUFFER_SIZE = 1024;

    public void handle(HttpExchange exchange) throws IOException {
        String requestMethod = exchange.getRequestMethod();
        LOGGER.debug("get request: " + requestMethod);
        if (requestMethod.equalsIgnoreCase("POST")) {
            String requestBodyString = readFromStream(exchange.getRequestBody());
            String response = processCommand(requestBodyString);
            if (response != null && (response.length() > 0)) {
                exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length());
                LOGGER.debug("JvmMonitorAgent response request: <" + requestBodyString + ">,"
                        + "response <" + new String(response.getBytes()) + ">");
                // RESPONSE Body
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.flush();
                os.close();
            } else {
                LOGGER.error("Can not generate response data of request "
                        + requestMethod + " (" + requestBodyString + ")");
                exchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_REQUEST, 0);
            }
        } else {
            exchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_METHOD,0);
        }
        exchange.close();
    }

    private static String readFromStream(InputStream is) {
        try {
            byte[] ba = new byte [BUFFER_SIZE];
            int off = 0;
            int c = 0;
            while ((c = is.read(ba, off, ba.length)) != -1) {
                off += c;
            }
            return new String(ba, 0, off, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("Fail read request body");
        }
        return null;
    }

    public abstract String processCommand(String requestBody);
}
